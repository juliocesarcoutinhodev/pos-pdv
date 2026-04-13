import api from './api.js';

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

function buildQuery(params = {}) {
    const query = {};

    if (params.name?.trim()) {
        query.name = params.name.trim();
    }

    if (typeof params.active === 'boolean') {
        query.active = params.active;
    }

    if (Number.isInteger(params.birthMonth) && params.birthMonth >= 1 && params.birthMonth <= 12) {
        query.birthMonth = params.birthMonth;
    }

    if (params.birthDateFrom) {
        query.birthDateFrom = params.birthDateFrom;
    }

    if (params.birthDateTo) {
        query.birthDateTo = params.birthDateTo;
    }

    return query;
}

async function downloadReport(endpoint, params, fallbackFileName) {
    const response = await api.get(endpoint, {
        params: buildQuery(params),
        responseType: 'blob'
    });

    return {
        blob: response.data,
        fileName: extractFileName(response.headers?.['content-disposition']) ?? fallbackFileName
    };
}

/**
 * Downloads summary customer report PDF.
 * @param {{ name?: string, active?: boolean | null, birthMonth?: number | null, birthDateFrom?: string | null, birthDateTo?: string | null }} params
 */
export async function downloadCustomerSummaryReport(params = {}) {
    return downloadReport('/api/v1/customers/reports/summary', params, 'clientes-resumido.pdf');
}

/**
 * Downloads detailed customer report PDF.
 * @param {{ name?: string, active?: boolean | null, birthMonth?: number | null, birthDateFrom?: string | null, birthDateTo?: string | null }} params
 */
export async function downloadCustomerDetailedReport(params = {}) {
    return downloadReport('/api/v1/customers/reports/detailed', params, 'clientes-detalhado.pdf');
}
