export const API_BASE_URL = (import.meta.env.PUBLIC_API_BASE_URL || '/api').replace(/\/+$/, '');

export const BUILD_API_BASE_URL = (
  import.meta.env.BUILD_API_BASE_URL ||
  import.meta.env.PUBLIC_API_BASE_URL ||
  'http://localhost:8080/api'
).replace(/\/+$/, '');
