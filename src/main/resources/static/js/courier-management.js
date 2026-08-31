// ==========================================
// COURIER SETTINGS MANAGEMENT
// ==========================================

const API_SHIPPING = '/api/admin/courier/shipping';
const API_COURIER_COMPANIES = '/api/admin/courier/companies';

// Store data in memory to populate edit forms easily
let shippingCache = [];
let courierCache = [];

async function loadCourierSettings() {
    await Promise.all([
        loadShippingMethods(),
        loadCourierCompanies()
    ]);
}

// ==========================================
// 1. SHIPPING METHODS LOGIC
// ==========================================

async function loadShippingMethods() {
    const container = document.getElementById('shipping-methods-list');
    if (!container) return;

    try {
        container.innerHTML = '<p class="text-xs text-gray-400 p-3">Loading methods...</p>';
        const response = await fetch(API_SHIPPING);
        if (!response.ok) throw new Error('Failed to fetch');

        shippingCache = await response.json(); // Store in cache

        if (shippingCache.length === 0) {
            container.innerHTML = '<p class="text-xs text-gray-400 p-3 text-center">No active shipping methods found.</p>';
            return;
        }

        container.innerHTML = shippingCache.map(m => `
            <div class="flex justify-between items-center bg-gray-50 p-3 border border-gray-200 hover:border-gold-200 transition group cursor-pointer" onclick="populateShippingEdit(${m.id})">
                <div>
                    <p class="text-sm font-bold text-gray-800">${m.shippingMethod}</p>
                    <p class="text-xs text-gray-500">${m.description} • <span class="text-gray-700 font-semibold">LKR ${m.value.toFixed(2)}</span></p>
                </div>
                <div class="flex gap-2">
                    <button type="button" onclick="populateShippingEdit(${m.id}); event.stopPropagation();" class="text-gray-300 hover:text-blue-500 transition opacity-0 group-hover:opacity-100" title="Edit">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button type="button" onclick="deleteShippingMethod(${m.id}); event.stopPropagation();" class="text-gray-300 hover:text-red-600 transition opacity-0 group-hover:opacity-100" title="Remove">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            </div>
        `).join('');

    } catch (err) {
        console.error(err);
        container.innerHTML = '<p class="text-xs text-red-400 p-3">Error loading data.</p>';
    }
}

// Populate Form for Edit
window.populateShippingEdit = function(id) {
    const item = shippingCache.find(s => s.id === id);
    if (!item) return;

    document.getElementById('shipping-id').value = item.id;
    document.getElementById('shipping-method').value = item.shippingMethod;
    document.getElementById('shipping-value').value = item.value;
    document.getElementById('shipping-desc').value = item.description;

    // Change UI to Update Mode
    document.getElementById('shipping-btn-text').innerText = "Update Method";
    document.getElementById('shipping-btn').classList.remove('text-gold-400');
    document.getElementById('shipping-btn').classList.add('text-white', 'bg-gold-600', 'border-gold-600');

    // Highlight input
    document.getElementById('shipping-method').focus();
};

window.resetShippingForm = function() {
    document.getElementById('shipping-form').reset();
    document.getElementById('shipping-id').value = ""; // Clear ID
    document.getElementById('shipping-btn-text').innerText = "Add Method";
    document.getElementById('shipping-btn').classList.add('text-gold-400');
    document.getElementById('shipping-btn').classList.remove('text-white', 'bg-gold-600', 'border-gold-600');
};

async function handleShippingMethodSubmit(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);

    const id = formData.get('id'); // Get hidden ID
    const method = formData.get('shippingMethod');
    const value = formData.get('value');
    const desc = formData.get('description');

    // --- Validation ---
    if (!method || method.trim().length < 2) {
        showToast("Enter a valid shipping method name", "error");
        return;
    }
    if (!value || value <= 0) {
        showToast("Cost must be greater than 0", "error");
        return;
    }
    if (!desc || desc.trim().length === 0) {
        showToast("Description is required", "error");
        return;
    }
    // ------------------

    const payload = {
        id: id ? parseInt(id) : null,
        shippingMethod: method,
        value: parseFloat(value),
        description: desc
    };

    try {
        const response = await fetch(API_SHIPPING, {
            method: 'POST', // Used for both Create and Update (Save)
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            resetShippingForm();
            const action = id ? 'Updated' : 'Added';
            showToast(`Shipping Method ${action} Successfully`, 'success');
            await loadShippingMethods();
        } else {
            throw new Error('Failed to save');
        }
    } catch (e) {
        console.error(e);
        showToast('Error saving shipping method', 'error');
    }
}

async function deleteShippingMethod(id) {
    showConfirm("Are you sure you want to remove this shipping method?", async () => {
        try {
            const response = await fetch(`${API_SHIPPING}/${id}/delete`, { method: 'POST' });
            if (response.ok) {
                showToast('Shipping Method Removed', 'success');
                await loadShippingMethods();
                resetShippingForm(); // Clear form if we deleted the item currently being edited
            } else {
                throw new Error('Failed');
            }
        } catch (e) {
            showToast('Error removing item', 'error');
        }
    });
}


// ==========================================
// 2. COURIER COMPANIES LOGIC
// ==========================================

async function loadCourierCompanies() {
    const container = document.getElementById('courier-companies-list');
    if (!container) return;

    try {
        container.innerHTML = '<p class="text-xs text-gray-400 p-3">Loading couriers...</p>';
        const response = await fetch(API_COURIER_COMPANIES);
        if (!response.ok) throw new Error('Failed');

        courierCache = await response.json();

        if (courierCache.length === 0) {
            container.innerHTML = '<p class="text-xs text-gray-400 p-3 text-center">No courier partners added.</p>';
            return;
        }

        container.innerHTML = courierCache.map(c => `
            <div class="flex justify-between items-center bg-gray-50 p-3 border border-gray-200 hover:border-gold-200 transition group cursor-pointer" onclick="populateCourierEdit(${c.id})">
                <div class="flex items-center">
                    <div class="w-8 h-8 bg-white border border-gray-200 flex items-center justify-center text-gray-500 mr-3 rounded-full shadow-sm">
                        <i class="fas fa-truck-fast text-xs"></i>
                    </div>
                    <p class="text-sm font-bold text-gray-800">${c.name}</p>
                </div>
                <div class="flex gap-2">
                     <button type="button" onclick="populateCourierEdit(${c.id}); event.stopPropagation();" class="text-gray-300 hover:text-blue-500 transition opacity-0 group-hover:opacity-100" title="Edit">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button type="button" onclick="deleteCourierCompany(${c.id}); event.stopPropagation();" class="text-gray-300 hover:text-red-600 transition opacity-0 group-hover:opacity-100" title="Remove">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            </div>
        `).join('');

    } catch (err) {
        console.error(err);
        container.innerHTML = '<p class="text-xs text-red-400 p-3">Error loading data.</p>';
    }
}

// Populate Form for Edit
window.populateCourierEdit = function(id) {
    const item = courierCache.find(c => c.id === id);
    if (!item) return;

    document.getElementById('courier-id').value = item.id;
    document.getElementById('courier-name').value = item.name;
    document.getElementById('courier-btn').innerText = "Update";
    document.getElementById('courier-btn').classList.add('bg-gold-600', 'text-white');

    document.getElementById('courier-name').focus();
};

window.resetCourierForm = function() {
    document.getElementById('courier-form').reset();
    document.getElementById('courier-id').value = "";
    document.getElementById('courier-btn').innerText = "Add";
    document.getElementById('courier-btn').classList.remove('bg-gold-600', 'text-white');
};

async function handleCourierCompanySubmit(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);

    const id = formData.get('id');
    const name = formData.get('companyName');

    // --- Validation ---
    if (!name || name.trim().length < 2) {
        showToast("Enter a valid company name", "error");
        return;
    }
    // ------------------

    const payload = {
        id: id ? parseInt(id) : null,
        name: name
    };

    try {
        const response = await fetch(API_COURIER_COMPANIES, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            resetCourierForm();
            const action = id ? 'Updated' : 'Added';
            showToast(`Courier Partner ${action}`, 'success');
            await loadCourierCompanies();
        } else {
            throw new Error('Failed to save');
        }
    } catch (e) {
        console.error(e);
        showToast('Error saving courier', 'error');
    }
}

async function deleteCourierCompany(id) {
    showConfirm("Are you sure you want to remove this courier partner?", async () => {
        try {
            const response = await fetch(`${API_COURIER_COMPANIES}/${id}/delete`, { method: 'POST' });
            if (response.ok) {
                showToast('Courier Partner Removed', 'success');
                await loadCourierCompanies();
                resetCourierForm();
            } else {
                throw new Error('Failed');
            }
        } catch (e) {
            showToast('Error removing item', 'error');
        }
    });
}