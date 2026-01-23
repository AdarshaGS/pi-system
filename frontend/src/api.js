const BASE_URL = 'http://localhost:8082/api';

export const apiCall = async (endpoint, method = 'GET', body = null, token = null) => {
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
    getExpenses: (userId, token) => apiCall(`/v1/budget/expense/${userId}`, 'GET', null, token),
    addExpense: (data, token) => apiCall('/v1/budget/expense', 'POST', data, token),
};
