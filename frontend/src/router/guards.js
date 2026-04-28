const PUBLIC_PATHS = ['/auth/login', '/auth/access', '/auth/error', '/landing'];

function isPublic(path) {
    if (path === '/') return true; // Login page
    if (PUBLIC_PATHS.includes(path)) return true;
    return PUBLIC_PATHS.some((p) => path.startsWith(p.split('/').slice(0, 3).join('/')));
}

function hasCashierRole(user) {
    if (!user || !Array.isArray(user.roles)) {
        return false;
    }

    const normalizedRoles = user.roles.map((role) => String(role).toUpperCase());
    return normalizedRoles.some((role) => role === 'CAIXA' || role === 'CASHIER');
}

function hasAdminRole(user) {
    if (!user || !Array.isArray(user.roles)) {
        return false;
    }

    const normalizedRoles = user.roles.map((role) => String(role).toUpperCase());
    return normalizedRoles.includes('ADMIN');
}

function readSessionUser() {
    const raw = sessionStorage.getItem('user');
    if (!raw) {
        return null;
    }

    try {
        return JSON.parse(raw);
    } catch {
        sessionStorage.removeItem('user');
        return null;
    }
}

export function setupAuthGuards(router) {
    router.beforeEach((to, from, next) => {
        const sessionUser = readSessionUser();
        const isAuthed = !!sessionUser;

        if (!isAuthed && !isPublic(to.path)) {
            next({ name: 'login' });
            return;
        }

        if (isAuthed && hasCashierRole(sessionUser) && !hasAdminRole(sessionUser) && to.path !== '/sales/pos') {
            next('/sales/pos');
            return;
        }

        if (isAuthed && hasAdminRole(sessionUser) && to.path === '/sales/pos') {
            next('/sales/monitoring');
            return;
        }

        if (isAuthed && !hasAdminRole(sessionUser) && to.path === '/sales/monitoring') {
            next('/dashboard');
            return;
        }

        next();
    });
}
