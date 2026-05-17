let currentTab = 'dashboard';
let pieChartInstance = null;
let lineChartInstance = null;
let currentExpenses = [];
let editingExpenseId = null;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('userNameDisplay').textContent = localStorage.getItem('userName') || 'User';
    
    // Initialize flatpickr for beautiful date selection
    flatpickr(".flatpickr-date", {
        dateFormat: "Y-m-d",
        defaultDate: new Date()
    });
    
    fetchDashboardData();

    document.getElementById('addExpenseForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const expense = {
            description: document.getElementById('expDesc').value,
            amount: parseFloat(document.getElementById('expAmount').value),
            categoryName: document.getElementById('expCat').value,
            date: document.getElementById('expDate').value
        };

        try {
            if (editingExpenseId) {
                await fetchAPI(`/expenses/${editingExpenseId}`, {
                    method: 'PUT',
                    body: JSON.stringify(expense)
                });
            } else {
                await fetchAPI('/expenses', {
                    method: 'POST',
                    body: JSON.stringify(expense)
                });
            }
            closeAddExpenseModal();
            fetchDashboardData();
            if (currentTab === 'expenses') fetchExpenses();
        } catch (err) {
            alert('Failed to add expense: ' + err.message);
        }
    });
});

function switchTab(tab, element) {
    currentTab = tab;
    
    // Update sidebar UI
    document.querySelectorAll('.sidebar-nav li').forEach(el => el.classList.remove('active'));
    element.classList.add('active');

    // Update Title
    const titles = {
        'dashboard': 'Financial Overview',
        'expenses': 'All Expenses'
    };
    document.getElementById('pageTitle').textContent = titles[tab];

    // Hide all tabs
    document.getElementById('tab-dashboard').classList.add('hidden');
    document.getElementById('tab-expenses').classList.add('hidden');

    // Show active tab
    document.getElementById(`tab-${tab}`).classList.remove('hidden');

    if (tab === 'dashboard') fetchDashboardData();
    if (tab === 'expenses') fetchExpenses();
}

function openAddExpenseModal() {
    editingExpenseId = null;
    document.querySelector('#addExpenseModal h2').textContent = 'Add New Expense';
    document.getElementById('addExpenseModal').classList.remove('hidden');
}

function closeAddExpenseModal() {
    document.getElementById('addExpenseModal').classList.add('hidden');
    document.getElementById('addExpenseForm').reset();
    editingExpenseId = null;
    const fp = document.querySelector('.flatpickr-date')._flatpickr;
    if (fp) fp.setDate(new Date());
}

async function fetchDashboardData() {
    try {
        const summary = await fetchAPI('/dashboard/summary');
        
        document.getElementById('summaryTotalMonth').textContent = `₹${summary.totalExpensesMonth?.toFixed(2) || 0}`;
        document.getElementById('summaryTotalWeek').textContent = `₹${summary.totalExpensesWeek?.toFixed(2) || 0}`;
        document.getElementById('summaryHighestCat').textContent = summary.highestSpendingCategory || 'N/A';

        // Populate Category Datalist
        const categoryList = document.getElementById('categoryList');
        const categories = Object.keys(summary.categoryWiseSpending || {});
        categoryList.innerHTML = categories.map(cat => `<option value="${cat}">`).join('');

        // Insights
        const insightsHtml = (summary.insights || []).map(insight => `
            <div class="glass" style="padding: 1rem 1.5rem; border-left: 4px solid var(--warning); border-radius: 0.5rem; margin-bottom: 1rem;">
                <b style="display: block; margin-bottom: 0.25rem;">Smart Insight</b>
                <span class="text-muted">${insight}</span>
            </div>
        `).join('');
        document.getElementById('insightsContainer').innerHTML = insightsHtml;

        // Transactions Table
        const tbody = document.getElementById('recentTransactionsTableBody');
        if (summary.recentTransactions && summary.recentTransactions.length > 0) {
            tbody.innerHTML = summary.recentTransactions.map(tx => `
                <tr>
                    <td>${tx.description}</td>
                    <td><span style="background: rgba(59, 130, 246, 0.2); color: var(--primary); padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.85rem;">${tx.categoryName}</span></td>
                    <td class="text-muted">${tx.date}</td>
                    <td style="font-weight: bold;">₹${tx.amount.toFixed(2)}</td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;" class="text-muted">No recent transactions</td></tr>';
        }

        // Fetch expenses to calculate weekly trend dynamically
        const expenses = await fetchAPI('/expenses');
        const weeklyData = [0, 0, 0, 0, 0, 0, 0];
        const weeklyLabels = [];
        
        const today = new Date();
        today.setHours(23, 59, 59, 999);
        const sevenDaysAgo = new Date(today);
        sevenDaysAgo.setDate(today.getDate() - 6);
        sevenDaysAgo.setHours(0, 0, 0, 0);

        for (let i = 6; i >= 0; i--) {
            const d = new Date();
            d.setDate(d.getDate() - i);
            weeklyLabels.push(d.toLocaleDateString('en-US', { weekday: 'short' }));
        }

        if (expenses && expenses.length > 0) {
            expenses.forEach(ex => {
                const exDate = new Date(ex.date);
                if (exDate >= sevenDaysAgo && exDate <= today) {
                    const dayDiff = Math.floor((today - exDate) / (1000 * 60 * 60 * 24));
                    if (dayDiff >= 0 && dayDiff <= 6) {
                        weeklyData[6 - dayDiff] += ex.amount;
                    }
                }
            });
        }

        renderCharts(summary.categoryWiseSpending || {}, weeklyData, weeklyLabels);

    } catch (err) {
        console.error("Dashboard fetch error:", err);
    }
}

async function fetchExpenses() {
    try {
        currentExpenses = await fetchAPI('/expenses');

        // Populate category filter dropdown
        const categories = [...new Set(currentExpenses.map(e => e.categoryName).filter(Boolean))];
        const filterCat = document.getElementById('filterCategory');
        const currentVal = filterCat.value;
        filterCat.innerHTML = '<option value="">All Categories</option>' +
            categories.map(c => `<option value="${c}" ${c === currentVal ? 'selected' : ''}>${c}</option>`).join('');

        applyFilters();
    } catch (err) {
        console.error("Expenses fetch error:", err);
    }
}

function applyFilters() {
    const cat = document.getElementById('filterCategory')?.value || '';
    const dateFrom = document.getElementById('filterDateFrom')?.value || '';
    const dateTo = document.getElementById('filterDateTo')?.value || '';
    const amtMin = parseFloat(document.getElementById('filterAmountMin')?.value) || 0;
    const amtMax = parseFloat(document.getElementById('filterAmountMax')?.value) || Infinity;

    const filtered = currentExpenses.filter(tx => {
        if (cat && tx.categoryName !== cat) return false;
        if (dateFrom && tx.date < dateFrom) return false;
        if (dateTo && tx.date > dateTo) return false;
        if (tx.amount < amtMin) return false;
        if (tx.amount > amtMax) return false;
        return true;
    });

    renderExpensesTable(filtered);

    // Show summary
    const total = filtered.reduce((sum, tx) => sum + tx.amount, 0);
    const summary = document.getElementById('filterSummary');
    if (filtered.length < currentExpenses.length) {
        summary.textContent = `Showing ${filtered.length} of ${currentExpenses.length} expenses — Total: ₹${total.toFixed(2)}`;
    } else {
        summary.textContent = `${currentExpenses.length} expenses — Total: ₹${total.toFixed(2)}`;
    }
}

function clearFilters() {
    document.getElementById('filterCategory').value = '';
    document.getElementById('filterDateFrom').value = '';
    document.getElementById('filterDateTo').value = '';
    document.getElementById('filterAmountMin').value = '';
    document.getElementById('filterAmountMax').value = '';
    applyFilters();
}

function renderExpensesTable(expenses) {
    const tbody = document.getElementById('expensesTableBody');
    if (expenses && expenses.length > 0) {
        tbody.innerHTML = expenses.map(tx => `
            <tr>
                <td>${tx.description}</td>
                <td><span style="background: rgba(37, 99, 235, 0.1); color: var(--primary); padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.85rem;">${tx.categoryName || 'Uncategorized'}</span></td>
                <td class="text-muted">${tx.date}</td>
                <td style="font-weight: bold;">₹${tx.amount.toFixed(2)}</td>
                <td style="text-align: right;">
                    <button onclick="editExpense(${tx.id})" class="btn btn-outline" style="padding: 0.4rem 0.8rem; font-size: 0.8rem; color: var(--primary); border-color: var(--primary); margin-right: 0.5rem;">Edit</button>
                    <button onclick="deleteExpense(${tx.id})" class="btn btn-outline" style="padding: 0.4rem 0.8rem; font-size: 0.8rem; color: var(--danger); border-color: var(--danger);">Delete</button>
                </td>
            </tr>
        `).join('');
    } else {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 3rem;" class="text-muted">No expenses match your filters.</td></tr>';
    }
}

function editExpense(id) {
    const expense = currentExpenses.find(e => e.id === id);
    if (!expense) return;
    
    editingExpenseId = id;
    document.querySelector('#addExpenseModal h2').textContent = 'Edit Expense';
    
    document.getElementById('expDesc').value = expense.description;
    document.getElementById('expAmount').value = expense.amount;
    document.getElementById('expCat').value = expense.categoryName || '';
    
    const fp = document.querySelector('.flatpickr-date')._flatpickr;
    if (fp) fp.setDate(expense.date);
    
    document.getElementById('addExpenseModal').classList.remove('hidden');
}

async function deleteExpense(id) {
    if (!confirm("Are you sure you want to delete this expense?")) return;
    try {
        await fetchAPI(`/expenses/${id}`, { method: 'DELETE' });
        if (currentTab === 'expenses') fetchExpenses();
        if (currentTab === 'dashboard') fetchDashboardData();
    } catch (err) {
        alert("Failed to delete: " + err.message);
    }
}

function renderCharts(categoryData, weeklyData = [], weeklyLabels = []) {
    const pieCtx = document.getElementById('categoryPieChart').getContext('2d');
    const lineCtx = document.getElementById('weeklyLineChart').getContext('2d');

    const legendOptions = {
        position: 'bottom',
        labels: {
            color: '#6B7280',
            boxWidth: 12,
            padding: 10,
            font: { size: 11 }
        }
    };

    const baseOptions = {
        responsive: true,
        maintainAspectRatio: false,
        layout: { padding: 0 },
        plugins: { legend: legendOptions }
    };

    if (pieChartInstance) pieChartInstance.destroy();
    pieChartInstance = new Chart(pieCtx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(categoryData),
            datasets: [{
                data: Object.values(categoryData),
                backgroundColor: ['#2563EB', '#0D9488', '#10B981', '#F59E0B', '#EF4444', '#06B6D4'],
                borderWidth: 0
            }]
        },
        options: {
            ...baseOptions,
            cutout: '65%'
        }
    });

    if (lineChartInstance) lineChartInstance.destroy();
    lineChartInstance = new Chart(lineCtx, {
        type: 'line',
        data: {
            labels: weeklyLabels.length ? weeklyLabels : ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
            datasets: [{
                label: 'Spending (₹)',
                data: weeklyData.length ? weeklyData : [0, 0, 0, 0, 0, 0, 0],
                borderColor: '#2563EB',
                backgroundColor: 'rgba(37, 99, 235, 0.08)',
                fill: true,
                tension: 0.4,
                borderWidth: 2.5,
                pointBackgroundColor: '#2563EB',
                pointRadius: 4
            }]
        },
        options: {
            ...baseOptions,
            scales: {
                x: {
                    ticks: { color: '#6B7280', font: { size: 11 } },
                    grid: { color: 'rgba(0,0,0,0.05)' }
                },
                y: {
                    ticks: { color: '#6B7280', font: { size: 11 } },
                    grid: { color: 'rgba(0,0,0,0.05)' }
                }
            }
        }
    });
}
