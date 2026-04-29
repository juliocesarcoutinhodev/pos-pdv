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

function toNumberOrNull(value) {
    if (value === null || value === undefined || value === '') {
        return null;
    }

    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
}

function normalizeProductListItem(data) {
    return {
        id: data?.id ?? null,
        sku: data?.sku ?? '',
        barcode: data?.barcode ?? '',
        name: data?.name ?? '',
        category: data?.category ?? '',
        supplierId: data?.supplierId ?? null,
        brand: data?.brand ?? '',
        unit: data?.unit ?? '',
        salePrice: toNumberOrNull(data?.salePrice),
        stockQuantity: toNumberOrNull(data?.stockQuantity),
        minimumStock: toNumberOrNull(data?.minimumStock),
        imageId: data?.imageId ?? null,
        createdAt: data?.createdAt ?? null,
        updatedAt: data?.updatedAt ?? null,
        active: Boolean(data?.active)
    };
}

function normalizeProductDetail(data) {
    return {
        id: data?.id ?? null,
        sku: data?.sku ?? '',
        barcode: data?.barcode ?? '',
        name: data?.name ?? '',
        description: data?.description ?? '',
        brand: data?.brand ?? '',
        category: data?.category ?? '',
        supplierId: data?.supplierId ?? null,
        unit: data?.unit ?? '',
        costPrice: toNumberOrNull(data?.costPrice),
        salePrice: toNumberOrNull(data?.salePrice),
        marginPercentage: toNumberOrNull(data?.marginPercentage),
        promotionalPrice: toNumberOrNull(data?.promotionalPrice),
        stockQuantity: toNumberOrNull(data?.stockQuantity),
        minimumStock: toNumberOrNull(data?.minimumStock),
        ncm: data?.ncm ?? '',
        cest: data?.cest ?? '',
        cfop: data?.cfop ?? '',
        taxOrigin: data?.taxOrigin ?? '',
        taxSituation: data?.taxSituation ?? '',
        icmsRate: toNumberOrNull(data?.icmsRate),
        pisSituation: data?.pisSituation ?? '',
        pisRate: toNumberOrNull(data?.pisRate),
        cofinsSituation: data?.cofinsSituation ?? '',
        cofinsRate: toNumberOrNull(data?.cofinsRate),
        imageId: data?.imageId ?? null,
        createdAt: data?.createdAt ?? null,
        updatedAt: data?.updatedAt ?? null,
        active: Boolean(data?.active)
    };
}

/**
 * Lists products with server-side pagination and optional filters.
 * @param {{ page?: number, size?: number, name?: string, sku?: string, barcode?: string, category?: string, active?: boolean | null, sortBy?: string | null, sortDirection?: string | null }} params
 */
export async function listProducts(params = {}) {
    const query = {
        page: params.page ?? 0,
        size: params.size ?? 10
    };

    if (params.name?.trim()) {
        query.name = params.name.trim();
    }

    if (params.sku?.trim()) {
        query.sku = params.sku.trim();
    }

    if (params.barcode?.trim()) {
        query.barcode = params.barcode.trim();
    }

    if (params.category?.trim()) {
        query.category = params.category.trim();
    }

    if (typeof params.active === 'boolean') {
        query.active = params.active;
    }

    if (params.sortBy?.trim()) {
        query.sortBy = params.sortBy.trim();
        query.sortDirection = params.sortDirection === 'desc' ? 'desc' : 'asc';
    }

    const response = await api.get('/api/v1/products', { params: query });
    const normalized = normalizePageResponse(response.data);

    return {
        ...normalized,
        content: normalized.content.map(normalizeProductListItem)
    };
}

/**
 * Gets details for a single product.
 * @param {string} id
 */
export async function getProductById(id) {
    const response = await api.get(`/api/v1/products/${id}`);
    return normalizeProductDetail(response.data);
}

/**
 * Gets next suggested SKU for product creation.
 * @returns {Promise<string>}
 */
export async function getNextProductSku() {
    const response = await api.get('/api/v1/products/next-sku');
    return String(response?.data?.sku || '');
}

/**
 * Creates a new product.
 * @param {{ sku: string, barcode?: string | null, name: string, description?: string | null, brand?: string | null, category?: string | null, supplierId?: string | null, unit: string, costPrice?: number | null, salePrice?: number | null, marginPercentage?: number | null, promotionalPrice?: number | null, stockQuantity?: number | null, minimumStock?: number | null, ncm?: string | null, cest?: string | null, cfop?: string | null, taxOrigin?: string | null, taxSituation?: string | null, icmsRate?: number | null, pisSituation?: string | null, pisRate?: number | null, cofinsSituation?: string | null, cofinsRate?: number | null, imageId?: string | null }} payload
 */
export async function createProduct(payload) {
    const response = await api.post('/api/v1/products', payload);
    return normalizeProductDetail(response.data);
}

/**
 * Fully updates a product by id using PUT.
 * @param {string} id
 * @param {{ sku: string, barcode?: string | null, name: string, description?: string | null, brand?: string | null, category?: string | null, supplierId?: string | null, unit: string, costPrice?: number | null, salePrice?: number | null, marginPercentage?: number | null, promotionalPrice?: number | null, stockQuantity?: number | null, minimumStock?: number | null, ncm?: string | null, cest?: string | null, cfop?: string | null, taxOrigin?: string | null, taxSituation?: string | null, icmsRate?: number | null, pisSituation?: string | null, pisRate?: number | null, cofinsSituation?: string | null, cofinsRate?: number | null, imageId?: string | null }} payload
 */
export async function updateProductPut(id, payload) {
    const response = await api.put(`/api/v1/products/${id}`, payload);
    return normalizeProductDetail(response.data);
}

/**
 * Partially updates a product by id using PATCH.
 * @param {string} id
 * @param {{ sku?: string, barcode?: string | null, name?: string, description?: string | null, brand?: string | null, category?: string | null, supplierId?: string | null, unit?: string, costPrice?: number | null, salePrice?: number | null, marginPercentage?: number | null, promotionalPrice?: number | null, stockQuantity?: number | null, minimumStock?: number | null, ncm?: string | null, cest?: string | null, cfop?: string | null, taxOrigin?: string | null, taxSituation?: string | null, icmsRate?: number | null, pisSituation?: string | null, pisRate?: number | null, cofinsSituation?: string | null, cofinsRate?: number | null, imageId?: string | null, active?: boolean }} payload
 */
export async function updateProductPatch(id, payload) {
    const response = await api.patch(`/api/v1/products/${id}`, payload);
    return normalizeProductDetail(response.data);
}

/**
 * Deactivates a product by id (soft delete).
 * @param {string} id
 */
export async function deactivateProduct(id) {
    await api.delete(`/api/v1/products/${id}`);
}

/**
 * Reactivates a product.
 * @param {string} id
 */
export async function reactivateProduct(id) {
    return updateProductPatch(id, { active: true });
}
