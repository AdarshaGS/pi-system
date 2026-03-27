/**
 * Recurring Transactions API Integration
 * 
 * Provides functions to interact with the recurring transactions backend endpoints.
 * Handles template creation, scheduling, and automatic transaction generation.
 * 
 * Author: Pi-System
 * Date: February 5, 2026
 */

import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1/recurring';

/**
 * Create a new recurring transaction template
 * @param {Object} templateData - Template data
 * @returns {Promise<Object>} Created template
 */
export const createTemplate = async (templateData) => {
    const response = await axios.post(`${BASE_URL}/templates`, templateData);
    return response.data;
};

/**
 * Get all recurring templates for a user
 * @param {number} userId - User ID
 * @returns {Promise<Array>} List of templates
 */
export const getUserTemplates = async (userId) => {
    const response = await axios.get(`${BASE_URL}/templates/user/${userId}`);
    return response.data;
};

/**
 * Get a specific template by ID
 * @param {number} templateId - Template ID
 * @returns {Promise<Object>} Template details
 */
export const getTemplateById = async (templateId) => {
    const response = await axios.get(`${BASE_URL}/templates/${templateId}`);
    return response.data;
};

/**
 * Update an existing template
 * @param {number} templateId - Template ID
 * @param {Object} templateData - Updated template data
 * @returns {Promise<Object>} Updated template
 */
export const updateTemplate = async (templateId, templateData) => {
    const response = await axios.put(`${BASE_URL}/templates/${templateId}`, templateData);
    return response.data;
};

/**
 * Delete a template
 * @param {number} templateId - Template ID
 * @returns {Promise<void>}
 */
export const deleteTemplate = async (templateId) => {
    await axios.delete(`${BASE_URL}/templates/${templateId}`);
};

/**
 * Get active templates for a user
 * @param {number} userId - User ID
 * @returns {Promise<Array>} List of active templates
 */
export const getActiveTemplates = async (userId) => {
    const response = await axios.get(`${BASE_URL}/templates/user/${userId}/active`);
    return response.data;
};

/**
 * Get templates by frequency
 * @param {number} userId - User ID
 * @param {string} frequency - Frequency (DAILY, WEEKLY, MONTHLY, YEARLY)
 * @returns {Promise<Array>} List of templates
 */
export const getTemplatesByFrequency = async (userId, frequency) => {
    const response = await axios.get(`${BASE_URL}/templates/user/${userId}/frequency/${frequency}`);
    return response.data;
};

/**
 * Get upcoming scheduled transactions
 * @param {number} userId - User ID
 * @param {number} days - Number of days to look ahead
 * @returns {Promise<Array>} List of upcoming transactions
 */
export const getUpcomingTransactions = async (userId, days = 30) => {
    const response = await axios.get(`${BASE_URL}/upcoming/${userId}`, {
        params: { days }
    });
    return response.data;
};

/**
 * Generate transactions for a specific template
 * @param {number} templateId - Template ID
 * @returns {Promise<Array>} Generated transactions
 */
export const generateTransactions = async (templateId) => {
    const response = await axios.post(`${BASE_URL}/generate/${templateId}`);
    return response.data;
};

/**
 * Pause a recurring template
 * @param {number} templateId - Template ID
 * @returns {Promise<Object>} Updated template
 */
export const pauseTemplate = async (templateId) => {
    const response = await axios.post(`${BASE_URL}/templates/${templateId}/pause`);
    return response.data;
};

/**
 * Resume a paused template
 * @param {number} templateId - Template ID
 * @returns {Promise<Object>} Updated template
 */
export const resumeTemplate = async (templateId) => {
    const response = await axios.post(`${BASE_URL}/templates/${templateId}/resume`);
    return response.data;
};

/**
 * Get generated transactions for a template
 * @param {number} templateId - Template ID
 * @returns {Promise<Array>} List of generated transactions
 */
export const getGeneratedTransactions = async (templateId) => {
    const response = await axios.get(`${BASE_URL}/templates/${templateId}/transactions`);
    return response.data;
};

/**
 * Skip the next occurrence of a template
 * @param {number} templateId - Template ID
 * @returns {Promise<Object>} Updated template
 */
export const skipNextOccurrence = async (templateId) => {
    const response = await axios.post(`${BASE_URL}/templates/${templateId}/skip-next`);
    return response.data;
};
