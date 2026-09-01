// ==========================================
// ANALYTICS MANAGEMENT — VELORA FINE JEWELLERY ADMIN
// Single fetch per filter → table + chart from same data
// ==========================================

let currentReportData = [];
let currentCategory = 'sales';
let analyticsChartInstance = null;

// ==========================================
// MAIN CONTROLLER
// ==========================================
function loadAnalytics(category, element) {
    currentCategory = category || 'sales';

    if (!element) {
        element = document.querySelector(`a[onclick*="loadAnalytics('${currentCategory}'"]`);
    }

    if (typeof window.showSection === 'function') {
        window.showSection('analytics', element);
    }

    updateAnalyticsUI();
    initializeFilters();
    setTimeout(fetchAndRender, 50);
}

// ==========================================
// FILTER INITIALIZATION
// ==========================================
function initializeFilters() {
    const today = new Date().toISOString().split('T')[0];
    const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth() + 1;

    const set = (id, val) => { const el = document.getElementById(id); if (el && !el.value) el.value = val; };

    set('anl-start-date', thirtyDaysAgo);
    set('anl-end-date', today);
    set('anl-year', currentYear);
    set('anl-month-year', currentYear);
    set('product-start-date', thirtyDaysAgo);
    set('product-end-date', today);
    set('finance-start-date', thirtyDaysAgo);
    set('finance-end-date', today);
    set('txn-start-date', today);
    set('txn-end-date', today);

    const monthSelect = document.getElementById('anl-month');
    if (monthSelect) monthSelect.value = currentMonth;
}

// ==========================================
// UI UPDATER
// ==========================================
function updateAnalyticsUI() {
    // Update tab buttons
    const allTabs = ['sales', 'product', 'finance', 'best-sellers', 'top-customers', 'order-status', 'transactions'];
    allTabs.forEach(type => {
        const btn = document.getElementById(`btn-anl-${type}`);
        if (!btn) return;
        if (type === currentCategory) {
            btn.classList.add('bg-black', 'text-gold-400', 'shadow-sm');
            btn.classList.remove('text-gray-600', 'hover:bg-gray-200', 'bg-gray-100');
        } else {
            btn.classList.remove('bg-black', 'text-gold-400', 'shadow-sm');
            btn.classList.add('text-gray-600', 'hover:bg-gray-200');
        }
    });

    // Title & description
    const titles = {
        'sales':          ['Sales & Returns Reports', 'Daily revenue, return analysis, and net income.'],
        'product':        ['Product Insights', 'Sales performance vs returns and stock.'],
        'finance':        ['Finance Reports', 'Payment method breakdown by date.'],
        'best-sellers':   ['Best Selling Products', 'Top products ranked by units sold.'],
        'top-customers':  ['Top Customers', 'Customers ranked by total spend.'],
        'order-status':   ['Order Status Breakdown', 'Distribution of order statuses.'],
        'transactions':   ['Transaction History', 'Full money log — every transaction from Web & POS with datetime, customer, method, discount & total.']
    };
    const [title, desc] = titles[currentCategory] || ['Analytics', ''];
    const titleEl = document.getElementById('analytics-header-title');
    const descEl  = document.getElementById('analytics-header-desc');
    if (titleEl) titleEl.innerText = title;
    if (descEl)  descEl.innerText  = desc;

    // Show/hide filter controls
    ['sales', 'product', 'finance', 'transactions'].forEach(type => {
        const el = document.getElementById(`${type}-controls`);
        if (!el) return;
        if (type === currentCategory) {
            el.classList.remove('hidden'); el.classList.add('flex');
        } else {
            el.classList.add('hidden'); el.classList.remove('flex');
        }
    });

    toggleAnalyticsFilters();
}

// ==========================================
// FILTER TOGGLE
// ==========================================
function toggleAnalyticsFilters() {
    if (currentCategory === 'sales') {
        const typeEl = document.getElementById('sales-report-type');
        if (!typeEl) return;
        const type = typeEl.value;
        ['filter-date-range', 'filter-month', 'filter-year'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.classList.add('hidden');
        });
        const show = type === 'range' ? 'filter-date-range' : type === 'monthly' ? 'filter-month' : type === 'yearly' ? 'filter-year' : null;
        if (show) { const el = document.getElementById(show); if (el) el.classList.remove('hidden'); }

    } else if (currentCategory === 'product') {
        const typeEl = document.getElementById('product-filter-type');
        const rangeDiv = document.getElementById('product-date-range');
        if (rangeDiv) rangeDiv.classList.toggle('hidden', !(typeEl && typeEl.value === 'range'));

    } else if (currentCategory === 'finance') {
        const typeEl = document.getElementById('finance-filter-type');
        const rangeDiv = document.getElementById('finance-date-range');
        if (rangeDiv) rangeDiv.classList.toggle('hidden', !(typeEl && typeEl.value === 'range'));
    }
}

function applyAnalyticsFilters() { fetchAndRender(); }

// ==========================================
// BUILD API URL FROM CURRENT FILTERS
// ==========================================
function buildAnalyticsUrl() {
    const params = new URLSearchParams();

    if (currentCategory === 'sales') {
        const typeEl = document.getElementById('sales-report-type');
        const type = typeEl ? typeEl.value : 'daily';
        params.append('type', type);

        if (type === 'range') {
            const start = document.getElementById('anl-start-date')?.value;
            const end   = document.getElementById('anl-end-date')?.value;
            if (start) params.append('start', start);
            if (end)   params.append('end', end);
        } else if (type === 'monthly') {
            const month = document.getElementById('anl-month')?.value;
            const year  = document.getElementById('anl-month-year')?.value;
            if (month) params.append('month', month);
            if (year)  params.append('year', year);
        } else if (type === 'yearly') {
            const year = document.getElementById('anl-year')?.value;
            if (year) params.append('year', year);
        }

    } else if (currentCategory === 'product') {
        const filterType = document.getElementById('product-filter-type');
        if (filterType?.value === 'range') {
            const start = document.getElementById('product-start-date')?.value;
            const end   = document.getElementById('product-end-date')?.value;
            if (start) params.append('start', start);
            if (end)   params.append('end', end);
        }

    } else if (currentCategory === 'finance') {
        const filterType = document.getElementById('finance-filter-type');
        if (filterType?.value === 'range') {
            const start = document.getElementById('finance-start-date')?.value;
            const end   = document.getElementById('finance-end-date')?.value;
            if (start) params.append('start', start);
            if (end)   params.append('end', end);
        }

    } else if (currentCategory === 'transactions') {
        const start = document.getElementById('txn-start-date')?.value;
        const end   = document.getElementById('txn-end-date')?.value;
        if (start) params.append('start', start);
        if (end)   params.append('end', end);
    }

    // best-sellers, top-customers, order-status — no extra params needed
    const p = params.toString();
    return `/api/admin/analytics/${currentCategory}${p ? '?' + p : ''}`;
}

// ==========================================
// SINGLE FETCH → TABLE + CHART
// ==========================================
async function fetchAndRender() {
    const tbody = document.getElementById('analytics-tbody');
    if (tbody) tbody.innerHTML = `
        <tr><td colspan="10" class="px-6 py-12 text-center text-gray-400">
            <i class="fas fa-spinner fa-spin text-2xl block mb-2"></i>
            <p class="text-sm">Loading Analytics...</p>
        </td></tr>`;

    // For transactions, inject filter controls if missing
    injectTransactionControls();

    const url = buildAnalyticsUrl();

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const data = await response.json();
        currentReportData = data;

        // Update record count
        const countEl = document.getElementById('analytics-total-count');
        if (countEl) countEl.innerText = data.length;

        // Render table
        renderAnalyticsTable(data);

        // Render footer
        updateFooterSummary(data);

        // Render chart from the SAME data (no extra API call)
        renderAnalyticsChart(data);

    } catch (err) {
        console.error('[Analytics] Fetch error:', err);
        if (tbody) tbody.innerHTML = `
            <tr><td colspan="10" class="px-6 py-8 text-center text-red-500">
                <i class="fas fa-exclamation-circle mr-2"></i>Failed to load data. Please try again.
            </td></tr>`;
        if (window.showToast) showToast('Error loading analytics', 'error');
    }
}

// ==========================================
// INJECT TRANSACTION FILTER CONTROLS DYNAMICALLY
// ==========================================
function injectTransactionControls() {
    if (currentCategory !== 'transactions') return;
    if (document.getElementById('transactions-controls')) return; // already exists

    const filtersContainer = document.getElementById('analytics-filters-container');
    if (!filtersContainer) return;

    const today = new Date().toISOString().split('T')[0];

    const div = document.createElement('div');
    div.id = 'transactions-controls';
    div.className = 'flex items-center gap-2 flex-wrap';
    div.innerHTML = `
        <label class="text-[10px] font-bold text-gray-500 uppercase">From</label>
        <input type="date" id="txn-start-date" value="${today}" class="border border-gray-300 p-2 text-xs outline-none focus:border-gray-500">
        <label class="text-[10px] font-bold text-gray-500 uppercase">To</label>
        <input type="date" id="txn-end-date" value="${today}" class="border border-gray-300 p-2 text-xs outline-none focus:border-gray-500">
        <select id="txn-channel-filter" class="border border-gray-300 p-2 text-xs outline-none focus:border-gray-500 font-bold">
            <option value="all">All Channels</option>
            <option value="web">WEB Only</option>
            <option value="pos">POS Only</option>
        </select>
        <button onclick="applyAnalyticsFilters()" class="bg-gray-800 text-white px-4 py-2 text-xs font-bold uppercase hover:bg-gray-700 transition">
            <i class="fas fa-search mr-1"></i>Filter
        </button>
        <button onclick="setTxnToday()" class="border border-gray-300 text-gray-600 px-3 py-2 text-xs font-bold uppercase hover:bg-gray-100 transition">Today</button>
        <button onclick="setTxnThisMonth()" class="border border-gray-300 text-gray-600 px-3 py-2 text-xs font-bold uppercase hover:bg-gray-100 transition">This Month</button>
    `;
    filtersContainer.appendChild(div);
}

function setTxnToday() {
    const today = new Date().toISOString().split('T')[0];
    const s = document.getElementById('txn-start-date');
    const e = document.getElementById('txn-end-date');
    if (s) s.value = today;
    if (e) e.value = today;
    fetchAndRender();
}

function setTxnThisMonth() {
    const now = new Date();
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0];
    const today = now.toISOString().split('T')[0];
    const s = document.getElementById('txn-start-date');
    const e = document.getElementById('txn-end-date');
    if (s) s.value = firstDay;
    if (e) e.value = today;
    fetchAndRender();
}

// ==========================================
// TABLE RENDERING
// ==========================================
function renderAnalyticsTable(data) {
    const thead = document.getElementById('analytics-thead');
    const tbody = document.getElementById('analytics-tbody');
    if (!thead || !tbody) return;

    if (!data || data.length === 0) {
        thead.innerHTML = '';
        tbody.innerHTML = `
            <tr><td colspan="10" class="px-6 py-16 text-center text-gray-400">
                <i class="fas fa-inbox text-3xl block mb-3 opacity-30"></i>
                <p class="text-sm">No data found for the selected filter.</p>
            </td></tr>`;
        return;
    }

    if (currentCategory === 'sales') {
        thead.innerHTML = `<tr>
            <th class="px-6 py-3 text-left">Date / Period</th>
            <th class="px-6 py-3 text-center">Orders</th>
            <th class="px-6 py-3 text-right text-green-600">Gross Revenue</th>
            <th class="px-6 py-3 text-center text-red-500">Returns</th>
            <th class="px-6 py-3 text-right text-red-500">Refunded</th>
            <th class="px-6 py-3 text-right font-bold">Net Revenue</th>
        </tr>`;
        tbody.innerHTML = data.map(r => `
            <tr class="hover:bg-gray-50 border-b border-gray-100 transition">
                <td class="px-6 py-4 font-mono text-gray-700 font-bold text-xs">${r.date || '-'}</td>
                <td class="px-6 py-4 text-center text-xs text-gray-600">${r.totalOrders || 0}</td>
                <td class="px-6 py-4 text-right text-green-600 text-xs">LKR ${fmt(r.totalRevenue)}</td>
                <td class="px-6 py-4 text-center font-bold text-red-500 text-xs">${r.totalReturns > 0 ? r.totalReturns : '-'}</td>
                <td class="px-6 py-4 text-right text-red-500 text-xs">${r.totalRefunded > 0 ? 'LKR ' + fmt(r.totalRefunded) : '-'}</td>
                <td class="px-6 py-4 text-right font-bold text-gray-900">LKR ${fmt(r.netRevenue)}</td>
            </tr>`).join('');

    } else if (currentCategory === 'product') {
        thead.innerHTML = `<tr>
            <th class="px-6 py-3 text-left">Product Name</th>
            <th class="px-6 py-3">Category</th>
            <th class="px-6 py-3 text-center">Sold</th>
            <th class="px-6 py-3 text-center text-red-500">Returned</th>
            <th class="px-6 py-3 text-center font-bold">Net Sold</th>
            <th class="px-6 py-3 text-right">Net Revenue</th>
            <th class="px-6 py-3 text-center">Stock</th>
        </tr>`;
        tbody.innerHTML = data.map(r => `
            <tr class="hover:bg-gray-50 border-b border-gray-100 transition">
                <td class="px-6 py-4 font-bold text-gray-800 text-xs">${r.productName}</td>
                <td class="px-6 py-4 text-[10px] font-bold text-gray-500 uppercase">${r.category || '-'}</td>
                <td class="px-6 py-4 text-center text-gray-500 text-xs">${r.unitsSold}</td>
                <td class="px-6 py-4 text-center text-red-500 font-bold text-xs">${r.unitsReturned > 0 ? r.unitsReturned : '-'}</td>
                <td class="px-6 py-4 text-center font-bold text-gray-900">${r.netUnitsSold}</td>
                <td class="px-6 py-4 text-right text-gold-600 font-bold text-xs">LKR ${fmt(r.netRevenue)}</td>
                <td class="px-6 py-4 text-center">
                    <span class="px-2 py-1 rounded text-[10px] font-bold ${r.currentStock < 5 ? 'bg-red-100 text-red-600' : 'bg-green-100 text-green-600'}">
                        ${r.currentStock}
                    </span>
                </td>
            </tr>`).join('');

    } else if (currentCategory === 'finance') {
        thead.innerHTML = `<tr>
            <th class="px-6 py-3 text-left">Payment Method</th>
            <th class="px-6 py-3 text-center">Transactions</th>
            <th class="px-6 py-3 text-right text-red-500">Discounts</th>
            <th class="px-6 py-3 text-right text-gray-500">Tax Collected</th>
            <th class="px-6 py-3 text-right font-bold">Total Volume</th>
        </tr>`;
        tbody.innerHTML = data.map(r => `
            <tr class="hover:bg-gray-50 border-b border-gray-100 transition">
                <td class="px-6 py-4 font-bold text-gray-800 text-xs">${r.paymentMethod}</td>
                <td class="px-6 py-4 text-center font-bold text-gray-600">${r.transactionCount}</td>
                <td class="px-6 py-4 text-right text-red-500 text-xs">${r.discountGiven > 0 ? '- LKR ' + fmt(r.discountGiven) : '-'}</td>
                <td class="px-6 py-4 text-right text-gray-500 text-xs">LKR ${fmt(r.taxCollected)}</td>
                <td class="px-6 py-4 text-right font-bold text-gold-600">LKR ${fmt(r.totalAmount)}</td>
            </tr>`).join('');

    } else if (currentCategory === 'best-sellers') {
        thead.innerHTML = `<tr>
            <th class="px-6 py-3 text-left">#</th>
            <th class="px-6 py-3 text-left">Product Name</th>
            <th class="px-6 py-3 text-center">Units Sold</th>
            <th class="px-6 py-3 text-right font-bold">Revenue Generated</th>
        </tr>`;
        tbody.innerHTML = data.map((r, i) => `
            <tr class="hover:bg-gray-50 border-b border-gray-100 transition">
                <td class="px-6 py-4 text-gray-400 font-bold text-sm">${i + 1}</td>
                <td class="px-6 py-4 font-bold text-gray-800 text-xs">${r.productName}</td>
                <td class="px-6 py-4 text-center font-bold text-gray-700 text-xs">${r.unitsSold}</td>
                <td class="px-6 py-4 text-right font-bold text-gold-600 text-xs">LKR ${fmt(r.revenueGenerated)}</td>
            </tr>`).join('');

    } else if (currentCategory === 'top-customers') {
        thead.innerHTML = `<tr>
            <th class="px-6 py-3 text-left">#</th>
            <th class="px-6 py-3 text-left">Customer Name</th>
            <th class="px-6 py-3 text-center">Total Orders</th>
            <th class="px-6 py-3 text-right font-bold">Total Spent</th>
        </tr>`;
        tbody.innerHTML = data.map((r, i) => `
            <tr class="hover:bg-gray-50 border-b border-gray-100 transition">
                <td class="px-6 py-4 text-gray-400 font-bold text-sm">${i + 1}</td>
                <td class="px-6 py-4 font-bold text-gray-800 text-xs">${r.customerName}</td>
                <td class="px-6 py-4 text-center font-bold text-gray-600 text-xs">${r.totalOrders}</td>
                <td class="px-6 py-4 text-right font-bold text-gold-600 text-xs">LKR ${fmt(r.totalSpent)}</td>
            </tr>`).join('');

    } else if (currentCategory === 'order-status') {
        thead.innerHTML = `<tr>
            <th class="px-6 py-3 text-left">Order Status</th>
            <th class="px-6 py-3 text-center font-bold">Total Count</th>
        </tr>`;
        tbody.innerHTML = data.map(r => `
            <tr class="hover:bg-gray-50 border-b border-gray-100 transition">
                <td class="px-6 py-4 font-bold text-gray-800 text-xs uppercase tracking-wide">${r.status}</td>
                <td class="px-6 py-4 text-center font-bold text-gray-700">${r.count}</td>
            </tr>`).join('');

    } else if (currentCategory === 'transactions') {
        // Apply channel filter client-side
        const channelFilter = document.getElementById('txn-channel-filter')?.value || 'all';
        let filtered = data;
        if (channelFilter === 'pos') filtered = data.filter(r => (r.channel||'').toUpperCase() === 'POS');
        else if (channelFilter === 'web') filtered = data.filter(r => (r.channel||'').toUpperCase() !== 'POS');

        // Cash Summary Panel for end-of-day reconciliation
        // ONLY count rows where payment was actually completed/paid!
        const validRows = filtered.filter(r => {
            const ps = (r.paymentStatus || '').toUpperCase();
            return ps.includes('COMPLET') || ps.includes('PAID') || ps.includes('SUCCESS');
        });

        const cashRows    = validRows.filter(r => (r.paymentMethod||'').toLowerCase().includes('cash'));
        const cardRows    = validRows.filter(r => (r.paymentMethod||'').toLowerCase().includes('card'));
        const bankRows    = validRows.filter(r => (r.paymentMethod||'').toLowerCase().includes('bank'));
        const cashTotal   = cashRows.reduce((s,r)  => s + (r.finalTotal||0), 0);
        const cardTotal   = cardRows.reduce((s,r)  => s + (r.finalTotal||0), 0);
        const bankTotal   = bankRows.reduce((s,r)  => s + (r.finalTotal||0), 0);
        const grandTotal  = validRows.reduce((s,r)  => s + (r.finalTotal||0), 0);
        const discTotal   = validRows.reduce((s,r)  => s + (r.discount||0), 0);
        const posCash     = cashRows.filter(r => (r.channel||'').toUpperCase()==='POS').reduce((s,r)=>s+(r.finalTotal||0),0);
        const pickupCash  = cashRows.filter(r => (r.channel||'').toUpperCase()==='WEB').reduce((s,r)=>s+(r.finalTotal||0),0);

        // Insert summary panel before table
        const summaryPanelId = 'txn-cash-summary';
        let existingPanel = document.getElementById(summaryPanelId);
        const tableWrapper = thead.closest('.overflow-x-auto') || thead.parentElement.parentElement;
        if (!existingPanel) {
            existingPanel = document.createElement('div');
            existingPanel.id = summaryPanelId;
            tableWrapper.parentElement.insertBefore(existingPanel, tableWrapper);
        }
        existingPanel.innerHTML = `
        <div class="grid grid-cols-2 md:grid-cols-4 gap-0 border border-gray-200 mb-0 text-center divide-x divide-gray-200">
            <div class="p-4 bg-white">
                <p class="text-[9px] text-gray-400 uppercase font-bold tracking-widest mb-1">💵 Cash In</p>
                <p class="text-lg font-bold text-gray-900">LKR ${fmt(cashTotal)}</p>
                <p class="text-[9px] text-gray-400 mt-1">POS: LKR ${fmt(posCash)} &nbsp;|&nbsp; Store Pickup: LKR ${fmt(pickupCash)}</p>
            </div>
            <div class="p-4 bg-white">
                <p class="text-[9px] text-gray-400 uppercase font-bold tracking-widest mb-1">💳 Card In</p>
                <p class="text-lg font-bold text-gray-900">LKR ${fmt(cardTotal)}</p>
                <p class="text-[9px] text-gray-400 mt-1">${cardRows.length} transactions</p>
            </div>
            <div class="p-4 bg-white">
                <p class="text-[9px] text-gray-400 uppercase font-bold tracking-widest mb-1">🏦 Bank Transfer</p>
                <p class="text-lg font-bold text-gray-900">LKR ${fmt(bankTotal)}</p>
                <p class="text-[9px] text-gray-400 mt-1">${bankRows.length} transactions</p>
            </div>
            <div class="p-4 bg-black">
                <p class="text-[9px] text-yellow-400 uppercase font-bold tracking-widest mb-1">Total Collected</p>
                <p class="text-lg font-bold text-yellow-400">LKR ${fmt(grandTotal)}</p>
                <p class="text-[9px] text-gray-400 mt-1">Discounts: - LKR ${fmt(discTotal)}</p>
            </div>
        </div>`;

        thead.innerHTML = `<tr>
            <th class="px-6 py-3 text-left">Date & Time</th>
            <th class="px-6 py-3 text-left">Order ID</th>
            <th class="px-6 py-3 text-left">Customer</th>
            <th class="px-6 py-3 text-center">Channel</th>
            <th class="px-6 py-3 text-left">Method</th>
            <th class="px-6 py-3 text-center">Status</th>
            <th class="px-6 py-3 text-right text-gray-500">Sub Total</th>
            <th class="px-6 py-3 text-right text-red-500">Discount</th>
            <th class="px-6 py-3 text-right font-bold">Collected</th>
        </tr>`;
        tbody.innerHTML = filtered.map(r => {
            const isPos = (r.channel || '').toUpperCase() === 'POS';
            const isCash = (r.paymentMethod||'').toLowerCase().includes('cash');
            const channelBadge = isPos
                ? `<span class="px-2 py-1 bg-purple-100 text-purple-700 text-[9px] font-bold uppercase rounded">POS</span>`
                : `<span class="px-2 py-1 bg-blue-100 text-blue-700 text-[9px] font-bold uppercase rounded">WEB</span>`;

            const ps = (r.paymentStatus || '').toUpperCase();
            let statusBadge;
            if (ps.includes('COMPLET') || ps.includes('PAID') || ps.includes('SUCCESS')) {
                statusBadge = `<span class="px-2 py-1 bg-green-100 text-green-700 text-[9px] font-bold uppercase rounded">Paid</span>`;
            } else if (ps.includes('PENDING')) {
                statusBadge = `<span class="px-2 py-1 bg-yellow-100 text-yellow-700 text-[9px] font-bold uppercase rounded">Pending</span>`;
            } else if (ps.includes('CANCEL') || ps.includes('FAIL') || ps.includes('REFUND')) {
                statusBadge = `<span class="px-2 py-1 bg-red-100 text-red-700 text-[9px] font-bold uppercase rounded">${r.paymentStatus}</span>`;
            } else {
                statusBadge = `<span class="px-2 py-1 bg-gray-100 text-gray-600 text-[9px] font-bold uppercase rounded">${r.paymentStatus || '-'}</span>`;
            }

            const rowBg = isCash ? 'bg-amber-50/50' : '';
            let paymentMethodCell = `<td class="px-6 py-3 text-xs font-bold ${isCash ? 'text-amber-700' : 'text-gray-600'}">${r.paymentMethod || '-'}</td>`;
            
            let finalTotalCell = `<td class="px-6 py-3 text-right font-bold text-gray-900">LKR ${fmt(r.finalTotal)}</td>`;
            if (isCash && r.tenderedAmount != null && r.tenderedAmount > 0) {
                finalTotalCell = `
                <td class="px-6 py-3 text-right">
                    <div class="font-bold text-gray-900">LKR ${fmt(r.finalTotal)}</div>
                    <div class="text-[9px] text-gray-500 font-normal mt-1">Given: LKR ${fmt(r.tenderedAmount)}</div>
                    ${r.changeDue > 0 ? `<div class="text-[9px] text-red-400 font-normal">Change: LKR ${fmt(r.changeDue)}</div>` : ''}
                </td>`;
            }

            return `
            <tr class="${rowBg} hover:bg-gray-50 border-b border-gray-100 transition">
                <td class="px-6 py-3 font-mono text-gray-600 text-xs whitespace-nowrap">${r.dateTime || '-'}</td>
                <td class="px-6 py-3 font-bold text-gray-800 text-xs whitespace-nowrap">#${r.orderId || '-'}</td>
                <td class="px-6 py-3 text-gray-700 text-xs">${r.customerName || '-'}</td>
                <td class="px-6 py-3 text-center">${channelBadge}</td>
                ${paymentMethodCell}
                <td class="px-6 py-3 text-center">${statusBadge}</td>
                <td class="px-6 py-3 text-right text-gray-500 text-xs">LKR ${fmt(r.subTotal)}</td>
                <td class="px-6 py-3 text-right text-red-500 text-xs">${r.discount > 0 ? '- LKR ' + fmt(r.discount) : '-'}</td>
                ${finalTotalCell}
            </tr>`;
        }).join('');
    }
}

// ==========================================
// CHART RENDERING (same data — no extra fetch)
// ==========================================
function renderAnalyticsChart(data) {
    const chartWrapper = document.getElementById('analytics-chart-wrapper');
    const canvas = document.getElementById('analyticsMainChart');
    if (!chartWrapper || !canvas) return;

    // Destroy old chart
    if (analyticsChartInstance) {
        analyticsChartInstance.destroy();
        analyticsChartInstance = null;
    }

    // Transactions: no chart needed (it's a pure ledger log)
    if (currentCategory === 'transactions') {
        chartWrapper.classList.add('hidden');
        return;
    }

    if (!data || data.length === 0) {
        chartWrapper.classList.add('hidden');
        return;
    }

    let labels = [], chartData = [], chartType = 'bar', chartLabel = '', chartColors = '#d4af37';
    const GOLD = '#d4af37', BLACK = '#1a1a1a', GRAY = '#6b7280';

    if (currentCategory === 'sales') {
        labels     = data.map(r => r.date || '-');
        chartData  = data.map(r => r.netRevenue || 0);
        chartType  = 'line';
        chartLabel = 'Net Revenue (LKR)';
        chartColors = GOLD;

    } else if (currentCategory === 'product') {
        labels     = data.slice(0, 15).map(r => r.productName?.substring(0, 18) || '-');
        chartData  = data.slice(0, 15).map(r => r.netUnitsSold || 0);
        chartType  = 'bar';
        chartLabel = 'Net Units Sold';
        chartColors = data.slice(0, 15).map((_, i) => i % 2 === 0 ? GOLD : BLACK);

    } else if (currentCategory === 'finance') {
        labels     = data.map(r => r.paymentMethod);
        chartData  = data.map(r => r.totalAmount || 0);
        chartType  = 'doughnut';
        chartLabel = 'Revenue Share';
        chartColors = [BLACK, GOLD, GRAY, '#374151', '#9ca3af'];

    } else if (currentCategory === 'best-sellers') {
        labels     = data.slice(0, 10).map(r => r.productName?.substring(0, 18) || '-');
        chartData  = data.slice(0, 10).map(r => r.unitsSold || 0);
        chartType  = 'bar';
        chartLabel = 'Units Sold';
        chartColors = data.slice(0, 10).map((_, i) => i % 2 === 0 ? GOLD : BLACK);

    } else if (currentCategory === 'top-customers') {
        labels     = data.slice(0, 10).map(r => r.customerName?.substring(0, 18) || '-');
        chartData  = data.slice(0, 10).map(r => r.totalSpent || 0);
        chartType  = 'bar';
        chartLabel = 'Total Spent (LKR)';
        chartColors = data.slice(0, 10).map((_, i) => i % 2 === 0 ? GOLD : BLACK);

    } else if (currentCategory === 'order-status') {
        labels     = data.map(r => r.status);
        chartData  = data.map(r => r.count || 0);
        chartType  = 'doughnut';
        chartLabel = 'Order Count';
        chartColors = ['#f59e0b', '#3b82f6', '#10b981', '#ef4444', '#6b7280', '#8b5cf6'];
    }

    chartWrapper.classList.remove('hidden');

    const ctx = canvas.getContext('2d');
    analyticsChartInstance = new Chart(ctx, {
        type: chartType,
        data: {
            labels,
            datasets: [{
                label: chartLabel,
                data: chartData,
                backgroundColor: Array.isArray(chartColors) ? chartColors : chartColors,
                borderColor: chartType === 'line' ? GOLD : '#fff',
                borderWidth: chartType === 'line' ? 2 : 1,
                tension: 0.4,
                fill: chartType === 'line' ? { target: 'origin', below: 'rgba(212,175,55,0.08)' } : false,
                pointBackgroundColor: GOLD,
                pointRadius: chartType === 'line' ? 4 : 0,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: ['doughnut', 'pie'].includes(chartType),
                    position: 'right',
                    labels: { font: { size: 12 }, padding: 16 }
                },
                tooltip: {
                    backgroundColor: 'rgba(0,0,0,0.85)',
                    titleFont: { size: 13 },
                    bodyFont: { size: 14, weight: 'bold' },
                    callbacks: {
                        label: (ctx) => {
                            const val = ctx.raw;
                            if (['sales', 'finance', 'top-customers', 'best-sellers'].includes(currentCategory) && chartType !== 'doughnut') {
                                return ` LKR ${Number(val).toLocaleString(undefined, { minimumFractionDigits: 2 })}`;
                            }
                            return ` ${Number(val).toLocaleString()}`;
                        }
                    }
                }
            },
            scales: ['line', 'bar'].includes(chartType) ? {
                y: {
                    beginAtZero: true,
                    grid: { color: '#f3f4f6' },
                    ticks: { font: { size: 11 } }
                },
                x: {
                    grid: { display: false },
                    ticks: { font: { size: 11 }, maxRotation: 45 }
                }
            } : {}
        }
    });
}

// ==========================================
// FOOTER SUMMARY
// ==========================================
function updateFooterSummary(data) {
    const footerStats = document.getElementById('analytics-footer-stats');
    if (!footerStats) return;

    let html = '';

    if (currentCategory === 'sales') {
        const netRev   = data.reduce((s, r) => s + (r.netRevenue || 0), 0);
        const refunds  = data.reduce((s, r) => s + (r.totalRefunded || 0), 0);
        const orders   = data.reduce((s, r) => s + (r.totalOrders || 0), 0);
        html = `
            <div class="text-right border-r border-gray-200 pr-8">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Orders</p>
                <p class="text-xl font-bold text-gray-700">${orders}</p>
            </div>
            <div class="text-right border-r border-gray-200 pr-8">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Refunds</p>
                <p class="text-xl font-bold text-red-500">LKR ${fmt(refunds)}</p>
            </div>
            <div class="text-right">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Net Revenue</p>
                <p class="text-xl font-bold text-gold-600">LKR ${fmt(netRev)}</p>
            </div>`;

    } else if (currentCategory === 'product') {
        const netSold = data.reduce((s, r) => s + (r.netUnitsSold || 0), 0);
        const netRev  = data.reduce((s, r) => s + (r.netRevenue || 0), 0);
        html = `
            <div class="text-right border-r border-gray-200 pr-8">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Net Units Sold</p>
                <p class="text-xl font-bold text-gray-800">${netSold.toLocaleString()}</p>
            </div>
            <div class="text-right">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Net Product Revenue</p>
                <p class="text-xl font-bold text-gold-600">LKR ${fmt(netRev)}</p>
            </div>`;

    } else if (currentCategory === 'finance') {
        const vol  = data.reduce((s, r) => s + (r.totalAmount || 0), 0);
        const tax  = data.reduce((s, r) => s + (r.taxCollected || 0), 0);
        const disc = data.reduce((s, r) => s + (r.discountGiven || 0), 0);
        html = `
            <div class="text-right border-r border-gray-200 pr-8">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Discounts</p>
                <p class="text-xl font-bold text-red-500">LKR ${fmt(disc)}</p>
            </div>
            <div class="text-right border-r border-gray-200 pr-8 pl-8">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Tax Collected</p>
                <p class="text-xl font-bold text-gray-600">LKR ${fmt(tax)}</p>
            </div>
            <div class="text-right pl-8">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Volume</p>
                <p class="text-xl font-bold text-gold-600">LKR ${fmt(vol)}</p>
            </div>`;

    } else if (currentCategory === 'best-sellers') {
        const units = data.reduce((s, r) => s + (r.unitsSold || 0), 0);
        const rev   = data.reduce((s, r) => s + (r.revenueGenerated || 0), 0);
        html = `
            <div class="text-right border-r border-gray-200 pr-8">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Units Sold</p>
                <p class="text-xl font-bold text-gray-800">${units.toLocaleString()}</p>
            </div>
            <div class="text-right">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Revenue</p>
                <p class="text-xl font-bold text-gold-600">LKR ${fmt(rev)}</p>
            </div>`;

    } else if (currentCategory === 'top-customers') {
        const orders = data.reduce((s, r) => s + (r.totalOrders || 0), 0);
        const spent  = data.reduce((s, r) => s + (r.totalSpent || 0), 0);
        html = `
            <div class="text-right border-r border-gray-200 pr-8">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Orders</p>
                <p class="text-xl font-bold text-gray-800">${orders.toLocaleString()}</p>
            </div>
            <div class="text-right">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Spent</p>
                <p class="text-xl font-bold text-gold-600">LKR ${fmt(spent)}</p>
            </div>`;

    } else if (currentCategory === 'order-status') {
        const total = data.reduce((s, r) => s + (r.count || 0), 0);
        html = `
            <div class="text-right">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Orders</p>
                <p class="text-xl font-bold text-gray-800">${total.toLocaleString()}</p>
            </div>`;

    } else if (currentCategory === 'transactions') {
        const channelFilter  = document.getElementById('txn-channel-filter')?.value || 'all';
        let filtered = data;
        if (channelFilter === 'pos') filtered = data.filter(r => (r.channel||'').toUpperCase() === 'POS');
        else if (channelFilter === 'web') filtered = data.filter(r => (r.channel||'').toUpperCase() !== 'POS');
        
        const validRows = filtered.filter(r => {
            const ps = (r.paymentStatus || '').toUpperCase();
            return ps.includes('COMPLET') || ps.includes('PAID') || ps.includes('SUCCESS');
        });

        const totalCollected  = validRows.reduce((s, r) => s + (r.finalTotal || 0), 0);
        const totalDiscounts  = validRows.reduce((s, r) => s + (r.discount || 0), 0);
        const cashTotal       = validRows.filter(r => (r.paymentMethod||'').toLowerCase().includes('cash')).reduce((s,r) => s+(r.finalTotal||0), 0);
        const webCount = filtered.filter(r => (r.channel || '').toUpperCase() !== 'POS').length;
        const posCount = filtered.filter(r => (r.channel || '').toUpperCase() === 'POS').length;
        html = `
            <div class="text-right border-r border-gray-200 pr-6 pt-1">
                <p class="text-sm font-bold text-gray-700"><span class="text-blue-600">WEB: ${webCount}</span> &bull; <span class="text-purple-600">POS: ${posCount}</span></p>
            </div>
            <div class="text-right border-r border-gray-200 pr-6 pl-6">
                <p class="text-[10px] text-gray-400 uppercase font-bold">💵 Cash to Count</p>
                <p class="text-xl font-bold text-amber-600">LKR ${fmt(cashTotal)}</p>
            </div>
            <div class="text-right border-r border-gray-200 pr-6 pl-6">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Discounts</p>
                <p class="text-xl font-bold text-red-500">- LKR ${fmt(totalDiscounts)}</p>
            </div>
            <div class="text-right pl-6">
                <p class="text-[10px] text-gray-400 uppercase font-bold">Total Collected</p>
                <p class="text-xl font-bold text-green-600">LKR ${fmt(totalCollected)}</p>
            </div>`;
    }

    footerStats.innerHTML = html;
}

// ==========================================
// CSV EXPORT
// ==========================================
function downloadAnalyticsCSV() {
    if (!currentReportData || currentReportData.length === 0) {
        if (window.showToast) showToast('No data to export', 'error');
        return;
    }

    let headers = [], rows = [];

    if (currentCategory === 'sales') {
        headers = ['Date', 'Orders', 'Gross Revenue', 'Returns', 'Refunded', 'Net Revenue'];
        rows = currentReportData.map(r => [r.date, r.totalOrders || 0, r.totalRevenue || 0, r.totalReturns || 0, r.totalRefunded || 0, r.netRevenue || 0]);
    } else if (currentCategory === 'product') {
        headers = ['Product', 'Category', 'Sold', 'Returned', 'Net Sold', 'Net Revenue', 'Stock'];
        rows = currentReportData.map(r => [r.productName, r.category, r.unitsSold, r.unitsReturned, r.netUnitsSold, r.netRevenue, r.currentStock]);
    } else if (currentCategory === 'finance') {
        headers = ['Method', 'Transactions', 'Discounts', 'Tax', 'Total Volume'];
        rows = currentReportData.map(r => [r.paymentMethod, r.transactionCount, r.discountGiven, r.taxCollected, r.totalAmount]);
    } else if (currentCategory === 'best-sellers') {
        headers = ['Rank', 'Product', 'Units Sold', 'Revenue'];
        rows = currentReportData.map((r, i) => [i + 1, r.productName, r.unitsSold, r.revenueGenerated]);
    } else if (currentCategory === 'top-customers') {
        headers = ['Rank', 'Customer', 'Orders', 'Total Spent'];
        rows = currentReportData.map((r, i) => [i + 1, r.customerName, r.totalOrders, r.totalSpent]);
    } else if (currentCategory === 'order-status') {
        headers = ['Status', 'Count'];
        rows = currentReportData.map(r => [r.status, r.count]);
    } else if (currentCategory === 'transactions') {
        headers = ['Date & Time', 'Order ID', 'Transaction ID', 'Customer', 'Channel', 'Payment Method', 'Status', 'Sub Total (LKR)', 'Discount (LKR)', 'Collected (LKR)'];
        rows = currentReportData.map(r => [
            r.dateTime, r.orderId, r.transactionId, r.customerName,
            r.channel, r.paymentMethod, r.paymentStatus,
            r.subTotal || 0, r.discount || 0, r.finalTotal || 0
        ]);
    }

    const csv = '\uFEFF' + [headers.join(','), ...rows.map(r => r.map(v => `"${v}"`).join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `VeloraJewellery_${currentCategory}_Report.csv`; a.click();
    URL.revokeObjectURL(url);
}

// ==========================================
// HELPER
// ==========================================
function fmt(val) {
    return Number(val || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// ==========================================
// EXPOSE GLOBALS
// ==========================================
window.loadAnalytics          = loadAnalytics;
window.toggleAnalyticsFilters = toggleAnalyticsFilters;
window.applyAnalyticsFilters  = applyAnalyticsFilters;
window.downloadAnalyticsCSV   = downloadAnalyticsCSV;
window.fetchAnalyticsData     = fetchAndRender;
window.setTxnToday            = setTxnToday;
window.setTxnThisMonth        = setTxnThisMonth;