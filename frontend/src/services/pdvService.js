import api from './api.js';

export async function getCurrentCashRegister() {
    const response = await api.get('/api/v1/pdv/cash/current');
    return response.data;
}

export async function openCashRegister(payload) {
    const response = await api.post('/api/v1/pdv/cash/open', payload);
    return response.data;
}

export async function closeCashRegister(payload) {
    const response = await api.post('/api/v1/pdv/cash/close', payload);
    return response.data;
}

export async function createCashMovement(payload) {
    const response = await api.post('/api/v1/pdv/cash/movements', payload);
    return response.data;
}

export async function lookupPdvProduct(code) {
    const response = await api.get('/api/v1/pdv/products/lookup', {
        params: { code }
    });
    return response.data;
}

export async function createPdvSale(payload) {
    const response = await api.post('/api/v1/pdv/sales', payload);
    return response.data;
}

export async function listRecentPdvSales(limit = 10) {
    const response = await api.get('/api/v1/pdv/sales/recent', {
        params: { limit }
    });
    return response.data;
}

function normalizePageResponse(data) {
    return {
        content: Array.isArray(data?.content) ? data.content : [],
        page: Number.isInteger(data?.page) ? data.page : 0,
        size: Number.isInteger(data?.size) ? data.size : 20,
        totalElements: Number.isInteger(data?.totalElements) ? data.totalElements : 0,
        totalPages: Number.isInteger(data?.totalPages) ? data.totalPages : 0
    };
}

export async function listPdvSalesHistory(params = {}) {
    const query = {
        page: params.page ?? 0,
        size: params.size ?? 20
    };

    if (params.dateFrom) {
        query.dateFrom = params.dateFrom;
    }

    if (params.dateTo) {
        query.dateTo = params.dateTo;
    }

    if (params.userId) {
        query.userId = params.userId;
    }

    if (params.paymentMethod) {
        query.paymentMethod = params.paymentMethod;
    }

    if (params.sortBy) {
        query.sortBy = params.sortBy;
        query.sortDirection = params.sortDirection === 'asc' ? 'asc' : 'desc';
    }

    const response = await api.get('/api/v1/pdv/sales/history', { params: query });
    return normalizePageResponse(response.data);
}

export async function getDailySalesReport(date) {
    const response = await api.get('/api/v1/pdv/reports/daily-sales', {
        params: date ? { date } : undefined
    });
    return response.data;
}

export async function getCashClosingReport(date) {
    const response = await api.get('/api/v1/pdv/reports/closing', {
        params: date ? { date } : undefined
    });
    return response.data;
}

export async function listOpenCashRegistersForMonitoring() {
    const response = await api.get('/api/v1/pdv/monitor/open-cash-registers');
    return Array.isArray(response.data) ? response.data : [];
}

export async function getOpenCashRegisterMonitoringSummary(sessionId) {
    const response = await api.get(`/api/v1/pdv/monitor/open-cash-registers/${sessionId}/summary`);
    return response.data;
}
