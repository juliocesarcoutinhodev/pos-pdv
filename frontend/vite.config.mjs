import { fileURLToPath, URL } from 'node:url';

import { PrimeVueResolver } from '@primevue/auto-import-resolver';
import tailwindcss from '@tailwindcss/vite';
import vue from '@vitejs/plugin-vue';
import Components from 'unplugin-vue-components/vite';
import { defineConfig, loadEnv } from 'vite';

function normalizeBasePath(basePath) {
    const raw = String(basePath || '/').trim();
    if (!raw || raw === '/') {
        return '/';
    }

    const withLeadingSlash = raw.startsWith('/') ? raw : `/${raw}`;
    return withLeadingSlash.endsWith('/') ? withLeadingSlash : `${withLeadingSlash}/`;
}

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), '');
    const appBasePath = normalizeBasePath(env.VITE_APP_BASE_PATH);

    return {
        base: appBasePath,
        optimizeDeps: {
            noDiscovery: true
        },
        plugins: [
            vue(),
            tailwindcss(),
            Components({
                resolvers: [PrimeVueResolver()]
            })
        ],
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url))
            }
        },
        css: {
            preprocessorOptions: {
                scss: {
                    api: 'modern-compiler'
                }
            }
        }
    };
});
