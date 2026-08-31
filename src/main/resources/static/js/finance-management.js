// ==========================================
// FINANCE MANAGEMENT LOGIC
// ==========================================

async function loadFinanceData() {
    loadPaymentMethods();
    loadTaxRate();
}

// 1. Payment Methods
async function loadPaymentMethods() {
    try {
        const response = await fetch('/api/admin/finance/payments');
        if (!response.ok) throw new Error('Failed to fetch payments');

        const methods = await response.json();
        const container = document.getElementById('payment-methods-container');

        container.innerHTML = methods.map(m => `
            <div class="flex items-center justify-between py-3 border-b border-gray-50 last:border-0">
                <span class="text-sm font-medium text-gray-700">${m.method}</span>
                <label class="inline-flex relative items-center cursor-pointer">
                    <input type="checkbox" class="sr-only peer" 
                           ${m.active ? 'checked' : ''} 
                           onchange="togglePaymentMethod(${m.id}, this.checked)">
                    <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-gold-300 rounded-full peer peer-checked:bg-gold-400 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border after:rounded-full after:h-4 after:w-4 after:transition-all"></div>
                </label>
            </div>
        `).join('');

    } catch (err) {
        console.error(err);
    }
}

async function togglePaymentMethod(id, active) {
    try {
        const response = await fetch(`/api/admin/finance/payments/${id}/toggle`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ active })
        });

        if(response.ok && window.showToast) {
            showToast(active ? 'Payment Method Enabled' : 'Payment Method Disabled', 'success');
        }
    } catch (e) {
        console.error(e);
        if(window.showToast) showToast('Error updating status', 'error');
        loadPaymentMethods(); // Revert UI on error
    }
}

// 2. Tax Rate
async function loadTaxRate() {
    try {
        const response = await fetch('/api/admin/finance/tax');
        if (!response.ok) throw new Error('Failed');
        const data = await response.json();

        document.getElementById('tax-rate-input').value = data.rate;
    } catch (e) { console.error(e); }
}

async function handleTaxSubmit(e) {
    e.preventDefault();
    const rate = document.getElementById('tax-rate-input').value;

    try {
        const response = await fetch('/api/admin/finance/tax', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ rate })
        });

        if (response.ok) {
            if(window.showToast) showToast('Tax Rate Updated', 'success');
        } else {
            throw new Error('Failed');
        }
    } catch (e) {
        console.error(e);
        if(window.showToast) showToast('Error updating tax', 'error');
    }
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadFinanceData(); // ✅ FIX: Automatically load data on page load
});