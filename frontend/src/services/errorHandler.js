const DEFAULT_MESSAGES = {
    network: 'Não foi possível conectar ao servidor. Verifique sua conexão.',
    timeout: 'Tempo limite excedido. Tente novamente.',
    notFound: 'Recurso não encontrado.',
    generic: 'Ocorreu um erro inesperado. Tente novamente em instantes.'
};

const TOAST_BY_STATUS = {
    400: 'warn',
    401: 'error',
    403: 'error',
    404: 'warn',
    409: 'error',
    410: 'error',
    500: 'error'
};

/**
 * Extracts a human-readable message from an API error response.
 * Handles both simple { message } and validation { details[] } shapes.
 * @param {import('axios').AxiosError} error
 * @returns {string | null}
 */
function extractApiMessage(error) {
    const data = error.response?.data;
    if (!data) return null;

    if (data.message) return data.message;

    if (Array.isArray(data.details) && data.details.length > 0) {
        return data.details.map((d) => d.message).join('\n');
    }

    return null;
}

function mapSeverity(status) {
    return TOAST_BY_STATUS[status] || 'error';
}

/**
 * Shows a toast extracted from the backend error response.
 * @param {import('primevue/usetoast').ToastServiceMethods} toast - Toast instance from useToast()
 * @param {import('axios').AxiosError} error
 * @param {object} [options]
 * @param {number} [options.life=5000]
 */
function showApiErrorToast(toast, error, options = {}) {
    const { life = 5000 } = options;

    if (error.code === 'ERR_NETWORK') {
        toast.add({
            severity: 'error',
            summary: 'Erro de conexão',
            detail: DEFAULT_MESSAGES.network,
            life: 6000
        });
        return;
    }

    if (error.code === 'ERR_CANCELED' || error.code === 'ECONNABORTED') {
        toast.add({
            severity: 'warn',
            summary: 'Tempo expirado',
            detail: DEFAULT_MESSAGES.timeout,
            life: 4000
        });
        return;
    }

    const status = error.response?.status;
    const data = error.response?.data;
    const summary = data?.error || 'Erro';
    const detail = data?.message || data?.details?.map((d) => d.message).join('\n') || DEFAULT_MESSAGES.generic;

    toast.add({
        severity: mapSeverity(status),
        summary,
        detail,
        life
    });
}

/**
 * Extracts rate limit info from an axios response error.
 * @param {import('axios').AxiosError} error
 * @returns {{ retryAfter?: number } | null}
 */
function extractRateLimit(error) {
    if (error.response?.status !== 429) return null;

    const retryAfter = parseInt(error.response.headers['retry-after'], 10) || 20;
    return { retryAfter };
}

export function useErrorHandler() {
    return {
        extractApiMessage,
        showApiErrorToast,
        extractRateLimit
    };
}
