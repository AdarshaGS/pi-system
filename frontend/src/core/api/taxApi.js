import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

/**
 * Tax API Integration Layer
 */

// Create or update tax details
export const createOrUpdateTaxDetails = async (userId, taxData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/tax`, {
      userId,
      ...taxData
    });
    return response.data;
  } catch (error) {
    console.error('Error creating/updating tax details:', error);
    throw error;
  }
};

// Get tax details for a user
export const getTaxDetails = async (userId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/tax/${userId}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching tax details:', error);
    throw error;
  }
};

// Get regime comparison
export const getRegimeComparison = async (userId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/tax/${userId}/regime-comparison`);
    return response.data;
  } catch (error) {
    console.error('Error fetching regime comparison:', error);
    throw error;
  }
};

// Record capital gains transaction
export const recordCapitalGains = async (userId, capitalGainsData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/tax/${userId}/capital-gains`, capitalGainsData);
    return response.data;
  } catch (error) {
    console.error('Error recording capital gains:', error);
    throw error;
  }
};

// Get capital gains summary
export const getCapitalGainsSummary = async (userId, financialYear) => {
  try {
    const url = financialYear 
      ? `${API_BASE_URL}/tax/${userId}/capital-gains/summary?financialYear=${financialYear}`
      : `${API_BASE_URL}/tax/${userId}/capital-gains/summary`;
    const response = await axios.get(url);
    return response.data;
  } catch (error) {
    console.error('Error fetching capital gains summary:', error);
    throw error;
  }
};

// Get capital gains by type
export const getCapitalGainsByType = async (userId, type) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/tax/${userId}/capital-gains/type/${type}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching capital gains by type:', error);
    throw error;
  }
};

// Update capital gains
export const updateCapitalGains = async (id, capitalGainsData) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/tax/capital-gains/${id}`, capitalGainsData);
    return response.data;
  } catch (error) {
    console.error('Error updating capital gains:', error);
    throw error;
  }
};

// Delete capital gains
export const deleteCapitalGains = async (id) => {
  try {
    await axios.delete(`${API_BASE_URL}/tax/capital-gains/${id}`);
  } catch (error) {
    console.error('Error deleting capital gains:', error);
    throw error;
  }
};

// Record TDS
export const recordTDS = async (userId, tdsData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/tax/${userId}/tds`, tdsData);
    return response.data;
  } catch (error) {
    console.error('Error recording TDS:', error);
    throw error;
  }
};

// Get TDS records
export const getTDSRecords = async (userId, financialYear) => {
  try {
    const url = financialYear 
      ? `${API_BASE_URL}/tax/${userId}/tds?financialYear=${financialYear}`
      : `${API_BASE_URL}/tax/${userId}/tds`;
    const response = await axios.get(url);
    return response.data;
  } catch (error) {
    console.error('Error fetching TDS records:', error);
    throw error;
  }
};

// Get TDS reconciliation
export const getTDSReconciliation = async (userId, financialYear) => {
  try {
    const url = financialYear 
      ? `${API_BASE_URL}/tax/${userId}/tds/reconciliation?financialYear=${financialYear}`
      : `${API_BASE_URL}/tax/${userId}/tds/reconciliation`;
    const response = await axios.get(url);
    return response.data;
  } catch (error) {
    console.error('Error fetching TDS reconciliation:', error);
    throw error;
  }
};

// Update TDS
export const updateTDS = async (id, tdsData) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/tax/tds/${id}`, tdsData);
    return response.data;
  } catch (error) {
    console.error('Error updating TDS:', error);
    throw error;
  }
};

// Delete TDS
export const deleteTDS = async (id) => {
  try {
    await axios.delete(`${API_BASE_URL}/tax/tds/${id}`);
  } catch (error) {
    console.error('Error deleting TDS:', error);
    throw error;
  }
};

// Get tax projection
export const getTaxProjection = async (userId, financialYear) => {
  try {
    const url = financialYear 
      ? `${API_BASE_URL}/tax/${userId}/projection?financialYear=${financialYear}`
      : `${API_BASE_URL}/tax/${userId}/projection`;
    const response = await axios.get(url);
    return response.data;
  } catch (error) {
    console.error('Error fetching tax projection:', error);
    throw error;
  }
};

// Get ITR prefill data
export const getITRPrefillData = async (userId, financialYear) => {
  try {
    const url = financialYear 
      ? `${API_BASE_URL}/tax/${userId}/itr-prefill?financialYear=${financialYear}`
      : `${API_BASE_URL}/tax/${userId}/itr-prefill`;
    const response = await axios.get(url);
    return response.data;
  } catch (error) {
    console.error('Error fetching ITR prefill data:', error);
    throw error;
  }
};

// Get tax saving suggestions
export const getTaxSavingSuggestions = async (userId) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/tax/${userId}/suggestions`);
    return response.data;
  } catch (error) {
    console.error('Error fetching tax saving suggestions:', error);
    throw error;
  }
};

export default {
  createOrUpdateTaxDetails,
  getTaxDetails,
  getRegimeComparison,
  recordCapitalGains,
  getCapitalGainsSummary,
  getCapitalGainsByType,
  updateCapitalGains,
  deleteCapitalGains,
  recordTDS,
  getTDSRecords,
  getTDSReconciliation,
  updateTDS,
  deleteTDS,
  getTaxProjection,
  getITRPrefillData,
  getTaxSavingSuggestions
};
