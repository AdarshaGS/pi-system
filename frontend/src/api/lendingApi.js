import apiClient from '../api';

const LENDING_BASE_URL = '/v1/lending';

export const lendingApi = {
    // Get all lendings for a user
    getAllLendings: (userId) => {
        return apiClient.get(`${LENDING_BASE_URL}`, { params: { userId } });
    },

    // Get single lending by ID
    getLendingById: (id) => {
        return apiClient.get(`${LENDING_BASE_URL}/${id}`);
    },

    // Add new lending record
    addLending: (lendingData) => {
        return apiClient.post(`${LENDING_BASE_URL}`, lendingData);
    },

    // Update existing lending record
    updateLending: (id, lendingData) => {
        return apiClient.put(`${LENDING_BASE_URL}/${id}`, lendingData);
    },

    // Add repayment to a lending
    addRepayment: (lendingId, repaymentData) => {
        return apiClient.post(`${LENDING_BASE_URL}/${lendingId}/repayment`, repaymentData);
    },

    // Mark lending as fully paid/closed
    closeLending: (lendingId) => {
        return apiClient.put(`${LENDING_BASE_URL}/${lendingId}/close`);
    },

    // Send payment reminder notification
    sendReminder: (lendingId) => {
        return apiClient.post(`${LENDING_BASE_URL}/${lendingId}/send-reminder`);
    }
};
