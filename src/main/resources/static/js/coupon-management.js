// ==========================================
// COUPON MANAGEMENT LOGIC
// ==========================================

let editingCouponId = null;

// 1. Load Coupons
async function loadCoupons() {
    try {
        const response = await fetch('/api/admin/coupons');
        if (!response.ok) throw new Error('Failed to fetch coupons');

        const coupons = await response.json();
        renderCouponsTable(coupons);

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading coupons', 'error');
    }
}

// 2. Render Table
function renderCouponsTable(data) {
    const tbody = document.getElementById('coupons-body');

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="px-6 py-4 text-center text-gray-500">No coupons found.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(c => {
        const isExpired = new Date(c.expirationDate) < new Date();
        const usageText = (c.usageLimit && c.usageLimit > 0) ? `${c.usedCount}/${c.usageLimit}` : `${c.usedCount}/∞`;

        let statusBadge = '';
        if (!c.active) {
            statusBadge = '<span class="text-xs font-bold text-red-600 bg-red-100 px-2 py-1 rounded">Inactive</span>';
        } else if (isExpired) {
            statusBadge = '<span class="text-xs font-bold text-orange-600 bg-orange-100 px-2 py-1 rounded">Expired</span>';
        } else {
            statusBadge = '<span class="text-xs font-bold text-green-600 bg-green-100 px-2 py-1 rounded">Active</span>';
        }

        return `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100 ${!c.active ? 'opacity-60 bg-gray-50' : ''}">
                <td class="px-6 py-4 font-mono font-bold text-gray-800">${c.code}</td>
                <td class="px-6 py-4 font-bold text-green-700">${c.value}% OFF</td>
                <td class="px-6 py-4 text-xs text-gray-600">
                    ${new Date(c.expirationDate).toLocaleDateString()}
                </td>
                <td class="px-6 py-4 text-xs font-bold text-gray-700">${usageText}</td>
                <td class="px-6 py-4">${statusBadge}</td>
                <td class="px-6 py-4 text-right">
                    ${c.active ? `
                        <button onclick="openEditCoupon(${c.id})" class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase mr-3">Edit</button>
                        <button onclick="deleteCoupon(${c.id})" class="text-red-600 hover:text-red-800 text-xs font-bold uppercase">Remove</button>
                    ` : '<span class="text-xs text-gray-400 italic">Removed</span>'}
                </td>
            </tr>
        `;
    }).join('');
}

// 3. Open Modal: Create
function openAddCouponModal() {
    editingCouponId = null;
    document.getElementById('coupon-edit-id').value = '';
    document.getElementById('coupon-code').value = '';
    document.getElementById('coupon-value').value = '';
    document.getElementById('coupon-expiry').value = '';
    document.getElementById('coupon-limit').value = '';

    document.getElementById('coupon-form-title').innerText = 'Create New Coupon';
    document.getElementById('coupon-submit-text').innerText = 'Create Coupon';

    showSection('add-coupon');
}

// 4. Open Modal: Edit
async function openEditCoupon(id) {
    try {
        const response = await fetch('/api/admin/coupons');
        const coupons = await response.json();
        const c = coupons.find(x => x.id === id);

        if (!c) throw new Error("Coupon not found");

        editingCouponId = id;
        document.getElementById('coupon-edit-id').value = c.id;
        document.getElementById('coupon-code').value = c.code;
        document.getElementById('coupon-value').value = c.value;

        const dateObj = new Date(c.expirationDate);
        const dateStr = dateObj.toISOString().split('T')[0];
        document.getElementById('coupon-expiry').value = dateStr;

        document.getElementById('coupon-limit').value = c.usageLimit || '';

        document.getElementById('coupon-form-title').innerText = 'Edit Coupon';
        document.getElementById('coupon-submit-text').innerText = 'Update Coupon';

        showSection('add-coupon');
    } catch(err) {
        console.error(err);
        showToast('Error loading details', 'error');
    }
}

// 5. Submit Handler
async function handleCouponSubmit(e) {
    e.preventDefault();

    const code = document.getElementById('coupon-code').value.trim().toUpperCase();
    const value = parseFloat(document.getElementById('coupon-value').value);
    const expirationDate = document.getElementById('coupon-expiry').value;
    const usageLimitInput = document.getElementById('coupon-limit').value;
    const usageLimit = usageLimitInput ? parseInt(usageLimitInput) : null;

    // -----------------------------
    // 🔥 CUSTOM VALIDATION
    // -----------------------------
    if (!code) {
        if (window.showToast) showToast('Coupon code is required', 'error');
        return;
    }

    if (!value || isNaN(value) || value <= 0 || value > 100) {
        if (window.showToast) showToast('Coupon value must be between 1% and 100%', 'error');
        return;
    }

    if (!expirationDate) {
        if (window.showToast) showToast('Expiration date is required', 'error');
        return;
    }

    if (usageLimitInput && (isNaN(usageLimit) || usageLimit <= 0)) {
        if (window.showToast) showToast('Usage limit must be a positive number', 'error');
        return;
    }

    const payload = {
        code,
        value,
        expirationDate,
        usageLimit
    };

    try {
        const url = editingCouponId ? `/api/admin/coupons/${editingCouponId}/update` : '/api/admin/coupons';
        const method = 'POST';

        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to save');
        }

        if(window.showToast) showToast(editingCouponId ? 'Coupon updated' : 'Coupon created', 'success');
        showSection('coupons');
        loadCoupons();
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast(err.message, 'error');
    }
}


// 6. Delete (Deactivate) with Custom Modal
function deleteCoupon(id) {
    // Replace native confirm with custom dialog
    showConfirmDialog(
        "Are you sure you want to remove this coupon? It will be marked as inactive.",
        async () => {
            try {
                const response = await fetch(`/api/admin/coupons/${id}/delete`, { method: 'POST' });

                if (!response.ok) throw new Error('Failed to delete');

                if(window.showToast) showToast('Coupon deactivated', 'success');
                loadCoupons();
            } catch (err) {
                console.error(err);
                if(window.showToast) showToast('Error deleting coupon', 'error');
            }
        }
    );
}

// ----------------------------------------------------
// CUSTOM CONFIRMATION DIALOG (Matches Toast Style)
// ----------------------------------------------------
function showConfirmDialog(message, onConfirm) {
    // Check if modal already exists
    let modal = document.getElementById('custom-confirm-modal');

    // If not, create it dynamically
    if (!modal) {
        const modalHTML = `
            <div id="custom-confirm-modal" class="fixed inset-0 z-[60] flex items-center justify-center bg-black bg-opacity-50 hidden transition-opacity duration-300 opacity-0">
                <div class="bg-white rounded shadow-xl w-full max-w-sm p-0 border-t-4 border-gold-500 transform scale-95 transition-transform duration-300">
                    <div class="p-6">
                        <h3 class="text-xs font-bold uppercase tracking-wider text-gray-900 mb-2 flex items-center gap-2">
                             Confirmation
                        </h3>
                        <p class="text-sm text-gray-600 leading-relaxed" id="confirm-modal-message"></p>
                    </div>
                    <div class="bg-gray-50 px-6 py-3 flex justify-end gap-3 rounded-b">
                        <button id="btn-confirm-cancel" class="px-4 py-2 text-xs font-bold uppercase text-gray-500 hover:text-gray-700 hover:bg-gray-200 rounded transition">Cancel</button>
                        <button id="btn-confirm-yes" class="bg-black text-gold-400 px-5 py-2 text-xs font-bold uppercase hover:bg-gray-900 rounded shadow-sm transition">Yes, Proceed</button>
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        modal = document.getElementById('custom-confirm-modal');
    }

    // Set Message
    document.getElementById('confirm-modal-message').innerText = message;

    // Handle Confirm Click
    const yesBtn = document.getElementById('btn-confirm-yes');
    // Clone button to remove old listeners
    const newYesBtn = yesBtn.cloneNode(true);
    yesBtn.parentNode.replaceChild(newYesBtn, yesBtn);

    newYesBtn.addEventListener('click', () => {
        onConfirm();
        closeConfirmModal();
    });

    // Handle Cancel Click
    const cancelBtn = document.getElementById('btn-confirm-cancel');
    const newCancelBtn = cancelBtn.cloneNode(true);
    cancelBtn.parentNode.replaceChild(newCancelBtn, cancelBtn);

    newCancelBtn.addEventListener('click', closeConfirmModal);

    // Show Modal
    modal.classList.remove('hidden');
    // Slight delay to allow display block to apply before opacity transition
    setTimeout(() => {
        modal.classList.remove('opacity-0');
        modal.querySelector('div').classList.remove('scale-95');
        modal.querySelector('div').classList.add('scale-100');
    }, 10);
}

function closeConfirmModal() {
    const modal = document.getElementById('custom-confirm-modal');
    if (modal) {
        modal.classList.add('opacity-0');
        modal.querySelector('div').classList.remove('scale-100');
        modal.querySelector('div').classList.add('scale-95');
        setTimeout(() => {
            modal.classList.add('hidden');
        }, 300);
    }
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadCoupons();
});