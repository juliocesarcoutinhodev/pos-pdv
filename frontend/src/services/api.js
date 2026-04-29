import axios from 'axios';
import { toAppPath } from '@/utils/appBasePath.js';

const apiBaseUrl = String(import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');

const api = axios.create({
    baseURL: apiBaseUrl,
    withCredentials: true
});

let onRefreshTokenCallback = null;
let isRefreshing = false;
let hasRedirectedToLogin = false;
const pendingRequests = [];

function redirectToLoginSessionExpired() {
    if (hasRedirectedToLogin) {
        return;
    }
    hasRedirectedToLogin = true;
    window.location.href = toAppPath('/auth/login?sessionExpired=true');
}

function flushPendingRequests(error) {
    while (pendingRequests.length > 0) {
        const pending = pendingRequests.shift();
        if (!pending) {
            continue;
        }

        if (error) {
            pending.reject(error);
            continue;
        }

        pending.resolve(pending.requestConfig);
    }
}

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
        if (!originalRequest) {
            return Promise.reject(error);
        }

        const isAuthEndpoint = originalRequest.url?.startsWith('/api/v1/auth/');

        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isAuthEndpoint) {
                return Promise.reject(error);
            }

            if (!onRefreshTokenCallback) {
                redirectToLoginSessionExpired();
                return Promise.reject(error);
            }

            originalRequest._retry = true;

            if (!isRefreshing) {
                isRefreshing = true;
                onRefreshTokenCallback()
                    .then(() => {
                        flushPendingRequests(null);
                    })
                    .catch((refreshError) => {
                        flushPendingRequests(refreshError);
                        redirectToLoginSessionExpired();
                    })
                    .finally(() => {
                        isRefreshing = false;
                    });
            }

            return new Promise((resolve, reject) => {
                pendingRequests.push({
                    resolve,
                    reject,
                    requestConfig: originalRequest
                });
            }).then((requestConfig) => api(requestConfig));
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
