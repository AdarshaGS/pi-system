/**
 * Insurance API Integration Layer
 * 
 * Provides functions to interact with Insurance Module backend APIs
 * Base URL: http://localhost:8080/api/v1/insurance
 * 
 * Features:
 * - Create, read, update, delete insurance policies
 * - Premium payment tracking
 * - Policy expiry alerts
 * - Coverage summaries
 * 
 * Author: Pi-System
 * Date: February 5, 2026
 */

import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1/insurance';

// ===========================
// INSURANCE POLICY CRUD
// ===========================

/**
 * Create a new insurance policy
 * @param {Object} policyData - Policy details (policyNumber, type, provider, premium, etc.)
 * @returns {Promise<Object>} Created policy object
 */
export const createInsurancePolicy = async (policyData) => {
    try {
        const response = await axios.post(BASE_URL, policyData);
        return response.data;
    } catch (error) {
        console.error('Error creating insurance policy:', error);
        throw error;
    }
};

/**
 * Get all insurance policies for a user
 * @param {number} userId - User ID
 * @returns {Promise<Array>} List of insurance policies
 */
export const getInsurancePolicies = async (userId) => {
    try {
        const response = await axios.get(`${BASE_URL}/user/${userId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching insurance policies:', error);
        throw error;
    }
};

/**
 * Get a specific insurance policy by ID
 * @param {number} policyId - Policy ID
 * @returns {Promise<Object>} Policy details
 */
export const getInsurancePolicyById = async (policyId) => {
    try {
        const response = await axios.get(`${BASE_URL}/${policyId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching insurance policy:', error);
        throw error;
    }
};

/**
 * Update an existing insurance policy
 * @param {number} policyId - Policy ID
 * @param {Object} policyData - Updated policy details
 * @returns {Promise<Object>} Updated policy object
 */
export const updateInsurancePolicy = async (policyId, policyData) => {
    try {
        const response = await axios.put(`${BASE_URL}/${policyId}`, policyData);
        return response.data;
    } catch (error) {
        console.error('Error updating insurance policy:', error);
        throw error;
    }
};

/**
 * Delete an insurance policy
 * @param {number} policyId - Policy ID
 * @returns {Promise<void>}
 */
export const deleteInsurancePolicy = async (policyId) => {
    try {
        const response = await axios.delete(`${BASE_URL}/${policyId}`);
        return response.data;
    } catch (error) {
        console.error('Error deleting insurance policy:', error);
        throw error;
    }
};

// ===========================
// PREMIUM PAYMENTS
// ===========================

/**
 * Record a premium payment
 * @param {number} policyId - Policy ID
 * @param {Object} paymentData - Payment details (amount, date, method)
 * @returns {Promise<Object>} Payment record
 */
export const recordPremiumPayment = async (policyId, paymentData) => {
    try {
        const response = await axios.post(`${BASE_URL}/${policyId}/premium-payment`, paymentData);
        return response.data;
    } catch (error) {
        console.error('Error recording premium payment:', error);
        throw error;
    }
};

/**
 * Get premium payment history for a policy
 * @param {number} policyId - Policy ID
 * @returns {Promise<Array>} List of premium payments
 */
export const getPremiumPayments = async (policyId) => {
    try {
        const response = await axios.get(`${BASE_URL}/${policyId}/premium-payments`);
        return response.data;
    } catch (error) {
        console.error('Error fetching premium payments:', error);
        throw error;
    }
};

// ===========================
// ANALYTICS & SUMMARIES
// ===========================

/**
 * Get insurance coverage summary for a user
 * @param {number} userId - User ID
 * @returns {Promise<Object>} Coverage summary (total premium, coverage amount, etc.)
 */
export const getInsuranceSummary = async (userId) => {
    try {
        const response = await axios.get(`${BASE_URL}/user/${userId}/summary`);
        return response.data;
    } catch (error) {
        console.error('Error fetching insurance summary:', error);
        throw error;
    }
};

/**
 * Get policies expiring soon (within specified days)
 * @param {number} userId - User ID
 * @param {number} days - Number of days (default 30)
 * @returns {Promise<Array>} List of expiring policies
 */
export const getExpiringPolicies = async (userId, days = 30) => {
    try {
        const response = await axios.get(`${BASE_URL}/user/${userId}/expiring?days=${days}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching expiring policies:', error);
        throw error;
    }
};

/**
 * Get premium due reminders
 * @param {number} userId - User ID
 * @returns {Promise<Array>} List of policies with upcoming premium due
 */
export const getPremiumDueReminders = async (userId) => {
    try {
        const response = await axios.get(`${BASE_URL}/user/${userId}/premium-due`);
        return response.data;
    } catch (error) {
        console.error('Error fetching premium due reminders:', error);
        throw error;
    }
};

/**
 * Get insurance policies by type
 * @param {number} userId - User ID
 * @param {string} type - Policy type (LIFE, HEALTH, VEHICLE, PROPERTY, TERM, etc.)
 * @returns {Promise<Array>} Filtered list of policies
 */
export const getPoliciesByType = async (userId, type) => {
    try {
        const response = await axios.get(`${BASE_URL}/user/${userId}/type/${type}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching policies by type:', error);
        throw error;
    }
};

// ===========================
// CLAIMS MANAGEMENT
// ===========================

/**
 * File an insurance claim
 * @param {number} policyId - Policy ID
 * @param {Object} claimData - Claim details (amount, date, reason, documents)
 * @returns {Promise<Object>} Claim record
 */
export const fileInsuranceClaim = async (policyId, claimData) => {
    try {
        const response = await axios.post(`${BASE_URL}/${policyId}/claim`, claimData);
        return response.data;
    } catch (error) {
        console.error('Error filing insurance claim:', error);
        throw error;
    }
};

/**
 * Get claim history for a policy
 * @param {number} policyId - Policy ID
 * @returns {Promise<Array>} List of claims
 */
export const getPolicyClaims = async (policyId) => {
    try {
        const response = await axios.get(`${BASE_URL}/${policyId}/claims`);
        return response.data;
    } catch (error) {
        console.error('Error fetching policy claims:', error);
        throw error;
    }
};

export default {
    createInsurancePolicy,
    getInsurancePolicies,
    getInsurancePolicyById,
    updateInsurancePolicy,
    deleteInsurancePolicy,
    recordPremiumPayment,
    getPremiumPayments,
    getInsuranceSummary,
    getExpiringPolicies,
    getPremiumDueReminders,
    getPoliciesByType,
    fileInsuranceClaim,
    getPolicyClaims
};
