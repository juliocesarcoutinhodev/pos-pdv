import api from './api.js';

/**
 * Maps the backend login response to a frontend-friendly shape.
 * The refresh token is delivered via HttpOnly cookie.
 */
export async function login(email, password) {
    const response = await api.post('/api/v1/auth/login', { email, password });
    const data = response.data;

    return {
        id: data.user.id,
        email: data.user.email,
        name: data.user.name,
        provider: data.user.provider,
        accessToken: data.accessToken,
        expiresIn: data.expiresIn
    };
}

/**
 * Revokes the current session — calls the backend.
 */
export async function logout() {
    await api.post('/api/v1/auth/logout');
}

/**
 * Gets the remaining tokens from the rate limiter.
 */
export function extractRateLimitInfo(response) {
    return {
        remaining: response.headers['x-ratelimit-remaining'] ? parseInt(response.headers['x-ratelimit-remaining']) : null,
        limit: response.headers['x-ratelimit-limit'] ? parseInt(response.headers['x-ratelimit-limit']) : null,
        retryAfter: response.headers['retry-after'] ? parseInt(response.headers['retry-after']) : null
    };
}
