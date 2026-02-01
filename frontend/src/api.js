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
};

export const featureApi = {
    getAllFeatures: (token) => apiCall('/v1/admin/features', 'GET', null, token),
    getEnabledFeatures: (token) => apiCall('/v1/admin/features/enabled', 'GET', null, token),
    isFeatureEnabled: (featureName, token) => apiCall(`/v1/admin/features/${featureName}/enabled`, 'GET', null, token),
    enableFeature: (featureName, token) => apiCall(`/v1/admin/features/${featureName}/enable`, 'POST', null, token),
    disableFeature: (featureName, token) => apiCall(`/v1/admin/features/${featureName}/disable`, 'POST', null, token),
};
