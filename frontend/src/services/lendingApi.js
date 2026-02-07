import apiClient from '../api';

const LENDING_BASE_URL = '/v1/lending';

// Loan Management
export const getAllLoans = (userId) => {
  return apiClient.get(`${LENDING_BASE_URL}/loans`, { params: { userId } });
};

export const getLoanById = (loanId) => {
  return apiClient.get(`${LENDING_BASE_URL}/loans/${loanId}`);
};

export const createLoan = (loanData) => {
  return apiClient.post(`${LENDING_BASE_URL}/loans`, loanData);
};

export const updateLoan = (loanId, loanData) => {
  return apiClient.put(`${LENDING_BASE_URL}/loans/${loanId}`, loanData);
};

export const deleteLoan = (loanId) => {
  return apiClient.delete(`${LENDING_BASE_URL}/loans/${loanId}`);
};

export const getLoanSummary = (userId) => {
  return apiClient.get(`${LENDING_BASE_URL}/loans/summary`, { params: { userId } });
};

// Repayment Management
export const getAllRepayments = (userId) => {
  return apiClient.get(`${LENDING_BASE_URL}/repayments`, { params: { userId } });
};

export const getRepaymentsByLoan = (loanId) => {
  return apiClient.get(`${LENDING_BASE_URL}/repayments/loan/${loanId}`);
};

export const recordRepayment = (repaymentData) => {
  return apiClient.post(`${LENDING_BASE_URL}/repayments`, repaymentData);
};

export const updateRepayment = (repaymentId, repaymentData) => {
  return apiClient.put(`${LENDING_BASE_URL}/repayments/${repaymentId}`, repaymentData);
};

export const deleteRepayment = (repaymentId) => {
  return apiClient.delete(`${LENDING_BASE_URL}/repayments/${repaymentId}`);
};

export const getUpcomingPayments = (userId) => {
  return apiClient.get(`${LENDING_BASE_URL}/repayments/upcoming`, { params: { userId } });
};

export const getOverduePayments = (userId) => {
  return apiClient.get(`${LENDING_BASE_URL}/repayments/overdue`, { params: { userId } });
};

// Analytics
export const getLendingAnalytics = (userId) => {
  return apiClient.get(`${LENDING_BASE_URL}/analytics`, { params: { userId } });
};

export const getRepaymentSchedule = (loanId) => {
  return apiClient.get(`${LENDING_BASE_URL}/analytics/schedule/${loanId}`);
};

export const getAmortizationSchedule = (loanId) => {
  return apiClient.get(`${LENDING_BASE_URL}/analytics/amortization/${loanId}`);
};

export const getInterestBreakdown = (userId) => {
  return apiClient.get(`${LENDING_BASE_URL}/analytics/interest-breakdown`, { params: { userId } });
};

const lendingApi = {
  getAllLoans,
  getLoanById,
  createLoan,
  updateLoan,
  deleteLoan,
  getLoanSummary,
  getAllRepayments,
  getRepaymentsByLoan,
  recordRepayment,
  updateRepayment,
  deleteRepayment,
  getUpcomingPayments,
  getOverduePayments,
  getLendingAnalytics,
  getRepaymentSchedule,
  getAmortizationSchedule,
  getInterestBreakdown
};

export default lendingApi;
