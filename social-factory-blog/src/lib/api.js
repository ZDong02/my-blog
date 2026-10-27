// API client for communicating with the Spring Boot backend

import { API_BASE_URL } from './api-config.js';

// 会话失效（未登录或凭证过期且无法续期）的专用错误标识。
// 调用方据此区分「用户本来就没登录」与「接口真的报错」，避免未登录场景刷出错误提示。
export const UNAUTHENTICATED_ERROR = 'UNAUTHENTICATED';

function isUsableToken(value) {
  return (
    typeof value === 'string' &&
    value.trim() !== '' &&
    value !== 'undefined' &&
    value !== 'null'
  );
}

function createUnauthenticatedError(message = 'Authentication required') {
  const error = new Error(message);
  error.code = UNAUTHENTICATED_ERROR;
  return error;
}

class ApiClient {
  constructor() {
    this.token = null;
    this.refreshToken = null;
    this.refreshPromise = null;
    this.requestTimeoutMs = 15000;
  }

  setTokens(token, refreshToken) {
    // 只接受真正可用的字符串，避免把 undefined / null 写进 localStorage
    // （写入后会变成字符串 "undefined"，下次启动就成了永远刷不掉的坏凭证）
    this.token = isUsableToken(token) ? token : null;
    this.refreshToken = isUsableToken(refreshToken) ? refreshToken : null;
    // Store in localStorage for persistence
    if (typeof window !== 'undefined') {
      if (this.token) {
        localStorage.setItem('auth_token', this.token);
      } else {
        localStorage.removeItem('auth_token');
      }

      if (this.refreshToken) {
        localStorage.setItem('refresh_token', this.refreshToken);
      } else {
        localStorage.removeItem('refresh_token');
      }
    }
  }

  loadTokens() {
    if (typeof window !== 'undefined') {
      const storedToken = localStorage.getItem('auth_token');
      const storedRefreshToken = localStorage.getItem('refresh_token');

      this.token = isUsableToken(storedToken) ? storedToken : null;
      this.refreshToken = isUsableToken(storedRefreshToken) ? storedRefreshToken : null;

      // 清理历史遗留的坏值（"undefined" / "null" / 空串），让状态自愈
      if (storedToken !== null && !this.token) {
        localStorage.removeItem('auth_token');
      }
      if (storedRefreshToken !== null && !this.refreshToken) {
        localStorage.removeItem('refresh_token');
      }
    }
  }

  resolveMediaUrl(url) {
    if (!url) return '';

    if (
      /^(https?:)?\/\//i.test(url) ||
      url.startsWith('data:') ||
      url.startsWith('blob:')
    ) {
      return url;
    }

    const apiRoot = API_BASE_URL.replace(/\/+$/, '');
    const originMatch = apiRoot.match(/^(https?:\/\/[^/]+)/i);
    const apiOrigin = originMatch ? originMatch[1] : '';

    // 后端返回的已是完整路径，补上 API 源地址即可
    if (url.startsWith('/api/uploads/') || url.startsWith('/uploads/')) {
      return apiOrigin ? `${apiOrigin}${url}` : url;
    }

    // 缺少前导斜杠的相对路径
    if (url.startsWith('uploads/') || url.startsWith('api/uploads/')) {
      return apiOrigin ? `${apiOrigin}/${url}` : `/${url}`;
    }

    // 纯文件名
    if (!url.includes('/')) {
      return apiOrigin ? `${apiOrigin}/uploads/${url}` : `/uploads/${url}`;
    }

    return url;
  }

  normalizePayload(value) {
    if (Array.isArray(value)) {
      return value.map((item) => this.normalizePayload(item));
    }

    if (!value || typeof value !== 'object') {
      return value;
    }

    const normalized = {};

    for (const [key, nestedValue] of Object.entries(value)) {
      if ((key === 'avatar' || key === 'featuredImage') && typeof nestedValue === 'string') {
        normalized[key] = this.resolveMediaUrl(nestedValue);
        continue;
      }

      normalized[key] = this.normalizePayload(nestedValue);
    }

    return normalized;
  }

  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const method = (options.method || 'GET').toUpperCase();
    const hasBody = options.body !== undefined && options.body !== null;
    const isFormData =
      typeof FormData !== 'undefined' && options.body instanceof FormData;
    const headers = {
      ...(options.headers || {}),
    };

    if (!isFormData && hasBody && method !== 'GET' && method !== 'HEAD' && !headers['Content-Type']) {
      headers['Content-Type'] = 'application/json';
    }

    // Add authorization header if token exists
    if (isUsableToken(this.token)) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    const { timeoutMs, ...requestOptions } = options;
    const resolvedTimeoutMs =
      Number.isFinite(timeoutMs) && timeoutMs > 0 ? timeoutMs : this.requestTimeoutMs;

    try {
      const response = await this.send(url, requestOptions, headers, resolvedTimeoutMs);

      if (response.status === 401) {
        // 认证接口本身（登录/注册/刷新）返回 401 时不再触发续期，避免递归刷新
        const canRefresh =
          !endpoint.startsWith('/auth/') && isUsableToken(this.refreshToken);
        const refreshed = canRefresh && (await this.refreshAuthTokenWithLock());

        if (refreshed) {
          headers['Authorization'] = `Bearer ${this.token}`;
          const retryResponse = await this.send(
            url,
            requestOptions,
            headers,
            resolvedTimeoutMs
          );
          if (retryResponse.status !== 401) {
            return this.handleResponse(retryResponse);
          }
        }

        // 续期失败：清理本地凭证，按「未登录」处理，不当作错误暴露给用户
        this.clearTokens();
        throw createUnauthenticatedError();
      }

      return this.handleResponse(response);
    } catch (error) {
      if (error?.name === 'AbortError') {
        error = new Error('Request timeout. Please try again.');
      }

      // 未登录 / 会话失效属于正常状态，不刷错误日志、不弹错误提示
      if (error?.code === UNAUTHENTICATED_ERROR) {
        console.debug('[api] request skipped, session is not authenticated:', endpoint);
        throw error;
      }

      console.error('API request failed:', error);
      // Show error toast with better error message
      if (typeof window !== 'undefined' && window.showToast) {
        let errorMessage = 'Request failed. Please try again.';
        if (error.message) {
          // Don't expose internal error details to users
          if (error.message.includes('Network Error') || error.message.includes('fetch')) {
            errorMessage = 'Network error. Please check your connection.';
          } else {
            errorMessage = error.message;
          }
        }
        window.showToast(errorMessage, 'error');
      }
      throw error;
    }
  }

  /**
   * 带超时控制的单次请求，便于「原始请求 + 续期后重试」共用同一套超时逻辑。
   * 每次调用使用独立的 AbortController，重试不会复用已被中止的 signal。
   */
  async send(url, requestOptions, headers, timeoutMs) {
    const abortController =
      typeof AbortController !== 'undefined' ? new AbortController() : null;
    const timeoutId =
      abortController && timeoutMs > 0
        ? setTimeout(() => abortController.abort(), timeoutMs)
        : null;

    try {
      return await fetch(url, {
        ...requestOptions,
        headers,
        credentials: 'include', // Include credentials for CORS requests
        ...(abortController ? { signal: abortController.signal } : {}),
      });
    } finally {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
    }
  }

  async handleResponse(response) {
    const contentType = response.headers.get('content-type');
    const text = await response.text();

    if (!response.ok) {
      // Try to parse error message from response, fallback to status text
      let errorMessage = `Request failed with status ${response.status}`;
      if (contentType && contentType.includes('application/json') && text) {
        try {
          const data = JSON.parse(text);
          errorMessage = data.message || errorMessage;
        } catch (e) {
          errorMessage = text || errorMessage;
        }
      }
      throw new Error(errorMessage);
    }

    // Handle empty response
    if (!text) {
      return { success: true, data: null };
    }

    if (contentType && contentType.includes('application/json')) {
      const payload = JSON.parse(text);
      if (payload && typeof payload === 'object' && 'data' in payload) {
        payload.data = this.normalizePayload(payload.data);
      }
      return payload;
    }

    return { success: true, data: text };
  }

  async refreshAuthTokenWithLock() {
    if (this.refreshPromise) {
      return this.refreshPromise;
    }

    this.refreshPromise = this.refreshAuthToken().finally(() => {
      this.refreshPromise = null;
    });

    return this.refreshPromise;
  }

  async refreshAuthToken() {
    if (!isUsableToken(this.refreshToken)) {
      return false;
    }

    const abortController =
      typeof AbortController !== 'undefined' ? new AbortController() : null;
    const timeoutId = abortController
      ? setTimeout(() => abortController.abort(), 10000)
      : null;

    try {
      const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.refreshToken}`,
        },
        credentials: 'include',
        ...(abortController ? { signal: abortController.signal } : {}),
      });

      if (!response.ok) {
        return false;
      }

      const data = await response.json();
      if (data?.success && isUsableToken(data?.data?.token)) {
        this.setTokens(data.data.token, data.data.refreshToken);
        return true;
      }

      return false;
    } catch (error) {
      // 续期失败是预期内的情况（凭证过期 / 后端不可用），交由调用方按未登录处理
      console.debug('[api] token refresh unavailable:', error?.message || error);
      return false;
    } finally {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
    }
  }

  clearTokens() {
    this.token = null;
    this.refreshToken = null;
    if (typeof window !== 'undefined') {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('refresh_token');
    }
  }

  // Auth methods
  async register(userData) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
  }

  async login(credentials) {
    const response = await this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });

    if (response.success && response.data) {
      this.setTokens(response.data.token, response.data.refreshToken);
    }

    return response;
  }

  async logout() {
    this.clearTokens();
  }

  // Post methods
  async getPosts(page = 1, size = 10) {
    return this.request(`/posts?page=${page}&size=${size}`);
  }

  async getPost(id) {
    return this.request(`/posts/${id}`);
  }

  async createPost(postData) {
    return this.request('/posts', {
      method: 'POST',
      body: JSON.stringify(postData),
    });
  }

  async updatePost(id, postData) {
    return this.request(`/posts/${id}`, {
      method: 'PUT',
      body: JSON.stringify(postData),
    });
  }

  async deletePost(id) {
    return this.request(`/posts/${id}`, {
      method: 'DELETE',
    });
  }

  // Comment methods
  async getComments(postId) {
    return this.request(`/comments/post/${postId}`);
  }

  async addComment(postId, commentData) {
    return this.request(`/comments/post/${postId}`, {
      method: 'POST',
      body: JSON.stringify(commentData),
    });
  }

  async editComment(commentId, content) {
    return this.request(`/comments/${commentId}`, {
      method: 'PUT',
      body: JSON.stringify({ content }),
    });
  }

  async deleteComment(commentId) {
    return this.request(`/comments/${commentId}`, {
      method: 'DELETE',
    });
  }

  // Admin comment methods
  async getAdminComments(page = 1, size = 20, status = null, postId = null) {
    let url = `/admin/comments?page=${page}&size=${size}`;
    if (status !== null) url += `&status=${status}`;
    if (postId !== null) url += `&postId=${postId}`;
    return this.request(url);
  }

  async updateCommentStatus(commentId, status) {
    return this.request(`/admin/comments/${commentId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  }

  async adminDeleteComment(commentId) {
    return this.request(`/admin/comments/${commentId}`, {
      method: 'DELETE',
    });
  }

  async batchUpdateCommentStatus(commentIds, status) {
    return this.request('/admin/comments/batch/status', {
      method: 'PUT',
      body: JSON.stringify({ commentIds, status }),
    });
  }

  async batchDeleteComments(commentIds) {
    return this.request('/admin/comments/batch', {
      method: 'DELETE',
      body: JSON.stringify({ commentIds }),
    });
  }

  async getCommentStats() {
    return this.request('/admin/comments/stats');
  }

  // Interaction methods
  async likePost(postId) {
    return this.request(`/interactions/like/${postId}`, {
      method: 'POST',
    });
  }

  async unlikePost(postId) {
    return this.request(`/interactions/like/${postId}`, {
      method: 'DELETE',
    });
  }

  async bookmarkPost(postId) {
    return this.request(`/interactions/bookmark/${postId}`, {
      method: 'POST',
    });
  }

  async removeBookmark(postId) {
    return this.request(`/interactions/bookmark/${postId}`, {
      method: 'DELETE',
    });
  }

  async checkLikeStatus(postId) {
    return this.request(`/interactions/like/check/${postId}`);
  }

  async checkBookmarkStatus(postId) {
    return this.request(`/interactions/bookmark/check/${postId}`);
  }

  // User methods
  async getProfile() {
    return this.request('/users/profile');
  }

  async verifyAdmin() {
    try {
      const response = await this.getProfile();
      return Boolean(response?.success && response?.data?.role === 'ADMIN');
    } catch (error) {
      return false;
    }
  }

  async updateProfile(profileData) {
    return this.request('/users/profile', {
      method: 'PUT',
      body: JSON.stringify(profileData),
    });
  }

  async changePassword(passwordData) {
    return this.request('/users/change-password', {
      method: 'PUT',
      body: JSON.stringify(passwordData),
    });
  }

  // Dashboard methods
  async getDashboardStats() {
    return this.request('/dashboard/stats');
  }

  async getRecentActivity() {
    return this.request('/dashboard/recent-activity');
  }

  // Category methods
  async getCategories() {
    return this.request('/categories');
  }

  async createCategory(categoryData) {
    return this.request('/categories', {
      method: 'POST',
      body: JSON.stringify(categoryData),
    });
  }

  async updateCategory(id, categoryData) {
    return this.request(`/categories/${id}`, {
      method: 'PUT',
      body: JSON.stringify(categoryData),
    });
  }

  async deleteCategory(id) {
    return this.request(`/categories/${id}`, {
      method: 'DELETE',
    });
  }

  // User management methods (Admin)
  async getAllUsers() {
    return this.request('/admin/users');
  }

  async getUserById(userId) {
    return this.request(`/admin/users/${userId}`);
  }

  async updateUserRole(userId, role) {
    return this.request(`/admin/users/${userId}/role`, {
      method: 'PUT',
      body: JSON.stringify({ role }),
    });
  }

  async updateUserStatus(userId, status) {
    return this.request(`/admin/users/${userId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  }

  async deleteUser(userId) {
    return this.request(`/admin/users/${userId}`, {
      method: 'DELETE',
    });
  }

  // Batch operations (Admin)
  async batchUpdateUserStatus(userIds, status) {
    return this.request('/admin/users/batch/status', {
      method: 'PUT',
      body: JSON.stringify({ userIds, status }),
    });
  }

  async batchDeleteUsers(userIds) {
    return this.request('/admin/users/batch', {
      method: 'DELETE',
      body: JSON.stringify({ userIds }),
    });
  }

  async getUserStats() {
    return this.request('/admin/users/stats');
  }

  // Get user's liked posts
  async getMyLikes(page = 1, size = 10) {
    return this.request(`/interactions/likes/my-likes?page=${page}&size=${size}`);
  }

  // Get user's bookmarked posts
  async getMyBookmarks(page = 1, size = 10) {
    return this.request(`/interactions/bookmarks/my-bookmarks?page=${page}&size=${size}`);
  }

  // Get user's comments
  async getMyComments(page = 1, size = 10) {
    return this.request(`/comments/my-comments?page=${page}&size=${size}`);
  }

  // Search posts
  async searchPosts(keyword, categoryId, page = 1, size = 10) {
    let url = `/posts/search?page=${page}&size=${size}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
    if (categoryId) url += `&categoryId=${categoryId}`;
    return this.request(url);
  }

  // Get hot posts
  async getHotPosts(size = 10) {
    return this.request(`/posts/hot?size=${size}`);
  }

  // Get archive stats
  async getArchiveStats() {
    return this.request(`/posts/archive`);
  }

  // Upload file
  async uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);
    const response = await this.request('/upload', {
      method: 'POST',
      body: formData,
    });

    if (response?.success && response?.data?.url) {
      response.data.url = this.resolveMediaUrl(response.data.url);
    }

    return response;
  }

  // Tags
  async getTags() {
    return this.request('/tags');
  }

  async getTagById(id) {
    return this.request(`/tags/${id}`);
  }

  async getTagBySlug(slug) {
    return this.request(`/tags/slug/${slug}`);
  }

  async getPostsByTag(tagId, page = 1, size = 10) {
    return this.request(`/tags/${tagId}/posts?page=${page}&size=${size}`);
  }

  async getHotTags(limit = 20) {
    return this.request(`/tags/hot?limit=${limit}`);
  }

  async createTag(tagData) {
    return this.request('/tags', {
      method: 'POST',
      body: JSON.stringify(tagData),
    });
  }

  async updateTag(id, tagData) {
    return this.request(`/tags/${id}`, {
      method: 'PUT',
      body: JSON.stringify(tagData),
    });
  }

  async deleteTag(id) {
    return this.request(`/tags/${id}`, {
      method: 'DELETE',
    });
  }

}

// Create and export singleton instance
export const apiClient = new ApiClient();
export { API_BASE_URL };

// Load tokens on initialization
if (typeof window !== 'undefined') {
  apiClient.loadTokens();
}
