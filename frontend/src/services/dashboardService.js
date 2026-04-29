import api from './api.js';

export async function getDashboardOverview() {
    const response = await api.get('/api/v1/dashboard/overview');
    const data = response.data ?? {};

    return {
        todaySalesAmount: Number(data.todaySalesAmount ?? 0),
        todaySalesCount: Number(data.todaySalesCount ?? 0),
        todayAverageTicket: Number(data.todayAverageTicket ?? 0),
        activeUsers: Number(data.activeUsers ?? 0),
        activeCustomers: Number(data.activeCustomers ?? 0),
        activeSuppliers: Number(data.activeSuppliers ?? 0),
        activeProducts: Number(data.activeProducts ?? 0),
        lowStockProducts: Number(data.lowStockProducts ?? 0),
        openCashRegisters: Number(data.openCashRegisters ?? 0),
        recentSales: Array.isArray(data.recentSales) ? data.recentSales : [],
        topProducts: Array.isArray(data.topProducts) ? data.topProducts : [],
        paymentSummary: Array.isArray(data.paymentSummary) ? data.paymentSummary : []
    };
}
