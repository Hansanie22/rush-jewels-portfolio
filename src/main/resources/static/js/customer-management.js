// ==========================================
// CUSTOMER MANAGEMENT LOGIC
// ==========================================

let cachedCustomers = [];

// 1. Load Customers
async function loadCustomers() {
    try {
        const response = await fetch('/api/admin/customers');
        if (!response.ok) throw new Error('Failed to fetch customers');

        const customers = await response.json();
        cachedCustomers = customers; // Cache for searching

        renderCustomerTable(customers);

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading customers', 'error');
    }
}

// 2. Render Table
function renderCustomerTable(data) {
    const tbody = document.getElementById('customers-body');

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="px-6 py-4 text-center text-gray-500">No customers found.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(c => `
        <tr class="hover:bg-gray-50 transition border-b border-gray-100">
            <td class="px-6 py-4 font-bold text-gray-800">${c.name}</td>
            <td class="px-6 py-4 text-gray-600 text-xs">${c.email}</td>
            <td class="px-6 py-4 font-bold text-gray-900">LKR ${c.totalSpent.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
            <td class="px-6 py-4 text-center">
                <span class="bg-gray-100 text-gray-800 text-xs font-bold px-2.5 py-0.5 rounded border border-gray-200">
                    ${c.orderCount}
                </span>
            </td>
            <td class="px-6 py-4 text-right">
                <label class="inline-flex relative items-center cursor-pointer">
                    <input type="checkbox" class="sr-only peer" 
                           ${c.statusId === 1 ? 'checked' : ''} 
                           onchange="toggleCustomerStatus(${c.id}, this.checked)">
                    <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-gold-300 rounded-full peer peer-checked:bg-gold-400 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border after:rounded-full after:h-4 after:w-4 after:transition-all"></div>
                </label>
            </td>
        </tr>
    `).join('');
}

// 3. Toggle Status
async function toggleCustomerStatus(id, active) {
    try {
        const response = await fetch(`/api/admin/customers/${id}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ active: active })
        });

        if (!response.ok) throw new Error('Status update failed');

        if(window.showToast) showToast(active ? 'Customer Activated' : 'Customer Deactivated', 'success');

        // Optional: Update local cache if needed, or reload
        // loadCustomers();
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error updating status', 'error');
        loadCustomers(); // Revert switch UI on failure
    }
}

// 4. Search Filter
function filterCustomers() {
    const search = document.getElementById('customer-search').value.toLowerCase();

    const filtered = cachedCustomers.filter(c =>
        c.name.toLowerCase().includes(search) ||
        c.email.toLowerCase().includes(search)
    );

    renderCustomerTable(filtered);
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadCustomers();
});