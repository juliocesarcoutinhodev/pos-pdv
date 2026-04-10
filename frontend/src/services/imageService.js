import api from './api.js';

function normalizeImageUpload(data) {
    return {
        imageId: data?.imageId ?? null,
        fileName: data?.fileName ?? '',
        contentType: data?.contentType ?? '',
        size: Number.isFinite(data?.size) ? data.size : 0,
        uploadedAt: data?.uploadedAt ?? null,
        downloadUrl: data?.downloadUrl ?? '',
        metadataUrl: data?.metadataUrl ?? ''
    };
}

function normalizeImageMetadata(data) {
    return {
        imageId: data?.imageId ?? null,
        fileName: data?.fileName ?? '',
        contentType: data?.contentType ?? '',
        size: Number.isFinite(data?.size) ? data.size : 0,
        lastModified: data?.lastModified ?? null,
        downloadUrl: data?.downloadUrl ?? ''
    };
}

/**
 * Uploads an image to storage.
 * @param {File} file
 */
export async function uploadImage(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post('/api/v1/images/upload', formData);
    return normalizeImageUpload(response.data);
}

/**
 * Downloads image as blob.
 * @param {string} imageId
 */
export async function downloadImageBlob(imageId) {
    const response = await api.get(`/api/v1/images/${imageId}`, {
        responseType: 'blob'
    });
    return response.data;
}

/**
 * Gets image metadata.
 * @param {string} imageId
 */
export async function getImageMetadata(imageId) {
    const response = await api.get(`/api/v1/images/${imageId}/metadata`);
    return normalizeImageMetadata(response.data);
}
