/**
 * AI-Powered Employee Management System - Main Application Logic
 * Coordinates UI views, Chart.js graphs, Employee CRUD, and AI features.
 */

const App = (() => {
    // State
    const state = {
        currentView: 'dashboard',
        departments: [],
        employeesPage: {
            content: [],
            number: 0,
            size: 10,
            totalElements: 0,
            totalPages: 0,
            first: true,
            last: true
        },
        filters: {
            departmentId: '',
            status: '',
            query: '',
            sortBy: 'id',
            sortDir: 'asc'
        },
        selectedEmployee: null,
        activeReport: null,
        charts: {
            deptChart: null,
            statusChart: null
        }
    };

    let searchDebounceTimer = null;

    /**
     * Initialization on DOM ready
     */
    async function init() {
        initTheme();
        bindNavigation();
        bindAuthEvents();
        bindEmployeeEvents();
        bindAiEvents();

        // Check active authentication
        if (API.isAuthenticated()) {
            try {
                await API.auth.getMe();
                showToast('Welcome back to Enterprise Workforce Hub', 'info');
            } catch (e) {
                showLoginModal();
            }
        } else {
            // Preset default credentials for effortless initial evaluation
            API.setAuth('admin@company.com', 'Admin@123', {
                username: 'admin@company.com',
                fullName: 'Alex Mercer (Admin)',
                roles: ['ROLE_ADMIN', 'ROLE_USER'],
                isAdmin: true
            });
        }

        updateAuthUI();
        await loadDepartments();

        // Support URL hash navigation (e.g. #dashboard, #employees, #ai-insights, #ai-reports)
        window.addEventListener('hashchange', () => {
            const hash = window.location.hash.replace('#', '') || 'dashboard';
            navigateTo(hash);
        });

        const initialHash = window.location.hash.replace('#', '') || 'dashboard';
        navigateTo(initialHash);
    }

    /**
     * Theme Toggle (Light / Dark)
     */
    function initTheme() {
        const savedTheme = localStorage.getItem('ems_theme') || 'dark';
        if (savedTheme === 'dark') {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }

        const themeToggleBtn = document.getElementById('theme-toggle-btn');
        if (themeToggleBtn) {
            themeToggleBtn.addEventListener('click', () => {
                const isDark = document.documentElement.classList.toggle('dark');
                localStorage.setItem('ems_theme', isDark ? 'dark' : 'light');
                updateChartThemes();
            });
        }
    }

    /**
     * View Navigation
     */
    function bindNavigation() {
        document.querySelectorAll('[data-view-target]').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const targetView = link.getAttribute('data-view-target');
                navigateTo(targetView);
            });
        });
    }

    function navigateTo(viewName) {
        state.currentView = viewName;

        // Hide all views
        document.querySelectorAll('.app-view').forEach(view => {
            view.classList.add('hidden');
        });

        // Show target view
        const targetElement = document.getElementById(`view-${viewName}`);
        if (targetElement) {
            targetElement.classList.remove('hidden');
            targetElement.classList.add('animate-fade-in');
        }

        // Update active sidebar nav
        document.querySelectorAll('[data-view-target]').forEach(link => {
            const isActive = link.getAttribute('data-view-target') === viewName;
            link.classList.toggle('bg-indigo-600', isActive);
            link.classList.toggle('text-white', isActive);
            link.classList.toggle('text-slate-400', !isActive);
            link.classList.toggle('hover:bg-slate-800', !isActive);
        });

        // Trigger view-specific data loader
        if (viewName === 'dashboard') {
            loadDashboard();
        } else if (viewName === 'employees') {
            loadEmployees();
        } else if (viewName === 'ai-insights') {
            loadAiInsights();
        } else if (viewName === 'ai-reports') {
            loadAiReportsView();
        }
    }

    /**
     * Auth Event Handlers & Modal
     */
    function bindAuthEvents() {
        window.addEventListener('ems:unauthorized', () => {
            showToast('Authentication required. Please log in.', 'error');
            showLoginModal();
        });

        window.addEventListener('ems:forbidden', () => {
            showToast('Access Denied: Admin permission is required for this action.', 'warning');
        });

        const loginBtn = document.getElementById('btn-open-login');
        if (loginBtn) {
            loginBtn.addEventListener('click', showLoginModal);
        }

        const logoutBtn = document.getElementById('btn-logout');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', async () => {
                await API.auth.logout();
                showToast('Successfully logged out.', 'info');
                updateAuthUI();
                showLoginModal();
            });
        }

        const quickAdminBtn = document.getElementById('quick-login-admin');
        if (quickAdminBtn) {
            quickAdminBtn.addEventListener('click', () => performLogin('admin@company.com', 'Admin@123'));
        }

        const quickUserBtn = document.getElementById('quick-login-user');
        if (quickUserBtn) {
            quickUserBtn.addEventListener('click', () => performLogin('user@company.com', 'User@123'));
        }

        const loginForm = document.getElementById('login-form');
        if (loginForm) {
            loginForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const username = document.getElementById('login-username').value.trim();
                const password = document.getElementById('login-password').value;
                await performLogin(username, password);
            });
        }
    }

    async function performLogin(username, password) {
        try {
            const loginSpinner = document.getElementById('login-spinner');
            if (loginSpinner) loginSpinner.classList.remove('hidden');

            const userData = await API.auth.login(username, password);
            hideLoginModal();
            updateAuthUI();
            showToast(`Logged in successfully as ${userData.fullName || userData.username}`, 'success');
            navigateTo(state.currentView);
        } catch (err) {
            showToast(err.message || 'Login failed', 'error');
        } finally {
            const loginSpinner = document.getElementById('login-spinner');
            if (loginSpinner) loginSpinner.classList.add('hidden');
        }
    }

    function showLoginModal() {
        const modal = document.getElementById('login-modal');
        if (modal) modal.classList.remove('hidden');
    }

    function hideLoginModal() {
        const modal = document.getElementById('login-modal');
        if (modal) modal.classList.add('hidden');
    }

    function updateAuthUI() {
        const currentUser = API.getCurrentUser();
        const isAdmin = API.isAdmin();

        const userBadge = document.getElementById('user-badge');
        const roleBadge = document.getElementById('role-badge');
        const adminOnlyElements = document.querySelectorAll('.admin-only');

        if (currentUser) {
            if (userBadge) userBadge.textContent = currentUser.fullName || currentUser.username;
            if (roleBadge) {
                roleBadge.textContent = isAdmin ? 'ADMIN' : 'USER';
                roleBadge.className = isAdmin
                    ? 'px-2 py-0.5 text-xs font-semibold rounded bg-amber-500/20 text-amber-400 border border-amber-500/40'
                    : 'px-2 py-0.5 text-xs font-semibold rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/40';
            }

            adminOnlyElements.forEach(el => {
                if (isAdmin) {
                    el.classList.remove('opacity-40', 'cursor-not-allowed');
                    el.removeAttribute('disabled');
                    el.title = '';
                } else {
                    el.classList.add('opacity-40', 'cursor-not-allowed');
                    el.setAttribute('disabled', 'true');
                    el.title = 'Requires ADMIN privileges';
                }
            });
        }
    }

    /**
     * Load Departments for Filters & Form selects
     */
    async function loadDepartments() {
        try {
            state.departments = await API.departments.getAll();
            const filterSelect = document.getElementById('filter-department');
            const formSelect = document.getElementById('employee-form-department');

            if (filterSelect) {
                filterSelect.innerHTML = '<option value="">All Departments</option>' +
                    state.departments.map(d => `<option value="${d.id}">${d.name} (${d.code})</option>`).join('');
            }

            if (formSelect) {
                formSelect.innerHTML = '<option value="">Select Department</option>' +
                    state.departments.map(d => `<option value="${d.id}">${d.name} (${d.code})</option>`).join('');
            }
        } catch (err) {
            console.error('Failed to load departments', err);
        }
    }

    /**
     * Dashboard View & Charts
     */
    async function loadDashboard() {
        try {
            const summary = await API.dashboard.getSummary();

            // Populate Metrics Cards
            document.getElementById('metric-total').textContent = summary.totalEmployees || 0;
            document.getElementById('metric-active').textContent = summary.activeEmployees || 0;
            document.getElementById('metric-leave').textContent = summary.onLeaveEmployees || 0;
            document.getElementById('metric-inactive').textContent = summary.inactiveEmployees || 0;

            const activePercentEl = document.getElementById('metric-active-percent');
            if (activePercentEl) {
                activePercentEl.textContent = `${summary.activePercentage || 0}%`;
                document.getElementById('metric-active-progress').style.width = `${summary.activePercentage || 0}%`;
            }

            // AI Executive Summary Banner
            const summaryEl = document.getElementById('dashboard-ai-summary');
            if (summaryEl) {
                summaryEl.textContent = summary.aiExecutiveSummary || 'Telemetric analysis underway.';
            }

            // Render Charts
            renderDepartmentChart(summary.departmentDistribution || []);
            renderStatusChart(summary.activeEmployees, summary.onLeaveEmployees, summary.inactiveEmployees);

            // Populate Recent Additions Table
            renderRecentEmployees(summary.recentEmployees || []);
        } catch (err) {
            showToast('Failed to load dashboard metrics: ' + err.message, 'error');
        }
    }

    function renderDepartmentChart(departments) {
        const canvas = document.getElementById('dept-headcount-chart');
        if (!canvas) return;

        if (state.charts.deptChart) {
            state.charts.deptChart.destroy();
        }

        const labels = departments.map(d => d.departmentCode || d.departmentName);
        const dataValues = departments.map(d => d.totalHeadcount);
        const isDark = document.documentElement.classList.contains('dark');

        const ctx = canvas.getContext('2d');
        const gradient = ctx.createLinearGradient(0, 0, 0, 300);
        gradient.addColorStop(0, '#6366f1');
        gradient.addColorStop(1, '#4338ca');

        state.charts.deptChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Team Members',
                    data: dataValues,
                    backgroundColor: gradient,
                    borderRadius: 6,
                    borderSkipped: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            title: (items) => departments[items[0].dataIndex]?.departmentName || ''
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { color: isDark ? '#94a3b8' : '#64748b' }
                    },
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0,
                            color: isDark ? '#94a3b8' : '#64748b'
                        },
                        grid: {
                            color: isDark ? 'rgba(51, 65, 85, 0.4)' : 'rgba(226, 232, 240, 0.8)'
                        }
                    }
                }
            }
        });
    }

    function renderStatusChart(active, leave, inactive) {
        const canvas = document.getElementById('status-doughnut-chart');
        if (!canvas) return;

        if (state.charts.statusChart) {
            state.charts.statusChart.destroy();
        }

        const ctx = canvas.getContext('2d');
        state.charts.statusChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Active', 'On Leave', 'Inactive'],
                datasets: [{
                    data: [active || 0, leave || 0, inactive || 0],
                    backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
                    borderWidth: 0,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '72%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            boxWidth: 12,
                            padding: 16,
                            color: document.documentElement.classList.contains('dark') ? '#94a3b8' : '#64748b'
                        }
                    }
                }
            }
        });
    }

    function updateChartThemes() {
        if (state.charts.deptChart) {
            const isDark = document.documentElement.classList.contains('dark');
            state.charts.deptChart.options.scales.x.ticks.color = isDark ? '#94a3b8' : '#64748b';
            state.charts.deptChart.options.scales.y.ticks.color = isDark ? '#94a3b8' : '#64748b';
            state.charts.deptChart.options.scales.y.grid.color = isDark ? 'rgba(51, 65, 85, 0.4)' : 'rgba(226, 232, 240, 0.8)';
            state.charts.deptChart.update();
        }
        if (state.charts.statusChart) {
            const isDark = document.documentElement.classList.contains('dark');
            state.charts.statusChart.options.plugins.legend.labels.color = isDark ? '#94a3b8' : '#64748b';
            state.charts.statusChart.update();
        }
    }

    function renderRecentEmployees(recent) {
        const tbody = document.getElementById('dashboard-recent-table');
        if (!tbody) return;

        if (recent.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="p-4 text-center text-slate-500">No recent employees found.</td></tr>';
            return;
        }

        tbody.innerHTML = recent.map(emp => `
            <tr class="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                <td class="px-4 py-3 font-medium text-slate-900 dark:text-slate-100 flex items-center gap-2">
                    <span class="w-7 h-7 rounded-full bg-indigo-500/20 text-indigo-400 flex items-center justify-center font-bold text-xs">
                        ${emp.fullName.charAt(0)}
                    </span>
                    ${escapeHtml(emp.fullName)}
                </td>
                <td class="px-4 py-3 text-sm text-slate-600 dark:text-slate-300">${escapeHtml(emp.jobTitle)}</td>
                <td class="px-4 py-3 text-sm">
                    <span class="px-2 py-0.5 rounded text-xs font-semibold bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                        ${escapeHtml(emp.departmentName || '')}
                    </span>
                </td>
                <td class="px-4 py-3 text-sm">${renderStatusPill(emp.employmentStatus)}</td>
            </tr>
        `).join('');
    }

    /**
     * Employee Directory CRUD
     */
    function bindEmployeeEvents() {
        // Search input with debounce
        const searchInput = document.getElementById('search-employee');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                clearTimeout(searchDebounceTimer);
                searchDebounceTimer = setTimeout(() => {
                    state.filters.query = e.target.value;
                    state.employeesPage.number = 0;
                    loadEmployees();
                }, 350);
            });
        }

        // Filters
        const deptFilter = document.getElementById('filter-department');
        if (deptFilter) {
            deptFilter.addEventListener('change', (e) => {
                state.filters.departmentId = e.target.value;
                state.employeesPage.number = 0;
                loadEmployees();
            });
        }

        const statusFilter = document.getElementById('filter-status');
        if (statusFilter) {
            statusFilter.addEventListener('change', (e) => {
                state.filters.status = e.target.value;
                state.employeesPage.number = 0;
                loadEmployees();
            });
        }

        // Add Employee button
        const addBtn = document.getElementById('btn-add-employee');
        if (addBtn) {
            addBtn.addEventListener('click', () => {
                if (!API.isAdmin()) {
                    showToast('Admin role required to add employee', 'warning');
                    return;
                }
                openEmployeeModal(null);
            });
        }

        // Employee Form Submit
        const employeeForm = document.getElementById('employee-form');
        if (employeeForm) {
            employeeForm.addEventListener('submit', handleEmployeeFormSubmit);
        }

        // Close Modals
        document.querySelectorAll('[data-close-modal]').forEach(btn => {
            btn.addEventListener('click', () => {
                const modalId = btn.getAttribute('data-close-modal');
                const modal = document.getElementById(modalId);
                if (modal) modal.classList.add('hidden');
            });
        });

        // Delete Confirm
        const confirmDeleteBtn = document.getElementById('btn-confirm-delete');
        if (confirmDeleteBtn) {
            confirmDeleteBtn.addEventListener('click', handleEmployeeDelete);
        }
    }

    async function loadEmployees() {
        const loadingIndicator = document.getElementById('employees-loading');
        const tableBody = document.getElementById('employees-table-body');
        const emptyState = document.getElementById('employees-empty');

        if (loadingIndicator) loadingIndicator.classList.remove('hidden');
        if (tableBody) tableBody.innerHTML = '';
        if (emptyState) emptyState.classList.add('hidden');

        try {
            const pageData = await API.employees.getAll({
                page: state.employeesPage.number,
                size: state.employeesPage.size,
                sortBy: state.filters.sortBy,
                sortDir: state.filters.sortDir,
                departmentId: state.filters.departmentId,
                status: state.filters.status,
                query: state.filters.query
            });

            const content = pageData.content || (Array.isArray(pageData) ? pageData : []);
            const pageMeta = pageData.page || pageData;
            const number = pageMeta.number ?? 0;
            const size = pageMeta.size ?? 10;
            const totalElements = pageMeta.totalElements ?? content.length;
            const totalPages = pageMeta.totalPages ?? Math.max(1, Math.ceil(totalElements / size));

            state.employeesPage = {
                content,
                number,
                size,
                totalElements,
                totalPages,
                first: number === 0,
                last: number >= totalPages - 1
            };

            renderEmployeeTable(content);
            renderPagination(state.employeesPage);
        } catch (err) {
            showToast('Failed to load employees: ' + err.message, 'error');
        } finally {
            if (loadingIndicator) loadingIndicator.classList.add('hidden');
        }
    }

    function renderEmployeeTable(employees) {
        const tableBody = document.getElementById('employees-table-body');
        const emptyState = document.getElementById('employees-empty');
        if (!tableBody) return;

        if (employees.length === 0) {
            if (emptyState) emptyState.classList.remove('hidden');
            return;
        }

        const isAdmin = API.isAdmin();

        tableBody.innerHTML = employees.map(emp => {
            const formattedSalary = emp.salary !== null && emp.salary !== undefined
                ? `$${Number(emp.salary).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                : '<span class="text-slate-400 dark:text-slate-500 text-xs flex items-center gap-1"><i class="fas fa-lock text-[10px]"></i> Restricted</span>';

            return `
                <tr class="border-b border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                    <td class="px-6 py-4 font-mono text-xs text-slate-500 dark:text-slate-400">#${emp.id}</td>
                    <td class="px-6 py-4">
                        <div class="flex items-center gap-3">
                            <div class="w-9 h-9 rounded-full bg-gradient-to-tr from-indigo-500 to-purple-600 text-white flex items-center justify-center font-bold text-sm shadow-sm">
                                ${emp.fullName.charAt(0)}
                            </div>
                            <div>
                                <div class="font-semibold text-slate-900 dark:text-slate-100">${escapeHtml(emp.fullName)}</div>
                                <div class="text-xs text-slate-500 dark:text-slate-400">${escapeHtml(emp.email)}</div>
                            </div>
                        </div>
                    </td>
                    <td class="px-6 py-4">
                        <div class="text-sm font-medium text-slate-800 dark:text-slate-200">${escapeHtml(emp.jobTitle)}</div>
                        <div class="text-xs text-slate-500 dark:text-slate-400">${escapeHtml(emp.phoneNumber || '')}</div>
                    </td>
                    <td class="px-6 py-4">
                        <span class="px-2.5 py-1 rounded-md text-xs font-semibold bg-indigo-50 dark:bg-indigo-950/40 text-indigo-700 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-800/40">
                            ${escapeHtml(emp.departmentName || 'General')}
                        </span>
                    </td>
                    <td class="px-6 py-4 text-sm">${renderStatusPill(emp.employmentStatus)}</td>
                    <td class="px-6 py-4 text-sm font-mono text-slate-700 dark:text-slate-300">${formattedSalary}</td>
                    <td class="px-6 py-4 text-sm text-slate-500 dark:text-slate-400 font-mono text-xs">${emp.joiningDate || '-'}</td>
                    <td class="px-6 py-4 text-right">
                        <div class="flex items-center justify-end gap-2">
                            <button onclick="App.viewEmployeeDetails(${emp.id})" title="View Details"
                                class="p-1.5 text-slate-500 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors">
                                <i class="fas fa-eye text-sm"></i>
                            </button>
                            <button onclick="App.openEmployeeModal(${emp.id})" title="${isAdmin ? 'Edit Employee' : 'Admin only'}"
                                class="p-1.5 ${isAdmin ? 'text-slate-500 hover:text-amber-500' : 'text-slate-300 dark:text-slate-600 cursor-not-allowed'} transition-colors"
                                ${!isAdmin ? 'disabled' : ''}>
                                <i class="fas fa-pen text-sm"></i>
                            </button>
                            <button onclick="App.confirmDelete(${emp.id}, '${escapeHtml(emp.fullName)}')" title="${isAdmin ? 'Delete Employee' : 'Admin only'}"
                                class="p-1.5 ${isAdmin ? 'text-slate-500 hover:text-rose-500' : 'text-slate-300 dark:text-slate-600 cursor-not-allowed'} transition-colors"
                                ${!isAdmin ? 'disabled' : ''}>
                                <i class="fas fa-trash-alt text-sm"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    function renderPagination(page) {
        const infoEl = document.getElementById('pagination-info');
        const prevBtn = document.getElementById('pagination-prev');
        const nextBtn = document.getElementById('pagination-next');
        const pagesContainer = document.getElementById('pagination-pages');

        const totalElements = page.totalElements ?? 0;
        const pageNumber = page.number ?? 0;
        const pageSize = page.size ?? 10;
        const totalPages = page.totalPages ?? Math.max(1, Math.ceil(totalElements / pageSize));

        if (infoEl) {
            const start = totalElements === 0 ? 0 : pageNumber * pageSize + 1;
            const end = Math.min((pageNumber + 1) * pageSize, totalElements);
            infoEl.textContent = `Showing ${start}-${end} of ${totalElements} records`;
        }

        if (prevBtn) {
            prevBtn.disabled = page.first;
            prevBtn.onclick = () => {
                if (!page.first) {
                    state.employeesPage.number--;
                    loadEmployees();
                }
            };
        }

        if (nextBtn) {
            nextBtn.disabled = page.last;
            nextBtn.onclick = () => {
                if (!page.last) {
                    state.employeesPage.number++;
                    loadEmployees();
                }
            };
        }

        if (pagesContainer) {
            pagesContainer.innerHTML = '';
            const totalPages = page.totalPages || 1;
            for (let i = 0; i < Math.min(totalPages, 5); i++) {
                const btn = document.createElement('button');
                btn.className = `px-3 py-1 rounded text-xs font-semibold ${i === page.number ? 'bg-indigo-600 text-white' : 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700'}`;
                btn.textContent = i + 1;
                btn.onclick = () => {
                    state.employeesPage.number = i;
                    loadEmployees();
                };
                pagesContainer.appendChild(btn);
            }
        }
    }

    function renderStatusPill(status) {
        switch (status) {
            case 'ACTIVE':
                return '<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-500 border border-emerald-500/20"><span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Active</span>';
            case 'ON_LEAVE':
                return '<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-500/10 text-amber-500 border border-amber-500/20"><span class="w-1.5 h-1.5 rounded-full bg-amber-500"></span> On Leave</span>';
            case 'INACTIVE':
                return '<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-rose-500/10 text-rose-500 border border-rose-500/20"><span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span> Inactive</span>';
            default:
                return `<span class="px-2 py-0.5 rounded text-xs bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">${status}</span>`;
        }
    }

    async function openEmployeeModal(employeeId) {
        const modal = document.getElementById('employee-modal');
        const titleEl = document.getElementById('employee-modal-title');
        const form = document.getElementById('employee-form');
        form.reset();

        document.getElementById('form-error-banner').classList.add('hidden');
        document.getElementById('form-error-details').innerHTML = '';

        if (employeeId) {
            titleEl.textContent = 'Edit Employee Record';
            document.getElementById('employee-form-id').value = employeeId;
            try {
                const emp = await API.employees.getById(employeeId);
                document.getElementById('employee-form-fullname').value = emp.fullName || '';
                document.getElementById('employee-form-email').value = emp.email || '';
                document.getElementById('employee-form-department').value = emp.departmentId || '';
                document.getElementById('employee-form-jobtitle').value = emp.jobTitle || '';
                document.getElementById('employee-form-phone').value = emp.phoneNumber || '';
                document.getElementById('employee-form-joiningdate').value = emp.joiningDate || '';
                document.getElementById('employee-form-status').value = emp.employmentStatus || 'ACTIVE';
                document.getElementById('employee-form-salary').value = emp.salary || '';
            } catch (err) {
                showToast('Failed to fetch employee details: ' + err.message, 'error');
                return;
            }
        } else {
            titleEl.textContent = 'Add New Employee';
            document.getElementById('employee-form-id').value = '';
            document.getElementById('employee-form-status').value = 'ACTIVE';
            document.getElementById('employee-form-joiningdate').value = new Date().toISOString().split('T')[0];
        }

        modal.classList.remove('hidden');
    }

    async function handleEmployeeFormSubmit(e) {
        e.preventDefault();
        const id = document.getElementById('employee-form-id').value;
        const payload = {
            fullName: document.getElementById('employee-form-fullname').value.trim(),
            email: document.getElementById('employee-form-email').value.trim(),
            departmentId: parseInt(document.getElementById('employee-form-department').value, 10),
            jobTitle: document.getElementById('employee-form-jobtitle').value.trim(),
            phoneNumber: document.getElementById('employee-form-phone').value.trim(),
            joiningDate: document.getElementById('employee-form-joiningdate').value,
            employmentStatus: document.getElementById('employee-form-status').value,
            salary: parseFloat(document.getElementById('employee-form-salary').value)
        };

        const errorBanner = document.getElementById('form-error-banner');
        const errorDetails = document.getElementById('form-error-details');
        errorBanner.classList.add('hidden');
        errorDetails.innerHTML = '';

        try {
            if (id) {
                await API.employees.update(id, payload);
                showToast('Employee updated successfully', 'success');
            } else {
                await API.employees.create(payload);
                showToast('New employee created successfully', 'success');
            }
            document.getElementById('employee-modal').classList.add('hidden');
            loadEmployees();
            if (state.currentView === 'dashboard') loadDashboard();
        } catch (err) {
            errorBanner.classList.remove('hidden');
            errorBanner.querySelector('span').textContent = err.message || 'Operation failed';
            if (err.details && err.details.length > 0) {
                errorDetails.innerHTML = err.details.map(d => `<li>${escapeHtml(d)}</li>`).join('');
            }
        }
    }

    async function viewEmployeeDetails(id) {
        try {
            const emp = await API.employees.getById(id);
            state.selectedEmployee = emp;

            document.getElementById('details-name').textContent = emp.fullName;
            document.getElementById('details-email').textContent = emp.email;
            document.getElementById('details-jobtitle').textContent = emp.jobTitle;
            document.getElementById('details-department').textContent = `${emp.departmentName} (${emp.departmentCode})`;
            document.getElementById('details-phone').textContent = emp.phoneNumber;
            document.getElementById('details-joining').textContent = emp.joiningDate;
            document.getElementById('details-status-pill').innerHTML = renderStatusPill(emp.employmentStatus);

            const salaryEl = document.getElementById('details-salary');
            if (emp.salary !== null && emp.salary !== undefined) {
                salaryEl.textContent = `$${Number(emp.salary).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
            } else {
                salaryEl.innerHTML = '<span class="text-slate-400 text-xs flex items-center gap-1"><i class="fas fa-lock"></i> Restricted to ADMIN role</span>';
            }

            document.getElementById('employee-details-modal').classList.remove('hidden');
        } catch (err) {
            showToast('Unable to view employee details: ' + err.message, 'error');
        }
    }

    function confirmDelete(id, name) {
        if (!API.isAdmin()) {
            showToast('Requires ADMIN permissions', 'warning');
            return;
        }
        document.getElementById('delete-employee-id').value = id;
        document.getElementById('delete-employee-name').textContent = name;
        document.getElementById('delete-confirm-modal').classList.remove('hidden');
    }

    async function handleEmployeeDelete() {
        const id = document.getElementById('delete-employee-id').value;
        try {
            await API.employees.delete(id);
            showToast('Employee record deleted', 'success');
            document.getElementById('delete-confirm-modal').classList.add('hidden');
            loadEmployees();
            if (state.currentView === 'dashboard') loadDashboard();
        } catch (err) {
            showToast('Failed to delete employee: ' + err.message, 'error');
        }
    }

    /**
     * AI Intelligence View & Reporting
     */
    function bindAiEvents() {
        // Natural language query submit
        const aiForm = document.getElementById('ai-query-form');
        if (aiForm) {
            aiForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const input = document.getElementById('ai-query-input');
                const question = input.value.trim();
                if (!question) return;
                await askAiQuestion(question);
            });
        }

        // Quick prompt chips
        document.querySelectorAll('.ai-prompt-chip').forEach(chip => {
            chip.addEventListener('click', () => {
                const prompt = chip.getAttribute('data-prompt');
                const input = document.getElementById('ai-query-input');
                if (input) input.value = prompt;
                askAiQuestion(prompt);
            });
        });

        // Report Generator Form
        const reportForm = document.getElementById('ai-report-form');
        if (reportForm) {
            reportForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                await generateReport();
            });
        }

        // CSV Export button
        const exportCsvBtn = document.getElementById('btn-export-csv');
        if (exportCsvBtn) {
            exportCsvBtn.addEventListener('click', exportReportToCsv);
        }

        // Print Report button
        const printBtn = document.getElementById('btn-print-report');
        if (printBtn) {
            printBtn.addEventListener('click', () => window.print());
        }
    }

    async function askAiQuestion(question) {
        const answerBox = document.getElementById('ai-response-container');
        const loadingBox = document.getElementById('ai-query-loading');
        const textEl = document.getElementById('ai-response-text');
        const sourceBadge = document.getElementById('ai-source-badge');
        const factsBox = document.getElementById('ai-facts-box');

        if (loadingBox) loadingBox.classList.remove('hidden');
        if (answerBox) answerBox.classList.add('hidden');

        try {
            const result = await API.ai.query(question);

            if (textEl) {
                // Render simple markdown headers, bold, bullets
                textEl.innerHTML = formatMarkdown(result.answer);
            }

            if (sourceBadge) {
                const isLlm = result.source === 'AI_LLM';
                sourceBadge.innerHTML = isLlm
                    ? '<span class="px-2 py-0.5 rounded text-xs font-semibold bg-purple-500/20 text-purple-300 border border-purple-500/40"><i class="fas fa-brain mr-1"></i> AI Model (LLM)</span>'
                    : '<span class="px-2 py-0.5 rounded text-xs font-semibold bg-cyan-500/20 text-cyan-300 border border-cyan-500/40"><i class="fas fa-shield-alt mr-1"></i> Rule-Based Analytical Fallback</span>';
            }

            if (factsBox && result.facts) {
                factsBox.innerHTML = Object.entries(result.facts)
                    .map(([k, v]) => `<span class="px-2.5 py-1 rounded bg-slate-100 dark:bg-slate-800 text-xs font-mono text-slate-700 dark:text-slate-300"><span class="text-slate-400">${k}:</span> <strong>${v}</strong></span>`)
                    .join('');
            }

            if (answerBox) answerBox.classList.remove('hidden');
        } catch (err) {
            showToast('AI Query error: ' + err.message, 'error');
        } finally {
            if (loadingBox) loadingBox.classList.add('hidden');
        }
    }

    async function loadAiInsights() {
        const trendsList = document.getElementById('ai-insights-trends');
        const recsList = document.getElementById('ai-insights-recs');

        try {
            const data = await API.ai.getInsights();

            if (trendsList && data.trends) {
                trendsList.innerHTML = data.trends.map(t => `
                    <li class="flex items-start gap-3 p-3 rounded-lg bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-800">
                        <i class="fas fa-chart-line text-indigo-500 mt-1"></i>
                        <span class="text-sm text-slate-700 dark:text-slate-300">${escapeHtml(t)}</span>
                    </li>
                `).join('');
            }

            if (recsList && data.recommendations) {
                recsList.innerHTML = data.recommendations.map(r => `
                    <li class="flex items-start gap-3 p-3 rounded-lg bg-emerald-500/5 dark:bg-emerald-950/20 border border-emerald-500/20">
                        <i class="fas fa-check-circle text-emerald-500 mt-1"></i>
                        <span class="text-sm text-slate-700 dark:text-slate-300">${escapeHtml(r)}</span>
                    </li>
                `).join('');
            }
        } catch (err) {
            console.error('Failed to load AI insights', err);
        }
    }

    function loadAiReportsView() {
        const today = new Date().toISOString().split('T')[0];
        const sixMonthsAgo = new Date(Date.now() - 180 * 86400000).toISOString().split('T')[0];

        const dateFromInput = document.getElementById('report-date-from');
        const dateToInput = document.getElementById('report-date-to');

        if (dateFromInput && !dateFromInput.value) dateFromInput.value = sixMonthsAgo;
        if (dateToInput && !dateToInput.value) dateToInput.value = today;
    }

    async function generateReport() {
        if (!API.isAdmin()) {
            showToast('Report generation is restricted to ADMIN', 'warning');
            return;
        }

        const dateFrom = document.getElementById('report-date-from').value;
        const dateTo = document.getElementById('report-date-to').value;
        const focusArea = document.getElementById('report-focus-area').value;

        const loading = document.getElementById('report-loading');
        const preview = document.getElementById('report-preview');

        if (loading) loading.classList.remove('hidden');
        if (preview) preview.classList.add('hidden');

        try {
            const report = await API.ai.generateReport({ dateFrom, dateTo, focusArea });
            state.activeReport = report;
            renderReport(report);
            if (preview) preview.classList.remove('hidden');
            showToast('Workforce report generated successfully', 'success');
        } catch (err) {
            showToast('Report generation failed: ' + err.message, 'error');
        } finally {
            if (loading) loading.classList.add('hidden');
        }
    }

    function renderReport(report) {
        document.getElementById('report-title-display').textContent = report.title || 'Workforce Intelligence Report';
        document.getElementById('report-meta-display').textContent = `Period: ${report.dateFrom || 'All'} to ${report.dateTo || 'Current'} | Generated at: ${new Date(report.generatedAt).toLocaleString()} | Source: ${report.source}`;
        document.getElementById('report-summary-display').textContent = report.executiveSummary || '';

        // Metrics grid
        const metricsGrid = document.getElementById('report-metrics-grid');
        if (metricsGrid && report.keyMetrics) {
            metricsGrid.innerHTML = Object.entries(report.keyMetrics).map(([k, v]) => `
                <div class="p-3 rounded-lg bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700">
                    <div class="text-xs text-slate-500 uppercase tracking-wide">${escapeHtml(k)}</div>
                    <div class="text-lg font-bold text-slate-900 dark:text-slate-100">${escapeHtml(String(v))}</div>
                </div>
            `).join('');
        }

        // Department breakdown table
        const deptTbody = document.getElementById('report-dept-tbody');
        if (deptTbody && report.departmentBreakdown) {
            deptTbody.innerHTML = report.departmentBreakdown.map(d => `
                <tr class="border-b border-slate-200 dark:border-slate-800">
                    <td class="px-4 py-2 font-medium">${escapeHtml(d.departmentName)} (${escapeHtml(d.departmentCode)})</td>
                    <td class="px-4 py-2 text-right font-mono">${d.totalHeadcount}</td>
                    <td class="px-4 py-2 text-right font-mono">${d.activeHeadcount}</td>
                    <td class="px-4 py-2 text-right font-mono">${d.percentage}%</td>
                </tr>
            `).join('');
        }

        // Trends & Recommendations
        const trendsList = document.getElementById('report-trends-list');
        if (trendsList && report.observedTrends) {
            trendsList.innerHTML = report.observedTrends.map(t => `<li class="text-sm py-1">• ${escapeHtml(t)}</li>`).join('');
        }

        const recsList = document.getElementById('report-recs-list');
        if (recsList && report.actionableRecommendations) {
            recsList.innerHTML = report.actionableRecommendations.map(r => `<li class="text-sm py-1">• ${escapeHtml(r)}</li>`).join('');
        }
    }

    function exportReportToCsv() {
        if (!state.activeReport) {
            showToast('No generated report to export', 'warning');
            return;
        }

        const report = state.activeReport;
        let csv = '\uFEFF'; // BOM for Excel UTF-8 compatibility
        csv += `Report Title,"${report.title}"\n`;
        csv += `Period,"${report.dateFrom} to ${report.dateTo}"\n`;
        csv += `Source,"${report.source}"\n\n`;

        csv += `Executive Summary,"${(report.executiveSummary || '').replace(/"/g, '""')}"\n\n`;

        csv += "Department,Code,Headcount,Active Members,Share Percentage\n";
        (report.departmentBreakdown || []).forEach(d => {
            csv += `"${d.departmentName}","${d.departmentCode}",${d.totalHeadcount},${d.activeHeadcount},${d.percentage}%\n`;
        });

        csv += "\nObserved Trends\n";
        (report.observedTrends || []).forEach(t => {
            csv += `"${t.replace(/"/g, '""')}"\n`;
        });

        csv += "\nActionable Recommendations\n";
        (report.actionableRecommendations || []).forEach(r => {
            csv += `"${r.replace(/"/g, '""')}"\n`;
        });

        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Workforce_Report_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        showToast('CSV report downloaded', 'info');
    }

    /**
     * Helpers & Toast Notifications
     */
    function showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        const icons = {
            success: 'fa-check-circle text-emerald-400',
            error: 'fa-exclamation-circle text-rose-400',
            warning: 'fa-exclamation-triangle text-amber-400',
            info: 'fa-info-circle text-indigo-400'
        };

        toast.className = 'flex items-center gap-3 px-4 py-3 rounded-xl bg-slate-900/95 text-white shadow-2xl border border-slate-700/80 backdrop-blur-md animate-fade-in text-sm max-w-md pointer-events-auto';
        toast.innerHTML = `
            <i class="fas ${icons[type] || icons.info} text-base flex-shrink-0"></i>
            <span class="flex-1 font-medium">${escapeHtml(message)}</span>
            <button class="text-slate-400 hover:text-white" onclick="this.parentElement.remove()"><i class="fas fa-times"></i></button>
        `;

        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(8px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 4500);
    }

    function formatMarkdown(text) {
        if (!text) return '';
        let formatted = escapeHtml(text);
        // Bold
        formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong class="font-semibold text-indigo-300">$1</strong>');
        // Headers
        formatted = formatted.replace(/^### (.*$)/gim, '<h4 class="font-bold text-base text-slate-100 mt-3 mb-1">$1</h4>');
        // Bullet lists
        formatted = formatted.replace(/^\- (.*$)/gim, '<li class="ml-4 list-disc text-slate-300 py-0.5">$1</li>');
        // Tables (simple conversion)
        formatted = formatted.replace(/\n\n/g, '<br/><br/>');
        return formatted;
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // Public API
    return {
        init,
        navigateTo,
        viewEmployeeDetails,
        openEmployeeModal,
        confirmDelete,
        showToast
    };
})();

// Bootstrap on DOM ready
document.addEventListener('DOMContentLoaded', App.init);
