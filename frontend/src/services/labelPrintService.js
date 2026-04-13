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

function normalizeSuggestion(data) {
    return {
        id: data?.id ?? null,
        sku: data?.sku ?? '',
        barcode: data?.barcode ?? '',
        name: data?.name ?? '',
        category: data?.category ?? '',
        unit: data?.unit ?? '',
        salePrice: toNumberOrNull(data?.salePrice),
        promotionalPrice: toNumberOrNull(data?.promotionalPrice),
        createdAt: data?.createdAt ?? null
    };
}

function normalizeJobItem(data) {
    return {
        productId: data?.productId ?? null,
        sku: data?.sku ?? '',
        barcode: data?.barcode ?? '',
        name: data?.name ?? '',
        unit: data?.unit ?? '',
        salePrice: toNumberOrNull(data?.salePrice),
        promotionalPrice: toNumberOrNull(data?.promotionalPrice),
        quantity: Number.isInteger(data?.quantity) ? data.quantity : 1
    };
}

function normalizeJobSummary(data) {
    return {
        id: data?.id ?? null,
        referenceDate: data?.referenceDate ?? null,
        totalProducts: Number.isInteger(data?.totalProducts) ? data.totalProducts : 0,
        totalLabels: Number.isInteger(data?.totalLabels) ? data.totalLabels : 0,
        createdAt: data?.createdAt ?? null
    };
}

function normalizeJobDetail(data) {
    return {
        id: data?.id ?? null,
        referenceDate: data?.referenceDate ?? null,
        totalProducts: Number.isInteger(data?.totalProducts) ? data.totalProducts : 0,
        totalLabels: Number.isInteger(data?.totalLabels) ? data.totalLabels : 0,
        createdAt: data?.createdAt ?? null,
        items: Array.isArray(data?.items) ? data.items.map(normalizeJobItem) : []
    };
}

/**
 * Lists product suggestions for label printing by date.
 * @param {{ date?: string | null, page?: number, size?: number, name?: string, sku?: string, category?: string, sortBy?: string | null, sortDirection?: string | null }} params
 */
export async function listLabelSuggestions(params = {}) {
    const query = {
        page: params.page ?? 0,
        size: params.size ?? 20
    };

    if (params.date) {
        query.date = params.date;
    }

    if (params.name?.trim()) {
        query.name = params.name.trim();
    }

    if (params.sku?.trim()) {
        query.sku = params.sku.trim();
    }

    if (params.category?.trim()) {
        query.category = params.category.trim();
    }

    if (params.sortBy?.trim()) {
        query.sortBy = params.sortBy.trim();
        query.sortDirection = params.sortDirection === 'desc' ? 'desc' : 'asc';
    }

    const response = await api.get('/api/v1/labels/suggestions', { params: query });
    const normalized = normalizePageResponse(response.data);
    return {
        ...normalized,
        content: normalized.content.map(normalizeSuggestion)
    };
}

/**
 * Creates a label print job.
 * @param {{ referenceDate?: string | null, items: Array<{ productId: string, quantity: number }> }} payload
 */
export async function createLabelPrintJob(payload) {
    const response = await api.post('/api/v1/labels/jobs', payload);
    return normalizeJobDetail(response.data);
}

/**
 * Lists label print jobs history.
 * @param {{ referenceDate?: string | null, page?: number, size?: number, sortBy?: string | null, sortDirection?: string | null }} params
 */
export async function listLabelPrintJobs(params = {}) {
    const query = {
        page: params.page ?? 0,
        size: params.size ?? 20
    };

    if (params.referenceDate) {
        query.referenceDate = params.referenceDate;
    }

    if (params.sortBy?.trim()) {
        query.sortBy = params.sortBy.trim();
        query.sortDirection = params.sortDirection === 'asc' ? 'asc' : 'desc';
    }

    const response = await api.get('/api/v1/labels/jobs', { params: query });
    const normalized = normalizePageResponse(response.data);
    return {
        ...normalized,
        content: normalized.content.map(normalizeJobSummary)
    };
}

/**
 * Gets a label print job by id.
 * @param {string} id
 */
export async function getLabelPrintJobById(id) {
    const response = await api.get(`/api/v1/labels/jobs/${id}`);
    return normalizeJobDetail(response.data);
}

function extractFileName(contentDisposition) {
    if (!contentDisposition) {
        return null;
    }

    const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8Match?.[1]) {
        return decodeURIComponent(utf8Match[1]);
    }

    const simpleMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
    if (simpleMatch?.[1]) {
        return simpleMatch[1];
    }

    return null;
}

/**
 * Downloads the report PDF for a label print job.
 * @param {string} id
 */
export async function downloadLabelPrintJobReport(id) {
    const response = await api.get(`/api/v1/labels/jobs/${id}/report`, {
        responseType: 'blob'
    });

    return {
        blob: response.data,
        fileName: extractFileName(response.headers?.['content-disposition']) ?? `etiquetas-${id}.pdf`
    };
}
