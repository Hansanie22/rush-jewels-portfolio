// ==========================================
// WAREHOUSE MANAGEMENT LOGIC
// ==========================================

let cachedWhInventory = [];

async function loadWarehouseData() {
    loadWarehouseStats();
    loadWarehouseActivity();

    // Load Dropdown first, then load table
    await loadWarehouseLocationsDropdown();
    loadWarehouseTable();
}

// 1. Load Locations Dropdown (For Table Filter)
async function loadWarehouseLocationsDropdown() {
    try {
        const response = await fetch('/api/admin/warehouse/locations');
        if (!response.ok) return;
        const locations = await response.json();

        const select = document.getElementById('wh-location-select');
        if(select) {
            select.innerHTML = locations.map(w => `<option value="${w.id}">${w.warehouse}</option>`).join('');
            // Default to Shop (ID 2) if available
            if (locations.some(l => l.id === 2)) select.value = 2;
        }
    } catch (e) { console.error("Dropdown Error:", e); }
}

// 2. Load Warehouse Table
async function loadWarehouseTable() {
    try {
        const warehouseId = document.getElementById('wh-location-select').value || 1;
        const response = await fetch(`/api/admin/inventory?warehouseId=${warehouseId}`);

        if (!response.ok) throw new Error('Failed to fetch inventory');

        const inventory = await response.json();
        cachedWhInventory = inventory;

        renderWhTable(inventory);
    } catch (err) {
        console.error("Table Error:", err);
    }
}

function renderWhTable(data) {
    const tbody = document.getElementById('wh-inventory-body');
    if (!tbody) return;

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="px-6 py-4 text-center text-gray-500">No items found in this warehouse.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(item => {
        let statusClass = 'bg-green-100 text-green-800';
        if (item.status === 'Out of Stock') statusClass = 'bg-gray-200 text-gray-800';
        else if (item.status === 'Low Stock') statusClass = 'bg-red-100 text-red-800';

        return `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-6 py-4 font-bold text-gray-800 text-xs">
                    ${item.itemName} <br>
                    <span class="text-[10px] text-gray-400 font-normal">${item.sku}</span>
                </td>
                <td class="px-6 py-4 text-xs text-gray-600">${item.category}</td>
                <td class="px-6 py-4 text-xs text-gray-600">${item.warehouseName}</td>
                <td class="px-6 py-4 font-bold text-gray-900">${item.currentStock}</td>
                <td class="px-6 py-4">
                    <span class="px-2 py-1 text-[10px] uppercase font-bold rounded ${statusClass}">${item.status}</span>
                </td>
                <td class="px-6 py-4 text-right">
                    <button onclick="openWhStockAdjustment(${item.stockId})" 
                            class="bg-black text-gold-400 hover:bg-gray-800 text-xs font-bold px-3 py-1 rounded transition uppercase">
                        Adjust
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

// 3. Load Stats
async function loadWarehouseStats() {
    try {
        const response = await fetch('/api/admin/warehouse/stats');
        if (!response.ok) return;
        const stats = await response.json();

        document.getElementById('wh-total-items').innerText = stats.totalItems.toLocaleString();
        document.getElementById('wh-low-stock').innerText = stats.lowStockCount;
        document.getElementById('wh-ready-stock').innerText = stats.inStockCount;
    } catch (err) {
        console.error("Stats Error:", err);
    }
}

// 4. Load Activity
async function loadWarehouseActivity() {
    try {
        const response = await fetch('/api/admin/warehouse/activity');
        if (!response.ok) return;
        const activities = await response.json();

        const container = document.getElementById('warehouse-activity-feed');
        if (activities.length === 0) {
            container.innerHTML = '<p class="text-gray-400 text-xs italic">No recent activity.</p>';
            return;
        }

        container.innerHTML = activities.map(act => `
            <div class="flex items-start border-l-2 border-${act.color}-500 pl-3">
                <div class="flex-1">
                    <p class="text-sm text-gray-800">${act.description}</p>
                    <p class="text-xs text-gray-500">${act.timeAgo}</p>
                </div>
            </div>
        `).join('');

    } catch (err) {
        console.error("Activity Error:", err);
    }
}

// ---------------------------------------------------------
// ADJUSTMENT MODAL LOGIC
// ---------------------------------------------------------

function openWhStockAdjustment(stockId) {
    const item = cachedWhInventory.find(i => i.stockId === stockId);
    if (!item) return;

    document.getElementById('wh-adjust-stock-id').value = stockId;
    document.getElementById('wh-adjust-item-name').innerText = item.itemName;
    document.getElementById('wh-adjust-location').innerText = item.warehouseName;
    document.getElementById('wh-adjust-current-qty').innerText = item.currentStock;

    document.getElementById('wh-adjust-type').value = 'add';
    document.getElementById('wh-adjust-qty').value = '';
    document.getElementById('wh-adjust-reason').value = '';
    document.getElementById('wh-adjust-new-qty').innerText = '-';

    const modal = document.getElementById('wh-stock-adjustment-modal');
    modal.classList.remove('hidden');
    modal.classList.add('flex'); // Center
}

function closeWhAdjustmentModal() {
    const modal = document.getElementById('wh-stock-adjustment-modal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
}

function calculateWhNewStock() {
    const currentQty = parseInt(document.getElementById('wh-adjust-current-qty').innerText) || 0;
    const type = document.getElementById('wh-adjust-type').value;
    const qty = parseInt(document.getElementById('wh-adjust-qty').value) || 0;

    let newQty = currentQty;
    if (type === 'add') newQty += qty;
    else if (type === 'remove') newQty = Math.max(0, currentQty - qty);
    else if (type === 'set') newQty = Math.max(0, qty);

    document.getElementById('wh-adjust-new-qty').innerText = newQty;
}

async function handleWhStockAdjustment(e) {
    e.preventDefault();

    const stockId = document.getElementById('wh-adjust-stock-id').value;
    const adjustmentType = document.getElementById('wh-adjust-type').value;
    const qty = document.getElementById('wh-adjust-qty').value;
    const reason = document.getElementById('wh-adjust-reason').value.trim();

    // -----------------------------
    // 🔥 CUSTOM VALIDATION
    // -----------------------------
    if (!stockId) {
        if(window.showToast) showToast('Please select a stock item', 'error');
        return;
    }

    if (!adjustmentType) {
        if(window.showToast) showToast('Please select an adjustment type', 'error');
        return;
    }

    const quantity = parseInt(qty);
    if (isNaN(quantity) || quantity <= 0) {
        if(window.showToast) showToast('Please enter a valid quantity greater than 0', 'error');
        return;
    }

    if (!reason) {
        if(window.showToast) showToast('Please provide a reason for the adjustment', 'error');
        return;
    }

    const payload = {
        stockId: parseInt(stockId),
        adjustmentType,
        quantity,
        reason
    };

    try {
        const response = await fetch('/api/admin/inventory/adjust', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error('Adjustment failed');

        if(window.showToast) showToast('Stock adjusted successfully', 'success');
        closeWhAdjustmentModal();
        loadWarehouseTable();
        loadWarehouseStats();
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast(err.message, 'error');
    }
}


// ---------------------------------------------------------
// TRANSFER MODAL LOGIC
// ---------------------------------------------------------

async function openStockTransferModal() {
    document.getElementById('transfer-qty').value = '';

    // Load Dropdowns
    await loadWarehousesForTransfer();
    await loadProductVariantsForTransfer();

    const modal = document.getElementById('stock-transfer-modal');
    modal.classList.remove('hidden');
    // ✅ FIX: Add 'flex' to properly center the modal on screen
    modal.classList.add('flex');
}

function closeStockTransferModal() {
    const modal = document.getElementById('stock-transfer-modal');
    modal.classList.add('hidden');
    // ✅ FIX: Remove 'flex' to clean up state
    modal.classList.remove('flex');
}

async function loadWarehousesForTransfer() {
    try {
        const response = await fetch('/api/admin/warehouse/locations');
        if (!response.ok) throw new Error("Failed JSON");
        const locations = await response.json();

        const options = locations.map(w => `<option value="${w.id}">${w.warehouse}</option>`).join('');

        document.getElementById('transfer-from').innerHTML = options;
        document.getElementById('transfer-to').innerHTML = options;

        // Default selection: Factory(1) -> Shop(2)
        if(locations.length >= 2) {
            document.getElementById('transfer-from').value = locations[0].id;
            document.getElementById('transfer-to').value = locations[1].id;
        }
    } catch (e) {
        console.error(e);
        if(window.showToast) showToast('Error loading warehouses', 'error');
    }
}

async function loadProductVariantsForTransfer() {
    try {
        const response = await fetch('/api/admin/variances');
        const variants = await response.json();

        const select = document.getElementById('transfer-product');
        select.innerHTML = '<option value="">Select Product</option>' +
            variants.map(v => `<option value="${v.id}">${v.productName} (${v.sizeName}/${v.colorName})</option>`).join('');
    } catch (e) { console.error(e); }
}

async function handleStockTransfer(e) {
    e.preventDefault();

    const varianceId = document.getElementById('transfer-product').value;
    const fromWarehouseId = document.getElementById('transfer-from').value;
    const toWarehouseId = document.getElementById('transfer-to').value;
    const qty = document.getElementById('transfer-qty').value;

    // -----------------------------
    // 🔥 CUSTOM VALIDATION
    // -----------------------------
    if (!varianceId) {
        if(window.showToast) showToast('Please select a product', 'error');
        return;
    }

    if (!fromWarehouseId) {
        if(window.showToast) showToast('Please select a source warehouse', 'error');
        return;
    }

    if (!toWarehouseId) {
        if(window.showToast) showToast('Please select a destination warehouse', 'error');
        return;
    }

    if (fromWarehouseId === toWarehouseId) {
        if(window.showToast) showToast('Source and Destination cannot be the same', 'error');
        return;
    }

    const quantity = parseInt(qty);
    if (isNaN(quantity) || quantity <= 0) {
        if(window.showToast) showToast('Please enter a valid quantity greater than 0', 'error');
        return;
    }

    const payload = {
        varianceId: parseInt(varianceId),
        fromWarehouseId: parseInt(fromWarehouseId),
        toWarehouseId: parseInt(toWarehouseId),
        quantity
    };

    try {
        const response = await fetch('/api/admin/warehouse/transfer', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const txt = await response.text();
            throw new Error(txt || 'Transfer failed');
        }

        if(window.showToast) showToast('Stock transferred successfully', 'success');
        closeStockTransferModal();
        loadWarehouseTable();
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast(err.message, 'error');
    }
}


// Filter
function filterWarehouseTable() {
    const search = document.getElementById('wh-search').value.toLowerCase();
    const filtered = cachedWhInventory.filter(item =>
        item.itemName.toLowerCase().includes(search) ||
        item.sku.toLowerCase().includes(search)
    );
    renderWhTable(filtered);
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadWarehouseData();
});