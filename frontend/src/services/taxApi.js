import apiClient from '../api';

const TAX_BASE_URL = '/v1/tax';

const taxApi = {
  // ==================== Basic Tax Management ====================
  
  createTaxDetails: (taxData) => 
    apiClient.post(TAX_BASE_URL, taxData),
  
  getTaxDetails: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}`, { params: { financialYear } }),
  
  getOutstandingLiability: (userId) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}/liability`),
  
  // ==================== Tax Regime Comparison ====================
  
  compareTaxRegimes: (userId, financialYear, grossIncome) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}/regime-comparison`, {
      params: { financialYear, grossIncome }
    }),
  
  // ==================== Capital Gains ====================
  
  recordCapitalGain: (userId, transaction) => 
    apiClient.post(`${TAX_BASE_URL}/${userId}/capital-gains`, transaction),
  
  getCapitalGainsSummary: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}/capital-gains/summary`, {
      params: { financialYear }
    }),
  
  getCapitalGainsTransactions: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}/capital-gains/transactions`, {
      params: { financialYear }
    }),
  
  calculateCapitalGains: (transaction) => 
    apiClient.post(`${TAX_BASE_URL}/capital-gains/calculate`, transaction),
  
  // ==================== Tax Savings ====================
  
  getTaxSavingRecommendations: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}/recommendations`, {
      params: { financialYear }
    }),
  
  recordTaxSavingInvestment: (userId, investment) => 
    apiClient.post(`${TAX_BASE_URL}/${userId}/tax-savings`, investment),
  
  getTaxSavingInvestments: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}/tax-savings`, {
      params: { financialYear }
    }),
  
  // ==================== TDS Management ====================
  
  recordTDSEntry: (userId, tdsEntry) => 
    apiClient.post(`${TAX_BASE_URL}/${userId}/tds`, tdsEntry),
  
  getTDSEntries: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}/tds`, {
      params: { financialYear }
    }),
  
  getTDSReconciliation: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}/tds/reconciliation`, {
      params: { financialYear }
    }),
  
  updateTDSStatus: (tdsId, status) => 
    apiClient.put(`${TAX_BASE_URL}/tds/${tdsId}/status`, null, {
      params: { status }
    }),
  
  // ==================== Tax Projections ====================
  
  getTaxProjection: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}/projection`, {
      params: { financialYear }
    }),
  
  // ==================== ITR Export ====================
  
  getITRPreFillData: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/${userId}/itr-prefill`, {
      params: { financialYear }
    }),
  
  // ==================== ITR Generation ====================
  
  buildITR1Data: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/itr/${userId}/itr1`, {
      params: { financialYear }
    }),
  
  generateITR1JSON: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/itr/${userId}/itr1/json`, {
      params: { financialYear },
      responseType: 'blob'
    }),
  
  buildITR2Data: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/itr/${userId}/itr2`, {
      params: { financialYear }
    }),
  
  generateITR2JSON: (userId, financialYear) => 
    apiClient.get(`${TAX_BASE_URL}/itr/${userId}/itr2/json`, {
      params: { financialYear },
      responseType: 'blob'
    }),
  
  // ==================== Auto-Population ====================
  
  autoPopulateAll: (userId, financialYear) => 
    apiClient.post(`${TAX_BASE_URL}/auto-populate/${userId}/all`, null, {
      params: { financialYear }
    }),
  
  autoPopulateCapitalGains: (userId, financialYear) => 
    apiClient.post(`${TAX_BASE_URL}/auto-populate/${userId}/capital-gains`, null, {
      params: { financialYear }
    }),
  
  autoPopulateSalaryIncome: (userId, financialYear) => 
    apiClient.post(`${TAX_BASE_URL}/auto-populate/${userId}/salary-income`, null, {
      params: { financialYear }
    }),
  
  autoPopulate80CInvestments: (userId, financialYear) => 
    apiClient.post(`${TAX_BASE_URL}/auto-populate/${userId}/80c-investments`, null, {
      params: { financialYear }
    }),
  
  autoPopulate80DInvestments: (userId, financialYear) => 
    apiClient.post(`${TAX_BASE_URL}/auto-populate/${userId}/80d-investments`, null, {
      params: { financialYear }
    }),
  
  // ==================== Advanced Calculations ====================
  
  calculateHousePropertyIncome: (data) => 
    apiClient.post(`${TAX_BASE_URL}/calculations/house-property`, data),
  
  calculateBusinessIncome: (data) => 
    apiClient.post(`${TAX_BASE_URL}/calculations/business-income`, data),
  
  processLossSetOff: (data) => 
    apiClient.post(`${TAX_BASE_URL}/calculations/loss-setoff`, data),
  
  calculateCompleteTax: (data) => 
    apiClient.post(`${TAX_BASE_URL}/calculations/complete-tax`, data),
};

export default taxApi;
