/**
 * Document Management API Integration
 * 
 * Provides functions to interact with the document management backend endpoints.
 * Handles file upload, download, categorization, and deletion.
 * 
 * Author: Pi-System
 * Date: February 6, 2026
 */

import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1/documents';

/**
 * Upload a document
 * @param {FormData} formData - Form data with file and metadata
 * @returns {Promise<Object>} Uploaded document details
 */
export const uploadDocument = async (formData) => {
    const response = await axios.post(`${BASE_URL}/upload`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
    return response.data;
};

/**
 * Get all documents for a user
 * @param {number} userId - User ID
 * @returns {Promise<Array>} List of documents
 */
export const getUserDocuments = async (userId) => {
    const response = await axios.get(`${BASE_URL}/user/${userId}`);
    return response.data;
};

/**
 * Get documents by category
 * @param {number} userId - User ID
 * @param {string} category - Document category
 * @returns {Promise<Array>} List of documents
 */
export const getDocumentsByCategory = async (userId, category) => {
    const response = await axios.get(`${BASE_URL}/user/${userId}/category/${category}`);
    return response.data;
};

/**
 * Get a specific document by ID
 * @param {number} documentId - Document ID
 * @returns {Promise<Object>} Document details
 */
export const getDocumentById = async (documentId) => {
    const response = await axios.get(`${BASE_URL}/${documentId}`);
    return response.data;
};

/**
 * Download a document
 * @param {number} documentId - Document ID
 * @returns {Promise<Blob>} File blob
 */
export const downloadDocument = async (documentId) => {
    const response = await axios.get(`${BASE_URL}/download/${documentId}`, {
        responseType: 'blob'
    });
    return response.data;
};

/**
 * Delete a document
 * @param {number} documentId - Document ID
 * @returns {Promise<void>}
 */
export const deleteDocument = async (documentId) => {
    await axios.delete(`${BASE_URL}/${documentId}`);
};

/**
 * Update document metadata
 * @param {number} documentId - Document ID
 * @param {Object} metadata - Updated metadata
 * @returns {Promise<Object>} Updated document
 */
export const updateDocumentMetadata = async (documentId, metadata) => {
    const response = await axios.put(`${BASE_URL}/${documentId}`, metadata);
    return response.data;
};

/**
 * Search documents
 * @param {number} userId - User ID
 * @param {string} query - Search query
 * @returns {Promise<Array>} Matching documents
 */
export const searchDocuments = async (userId, query) => {
    const response = await axios.get(`${BASE_URL}/search/${userId}`, {
        params: { query }
    });
    return response.data;
};

/**
 * Get document statistics
 * @param {number} userId - User ID
 * @returns {Promise<Object>} Document statistics
 */
export const getDocumentStats = async (userId) => {
    const response = await axios.get(`${BASE_URL}/stats/${userId}`);
    return response.data;
};
