/**
 * Cash Flow Analysis API Integration
 * 
 * Provides functions to interact with the cash flow analysis backend endpoints.
 * Handles monthly cash flow tracking, projections, and analysis.
 * 
 * Author: Pi-System
 * Date: February 5, 2026
 */

import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1/cashflow';

/**
 * Get monthly cash flow analysis
 * @param {number} userId - User ID
 * @param {string} month - Month in YYYY-MM format
 * @returns {Promise<Object>} Cash flow analysis data
 */
export const getMonthlyCashFlow = async (userId, month) => {
    const response = await axios.get(`${BASE_URL}/monthly/${userId}`, {
        params: { month }
    });
    return response.data;
};

/**
 * Get cash flow analysis for a date range
 * @param {number} userId - User ID
 * @param {string} startDate - Start date (YYYY-MM-DD)
 * @param {string} endDate - End date (YYYY-MM-DD)
 * @returns {Promise<Object>} Cash flow analysis
 */
export const getCashFlowRange = async (userId, startDate, endDate) => {
    const response = await axios.get(`${BASE_URL}/range/${userId}`, {
        params: { startDate, endDate }
    });
    return response.data;
};

/**
 * Get cash flow projection
 * @param {number} userId - User ID
 * @param {number} months - Number of months to project
 * @returns {Promise<Array>} Projected cash flow data
 */
export const getCashFlowProjection = async (userId, months = 6) => {
    const response = await axios.get(`${BASE_URL}/projection/${userId}`, {
        params: { months }
    });
    return response.data;
};

/**
 * Get category-wise cash flow breakdown
 * @param {number} userId - User ID
 * @param {string} month - Month in YYYY-MM format
 * @returns {Promise<Object>} Category breakdown
 */
export const getCategoryBreakdown = async (userId, month) => {
    const response = await axios.get(`${BASE_URL}/categories/${userId}`, {
        params: { month }
    });
    return response.data;
};

/**
 * Get cash flow trends
 * @param {number} userId - User ID
 * @param {number} months - Number of months for trend analysis
 * @returns {Promise<Array>} Trend data
 */
export const getCashFlowTrends = async (userId, months = 12) => {
    const response = await axios.get(`${BASE_URL}/trends/${userId}`, {
        params: { months }
    });
    return response.data;
};

/**
 * Get net cash flow summary
 * @param {number} userId - User ID
 * @returns {Promise<Object>} Net cash flow summary
 */
export const getNetCashFlow = async (userId) => {
    const response = await axios.get(`${BASE_URL}/net/${userId}`);
    return response.data;
};

/**
 * Get cash flow alerts (warnings about negative cash flow)
 * @param {number} userId - User ID
 * @returns {Promise<Array>} Cash flow alerts
 */
export const getCashFlowAlerts = async (userId) => {
    const response = await axios.get(`${BASE_URL}/alerts/${userId}`);
    return response.data;
};

/**
 * Get savings rate analysis
 * @param {number} userId - User ID
 * @param {number} months - Number of months to analyze
 * @returns {Promise<Object>} Savings rate data
 */
export const getSavingsRate = async (userId, months = 6) => {
    const response = await axios.get(`${BASE_URL}/savings-rate/${userId}`, {
        params: { months }
    });
    return response.data;
};
