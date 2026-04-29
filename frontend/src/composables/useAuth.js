import { ref, computed } from 'vue';
import { login as loginApi, logout as logoutApi } from '@/services/authService.js';
import { toAppPath } from '@/utils/appBasePath.js';

const user = ref(null);
const isAuthenticated = computed(() => user.value !== null);
const loading = ref(false);

function hasCashierRole(roles) {
    if (!Array.isArray(roles)) {
        return false;
    }

    const normalizedRoles = roles.map((role) => String(role).toUpperCase());
    return normalizedRoles.some((role) => role === 'CAIXA' || role === 'CASHIER');
}

function hasAdminRole(roles) {
    if (!Array.isArray(roles)) {
        return false;
    }

    const normalizedRoles = roles.map((role) => String(role).toUpperCase());
    return normalizedRoles.includes('ADMIN');
}

function hydrateFromStorage() {
    const stored = sessionStorage.getItem('user');
    if (stored) {
        try {
            user.value = JSON.parse(stored);
        } catch {
            sessionStorage.removeItem('user');
        }
    }
}

hydrateFromStorage();

async function handleLogin(email, password) {
    loading.value = true;
    try {
        const data = await loginApi(email, password);
        localStorage.setItem('access_token', data.accessToken);
        sessionStorage.setItem('user', JSON.stringify(data));
        user.value = data;
        const forcePdv = hasCashierRole(data.roles) && !hasAdminRole(data.roles);
        window.location.href = forcePdv ? toAppPath('/sales/pos') : toAppPath('/dashboard');
    } finally {
        loading.value = false;
    }
}

async function logout() {
    try {
        await logoutApi();
    } catch {
        // Best effort — clear local state anyway
    }
    localStorage.removeItem('access_token');
    sessionStorage.removeItem('user');
    user.value = null;
    window.location.href = toAppPath('/auth/login');
}

function getAccessToken() {
    return localStorage.getItem('access_token');
}

export function useAuth() {
    return {
        user,
        isAuthenticated,
        loading,
        hydrateFromStorage,
        handleLogin,
        logout,
        getAccessToken
    };
}
