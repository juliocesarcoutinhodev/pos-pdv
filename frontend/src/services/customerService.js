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

function normalizeAddress(data) {
    return {
        id: data?.id ?? null,
        zipCode: data?.zipCode ?? '',
        street: data?.street ?? '',
        number: data?.number ?? '',
        complement: data?.complement ?? '',
        district: data?.district ?? '',
        city: data?.city ?? '',
        state: data?.state ?? ''
    };
}

function normalizeCustomerListItem(data) {
    return {
        id: data?.id ?? null,
        name: data?.name ?? '',
        taxId: data?.taxId ?? '',
        email: data?.email ?? '',
        phone: data?.phone ?? '',
        imageId: data?.imageId ?? null,
        createdAt: data?.createdAt ?? null,
        updatedAt: data?.updatedAt ?? null,
        active: Boolean(data?.active)
    };
}

function normalizeCustomerDetail(data) {
    return {
        id: data?.id ?? null,
        name: data?.name ?? '',
        taxId: data?.taxId ?? '',
        email: data?.email ?? '',
        phone: data?.phone ?? '',
        birthDate: data?.birthDate ?? null,
        gender: data?.gender ?? null,
        ieOrRg: data?.ieOrRg ?? null,
        address: normalizeAddress(data?.address),
        imageId: data?.imageId ?? null,
        createdAt: data?.createdAt ?? null,
        updatedAt: data?.updatedAt ?? null,
        active: Boolean(data?.active)
    };
}

/**
 * Lists customers with server-side pagination and optional filters.
 * @param {{ page?: number, size?: number, name?: string, taxId?: string, email?: string, active?: boolean | null, sortBy?: string | null, sortDirection?: string | null }} params
 */
export async function listCustomers(params = {}) {
    const query = {
        page: params.page ?? 0,
        size: params.size ?? 10
    };

    if (params.name?.trim()) {
        query.name = params.name.trim();
    }

    if (params.taxId?.trim()) {
        query.taxId = params.taxId.trim();
    }

    if (params.email?.trim()) {
        query.email = params.email.trim();
    }

    if (typeof params.active === 'boolean') {
        query.active = params.active;
    }

    if (params.sortBy?.trim()) {
        query.sortBy = params.sortBy.trim();
        query.sortDirection = params.sortDirection === 'desc' ? 'desc' : 'asc';
    }

    const response = await api.get('/api/v1/customers', { params: query });
    const normalized = normalizePageResponse(response.data);

    return {
        ...normalized,
        content: normalized.content.map(normalizeCustomerListItem)
    };
}

/**
 * Gets details for a single customer.
 * @param {string} id
 */
export async function getCustomerById(id) {
    const response = await api.get(`/api/v1/customers/${id}`);
    return normalizeCustomerDetail(response.data);
}

/**
 * Creates a new customer.
 * @param {{ name: string, taxId: string, email?: string | null, phone?: string | null, birthDate?: string | null, gender?: string | null, ieOrRg?: string | null, imageId?: string | null, address: { zipCode: string, street: string, number?: string | null, complement?: string | null, district: string, city: string, state: string } }} payload
 */
export async function createCustomer(payload) {
    const response = await api.post('/api/v1/customers', payload);
    return normalizeCustomerDetail(response.data);
}

/**
 * Fully updates a customer by id using PUT.
 * @param {string} id
 * @param {{ name: string, taxId: string, email?: string | null, phone?: string | null, birthDate?: string | null, gender?: string | null, ieOrRg?: string | null, imageId?: string | null, address: { zipCode: string, street: string, number?: string | null, complement?: string | null, district: string, city: string, state: string } }} payload
 */
export async function updateCustomerPut(id, payload) {
    const response = await api.put(`/api/v1/customers/${id}`, payload);
    return normalizeCustomerDetail(response.data);
}

/**
 * Partially updates a customer by id using PATCH.
 * @param {string} id
 * @param {{ name?: string, taxId?: string, email?: string | null, phone?: string | null, birthDate?: string | null, gender?: string | null, ieOrRg?: string | null, imageId?: string | null, address?: { zipCode?: string, street?: string, number?: string | null, complement?: string | null, district?: string, city?: string, state?: string }, active?: boolean }} payload
 */
export async function updateCustomerPatch(id, payload) {
    const response = await api.patch(`/api/v1/customers/${id}`, payload);
    return normalizeCustomerDetail(response.data);
}

/**
 * Deactivates a customer by id (soft delete).
 * @param {string} id
 */
export async function deactivateCustomer(id) {
    await api.delete(`/api/v1/customers/${id}`);
}

/**
 * Reactivates a customer.
 * @param {string} id
 */
export async function reactivateCustomer(id) {
    return updateCustomerPatch(id, { active: true });
}
