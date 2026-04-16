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
            name: 'sales-pos',
            component: () => import('@/views/sales/pdv.vue')
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
            path: '/products/new',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/products/product-form.vue') }]
        },
        {
            path: '/products/:id/edit',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/products/product-form.vue') }]
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
            path: '/reports/labels',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/reports/labels.vue') }]
        },
        {
            path: '/reports/customers',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/reports/customers.vue') }]
        },
        {
            path: '/pages/customers',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/pages/customers.vue') }]
        },
        {
            path: '/pages/customers/new',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/pages/customer-form.vue') }]
        },
        {
            path: '/pages/customers/:id/edit',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/pages/customer-form.vue') }]
        },
        {
            path: '/pages/suppliers',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/pages/suppliers.vue') }]
        },
        {
            path: '/pages/suppliers/new',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/pages/supplier-form.vue') }]
        },
        {
            path: '/pages/suppliers/:id/edit',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/pages/supplier-form.vue') }]
        },
        {
            path: '/pages/users',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/pages/users.vue') }]
        },
        {
            path: '/pages/profiles',
            component: AppLayout,
            children: [{ path: '', component: () => import('@/views/pages/profiles.vue') }]
        },
        {
            path: '/entities/customers',
            redirect: '/pages/customers'
        },
        {
            path: '/entities/customers/new',
            redirect: '/pages/customers/new'
        },
        {
            path: '/entities/customers/:id/edit',
            redirect: (to) => `/pages/customers/${to.params.id}/edit`
        },
        {
            path: '/entities/suppliers',
            redirect: '/pages/suppliers'
        },
        {
            path: '/entities/suppliers/new',
            redirect: '/pages/suppliers/new'
        },
        {
            path: '/entities/suppliers/:id/edit',
            redirect: (to) => `/pages/suppliers/${to.params.id}/edit`
        },
        {
            path: '/entities/products',
            redirect: '/products/list'
        },
        {
            path: '/entities/products/new',
            redirect: '/products/new'
        },
        {
            path: '/entities/products/:id/edit',
            redirect: (to) => `/products/${to.params.id}/edit`
        },
        {
            path: '/entities/users',
            redirect: '/pages/users'
        },
        {
            path: '/entities/profiles',
            redirect: '/pages/profiles'
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
