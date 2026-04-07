import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory } from 'vue-router';
import { setupAuthGuards } from './guards.js';

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'login',
            component: () => import('@/views/pages/auth/Login.vue')
        },
        {
            path: '/dashboard',
            component: AppLayout,
            children: [{ path: '', name: 'dashboard', component: () => import('@/views/Dashboard.vue') }]
        },
        {
            path: '/sales/pos',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/sales/pdv.vue') }]
        },
        {
            path: '/sales/history',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/sales/sales-history.vue') }]
        },
        {
            path: '/products/list',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/products/products.vue') }]
        },
        {
            path: '/products/inventory',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/products/inventory.vue') }]
        },
        {
            path: '/reports/daily-sales',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/reports/daily-sales.vue') }]
        },
        {
            path: '/reports/closing',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/reports/closing.vue') }]
        },
        {
            path: '/entities/customers',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/entities/customers.vue') }]
        },
        {
            path: '/entities/suppliers',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/entities/suppliers.vue') }]
        },
        {
            path: '/entities/users',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/entities/users.vue') }]
        },
        {
            path: '/entities/profiles',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/entities/profiles.vue') }]
        },
        {
            path: '/landing',
            name: 'landing',
            component: () => import('@/views/pages/Landing.vue')
        },
        {
            path: '/pages/notfound',
            name: 'notfound',
            component: () => import('@/views/pages/NotFound.vue')
        },
        {
            path: '/auth/login',
            name: 'auth-login',
            component: () => import('@/views/pages/auth/Login.vue')
        },
        {
            path: '/auth/access',
            name: 'accessDenied',
            component: () => import('@/views/pages/auth/Access.vue')
        },
        {
            path: '/auth/error',
            name: 'error',
            component: () => import('@/views/pages/auth/Error.vue')
        }
    ]
});

setupAuthGuards(router);

export default router;
