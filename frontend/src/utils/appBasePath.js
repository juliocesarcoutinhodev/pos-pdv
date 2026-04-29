function normalizeBasePath(basePath) {
    const raw = String(basePath || '/').trim();
    if (!raw || raw === '/') {
        return '/';
    }

    const withLeadingSlash = raw.startsWith('/') ? raw : `/${raw}`;
    return withLeadingSlash.endsWith('/') ? withLeadingSlash : `${withLeadingSlash}/`;
}

export function getAppBasePath() {
    return normalizeBasePath(import.meta.env.BASE_URL);
}

export function toAppPath(path) {
    const normalizedPath = String(path || '').replace(/^\/+/, '');
    return `${getAppBasePath()}${normalizedPath}`;
}
