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

function normalizeContact(data) {
    return {
        id: data?.id ?? null,
        name: data?.name ?? '',
        email: data?.email ?? '',
        phone: data?.phone ?? ''
    };
}

function normalizeSupplierListItem(data) {
    return {
        id: data?.id ?? null,
        name: data?.name ?? '',
        taxId: data?.taxId ?? '',
        email: data?.email ?? '',
        phone: data?.phone ?? '',
        createdAt: data?.createdAt ?? null,
        updatedAt: data?.updatedAt ?? null,
        active: Boolean(data?.active)
    };
}

function normalizeSupplierDetail(data) {
    return {
        id: data?.id ?? null,
        name: data?.name ?? '',
        taxId: data?.taxId ?? '',
        email: data?.email ?? '',
        phone: data?.phone ?? '',
        address: normalizeAddress(data?.address),
        contacts: Array.isArray(data?.contacts) ? data.contacts.map(normalizeContact) : [],
        createdAt: data?.createdAt ?? null,
        updatedAt: data?.updatedAt ?? null,
        active: Boolean(data?.active)
    };
}

function normalizeCnpjLookup(data) {
    return {
        taxId: data?.taxId ?? '',
        name: data?.name ?? '',
        alias: data?.alias ?? '',
        address: {
            zipCode: data?.address?.zip ?? '',
            street: data?.address?.street ?? '',
            number: data?.address?.number ?? '',
            complement: data?.address?.details ?? '',
            district: data?.address?.district ?? '',
            city: data?.address?.city ?? '',
            state: data?.address?.state ?? ''
        },
        phones: Array.isArray(data?.phones)
            ? data.phones.map((phone) => ({
                  type: phone?.type ?? '',
                  area: phone?.area ?? '',
                  number: phone?.number ?? ''
              }))
            : [],
        emails: Array.isArray(data?.emails)
            ? data.emails.map((email) => ({
                  ownership: email?.ownership ?? '',
                  address: email?.address ?? '',
                  domain: email?.domain ?? ''
              }))
            : []
    };
}

function normalizeZipLookup(data) {
    return {
        updated: data?.updated ?? null,
        code: data?.code ?? '',
        municipality: data?.municipality ?? null,
        street: data?.street ?? '',
        number: data?.number ?? '',
        district: data?.district ?? '',
        city: data?.city ?? '',
        state: data?.state ?? ''
    };
}

/**
 * Lists suppliers with server-side pagination and optional filters.
 * @param {{ page?: number, size?: number, name?: string, taxId?: string, email?: string, active?: boolean | null, sortBy?: string | null, sortDirection?: string | null }} params
 */
export async function listSuppliers(params = {}) {
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

    const response = await api.get('/api/v1/suppliers', { params: query });
    const normalized = normalizePageResponse(response.data);

    return {
        ...normalized,
        content: normalized.content.map(normalizeSupplierListItem)
    };
}

/**
 * Gets details for a single supplier.
 * @param {string} id
 */
export async function getSupplierById(id) {
    const response = await api.get(`/api/v1/suppliers/${id}`);
    return normalizeSupplierDetail(response.data);
}

/**
 * Creates a new supplier.
 * @param {{ name: string, taxId: string, email?: string | null, phone?: string | null, address: { zipCode: string, street: string, number?: string | null, complement?: string | null, district: string, city: string, state: string }, contacts?: Array<{ name: string, email?: string | null, phone?: string | null }> }} payload
 */
export async function createSupplier(payload) {
    const response = await api.post('/api/v1/suppliers', payload);
    return normalizeSupplierDetail(response.data);
}

/**
 * Fully updates a supplier by id using PUT.
 * @param {string} id
 * @param {{ name: string, taxId: string, email?: string | null, phone?: string | null, address: { zipCode: string, street: string, number?: string | null, complement?: string | null, district: string, city: string, state: string }, contacts?: Array<{ name: string, email?: string | null, phone?: string | null }> }} payload
 */
export async function updateSupplierPut(id, payload) {
    const response = await api.put(`/api/v1/suppliers/${id}`, payload);
    return normalizeSupplierDetail(response.data);
}

/**
 * Partially updates a supplier by id using PATCH.
 * @param {string} id
 * @param {{ name?: string, taxId?: string, email?: string | null, phone?: string | null, address?: { zipCode?: string, street?: string, number?: string | null, complement?: string | null, district?: string, city?: string, state?: string }, contacts?: Array<{ name: string, email?: string | null, phone?: string | null }>, active?: boolean }} payload
 */
export async function updateSupplierPatch(id, payload) {
    const response = await api.patch(`/api/v1/suppliers/${id}`, payload);
    return normalizeSupplierDetail(response.data);
}

/**
 * Deactivates a supplier by id (soft delete).
 * @param {string} id
 */
export async function deactivateSupplier(id) {
    await api.delete(`/api/v1/suppliers/${id}`);
}

/**
 * Reactivates a supplier.
 * @param {string} id
 */
export async function reactivateSupplier(id) {
    return updateSupplierPatch(id, { active: true });
}

/**
 * Looks up supplier/company data by CNPJ.
 * @param {string} taxId
 */
export async function lookupCnpj(taxId) {
    const response = await api.get('/api/v1/cnpj', { params: { taxId } });
    return normalizeCnpjLookup(response.data);
}

/**
 * Looks up address data by ZIP code.
 * @param {string} code
 */
export async function lookupZip(code) {
    const response = await api.get('/api/v1/zip', { params: { code } });
    return normalizeZipLookup(response.data);
}
