import api from './api.js';

export async function getCurrentCashRegister() {
    const response = await api.get('/api/v1/pdv/cash/current');
    return response.data;
}

export async function openCashRegister(payload) {
    const response = await api.post('/api/v1/pdv/cash/open', payload);
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
