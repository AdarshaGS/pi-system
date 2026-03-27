/**
 * Credit Score API Integration
 * 
 * Provides functions to interact with the credit score tracking backend endpoints.
 * Handles credit score recording, history tracking, and analysis.
 * 
 * Author: Pi-System
 * Date: February 6, 2026
 */

import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1/credit-score';

/**
 * Record a new credit score
 * @param {Object} scoreData - Credit score data
 * @returns {Promise<Object>} Created credit score record
 */
export const recordCreditScore = async (scoreData) => {
    const response = await axios.post(`${BASE_URL}`, scoreData);
    return response.data;
};

/**
 * Get credit score history for a user
 * @param {number} userId - User ID
 * @returns {Promise<Array>} Credit score history
 */
export const getCreditScoreHistory = async (userId) => {
    const response = await axios.get(`${BASE_URL}/user/${userId}`);
    return response.data;
};

/**
 * Get latest credit score
 * @param {number} userId - User ID
 * @returns {Promise<Object>} Latest credit score
 */
export const getLatestCreditScore = async (userId) => {
    const response = await axios.get(`${BASE_URL}/user/${userId}/latest`);
    return response.data;
};

/**
 * Get credit score by ID
 * @param {number} scoreId - Score ID
 * @returns {Promise<Object>} Credit score details
 */
export const getCreditScoreById = async (scoreId) => {
    const response = await axios.get(`${BASE_URL}/${scoreId}`);
    return response.data;
};

/**
 * Delete a credit score record
 * @param {number} scoreId - Score ID
 * @returns {Promise<void>}
 */
export const deleteCreditScore = async (scoreId) => {
    await axios.delete(`${BASE_URL}/${scoreId}`);
};

/**
 * Get credit score trend analysis
 * @param {number} userId - User ID
 * @param {number} months - Number of months to analyze
 * @returns {Promise<Object>} Trend analysis
 */
export const getCreditScoreTrend = async (userId, months = 12) => {
    const response = await axios.get(`${BASE_URL}/user/${userId}/trend`, {
        params: { months }
    });
    return response.data;
};

/**
 * Get credit score statistics
 * @param {number} userId - User ID
 * @returns {Promise<Object>} Statistics
 */
export const getCreditScoreStats = async (userId) => {
    const response = await axios.get(`${BASE_URL}/user/${userId}/stats`);
    return response.data;
};
