// ==========================================
// LOGISTICS MANAGEMENT LOGIC
// ==========================================

let cachedShipments = [];
let editingShipmentId = null;

// 1. Load Shipments
async function loadShipments() {
    try {
        const response = await fetch('/api/admin/shipments');
        if (!response.ok) throw new Error('Failed to fetch shipments');

        const shipments = await response.json();
        cachedShipments = shipments;
        renderShipmentsTable(shipments);

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading shipments', 'error');
    }
}

// 2. Render Table
function renderShipmentsTable(data) {
    const tbody = document.getElementById('shipments-body');

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="px-6 py-4 text-center text-gray-500">No shipments found.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(s => {
        let badgeClass = 'bg-gray-100 text-gray-800';
        if (s.status === 'DELIVERED') badgeClass = 'bg-green-100 text-green-800';
        else if (s.status === 'IN_TRANSIT' || s.status === 'OUT_FOR_DELIVERY') badgeClass = 'bg-blue-100 text-blue-800';
        else if (s.status === 'RETURNED') badgeClass = 'bg-red-100 text-red-800';

        const dateStr = s.shippedDate ? new Date(s.shippedDate).toLocaleDateString() : '-';

        return `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-6 py-4 font-mono font-bold text-gray-800">${s.trackingNumber}</td>
                <td class="px-6 py-4 text-xs text-gray-600">${s.orderId}</td>
                <td class="px-6 py-4 text-xs">${s.courierName}</td>
                <td class="px-6 py-4 text-xs">${s.destinationCity || 'Unknown'}</td>
                <td class="px-6 py-4 text-xs text-gray-500">${dateStr}</td>
                <td class="px-6 py-4">
                    <span class="px-2 py-1 text-[10px] uppercase font-bold rounded ${badgeClass}">${s.status}</span>
                </td>
                <td class="px-6 py-4 text-right">
                    <button onclick="openEditShipment(${s.id})" class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase mr-3">Edit</button>
                    <button onclick="viewShipmentDetails(${s.id})" class="text-blue-600 hover:text-blue-800 text-xs font-bold uppercase">Details</button>
                </td>
            </tr>
        `;
    }).join('');
}

// 3. Open Modal: Create
function openAddShipmentModal() {
    editingShipmentId = null;

    document.getElementById('shipment-tracking').value = '';
    document.getElementById('shipment-order').value = '';
    document.getElementById('shipment-order').disabled = false; // Enable for new
    document.getElementById('shipment-date').value = new Date().toISOString().split('T')[0]; // Default Today

    // Default Status for New is SHIPPED (Hidden logic or disabled select)
    const statusSel = document.getElementById('shipment-status');
    statusSel.value = 'SHIPPED';
    statusSel.disabled = true; // Cannot change initial status

    document.getElementById('shipment-form-title').innerText = 'Create New Shipment';
    document.getElementById('shipment-submit-text').innerText = 'Create Shipment';

    showSection('add-shipment');
}

// 4. Open Modal: Edit
async function openEditShipment(id) {
    // Find from cache (simple fields)
    const s = cachedShipments.find(x => x.id === id);
    if (!s) return;

    editingShipmentId = id;

    document.getElementById('shipment-tracking').value = s.trackingNumber;
    document.getElementById('shipment-order').value = s.orderId;
    document.getElementById('shipment-order').disabled = true; // Cannot change order ID on edit

    if (s.shippedDate) {
        document.getElementById('shipment-date').value = new Date(s.shippedDate).toISOString().split('T')[0];
    }

    const statusSel = document.getElementById('shipment-status');
    statusSel.value = s.status;
    statusSel.disabled = false; // Allow status updates

    document.getElementById('shipment-form-title').innerText = 'Edit Shipment';
    document.getElementById('shipment-submit-text').innerText = 'Update Shipment';

    showSection('add-shipment');
}

// 5. Submit Handler
async function handleShipmentSubmit(e) {
    e.preventDefault();

    const trackingNumber = document.getElementById('shipment-tracking').value.trim().toUpperCase();
    const orderId = document.getElementById('shipment-order').value;
    const shippedDate = document.getElementById('shipment-date').value;
    const status = document.getElementById('shipment-status').value;

    // -----------------------------
    // 🔥 CUSTOM VALIDATION
    // -----------------------------
    if (!trackingNumber) {
        if(window.showToast) showToast('Tracking number is required', 'error');
        return;
    }

    if (!orderId) {
        if(window.showToast) showToast('Please enter an order', 'error');
        return;
    }

    if (!shippedDate) {
        if(window.showToast) showToast('Shipped date is required', 'error');
        return;
    }

    if (!status) {
        if(window.showToast) showToast('Please select a shipment status', 'error');
        return;
    }

    const payload = {
        trackingNumber,
        orderId,
        shippedDate,
        status
    };

    try {
        const url = editingShipmentId ? `/api/admin/shipments/${editingShipmentId}/update` : '/api/admin/shipments';
        const method = 'POST';

        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const txt = await response.text();
            throw new Error(txt || 'Failed to save');
        }

        if(window.showToast) showToast(editingShipmentId ? 'Shipment Updated' : 'Shipment Created', 'success');
        showSection('logistics');
        loadShipments();
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast(err.message, 'error');
    }
}

// 6. View Details Modal
async function viewShipmentDetails(id) {
    try {
        const response = await fetch(`/api/admin/shipments/${id}`);
        if (!response.ok) throw new Error("Failed to fetch details");
        const d = await response.json();

        // Populate Modal
        document.getElementById('view-shipment-tracking').innerText = d.trackingNumber;
        document.getElementById('view-shipment-status').innerText = d.status;
        document.getElementById('view-shipment-order').innerText = d.orderId;

        document.getElementById('view-shipment-cust-name').innerText = d.customerName;
        document.getElementById('view-shipment-cust-contact').innerText = d.contactNo || 'N/A';

        document.getElementById('view-shipment-address').innerHTML =
            `${d.addressLine1}, ${d.addressLine2 ? d.addressLine2 + ',' : ''} <br> ${d.city}`;

        document.getElementById('view-shipment-dates').innerHTML =
            `Shipped: ${new Date(d.shippedDate).toLocaleDateString()}<br>Est. Delivery: ${new Date(d.estimatedDate).toLocaleDateString()}`;

        // Show Modal
        document.getElementById('shipment-details-modal').classList.remove('hidden');
        document.getElementById('shipment-details-modal').classList.add('flex');

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading details', 'error');
    }
}

function closeShipmentDetailsModal() {
    const modal = document.getElementById('shipment-details-modal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
}

function closeShipmentModalOnBackdrop(e) {
    if (e.target.id === 'shipment-details-modal') closeShipmentDetailsModal();
}

// 7. Filter Logic
function filterShipments() {
    const search = document.getElementById('shipment-search').value.toLowerCase();
    const status = document.getElementById('status-filter').value;
    const dateInput = document.getElementById('date-filter').value;

    const filtered = cachedShipments.filter(s => {
        const matchesSearch = s.trackingNumber.toLowerCase().includes(search) ||
            s.orderId.toLowerCase().includes(search);

        const matchesStatus = !status || s.status === status;

        let matchesDate = true;
        if (dateInput && s.shippedDate) {
            matchesDate = s.shippedDate.startsWith(dateInput);
        }

        return matchesSearch && matchesStatus && matchesDate;
    });

    renderShipmentsTable(filtered);
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadShipments();
});