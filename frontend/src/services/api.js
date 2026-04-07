import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'https://vps7348.integrator.host/pospdv',
    withCredentials: true
});

let onRefreshTokenCallback = null;
let isRefreshing = false;

api.interceptors.request.use((config) => {
    // Don't attach token to auth endpoints
    if (config.url?.startsWith('/api/v1/auth/')) {
        return config;
    }
    const token = localStorage.getItem('access_token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        const isAuthEndpoint = originalRequest.url?.startsWith('/api/v1/auth/');

        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isAuthEndpoint) {
                return Promise.reject(error);
            }

            // Skip refresh on the /refresh endpoint itself to avoid infinite loop
            if (originalRequest.url === '/api/v1/auth/refresh') {
                window.location.href = '/auth/login?sessionExpired=true';
                return Promise.reject(error);
            }

            originalRequest._retry = true;

            if (onRefreshTokenCallback && !isRefreshing) {
                isRefreshing = true;
                try {
                    await onRefreshTokenCallback();
                    isRefreshing = false;
                    return api(originalRequest);
                } catch {
                    isRefreshing = false;
                    window.location.href = '/auth/login?sessionExpired=true';
                }
            } else {
                window.location.href = '/auth/login?sessionExpired=true';
            }
        }
        return Promise.reject(error);
    }
);

export function setRefreshTokenCallback(callback) {
    onRefreshTokenCallback = callback;
}

export function setupRefreshAuth() {
    onRefreshTokenCallback = async () => {
        const response = await api.post('/api/v1/auth/refresh');
        const { accessToken } = response.data;
        localStorage.setItem('access_token', accessToken);
    };
}

export default api;
