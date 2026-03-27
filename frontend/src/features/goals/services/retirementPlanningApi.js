/**
 * Retirement Planning API Integration
 * 
 * Provides functions to interact with retirement planning backend endpoints.
 * Handles retirement scenarios, calculations, and projections.
 * 
 * Author: Pi-System
 * Date: February 6, 2026
 */

import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1/retirement';

/**
 * Create a retirement plan
 * @param {Object} planData - Retirement plan data
 * @returns {Promise<Object>} Created plan
 */
export const createRetirementPlan = async (planData) => {
    const response = await axios.post(`${BASE_URL}`, planData);
    return response.data;
};

/**
 * Get retirement plan by user ID
 * @param {number} userId - User ID
 * @returns {Promise<Object>} Retirement plan
 */
export const getRetirementPlanByUser = async (userId) => {
    const response = await axios.get(`${BASE_URL}/user/${userId}`);
    return response.data;
};

/**
 * Get retirement plan by ID
 * @param {number} planId - Plan ID
 * @returns {Promise<Object>} Retirement plan details
 */
export const getRetirementPlanById = async (planId) => {
    const response = await axios.get(`${BASE_URL}/${planId}`);
    return response.data;
};

/**
 * Update retirement plan
 * @param {number} planId - Plan ID
 * @param {Object} planData - Updated plan data
 * @returns {Promise<Object>} Updated plan
 */
export const updateRetirementPlan = async (planId, planData) => {
    const response = await axios.put(`${BASE_URL}/${planId}`, planData);
    return response.data;
};

/**
 * Delete retirement plan
 * @param {number} planId - Plan ID
 * @returns {Promise<void>}
 */
export const deleteRetirementPlan = async (planId) => {
    await axios.delete(`${BASE_URL}/${planId}`);
};

/**
 * Calculate retirement corpus
 * @param {Object} calculationData - Calculation parameters
 * @returns {Promise<Object>} Calculation result
 */
export const calculateRetirementCorpus = async (calculationData) => {
    const response = await axios.post(`${BASE_URL}/calculate`, calculationData);
    return response.data;
};

/**
 * Get retirement projection
 * @param {number} planId - Plan ID
 * @returns {Promise<Object>} Projection data
 */
export const getRetirementProjection = async (planId) => {
    const response = await axios.get(`${BASE_URL}/${planId}/projection`);
    return response.data;
};

/**
 * Calculate monthly retirement income
 * @param {Object} incomeData - Income calculation parameters
 * @returns {Promise<Object>} Income calculation result
 */
export const calculateRetirementIncome = async (incomeData) => {
    const response = await axios.post(`${BASE_URL}/calculate-income`, incomeData);
    return response.data;
};

/**
 * Get retirement readiness assessment
 * @param {number} planId - Plan ID
 * @returns {Promise<Object>} Readiness assessment
 */
export const getRetirementReadiness = async (planId) => {
    const response = await axios.get(`${BASE_URL}/${planId}/readiness`);
    return response.data;
};
