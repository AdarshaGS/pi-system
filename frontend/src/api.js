const BASE_URL = 'http://localhost:8082/api';

export const apiCall = async (endpoint, method = 'GET', body = null, token = null, responseType = 'json') => {
    const headers = {
        'Content-Type': 'application/json',
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        method,
        headers,
    };

    if (body) {
        config.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, config);
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Something went wrong');
        }
        
        // Handle different response types
        if (responseType === 'blob') {
            return await response.blob();
        }
        return await response.json();
    } catch (error) {
        console.error('API Call Error:', error);
        throw error;
    }
};

export const authApi = {
    login: (credentials) => apiCall('/auth/login', 'POST', credentials),
    register: (userData) => apiCall('/auth/register', 'POST', userData),
    forgotPassword: (data) => apiCall('/auth/forgot-password', 'POST', data),
};

export const netWorthApi = {
    getNetWorth: (userId, token) => apiCall(`/v1/net-worth/${userId}`, 'GET', null, token),
};

export const budgetApi = {
    getReport: (userId, token) => apiCall(`/v1/budget/report/${userId}`, 'GET', null, token),
    getExpenses: (userId, token, params = {}) => {
        const queryParams = new URLSearchParams(params).toString();
        return apiCall(`/v1/budget/expense/${userId}${queryParams ? '?' + queryParams : ''}`, 'GET', null, token);
    },
    getExpenseById: (id, token) => apiCall(`/v1/budget/expense/detail/${id}`, 'GET', null, token),
    addExpense: (data, token) => apiCall('/v1/budget/expense', 'POST', data, token),
    updateExpense: (id, data, token) => apiCall(`/v1/budget/expense/${id}`, 'PUT', data, token),
    deleteExpense: (id, token) => apiCall(`/v1/budget/expense/${id}`, 'DELETE', null, token),
    getIncomes: (userId, token, params = {}) => {
        const queryParams = new URLSearchParams(params).toString();
        return apiCall(`/v1/budget/income/${userId}${queryParams ? '?' + queryParams : ''}`, 'GET', null, token);
    },
    getIncomeById: (id, token) => apiCall(`/v1/budget/income/detail/${id}`, 'GET', null, token),
    addIncome: (data, token) => apiCall('/v1/budget/income', 'POST', data, token),
    updateIncome: (id, data, token) => apiCall(`/v1/budget/income/${id}`, 'PUT', data, token),
    deleteIncome: (id, token) => apiCall(`/v1/budget/income/${id}`, 'DELETE', null, token),
    getCashFlow: (userId, token) => apiCall(`/v1/budget/cashflow/${userId}`, 'GET', null, token),
    getAllBudgets: (userId, token, monthYear = null) => {
        const url = monthYear ? `/v1/budget/limit/${userId}?monthYear=${monthYear}` : `/v1/budget/limit/${userId}`;
        return apiCall(url, 'GET', null, token);
    },
    setBudget: (data, token) => apiCall('/v1/budget/limit', 'POST', data, token),
    setBudgetsBatch: (budgets, token) => apiCall('/v1/budget/limit/batch', 'POST', budgets, token),
    deleteBudget: (id, token) => apiCall(`/v1/budget/limit/${id}`, 'DELETE', null, token),
    getTotalBudget: (userId, token, monthYear = null) => {
        const url = monthYear ? `/v1/budget/total/${userId}?monthYear=${monthYear}` : `/v1/budget/total/${userId}`;
        return apiCall(url, 'GET', null, token);
    },
    // Custom Categories
    createCustomCategory: (data, token) => apiCall('/v1/budget/category/custom', 'POST', data, token),
    getUserCustomCategories: (userId, token) => apiCall(`/v1/budget/category/custom/${userId}`, 'GET', null, token),
    getAllCategories: (userId, token) => apiCall(`/v1/budget/category/all/${userId}`, 'GET', null, token),
    updateCustomCategory: (id, data, token) => apiCall(`/v1/budget/category/custom/${id}`, 'PUT', data, token),
    deleteCustomCategory: (id, token) => apiCall(`/v1/budget/category/custom/${id}`, 'DELETE', null, token),
    hardDeleteCustomCategory: (id, token) => apiCall(`/v1/budget/category/custom/${id}/hard`, 'DELETE', null, token),
    
    // Export endpoints
    exportExpensesCSV: (userId, token, params = {}) => {
        const queryParams = new URLSearchParams(params).toString();
        return apiCall(`/v1/budget/expense/${userId}/export/csv${queryParams ? '?' + queryParams : ''}`, 'GET', null, token, 'blob');
    },
    exportExpensesExcel: (userId, token, params = {}) => {
        const queryParams = new URLSearchParams(params).toString();
        return apiCall(`/v1/budget/expense/${userId}/export/excel${queryParams ? '?' + queryParams : ''}`, 'GET', null, token, 'blob');
    },
    exportIncomesCSV: (userId, token, params = {}) => {
        const queryParams = new URLSearchParams(params).toString();
        return apiCall(`/v1/budget/income/${userId}/export/csv${queryParams ? '?' + queryParams : ''}`, 'GET', null, token, 'blob');
    },
    exportIncomesExcel: (userId, token, params = {}) => {
        const queryParams = new URLSearchParams(params).toString();
        return apiCall(`/v1/budget/income/${userId}/export/excel${queryParams ? '?' + queryParams : ''}`, 'GET', null, token, 'blob');
    },
    downloadPDFReport: (userId, token, monthYear) => {
        const params = monthYear ? `?monthYear=${monthYear}` : '';
        return apiCall(`/v1/budget/report/${userId}/pdf${params}`, 'GET', null, token, 'blob');
    },
    emailReport: (userId, token, data) => apiCall(`/v1/budget/report/${userId}/email`, 'POST', data, token),
    
    // Recurring transactions endpoints
    getRecurringTemplates: (userId, token) => apiCall(`/v1/budget/recurring/${userId}`, 'GET', null, token),
    getActiveRecurringTemplates: (userId, token) => apiCall(`/v1/budget/recurring/${userId}/active`, 'GET', null, token),
    createRecurringTemplate: (data, token) => apiCall('/v1/budget/recurring', 'POST', data, token),
    updateRecurringTemplate: (id, data, token) => apiCall(`/v1/budget/recurring/${id}`, 'PUT', data, token),
    deleteRecurringTemplate: (id, token) => apiCall(`/v1/budget/recurring/${id}`, 'DELETE', null, token),
    toggleRecurringTemplate: (id, token) => apiCall(`/v1/budget/recurring/${id}/toggle`, 'POST', null, token),
    getUpcomingDates: (id, token, months = 6) => apiCall(`/v1/budget/recurring/${id}/upcoming?months=${months}`, 'GET', null, token),
    manuallyGenerateRecurring: (token) => apiCall('/v1/budget/recurring/generate', 'POST', null, token),
    
    // Tags endpoints
    getUserTags: (userId, token) => apiCall(`/v1/budget/tags/${userId}`, 'GET', null, token),
    getTagById: (tagId, token) => apiCall(`/v1/budget/tags/detail/${tagId}`, 'GET', null, token),
    createTag: (data, token) => apiCall('/v1/budget/tags', 'POST', data, token),
    updateTag: (tagId, data, token) => apiCall(`/v1/budget/tags/${tagId}`, 'PUT', data, token),
    deleteTag: (tagId, token) => apiCall(`/v1/budget/tags/${tagId}`, 'DELETE', null, token),
    
    // Bulk operations
    bulkDeleteExpenses: (userId, expenseIds, token) => {
        const params = new URLSearchParams({ userId }).toString();
        return apiCall(`/v1/budget/expense/bulk-delete?${params}`, 'POST', expenseIds, token);
    },
    bulkUpdateCategory: (userId, category, customCategoryName, expenseIds, token) => {
        const params = new URLSearchParams({ userId, category });
        if (customCategoryName) params.append('customCategoryName', customCategoryName);
        return apiCall(`/v1/budget/expense/bulk-update-category?${params.toString()}`, 'POST', expenseIds, token);
    },
};

export const adminApi = {
    getDashboard: (token) => apiCall('/v1/admin/dashboard', 'GET', null, token),
    getAllUsers: (token, params = {}) => {
        const queryParams = new URLSearchParams(params).toString();
        return apiCall(`/v1/admin/users${queryParams ? '?' + queryParams : ''}`, 'GET', null, token);
    },
    getUserById: (userId, token) => apiCall(`/v1/admin/users/${userId}`, 'GET', null, token),
    updateUser: (userId, userData, token) => apiCall(`/v1/admin/users/${userId}`, 'PUT', userData, token),
    deleteUser: (userId, token) => apiCall(`/v1/admin/users/${userId}`, 'DELETE', null, token),
    addRoleToUser: (userId, roleName, token) => apiCall(`/v1/admin/users/${userId}/roles/${roleName}`, 'POST', null, token),
    removeRoleFromUser: (userId, roleName, token) => apiCall(`/v1/admin/users/${userId}/roles/${roleName}`, 'DELETE', null, token),
    getAllRoles: (token) => apiCall('/v1/admin/roles', 'GET', null, token),
    getCriticalLogs: (token) => apiCall('/v1/admin/utilities/critical-logs', 'GET', null, token),
    getActivityLogs: (token, params = {}) => {
        const queryParams = new URLSearchParams(params).toString();
        return apiCall(`/v1/admin/utilities/activity-logs${queryParams ? '?' + queryParams : ''}`, 'GET', null, token);
    },
};

export const externalServicesApi = {
    getAllServices: (token) => apiCall('/v1/external-services', 'GET', null, token),
    getServiceProperties: (serviceName, token) => apiCall(`/v1/external-services/${serviceName}`, 'GET', null, token),
    createService: (serviceName, token) => apiCall('/v1/external-services', 'POST', { serviceName }, token),
    createProperty: (serviceId, name, value, token) => apiCall('/v1/external-services/properties', 'POST', { serviceId, name, value }, token),
    updateProperty: (propertyId, value, token) => apiCall(`/v1/external-services/properties/${propertyId}`, 'PUT', { value }, token),
};

export const featureApi = {
    getAllFeatures: (token) => apiCall('/v1/admin/features', 'GET', null, token),
    getEnabledFeatures: (token) => apiCall('/v1/admin/features/enabled', 'GET', null, token),
    isFeatureEnabled: (featureName, token) => apiCall(`/v1/admin/features/${featureName}/enabled`, 'GET', null, token),
    enableFeature: (featureName, token) => apiCall(`/v1/admin/features/${featureName}/enable`, 'POST', null, token),
    disableFeature: (featureName, token) => apiCall(`/v1/admin/features/${featureName}/disable`, 'POST', null, token),
};

export const loansApi = {
    // CRUD Operations
    createLoan: (loanData, token) => apiCall('/v1/loans/create', 'POST', loanData, token),
    getAllLoans: (token) => apiCall('/v1/loans/all', 'GET', null, token),
    getUserLoans: (userId, token) => apiCall(`/v1/loans/user/${userId}`, 'GET', null, token),
    getLoanById: (loanId, token) => apiCall(`/v1/loans/${loanId}`, 'GET', null, token),
    deleteLoan: (loanId, token) => apiCall(`/v1/loans/${loanId}`, 'DELETE', null, token),
    
    // Advanced Calculations
    getAmortizationSchedule: (loanId, token) => apiCall(`/v1/loans/${loanId}/amortization-schedule`, 'GET', null, token),
    analyzeLoan: (loanId, token) => apiCall(`/v1/loans/${loanId}/analysis`, 'GET', null, token),
    getTotalInterest: (loanId, token) => apiCall(`/v1/loans/${loanId}/total-interest`, 'GET', null, token),
    simulatePrepayment: (loanId, amount, token) => apiCall(`/v1/loans/${loanId}/simulate-prepayment?amount=${amount}`, 'POST', null, token),
    
    // Payment Tracking
    recordPayment: (paymentData, token) => apiCall('/v1/loans/payments', 'POST', paymentData, token),
    getPaymentHistory: (loanId, token) => apiCall(`/v1/loans/${loanId}/payments`, 'GET', null, token),
    getMissedPayments: (loanId, token) => apiCall(`/v1/loans/${loanId}/missed-payments`, 'GET', null, token),
    
    // Foreclosure
    calculateForeclosure: (loanId, chargesPercentage, token) => 
        apiCall(`/v1/loans/${loanId}/foreclosure-calculation?foreclosureChargesPercentage=${chargesPercentage}`, 'GET', null, token),
    processForeclosure: (loanId, chargesPercentage, token) => 
        apiCall(`/v1/loans/${loanId}/foreclose?foreclosureChargesPercentage=${chargesPercentage}`, 'POST', null, token),
};

export const insuranceApi = {
    // Policy Management
    getAllPolicies: (token) => apiCall('/v1/insurance/policies', 'GET', null, token),
    createPolicy: (policyData, token) => apiCall('/v1/insurance/policies', 'POST', policyData, token),
    updatePolicy: (policyId, policyData, token) => apiCall(`/v1/insurance/policies/${policyId}`, 'PUT', policyData, token),
    deletePolicy: (policyId, token) => apiCall(`/v1/insurance/policies/${policyId}`, 'DELETE', null, token),
    getPolicyById: (policyId, token) => apiCall(`/v1/insurance/policies/${policyId}`, 'GET', null, token),
    
    // Policy Filtering
    getPoliciesByType: (type, token) => apiCall(`/v1/insurance/policies/type/${type}`, 'GET', null, token),
    getPoliciesByStatus: (status, token) => apiCall(`/v1/insurance/policies/status/${status}`, 'GET', null, token),
    getPoliciesMaturingSoon: (days, token) => apiCall(`/v1/insurance/policies/maturing-soon?days=${days}`, 'GET', null, token),
    
    // Analytics
    getSummary: (token) => apiCall('/v1/insurance/policies/summary', 'GET', null, token),
    getAnalytics: (token) => apiCall('/v1/insurance/policies/analytics', 'GET', null, token),
    
    // Premium Payments
    recordPremiumPayment: (paymentData, token) => apiCall('/v1/insurance/premiums', 'POST', paymentData, token),
    getAllPremiumPayments: (token) => apiCall('/v1/insurance/premiums', 'GET', null, token),
    getPremiumPaymentsByPolicy: (policyId, token) => apiCall(`/v1/insurance/premiums/policy/${policyId}`, 'GET', null, token),
    updatePremiumPayment: (paymentId, paymentData, token) => apiCall(`/v1/insurance/premiums/${paymentId}`, 'PUT', paymentData, token),
    deletePremiumPayment: (paymentId, token) => apiCall(`/v1/insurance/premiums/${paymentId}`, 'DELETE', null, token),
    getUpcomingPremiums: (days, token) => apiCall(`/v1/insurance/premiums/upcoming?days=${days}`, 'GET', null, token),
    
    // Claims Management
    fileClaim: (claimData, token) => apiCall('/v1/insurance/claims', 'POST', claimData, token),
    getAllClaims: (token) => apiCall('/v1/insurance/claims', 'GET', null, token),
    getClaimsByPolicy: (policyId, token) => apiCall(`/v1/insurance/claims/policy/${policyId}`, 'GET', null, token),
    getClaimsByStatus: (status, token) => apiCall(`/v1/insurance/claims/status/${status}`, 'GET', null, token),
    updateClaim: (claimId, claimData, token) => apiCall(`/v1/insurance/claims/${claimId}`, 'PUT', claimData, token),
    deleteClaim: (claimId, token) => apiCall(`/v1/insurance/claims/${claimId}`, 'DELETE', null, token),
};

// API Client wrapper for axios-like interface
const apiClient = {
    get: (url, config = {}) => {
        const token = JSON.parse(localStorage.getItem('user'))?.token;
        const params = config.params || {};
        const queryString = Object.keys(params).length 
            ? '?' + new URLSearchParams(params).toString() 
            : '';
        return apiCall(`${url}${queryString}`, 'GET', null, token);
    },
    post: (url, data, config = {}) => {
        const token = JSON.parse(localStorage.getItem('user'))?.token;
        const params = config.params || {};
        const queryString = Object.keys(params).length 
            ? '?' + new URLSearchParams(params).toString() 
            : '';
        return apiCall(`${url}${queryString}`, 'POST', data, token);
    },
    put: (url, data, config = {}) => {
        const token = JSON.parse(localStorage.getItem('user'))?.token;
        const params = config.params || {};
        const queryString = Object.keys(params).length 
            ? '?' + new URLSearchParams(params).toString() 
            : '';
        return apiCall(`${url}${queryString}`, 'PUT', data, token);
    },
    delete: (url, config = {}) => {
        const token = JSON.parse(localStorage.getItem('user'))?.token;
        const params = config.params || {};
        const queryString = Object.keys(params).length 
            ? '?' + new URLSearchParams(params).toString() 
            : '';
        return apiCall(`${url}${queryString}`, 'DELETE', null, token);
    }
};

export default apiClient;
