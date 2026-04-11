// API client for communicating with the Spring Boot backend

const API_BASE_URL =
  import.meta.env.PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

class ApiClient {
  constructor() {
    this.token = null;
    this.refreshToken = null;
    this.refreshPromise = null;
    this.requestTimeoutMs = 15000;
  }

  setTokens(token, refreshToken) {
    this.token = token;
    this.refreshToken = refreshToken;
    // Store in localStorage for persistence
    if (typeof window !== 'undefined') {
      if (token) {
        localStorage.setItem('auth_token', token);
        localStorage.setItem('refresh_token', refreshToken);
      } else {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('refresh_token');
      }
    }
  }

  loadTokens() {
    if (typeof window !== 'undefined') {
      this.token = localStorage.getItem('auth_token');
      this.refreshToken = localStorage.getItem('refresh_token');
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

    if (url.startsWith('/api/uploads/')) {
      return apiOrigin ? `${apiOrigin}${url}` : url;
    }

    if (url.startsWith('/uploads/')) {
      return apiOrigin ? `${apiOrigin}${url}` : url;
    }

    if (url.startsWith('uploads/')) {
      return `${apiRoot}/${url}`;
    }

    return url;
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
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    const { timeoutMs, ...requestOptions } = options;
    const resolvedTimeoutMs =
      Number.isFinite(timeoutMs) && timeoutMs > 0 ? timeoutMs : this.requestTimeoutMs;
    const abortController =
      typeof AbortController !== 'undefined' ? new AbortController() : null;
    const timeoutId =
      abortController && resolvedTimeoutMs > 0
        ? setTimeout(() => abortController.abort(), resolvedTimeoutMs)
        : null;

    const config = {
      ...requestOptions,
      headers,
      credentials: 'include', // Include credentials for CORS requests
      ...(abortController ? { signal: abortController.signal } : {}),
    };

    try {
      const response = await fetch(url, config);

      if (response.status === 401 && this.refreshToken) {
        // Token expired, try to refresh
        const refreshed = await this.refreshAuthTokenWithLock();
        if (refreshed) {
          // Retry the original request with new token
          headers['Authorization'] = `Bearer ${this.token}`;
          const retryResponse = await fetch(url, { ...config, headers });
          return this.handleResponse(retryResponse);
        } else {
          this.clearTokens();
          if (typeof window !== 'undefined' && window.showToast) {
            window.showToast('Session expired. Please login again.', 'error');
          }
          throw new Error('Authentication failed');
        }
      }

      return this.handleResponse(response);
    } catch (error) {
      if (error?.name === 'AbortError') {
        error = new Error('Request timeout. Please try again.');
      }

      console.error('API request failed:', error);
      // Show error toast with better error message
      if (typeof window !== 'undefined' && window.showToast) {
        let errorMessage = 'Request failed. Please try again.';
        if (error.message) {
          // Don't expose internal error details to users
          if (error.message.includes('Network Error') || error.message.includes('fetch')) {
            errorMessage = 'Network error. Please check your connection.';
          } else if (error.message.includes('Authentication failed')) {
            errorMessage = 'Authentication failed. Please login again.';
          } else {
            errorMessage = error.message;
          }
        }
        window.showToast(errorMessage, 'error');
      }
      throw error;
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
      return JSON.parse(text);
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

      if (response.ok) {
        const data = await response.json();
        if (data.success && data.data) {
          this.setTokens(data.data.token, data.data.refreshToken);
          return true;
        }
      }

      return false;
    } catch (error) {
      if (error?.name === 'AbortError') {
        console.error('Token refresh timed out');
        return false;
      }

      console.error('Token refresh failed:', error);
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

  async deleteComment(commentId) {
    return this.request(`/comments/${commentId}`, {
      method: 'DELETE',
    });
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

// Load tokens on initialization
if (typeof window !== 'undefined') {
  apiClient.loadTokens();
}
