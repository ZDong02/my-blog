// API client for communicating with the Spring Boot backend

const API_BASE_URL =
  import.meta.env.PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

class ApiClient {
  constructor() {
    this.token = null;
    this.refreshToken = null;
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

  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    // Add authorization header if token exists
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    const config = {
      ...options,
      headers,
      credentials: 'omit', // Don't send cookies for API requests
    };

    try {
      const response = await fetch(url, config);

      if (response.status === 401 && this.refreshToken) {
        // Token expired, try to refresh
        const refreshed = await this.refreshAuthToken();
        if (refreshed) {
          // Retry the original request with new token
          headers['Authorization'] = `Bearer ${this.token}`;
          const retryResponse = await fetch(url, { ...config, headers });
          return this.handleResponse(retryResponse);
        } else {
          // Refresh failed, redirect to login
          this.clearTokens();
          throw new Error('Authentication failed');
        }
      }

      return this.handleResponse(response);
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  async handleResponse(response) {
    // Check if response is empty (e.g., 403 with no body)
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

    const data = JSON.parse(text);
    return data;
  }

  async refreshAuthToken() {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.refreshToken}`,
        },
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
      console.error('Token refresh failed:', error);
      return false;
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
}

// Create and export singleton instance
export const apiClient = new ApiClient();

// Load tokens on initialization
if (typeof window !== 'undefined') {
  apiClient.loadTokens();
}

// Make apiClient available globally for use in other scripts
if (typeof window !== 'undefined') {
  window.apiClient = apiClient;
}
