const PUBLIC_PATHS = ['/auth/login', '/auth/access', '/auth/error', '/landing'];

function isPublic(path) {
    if (path === '/') return true; // Login page
    if (PUBLIC_PATHS.includes(path)) return true;
    return PUBLIC_PATHS.some((p) => path.startsWith(p.split('/').slice(0, 3).join('/')));
}

export function setupAuthGuards(router) {
    router.beforeEach((to, from, next) => {
        const isAuthed = !!sessionStorage.getItem('user');

        if (!isAuthed && !isPublic(to.path)) {
            next({ name: 'login' });
        } else {
            next();
        }
    });
}
