/**
 * AI-Powered Employee Management System - API Client Module
 * Centralized fetch handler with HTTP Basic authentication, error translation, and auth state.
 */

const API = (() => {
    const STORAGE_KEY_AUTH = 'ems_auth_credentials';
    const STORAGE_KEY_USER = 'ems_auth_user';

    let currentAuthHeader = localStorage.getItem(STORAGE_KEY_AUTH) || '';
    let currentUser = JSON.parse(localStorage.getItem(STORAGE_KEY_USER) || 'null');

    // Subscribers for auth state changes
    const authListeners = [];

    const notifyAuthChange = () => {
        authListeners.forEach(cb => cb(currentUser));
    };

    const setAuth = (username, password, userData) => {
        const token = btoa(`${username}:${password}`);
        currentAuthHeader = `Basic ${token}`;
        currentUser = userData || { username, roles: [], isAdmin: false };
        localStorage.setItem(STORAGE_KEY_AUTH, currentAuthHeader);
        localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(currentUser));
        notifyAuthChange();
    };

    const clearAuth = () => {
        currentAuthHeader = '';
        currentUser = null;
        localStorage.removeItem(STORAGE_KEY_AUTH);
        localStorage.removeItem(STORAGE_KEY_USER);
        notifyAuthChange();
    };

    const onAuthChange = (callback) => {
        authListeners.push(callback);
        callback(currentUser);
    };

    const getAuthHeader = () => currentAuthHeader;
    const getCurrentUser = () => currentUser;
    const isAuthenticated = () => !!currentAuthHeader && !!currentUser;
    const isAdmin = () => currentUser && (currentUser.isAdmin || (currentUser.roles && currentUser.roles.some(r => r.includes('ADMIN'))));

    /**
     * Core fetch wrapper
     */
    async function request(url, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        if (currentAuthHeader) {
            headers['Authorization'] = currentAuthHeader;
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(url, config);

            // Handle HTTP 401 Unauthorized
            if (response.status === 401) {
                clearAuth();
                window.dispatchEvent(new CustomEvent('ems:unauthorized', { detail: { url } }));
                throw new Error('Your session is unauthenticated. Please log in.');
            }

            // Handle HTTP 403 Forbidden
            if (response.status === 403) {
                window.dispatchEvent(new CustomEvent('ems:forbidden', { detail: { url } }));
                throw new Error('Access Denied: You do not have permission to perform this action.');
            }

            const isJson = response.headers.get('content-type')?.includes('application/json');
            const data = isJson ? await response.json() : await response.text();

            if (!response.ok) {
                const message = data.message || data.error || `Request failed with status ${response.status}`;
                const error = new Error(message);
                error.status = response.status;
                error.details = data.details || [];
                throw error;
            }

            return data;
        } catch (err) {
            if (err.name === 'TypeError' && err.message.includes('fetch')) {
                throw new Error('Network error: Unable to connect to backend server. Is the application running?');
            }
            throw err;
        }
    }

    return {
        setAuth,
        clearAuth,
        getAuthHeader,
        getCurrentUser,
        isAuthenticated,
        isAdmin,
        onAuthChange,

        // Authentication endpoints
        auth: {
            async login(username, password) {
                const token = btoa(`${username}:${password}`);
                const response = await fetch('/api/auth/me', {
                    headers: {
                        'Authorization': `Basic ${token}`,
                        'Accept': 'application/json'
                    }
                });

                if (response.status === 401) {
                    throw new Error('Invalid username or password. Please verify your credentials.');
                }

                if (!response.ok) {
                    throw new Error('Authentication failed with server error.');
                }

                const json = await response.json();
                const userData = json.data;
                setAuth(username, password, userData);
                return userData;
            },

            async getMe() {
                if (!currentAuthHeader) return null;
                const res = await request('/api/auth/me');
                if (res && res.data) {
                    currentUser = res.data;
                    localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(currentUser));
                    notifyAuthChange();
                }
                return res.data;
            },

            async logout() {
                try {
                    await request('/api/auth/logout', { method: 'POST' });
                } catch (ignored) {}
                clearAuth();
            }
        },

        // Employee CRUD & Search endpoints
        employees: {
            async getAll(params = {}) {
                const query = new URLSearchParams();
                if (params.page !== undefined) query.set('page', params.page);
                if (params.size !== undefined) query.set('size', params.size);
                if (params.sortBy) query.set('sortBy', params.sortBy);
                if (params.sortDir) query.set('sortDir', params.sortDir);
                if (params.departmentId) query.set('departmentId', params.departmentId);
                if (params.status) query.set('status', params.status);
                if (params.query) query.set('query', params.query);

                const res = await request(`/api/employees?${query.toString()}`);
                return res.data;
            },

            async getById(id) {
                const res = await request(`/api/employees/${id}`);
                return res.data;
            },

            async create(data) {
                const res = await request('/api/employees', {
                    method: 'POST',
                    body: JSON.stringify(data)
                });
                return res.data;
            },

            async update(id, data) {
                const res = await request(`/api/employees/${id}`, {
                    method: 'PUT',
                    body: JSON.stringify(data)
                });
                return res.data;
            },

            async updateStatus(id, status) {
                const res = await request(`/api/employees/${id}/status`, {
                    method: 'PATCH',
                    body: JSON.stringify({ status })
                });
                return res.data;
            },

            async delete(id) {
                const res = await request(`/api/employees/${id}`, {
                    method: 'DELETE'
                });
                return res.data;
            },

            async search(query, page = 0, size = 10, sortBy = 'id', sortDir = 'asc') {
                const q = new URLSearchParams({ query: query || '', page, size, sortBy, sortDir });
                const res = await request(`/api/employees/search?${q.toString()}`);
                return res.data;
            }
        },

        // Department endpoints
        departments: {
            async getAll() {
                const res = await request('/api/departments');
                return res.data;
            },
            async getById(id) {
                const res = await request(`/api/departments/${id}`);
                return res.data;
            }
        },

        // Dashboard endpoints
        dashboard: {
            async getSummary() {
                const res = await request('/api/dashboard/summary');
                return res.data;
            }
        },

        // AI Intelligence endpoints
        ai: {
            async query(question) {
                const res = await request('/api/ai/query', {
                    method: 'POST',
                    body: JSON.stringify({ question })
                });
                return res.data;
            },

            async getInsights() {
                const res = await request('/api/ai/insights');
                return res.data;
            },

            async generateReport(options = {}) {
                const res = await request('/api/ai/report', {
                    method: 'POST',
                    body: JSON.stringify(options)
                });
                return res.data;
            },

            async getReportHistory() {
                const res = await request('/api/ai/reports/history');
                return res.data;
            }
        }
    };
})();
