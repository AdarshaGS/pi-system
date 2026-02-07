/**
 * Financial Goals API Integration Layer
 * 
 * Provides functions to interact with Financial Goals backend APIs
 * Base URL: http://localhost:8080/api/v1/goals
 * 
 * Features:
 * - Create, read, update, delete financial goals
 * - Goal projections and progress tracking
 * - Milestone management
 * - Contribution recording
 * 
 * Author: Pi-System
 * Date: February 5, 2026
 */

import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/v1/goals';

// ===========================
// FINANCIAL GOALS CRUD
// ===========================

/**
 * Create a new financial goal
 * @param {Object} goalData - Goal details (name, type, target amount, date, etc.)
 * @returns {Promise<Object>} Created goal object
 */
export const createGoal = async (goalData) => {
    try {
        const response = await axios.post(BASE_URL, goalData);
        return response.data;
    } catch (error) {
        console.error('Error creating goal:', error);
        throw error;
    }
};

/**
 * Get all financial goals for a user
 * @param {number} userId - User ID
 * @returns {Promise<Array>} List of financial goals
 */
export const getUserGoals = async (userId) => {
    try {
        const response = await axios.get(`${BASE_URL}/user/${userId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching goals:', error);
        throw error;
    }
};

/**
 * Get a specific financial goal by ID
 * @param {number} goalId - Goal ID
 * @returns {Promise<Object>} Goal details with projections
 */
export const getGoalById = async (goalId) => {
    try {
        const response = await axios.get(`${BASE_URL}/${goalId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching goal:', error);
        throw error;
    }
};

/**
 * Update an existing financial goal
 * @param {number} goalId - Goal ID
 * @param {Object} goalData - Updated goal details
 * @returns {Promise<Object>} Updated goal object
 */
export const updateGoal = async (goalId, goalData) => {
    try {
        const response = await axios.put(`${BASE_URL}/${goalId}`, goalData);
        return response.data;
    } catch (error) {
        console.error('Error updating goal:', error);
        throw error;
    }
};

/**
 * Delete a financial goal
 * @param {number} goalId - Goal ID
 * @returns {Promise<void>}
 */
export const deleteGoal = async (goalId) => {
    try {
        const response = await axios.delete(`${BASE_URL}/${goalId}`);
        return response.data;
    } catch (error) {
        console.error('Error deleting goal:', error);
        throw error;
    }
};

// ===========================
// GOAL PROJECTIONS
// ===========================

/**
 * Get goal projection (if on track to meet target)
 * @param {number} goalId - Goal ID
 * @returns {Promise<Object>} Projection with shortfall, required contribution, etc.
 */
export const getGoalProjection = async (goalId) => {
    try {
        const response = await axios.get(`${BASE_URL}/${goalId}/projection`);
        return response.data;
    } catch (error) {
        console.error('Error fetching goal projection:', error);
        throw error;
    }
};

/**
 * Calculate what-if scenario with different contribution amount
 * @param {number} goalId - Goal ID
 * @param {number} monthlyContribution - Hypothetical monthly contribution
 * @returns {Promise<Object>} Projection with new contribution amount
 */
export const calculateWhatIf = async (goalId, monthlyContribution) => {
    try {
        const response = await axios.get(`${BASE_URL}/${goalId}/what-if`, {
            params: { monthlyContribution }
        });
        return response.data;
    } catch (error) {
        console.error('Error calculating what-if scenario:', error);
        throw error;
    }
};

// ===========================
// CONTRIBUTIONS
// ===========================

/**
 * Record a contribution towards a goal
 * @param {number} goalId - Goal ID
 * @param {Object} contributionData - Contribution details (amount, date, notes)
 * @returns {Promise<Object>} Contribution record
 */
export const recordContribution = async (goalId, contributionData) => {
    try {
        const response = await axios.post(`${BASE_URL}/${goalId}/contribution`, contributionData);
        return response.data;
    } catch (error) {
        console.error('Error recording contribution:', error);
        throw error;
    }
};

/**
 * Get contribution history for a goal
 * @param {number} goalId - Goal ID
 * @returns {Promise<Array>} List of contributions
 */
export const getContributions = async (goalId) => {
    try {
        const response = await axios.get(`${BASE_URL}/${goalId}/contributions`);
        return response.data;
    } catch (error) {
        console.error('Error fetching contributions:', error);
        throw error;
    }
};

// ===========================
// MILESTONES
// ===========================

/**
 * Add a milestone to a goal
 * @param {number} goalId - Goal ID
 * @param {Object} milestoneData - Milestone details (name, targetAmount, targetDate)
 * @returns {Promise<Object>} Milestone record
 */
export const addMilestone = async (goalId, milestoneData) => {
    try {
        const response = await axios.post(`${BASE_URL}/${goalId}/milestone`, milestoneData);
        return response.data;
    } catch (error) {
        console.error('Error adding milestone:', error);
        throw error;
    }
};

/**
 * Get milestones for a goal
 * @param {number} goalId - Goal ID
 * @returns {Promise<Array>} List of milestones
 */
export const getMilestones = async (goalId) => {
    try {
        const response = await axios.get(`${BASE_URL}/${goalId}/milestones`);
        return response.data;
    } catch (error) {
        console.error('Error fetching milestones:', error);
        throw error;
    }
};

// ===========================
// PROGRESS TRACKING
// ===========================

/**
 * Get monthly progress for a goal
 * @param {number} goalId - Goal ID
 * @returns {Promise<Array>} Monthly progress data
 */
export const getProgress = async (goalId) => {
    try {
        const response = await axios.get(`${BASE_URL}/${goalId}/progress`);
        return response.data;
    } catch (error) {
        console.error('Error fetching progress:', error);
        throw error;
    }
};

/**
 * Get goals by status
 * @param {number} userId - User ID
 * @param {string} status - Status filter (ACTIVE, COMPLETED, PAUSED, ABANDONED)
 * @returns {Promise<Array>} Filtered list of goals
 */
export const getGoalsByStatus = async (userId, status) => {
    try {
        const response = await axios.get(`${BASE_URL}/user/${userId}/status/${status}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching goals by status:', error);
        throw error;
    }
};

/**
 * Get goals by type
 * @param {number} userId - User ID
 * @param {string} type - Goal type (RETIREMENT, PROPERTY, EDUCATION, EMERGENCY_FUND, etc.)
 * @returns {Promise<Array>} Filtered list of goals
 */
export const getGoalsByType = async (userId, type) => {
    try {
        const response = await axios.get(`${BASE_URL}/user/${userId}/type/${type}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching goals by type:', error);
        throw error;
    }
};

/**
 * Get goals ordered by priority
 * @param {number} userId - User ID
 * @returns {Promise<Array>} Goals sorted by priority
 */
export const getGoalsByPriority = async (userId) => {
    try {
        const response = await axios.get(`${BASE_URL}/user/${userId}/priority`);
        return response.data;
    } catch (error) {
        console.error('Error fetching goals by priority:', error);
        throw error;
    }
};

export default {
    createGoal,
    getUserGoals,
    getGoalById,
    updateGoal,
    deleteGoal,
    getGoalProjection,
    calculateWhatIf,
    recordContribution,
    getContributions,
    addMilestone,
    getMilestones,
    getProgress,
    getGoalsByStatus,
    getGoalsByType,
    getGoalsByPriority
};
