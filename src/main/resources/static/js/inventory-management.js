// ==========================================
// INVENTORY MANAGEMENT LOGIC (WAREHOUSE 1)
// ==========================================

let cachedInventory = [];

// 1. Load Inventory Data
async function loadInventory() {
    try {
        const response = await fetch('/api/admin/inventory');

        if (!response.ok) throw new Error('Failed to fetch inventory');

        const allInventory = await response.json();

        // Filter: Only Warehouse 1
        const inventory = allInventory.filter(item => item.warehouseId === 1);
        cachedInventory = inventory;


        // 1. Update Stats FIRST
        updateDashboardStats(inventory);

        // 2. Render Table
        renderInventoryTable(inventory);

    } catch (err) {
        if(window.showToast) showToast('Error loading inventory', 'error');
    }
}

// 2. Update Dashboard Cards
function updateDashboardStats(data) {
    if (!data) data = [];

    // Calculate Totals (Handle nulls safely)
    const totalItems = data.reduce((sum, item) => sum + (item.currentStock || 0), 0);

    // Normalize Status Checks (Case Insensitive)
    const lowStockCount = data.filter(item => (item.status || '').trim().toLowerCase() === 'low stock').length;
    const outStockCount = data.filter(item => (item.status || '').trim().toLowerCase() === 'out of stock').length;

    const totalValue = data.reduce((sum, item) => sum + (item.totalValue || 0), 0);

    // Update DOM Elements (Must match HTML IDs)
    setStatValue('inv-total-items', totalItems.toLocaleString());

    setStatValue('inv-low-stock', lowStockCount.toString(),
        lowStockCount > 0 ? "text-2xl font-bold text-red-600 mt-1" : "text-2xl font-bold text-gray-900 mt-1"
    );

    setStatValue('inv-out-stock', outStockCount.toString(),
        outStockCount > 0 ? "text-2xl font-bold text-red-600 mt-1" : "text-2xl font-bold text-gray-900 mt-1"
    );

    setStatValue('inv-total-value', 'LKR ' + formatCompactNumber(totalValue));
}

// Helper to safely update values
function setStatValue(elementId, value, className = null) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerText = value;
        if (className) element.className = className;
    }
}

// 3. Render Table
function renderInventoryTable(data) {
    const tbody = document.getElementById('inventory-body');
    if (!tbody) return;

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="px-6 py-4 text-center text-gray-500">No inventory records found.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(item => {
        let statusClass = 'bg-green-100 text-green-800';
        const status = item.status || 'Unknown';

        if (status.toLowerCase() === 'out of stock') statusClass = 'bg-gray-200 text-gray-800';
        else if (status.toLowerCase() === 'low stock') statusClass = 'bg-red-100 text-red-800';

        const unitPrice = item.unitPrice || 0;
        const totalValue = item.totalValue || 0;

        return `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-6 py-4">
                    <div class="font-bold text-gray-800 text-xs">${item.itemName || 'Unknown'}</div>
                    <div class="text-[10px] text-gray-400 uppercase">${item.type || '-'}</div>
                </td>
                <td class="px-6 py-4 text-xs text-gray-500 font-mono">${item.sku || '-'}</td>
                <td class="px-6 py-4 text-xs text-gray-600">${item.category || '-'}</td>
                <td class="px-6 py-4 font-bold text-gray-900">${item.currentStock || 0}</td>
                <td class="px-6 py-4 text-xs text-gray-500">${item.minStockLimit || 0}</td>
                <td class="px-6 py-4">
                    <span class="px-2 py-1 text-[10px] uppercase font-bold rounded ${statusClass}">${status}</span>
                </td>
                <td class="px-6 py-4 text-xs text-gray-600">LKR ${unitPrice.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                <td class="px-6 py-4 font-bold text-gray-800 text-xs">LKR ${totalValue.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                <td class="px-6 py-4 text-right">
                    <button onclick="openStockAdjustment(${item.stockId})" 
                            class="bg-black text-gold-400 hover:bg-gray-800 text-xs font-bold px-3 py-1 rounded transition uppercase">Adjust</button>
                </td>
            </tr>
        `;
    }).join('');
}

function formatCompactNumber(number) {
    return new Intl.NumberFormat('en-US', { notation: "compact", maximumFractionDigits: 1 }).format(number);
}

// 4. Filter Functions
function filterInventoryByStatus(status) {
    if (!cachedInventory) return;

    let filteredData = cachedInventory;
    const statusKey = status.toLowerCase();

    if (statusKey === 'in-stock') filteredData = cachedInventory.filter(i => (i.status || '').toLowerCase() === 'in stock');
    if (statusKey === 'low-stock') filteredData = cachedInventory.filter(i => (i.status || '').toLowerCase() === 'low stock');
    if (statusKey === 'out-stock') filteredData = cachedInventory.filter(i => (i.status || '').toLowerCase() === 'out of stock');

    renderInventoryTable(filteredData);
    updateDashboardStats(filteredData);
}

// 🔥 RENAMED SEARCH FUNCTION TO AVOID CONFLICT WITH SEASONAL LOGIC
function filterInventoryTable() {
    const input = document.getElementById('inventory-search');
    if (!input) return;

    // Convert input to lower case for comparison
    const filter = input.value.toLowerCase().trim();

    // If cache is empty, do nothing
    if (!cachedInventory || cachedInventory.length === 0) return;

    const filteredData = cachedInventory.filter(item => {
        // Convert fields to String first to avoid crashes on numbers
        const name = String(item.itemName || '').toLowerCase();
        const sku = String(item.sku || '').toLowerCase();
        const category = String(item.category || '').toLowerCase();
        const type = String(item.type || '').toLowerCase();
        const status = String(item.status || '').toLowerCase();

        return name.includes(filter) ||
            sku.includes(filter) ||
            category.includes(filter) ||
            type.includes(filter) ||
            status.includes(filter);
    });

    renderInventoryTable(filteredData);
    updateDashboardStats(filteredData);
}
// Make it globally available just in case HTML uses onkeyup="filterInventoryTable()"
window.filterInventoryTable = filterInventoryTable;

// 5. Adjustment Logic (Warehouse 2)
function openStockAdjustment(stockId) {
    const select = document.getElementById('adjustment-product');
    if (!select) return;

    // Load Cached Data into dropdown
    select.innerHTML = '<option value="">Choose a product...</option>' +
        cachedInventory.map(item => `<option value="${item.stockId}">${item.itemName} (Curr: ${item.currentStock})</option>`).join('');

    if (stockId) {
        select.value = stockId;
        updateCurrentStockDisplay();
    }

    document.getElementById('adjustment-type').value = 'add';
    document.getElementById('adjustment-quantity').value = '';
    document.getElementById('adjustment-notes').value = '';
    document.getElementById('new-stock-display').innerText = '-';

    if (typeof showSection === 'function') showSection('stock-adjustment');
}

function updateCurrentStockDisplay() {
    const stockId = parseInt(document.getElementById('adjustment-product').value);
    const item = cachedInventory.find(i => i.stockId === stockId);
    if (item) {
        document.getElementById('current-stock-display').value = item.currentStock;
        calculateNewStock();
    }
}

function calculateNewStock() {
    const stockId = parseInt(document.getElementById('adjustment-product').value);
    const type = document.getElementById('adjustment-type').value;
    const qty = parseInt(document.getElementById('adjustment-quantity').value) || 0;
    const item = cachedInventory.find(i => i.stockId === stockId);

    if (!item) return;

    let newQty = item.currentStock;
    if (type === 'add') newQty += qty;
    else if (type === 'remove') newQty = Math.max(0, newQty - qty);
    else if (type === 'set') newQty = Math.max(0, qty);

    document.getElementById('new-stock-display').innerText = newQty;
}

async function handleStockAdjustment(e) {
    e.preventDefault(); // Disable default browser validation

    const stockId = document.getElementById('adjustment-product').value;
    const type = document.getElementById('adjustment-type').value;
    const qty = document.getElementById('adjustment-quantity').value;
    const notes = document.getElementById('adjustment-notes').value.trim();

    // -----------------------------
    // 🔥 CUSTOM VALIDATION
    // -----------------------------
    if (!stockId) {
        if (window.showToast) showToast('Please select a product', 'error');
        return;
    }

    if (!type) {
        if (window.showToast) showToast('Please select adjustment type', 'error');
        return;
    }

    if (!qty || isNaN(qty) || parseInt(qty) <= 0) {
        if (window.showToast) showToast('Please enter a valid quantity', 'error');
        return;
    }

    if (!notes) {
        if (window.showToast) showToast('Please provide a reason for adjustment', 'error');
        return;
    }

    // -----------------------------
    // 🔥 BUILD PAYLOAD
    // -----------------------------
    const payload = {
        stockId: parseInt(stockId),
        adjustmentType: type,
        quantity: parseInt(qty),
        reason: notes
    };

    // -----------------------------
    // 🔥 API REQUEST
    // -----------------------------
    try {
        const response = await fetch('/api/admin/inventory/adjust', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText || 'Adjustment failed');
        }

        if (window.showToast) showToast('Stock adjusted successfully', 'success');
        await loadInventory();

        if (typeof showSection === 'function') showSection('inventory');
    } catch (err) {
        if (window.showToast) showToast('Error applying adjustment: ' + err.message, 'error');
    }
}


// Init
document.addEventListener('DOMContentLoaded', () => {
    loadInventory();

    const qtyInput = document.getElementById('adjustment-quantity');
    const typeInput = document.getElementById('adjustment-type');
    const prodSelect = document.getElementById('adjustment-product');
    const searchInput = document.getElementById('inventory-search');

    if(qtyInput) qtyInput.addEventListener('input', calculateNewStock);
    if(typeInput) typeInput.addEventListener('change', calculateNewStock);
    if(prodSelect) prodSelect.addEventListener('change', updateCurrentStockDisplay);

    // UPDATED: Use the UNIQUE renamed function here
    if(searchInput) searchInput.addEventListener('input', filterInventoryTable);
});