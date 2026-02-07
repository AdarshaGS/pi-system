/**
 * Portfolio Rebalancing API Integration
 * 
 * Provides functions to interact with portfolio rebalancing backend endpoints.
 * Handles rebalancing suggestions, asset allocation, and portfolio adjustments.
 * 
 * Author: Pi-System
 * Date: February 6, 2026
 */

import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1/portfolio/rebalance';

/**
 * Get rebalancing suggestions for a portfolio
 * @param {number} portfolioId - Portfolio ID
 * @param {number} threshold - Drift threshold percentage (optional)
 * @returns {Promise<Object>} Rebalancing suggestions
 */
export const getRebalancingSuggestions = async (portfolioId, threshold = 5) => {
    const response = await axios.get(`${BASE_URL}/suggestions/${portfolioId}`, {
        params: { threshold }
    });
    return response.data;
};

/**
 * Get current asset allocation
 * @param {number} portfolioId - Portfolio ID
 * @returns {Promise<Array>} Asset allocation
 */
export const getCurrentAllocation = async (portfolioId) => {
    const response = await axios.get(`${BASE_URL}/allocation/${portfolioId}`);
    return response.data;
};

/**
 * Get target asset allocation
 * @param {number} portfolioId - Portfolio ID
 * @returns {Promise<Array>} Target allocation
 */
export const getTargetAllocation = async (portfolioId) => {
    const response = await axios.get(`${BASE_URL}/target/${portfolioId}`);
    return response.data;
};

/**
 * Execute portfolio rebalancing
 * @param {number} portfolioId - Portfolio ID
 * @param {Object} rebalanceData - Rebalancing instructions
 * @returns {Promise<Object>} Rebalance result
 */
export const executeRebalancing = async (portfolioId, rebalanceData) => {
    const response = await axios.post(`${BASE_URL}/execute/${portfolioId}`, rebalanceData);
    return response.data;
};

/**
 * Get allocation drift analysis
 * @param {number} portfolioId - Portfolio ID
 * @returns {Promise<Object>} Drift analysis
 */
export const getAllocationDrift = async (portfolioId) => {
    const response = await axios.get(`${BASE_URL}/drift/${portfolioId}`);
    return response.data;
};

/**
 * Update target allocation
 * @param {number} portfolioId - Portfolio ID
 * @param {Array} targetAllocation - New target allocation
 * @returns {Promise<Object>} Updated allocation
 */
export const updateTargetAllocation = async (portfolioId, targetAllocation) => {
    const response = await axios.put(`${BASE_URL}/target/${portfolioId}`, targetAllocation);
    return response.data;
};

/**
 * Get rebalancing history
 * @param {number} portfolioId - Portfolio ID
 * @returns {Promise<Array>} Rebalancing history
 */
export const getRebalancingHistory = async (portfolioId) => {
    const response = await axios.get(`${BASE_URL}/history/${portfolioId}`);
    return response.data;
};

/**
 * Simulate rebalancing
 * @param {number} portfolioId - Portfolio ID
 * @param {Object} simulationData - Simulation parameters
 * @returns {Promise<Object>} Simulation result
 */
export const simulateRebalancing = async (portfolioId, simulationData) => {
    const response = await axios.post(`${BASE_URL}/simulate/${portfolioId}`, simulationData);
    return response.data;
};
