import api from './api.js';

function normalizePageResponse(data) {
    return {
        content: Array.isArray(data?.content) ? data.content : [],
        page: Number.isInteger(data?.page) ? data.page : 0,
        size: Number.isInteger(data?.size) ? data.size : 10,
        totalElements: Number.isInteger(data?.totalElements) ? data.totalElements : 0,
        totalPages: Number.isInteger(data?.totalPages) ? data.totalPages : 0
    };
}

function normalizeUserDetail(data) {
    return {
        id: data?.id ?? null,
        email: data?.email ?? '',
        name: data?.name ?? '',
        provider: data?.provider ?? '',
        roles: Array.isArray(data?.roles) ? data.roles : [],
        createdAt: data?.createdAt ?? null,
        updatedAt: data?.updatedAt ?? null,
        active: Boolean(data?.active)
    };
}

/**
 * Lists users with server-side pagination and optional filters.
 * @param {{ page?: number, size?: number, name?: string, email?: string, active?: boolean | null }} params
 */
export async function listUsers(params = {}) {
    const query = {
        page: params.page ?? 0,
        size: params.size ?? 10
    };

    if (params.name?.trim()) {
        query.name = params.name.trim();
    }

    if (params.email?.trim()) {
        query.email = params.email.trim();
    }

    if (typeof params.active === 'boolean') {
        query.active = params.active;
    }

    const response = await api.get('/api/v1/users', { params: query });
    return normalizePageResponse(response.data);
}

/**
 * Gets details for a single user.
 * @param {string} id
 */
export async function getUserById(id) {
    const response = await api.get(`/api/v1/users/${id}`);
    return normalizeUserDetail(response.data);
}

/**
 * Creates a new user as admin.
 * @param {{ email: string, name: string, password: string, roleIds: string[] }} payload
 */
export async function createUser(payload) {
    const response = await api.post('/api/v1/users', payload);
    return normalizeUserDetail(response.data);
}

/**
 * Fully updates a user by id using PUT.
 * @param {string} id
 * @param {{ email: string, name: string, password?: string, roleIds: string[] }} payload
 */
export async function updateUserPut(id, payload) {
    const response = await api.put(`/api/v1/users/${id}`, payload);
    return normalizeUserDetail(response.data);
}

/**
 * Partially updates a user by id using PATCH.
 * @param {string} id
 * @param {{ email?: string, name?: string, password?: string, roleIds?: string[], active?: boolean }} payload
 */
export async function updateUserPatch(id, payload) {
    const response = await api.patch(`/api/v1/users/${id}`, payload);
    return normalizeUserDetail(response.data);
}
