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

function normalizeProfileDetail(data) {
    return {
        id: data?.id ?? null,
        name: data?.name ?? '',
        description: data?.description ?? '',
        createdAt: data?.createdAt ?? null,
        updatedAt: data?.updatedAt ?? null
    };
}

/**
 * Lists profiles with server-side pagination.
 * @param {{ page?: number, size?: number }} params
 */
export async function listProfiles(params = {}) {
    const response = await api.get('/api/v1/roles', {
        params: {
            page: params.page ?? 0,
            size: params.size ?? 100
        }
    });

    return normalizePageResponse(response.data);
}

/**
 * Gets details for a single profile.
 * @param {string} id
 */
export async function getProfileById(id) {
    const response = await api.get(`/api/v1/roles/${id}`);
    return normalizeProfileDetail(response.data);
}

/**
 * Creates a new profile.
 * @param {{ name: string, description: string }} payload
 */
export async function createProfile(payload) {
    const response = await api.post('/api/v1/roles', payload);
    return normalizeProfileDetail(response.data);
}

/**
 * Fully updates a profile by id using PUT.
 * @param {string} id
 * @param {{ name: string, description: string }} payload
 */
export async function updateProfilePut(id, payload) {
    const response = await api.put(`/api/v1/roles/${id}`, payload);
    return normalizeProfileDetail(response.data);
}

/**
 * Deletes a profile by id.
 * @param {string} id
 */
export async function deleteProfile(id) {
    await api.delete(`/api/v1/roles/${id}`);
}
