// ==========================================
// ORDER & RETURN MANAGEMENT LOGIC
// ==========================================

let cachedOrders = [];
let cachedReturns = [];
let currentTab = 'all';

// ✅ STATUS MAPPINGS (Colors & Labels)
const PAYMENT_STATUS_MAP = {
    'COMPLETED': { class: 'bg-green-100 text-green-800', label: 'Paid' },
    'PAID': { class: 'bg-green-100 text-green-800', label: 'Paid' },
    'SUCCESS': { class: 'bg-green-100 text-green-800', label: 'Paid' }, // Added SUCCESS
    'PENDING': { class: 'bg-yellow-100 text-yellow-800', label: 'Pending' },
    'COD': { class: 'bg-blue-100 text-blue-800', label: 'COD' },
    'FAILED': { class: 'bg-red-100 text-red-700', label: 'Failed' },
    'DEFAULT': { class: 'bg-gray-100 text-gray-600', label: 'Unknown' }
};

// ✅ UPDATED DELIVERY STATUS MAP
const DELIVERY_STATUS_MAP = {
    'ORDER_PLACED': { class: 'bg-gray-200 text-gray-700' },
    'READY': { class: 'bg-blue-100 text-blue-700' },
    'SHIPPED': { class: 'bg-indigo-100 text-indigo-700' },
    'DELIVERED': { class: 'bg-green-100 text-green-700' },
    'CANCELLED': { class: 'bg-red-100 text-red-700' },
    'RETURNED': { class: 'bg-orange-100 text-orange-800' },
    'RETURN_APPROVED': { class: 'bg-purple-100 text-purple-700' },
    'DEFAULT': { class: 'bg-gray-100 text-gray-600' }
};

const RETURN_STATUS_MAP = {
    'RETURN_REQUESTED': { class: 'bg-orange-100 text-orange-700' },
    'APPROVED': { class: 'bg-purple-100 text-purple-700' },
    'REJECTED': { class: 'bg-red-100 text-red-700' },
    'COMPLETED': { class: 'bg-green-100 text-green-700' }
};

// 1. Load Data
async function loadOrderData() {
    try {
        const [ordersRes, returnsRes] = await Promise.all([
            fetch('/api/admin/orders/list'),
            fetch('/api/admin/orders/returns')
        ]);

        if (ordersRes.ok) cachedOrders = await ordersRes.json();
        if (returnsRes.ok) cachedReturns = await returnsRes.json();

        updateTabCounts();
        switchOrderTab(currentTab); // Refresh view

    } catch (err) {
        console.error(err);
        if(window.showToast) window.showToast('Error loading data', 'error');
    }
}

// 2. Update Tab Counts
function updateTabCounts() {
    const newOrdersCount = cachedOrders.filter(o =>
        (o.deliveryStatus || '').toUpperCase().trim().replace(/ /g, '_') === 'ORDER_PLACED'
    ).length;

    const newReturnsCount = cachedReturns.filter(r =>
        (r.status || '').toUpperCase() === 'RETURN_REQUESTED'
    ).length;

    const allCountEl = document.getElementById('all-orders-count');
    const returnCountEl = document.getElementById('returns-count');

    if(allCountEl) allCountEl.innerText = newOrdersCount;
    if(returnCountEl) returnCountEl.innerText = newReturnsCount;
}

// 3. Tab Switcher
function switchOrderTab(tab) {
    currentTab = tab;

    const allTabBtn = document.getElementById('all-orders-tab');
    const retTabBtn = document.getElementById('returns-tab');
    const orderHead = document.getElementById('orders-head');
    const returnHead = document.getElementById('returns-head');

    const statusFilter = document.getElementById('status-filters');
    const payFilterWrapper = document.getElementById('payment-filter-wrapper');

    if (tab === 'all') {
        allTabBtn.classList.add('border-gold-400', 'text-gold-600');
        allTabBtn.classList.remove('border-transparent', 'text-gray-500');
        retTabBtn.classList.add('border-transparent', 'text-gray-500');
        retTabBtn.classList.remove('border-red-400', 'text-red-600');

        orderHead.classList.remove('hidden');
        returnHead.classList.add('hidden');

        if(payFilterWrapper) payFilterWrapper.classList.remove('hidden');

        statusFilter.innerHTML = `
            <option value="">All Delivery Status</option>
            <option value="ORDER_PLACED">Order Placed</option>
            <option value="READY">Ready</option>
            <option value="SHIPPED">Shipped</option>
            <option value="DELIVERED">Delivered</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="RETURNED">Returned</option>
        `;
    } else {
        retTabBtn.classList.add('border-red-400', 'text-red-600');
        retTabBtn.classList.remove('border-transparent', 'text-gray-500');
        allTabBtn.classList.add('border-transparent', 'text-gray-500');
        allTabBtn.classList.remove('border-gold-400', 'text-gold-600');

        orderHead.classList.add('hidden');
        returnHead.classList.remove('hidden');

        if(payFilterWrapper) payFilterWrapper.classList.add('hidden');

        statusFilter.innerHTML = `
            <option value="">All Return Status</option>
            <option value="RETURN_REQUESTED">Return Requested</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
            <option value="COMPLETED">Completed</option>
        `;
    }

    filterData();
}

// 4. Filter & Render Logic
function filterData() {
    const searchInput = document.getElementById('order-search');
    const dateInput = document.getElementById('order-date-filter');
    const payInput = document.getElementById('payment-status-filter');
    const statusInput = document.getElementById('status-filters');

    if (!searchInput || !dateInput || !payInput || !statusInput) {
        console.warn("One or more filter inputs not found");
        return;
    }

    const search = searchInput.value.toLowerCase().trim();
    const date = dateInput.value;
    const payStatus = payInput.value.toUpperCase();
    const delStatus = statusInput.value.toUpperCase();

    const tbody = document.getElementById('orders-body');
    tbody.innerHTML = '';

    if (currentTab === 'all') {
        const filtered = cachedOrders.filter(o => {
            const matchSearch = (o.orderId || '').toLowerCase().includes(search) ||
                (o.customerName || '').toLowerCase().includes(search);
            const matchDate = !date || o.date.startsWith(date);

            const dbPay = (o.paymentStatus || '').toUpperCase();
            const matchPay = !payStatus || dbPay === payStatus;

            // ✅ Normalize DB Status: 'Order Placed' -> 'ORDER_PLACED'
            const dbDel = (o.deliveryStatus || '').toUpperCase().trim().replace(/ /g, '_');
            const matchDel = !delStatus || dbDel === delStatus;

            return matchSearch && matchDate && matchPay && matchDel;
        });

        if (filtered.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="px-6 py-12 text-center text-gray-500">No orders found.</td></tr>';
            return;
        }

        tbody.innerHTML = filtered.map(o => {
            const pKey = (o.paymentStatus || '').toUpperCase();
            let payLabelHtml = '';

            if (['PAID', 'COMPLETED', 'SUCCESS'].includes(pKey)) {
                payLabelHtml = `<span class="bg-green-100 text-green-800 px-2 py-1 rounded text-[10px] font-bold uppercase">Paid</span>`;
            } else {
                const method = (o.paymentMethod || 'Pending').toUpperCase();
                const colorClass = (method === 'COD') ? 'bg-blue-100 text-blue-800' : 'bg-yellow-100 text-yellow-800';
                payLabelHtml = `<span class="${colorClass} px-2 py-1 rounded text-[10px] font-bold uppercase">${method}</span>`;
            }

            const statusDropdownHtml = getDeliveryStatusDropdown(o.deliveryStatus, o.orderId);

            return `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-6 py-4 font-mono font-bold text-xs">${o.orderId}</td>
                <td class="px-6 py-4 text-sm font-bold">
                    ${o.customerName}
                    <div class="text-[10px] font-normal text-gray-500">${o.customerEmail}</div>
                </td>
                <td class="px-6 py-4 text-xs text-gray-500">${new Date(o.date).toLocaleDateString()}</td>
                <td class="px-6 py-4 font-bold">LKR ${o.total.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                <td class="px-6 py-4">${payLabelHtml}</td>
                <td class="px-6 py-4">${statusDropdownHtml}</td>
                <td class="px-6 py-4 text-right">
                    <button onclick="viewOrderDetails('${o.orderId}')" class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase">View</button>
                </td>
            </tr>
        `}).join('');

    } else {
        const filtered = cachedReturns.filter(r => {
            const matchSearch = (r.returnId || '').toLowerCase().includes(search) ||
                (r.orderId || '').toLowerCase().includes(search);
            const matchDate = !date || r.date.startsWith(date);
            const dbStatus = (r.status || '').toUpperCase();
            const matchStatus = !delStatus || dbStatus === delStatus;
            return matchSearch && matchDate && matchStatus;
        });

        if (filtered.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="px-6 py-12 text-center text-gray-500">No returns found.</td></tr>';
            return;
        }

        tbody.innerHTML = filtered.map(r => {
            const rKey = (r.status || '').toUpperCase();
            const sBadge = RETURN_STATUS_MAP[rKey] || RETURN_STATUS_MAP.COMPLETED;

            let actionHtml = '';
            if (rKey === 'RETURN_REQUESTED') {
                actionHtml = `<button class="text-orange-600 font-bold text-xs mr-2" onclick="viewOrderDetails('${r.orderId}')">Review</button>`;
            } else if (rKey === 'APPROVED') {
                actionHtml = `<button class="text-purple-600 font-bold text-xs" onclick="viewOrderDetails('${r.orderId}')">Receive</button>`;
            } else {
                actionHtml = `<span class="text-gray-400 text-[10px]">Closed</span>`;
            }

            return `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100 bg-red-50/30">
                <td class="px-6 py-4 font-mono font-bold text-xs text-red-700">${r.returnId}</td>
                <td class="px-6 py-4 text-xs font-mono">${r.orderId}</td>
                <td class="px-6 py-4 text-sm font-bold">${r.customerName}</td>
                <td class="px-6 py-4 text-xs">${r.type}</td>
                <td class="px-6 py-4 text-xs text-gray-600 truncate max-w-xs" title="${r.reason}">${r.reason}</td>
                <td class="px-6 py-4"><span class="${sBadge.class} px-2 py-1 rounded text-[10px] font-bold uppercase">${r.status}</span></td>
                <td class="px-6 py-4 text-right">${actionHtml}</td>
            </tr>
        `}).join('');
    }
}

// --- HELPER: Generate Status Dropdown ---
function getDeliveryStatusDropdown(currentStatus, orderId) {

    const rawStatus = (currentStatus || '')
        .toUpperCase()
        .trim()
        .replace(/ /g, '_');

    const badge = DELIVERY_STATUS_MAP[rawStatus] || DELIVERY_STATUS_MAP.DEFAULT;

    const isPickup = (orderId || '').includes('BOPIS') || (rawStatus === 'PICKED_UP'); // Heuristic if order object isn't passed fully, but actually we should pass deliveryMethod.
    
    if (rawStatus === 'DELIVERED' || rawStatus === 'PICKED_UP' || rawStatus === 'CANCELLED' || rawStatus === 'COMPLETED') {
        return `<span class="${badge.class} px-2 py-1 rounded text-[10px] font-bold uppercase">
            ${currentStatus}
        </span>`;
    }

    // Build options dynamically
    let options = '';
    
    if (orderId && orderId.startsWith('POS-')) {
        if (rawStatus === 'PENDING_SLIP_VERIFICATION') {
            options += `<option value="PENDING_SLIP_VERIFICATION" selected>Pending Slip Verification</option>`;
            options += `<option value="PROCESSING">Processing</option>`;
            options += `<option value="COMPLETED">Completed</option>`;
            options += `<option value="CANCELLED">Cancelled</option>`;
        } else if (rawStatus === 'PROCESSING') {
            options += `<option value="PROCESSING" selected>Processing</option>`;
            options += `<option value="COMPLETED">Completed</option>`;
            options += `<option value="CANCELLED">Cancelled</option>`;
        } else {
            return `<span class="${badge.class} px-2 py-1 rounded text-[10px] font-bold uppercase">${currentStatus}</span>`;
        }
    } else if (rawStatus === 'PENDING_PICKUP') {
        options += `<option value="PENDING_PICKUP" selected>Pending Pickup</option>`;
        options += `<option value="READY">Ready for Pickup</option>`;
        options += `<option value="COMPLETED">Completed / Picked Up</option>`;
        options += `<option value="CANCELLED">Cancelled</option>`;
    } else if (rawStatus === 'READY' && isPickup) {
        options += `<option value="READY" selected>Ready for Pickup</option>`;
        options += `<option value="COMPLETED">Completed / Picked Up</option>`;
        options += `<option value="CANCELLED">Cancelled</option>`;
    } else {
        // Normal Delivery Flow
        if (rawStatus === 'ORDER_PLACED') {
            options += `<option value="ORDER_PLACED" selected>Order Placed</option>`;
            options += `<option value="READY">Ready</option>`;
            options += `<option value="CANCELLED">Cancelled</option>`;
        } else if (rawStatus === 'READY') {
            options += `<option value="READY" selected>Ready</option>`;
            options += `<option value="SHIPPED">Shipped</option>`;
            options += `<option value="CANCELLED">Cancelled</option>`;
        } else if (rawStatus === 'SHIPPED') {
            options += `<option value="SHIPPED" selected>Shipped</option>`;
            options += `<option value="DELIVERED">Delivered</option>`;
            options += `<option value="CANCELLED">Cancelled</option>`;
        }
    }

    if (!options) {
        return `<span class="${badge.class} px-2 py-1 rounded text-[10px] font-bold uppercase">${currentStatus || 'UNKNOWN'}</span>`;
    }

    return `
        <select onchange="updateOrderStatus('${orderId}', this.value)" 
            class="bg-white border border-gray-300 text-gray-700 text-xs rounded p-1 font-bold uppercase cursor-pointer">
            ${options}
        </select>
    `;
}

// --- ACTION: Update Order Status ---
async function updateOrderStatus(orderId, newStatus) {

    const statusToSend = newStatus.toUpperCase().trim();

    try {
        const res = await fetch(`/api/admin/orders/${encodeURIComponent(orderId)}/status`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ status: statusToSend })
        });

        if (res.ok) {
            if (window.showToast) window.showToast('Status Updated', 'success');
            loadOrderData();
        } else {
            const txt = await res.text();
            if (window.showToast) window.showToast(txt, 'error');
            loadOrderData();
        }
    } catch (e) {
        console.error(e);
        if (window.showToast) window.showToast('Server error', 'error');
    }
}

// 5. View Details & Other Actions (Fixed Payment Status Display)
async function viewOrderDetails(id) {
    try {
        const response = await fetch(`/api/admin/orders/${encodeURIComponent(id)}`);
        if (!response.ok) throw new Error("Failed to load details");
        const order = await response.json();

        let actionButtons = '';
        const retStatus = (order.returnStatus || '').toUpperCase();

        // --- පවතින Return logic එක ---
        if (order.return) {
            if (retStatus === 'RETURN_REQUESTED' || retStatus === 'PENDING') {
                actionButtons += `
                    <button onclick="handleReturnAction('${order.orderId}', 'REJECT')" class="bg-red-100 text-red-700 border border-red-200 py-2 px-4 font-bold uppercase text-xs hover:bg-red-200 rounded mr-2">Reject</button>
                    <button onclick="handleReturnAction('${order.orderId}', 'APPROVE')" class="bg-green-600 text-white py-2 px-4 font-bold uppercase text-xs hover:bg-green-700 rounded mr-2">Approve Return</button>
                `;
            } else if (retStatus === 'APPROVED') {
                actionButtons += `
                    <button onclick="handleReturnAction('${order.orderId}', 'COMPLETE')" class="bg-purple-600 text-white py-2 px-4 font-bold uppercase text-xs hover:bg-purple-700 rounded mr-2">Mark Item Received</button>
                `;
            }
        }

        // --- ✅ COD and Bank Payment logic ---
        const pKey = (order.paymentStatus || '').toUpperCase();
        const isPaid = ['PAID', 'COMPLETED', 'SUCCESS'].includes(pKey);
        const isDelivered = (order.deliveryStatus || '').toUpperCase() === 'DELIVERED';
        const method = (order.paymentMethod || '').toUpperCase();
        const isCOD = method.includes('COD') || method.includes('CASH ON DELIVERY');
        const isBank = method.includes('BANK');

        if (isBank && !isPaid && order.slipUrl && order.deliveryStatus.toUpperCase() === 'PENDING SLIP VERIFICATION') {
            actionButtons += `
                <a href="${order.slipUrl}" target="_blank" class="bg-indigo-100 text-indigo-700 border border-indigo-200 py-2 px-4 font-bold uppercase text-xs hover:bg-indigo-200 rounded mr-2 inline-flex items-center"><i class="fas fa-eye mr-2"></i>View Slip</a>
                <button onclick="verifyBankSlip('${order.orderId}')" class="bg-green-600 text-white py-2 px-4 font-bold uppercase text-xs hover:bg-green-700 rounded mr-2 inline-flex items-center"><i class="fas fa-check mr-2"></i>Verify</button>
                <button onclick="updateOrderStatus('${order.orderId}', 'Cancelled')" class="bg-red-600 text-white py-2 px-4 font-bold uppercase text-xs hover:bg-red-700 rounded mr-2 inline-flex items-center"><i class="fas fa-times mr-2"></i>Reject</button>
            `;
        } else if ((isCOD && isDelivered && !isPaid) || (isBank && !isPaid && order.slipUrl)) {
            actionButtons += `
                <button onclick="markOrderAsPaid('${order.orderId}')" 
                    class="bg-blue-600 text-white py-2 px-4 font-bold uppercase text-xs hover:bg-blue-700 rounded mr-2">
                    Mark as Paid
                </button>
            `;
        }

        // UI එක සඳහා Badge colors සකස් කිරීම
        const payBadge = PAYMENT_STATUS_MAP[pKey] || { class: 'bg-gray-100 text-gray-600', label: order.paymentStatus || 'Pending' };
        const dKey = (order.deliveryStatus || '').toUpperCase().replace(/ /g, '_');
        const delBadge = DELIVERY_STATUS_MAP[dKey] || { class: 'bg-gray-100 text-gray-600' };

        const isStorePickup = (order.deliveryStatus || '').toUpperCase().includes('PICKUP') || (order.orderId || '').includes('BOPIS');
        const isPosOrder = (order.orderId || '').startsWith('POS-');

        let cleanAddress = (order.address || '').replace(/Select a country first,?/ig, '').replace(/Select a province,?/ig, '').replace(/,\\s*,/g, ',').replace(/^,\\s*/, '').trim();
        if (!cleanAddress || cleanAddress === 'Sri Lanka' || cleanAddress === ', Sri Lanka') cleanAddress = 'Address not provided';

        let shippingSectionHtml = '';
        if (isPosOrder) {
            shippingSectionHtml = `
                <div class="p-4 bg-gray-50 rounded border border-gray-200">
                    <h4 class="text-xs font-bold text-gray-400 uppercase mb-3">Order Type</h4>
                    <div class="space-y-2 text-sm">
                        <div class="flex"><span class="w-24 text-gray-500">Method:</span> <span class="font-medium text-gray-900">In-Store Purchase (POS)</span></div>
                        <div class="flex"><span class="w-24 text-gray-500">Payment:</span> <span>${order.paymentMethod}</span></div>
                    </div>
                </div>
            `;
        } else if (isStorePickup) {
            shippingSectionHtml = `
                <div class="p-4 bg-gray-50 rounded border border-gray-200">
                    <h4 class="text-xs font-bold text-gray-400 uppercase mb-3">Pickup Details</h4>
                    <div class="space-y-2 text-sm">
                        <div class="flex"><span class="w-24 text-gray-500">Location:</span> <span class="font-medium text-gray-900">Rush Jewels (Kandy Store)</span></div>
                        <div class="flex"><span class="w-24 text-gray-500">Method:</span> <span>Store Pickup</span></div>
                        <div class="flex"><span class="w-24 text-gray-500">Payment:</span> <span>${order.paymentMethod}</span></div>
                    </div>
                </div>
            `;
        } else {
            shippingSectionHtml = `
                <div class="p-4 bg-gray-50 rounded border border-gray-200">
                    <h4 class="text-xs font-bold text-gray-400 uppercase mb-3">Shipping & Payment</h4>
                    <div class="space-y-2 text-sm">
                        <div class="flex"><span class="w-24 text-gray-500">Address:</span> <span class="font-medium">${cleanAddress}</span></div>
                        <div class="flex"><span class="w-24 text-gray-500">Method:</span> <span>${order.paymentMethod}</span></div>
                        <div class="flex"><span class="w-24 text-gray-500">Tracking:</span> <span class="font-mono text-blue-600">${order.trackingNumber || 'N/A'}</span></div>
                    </div>
                </div>
            `;
        }

        // --- සම්පූර්ණ UI Content එක ---
        const content = `
            <div class="space-y-6">
                <div class="flex justify-between items-start border-b border-gray-100 pb-4">
                    <div>
                        <h3 class="text-2xl font-bold text-gray-900">${order.orderId}</h3>
                        <p class="text-xs text-gray-500 mt-1">Ordered: ${new Date(order.date).toLocaleString()}</p>
                    </div>
                     <div class="text-right">
                         <div class="text-xl font-bold text-gold-600">LKR ${order.total.toLocaleString(undefined, {minimumFractionDigits: 2})}</div>
                         <div class="flex gap-2 justify-end mt-2">
                            ${order.gift ? `<span class="bg-pink-100 text-pink-700 px-2 py-1 rounded text-[10px] uppercase font-bold"><i class="fas fa-gift mr-1"></i>Gift</span>` : ''}
                            <span class="${payBadge.class} px-2 py-1 rounded text-[10px] uppercase font-bold">${payBadge.label}</span>
                            <span class="${delBadge.class} px-2 py-1 rounded text-[10px] uppercase font-bold">${order.deliveryStatus}</span>
                            ${order.slipUrl ? `<a href="${order.slipUrl}" target="_blank" class="bg-blue-100 text-blue-700 px-2 py-1 rounded text-[10px] uppercase font-bold hover:bg-blue-200"><i class="fas fa-file-invoice mr-1"></i>View Slip</a>` : ''}
                         </div>
                    </div>
                </div>

                ${order.notes ? `
                <div class="bg-yellow-50 border-l-4 border-yellow-400 p-3 rounded">
                    <p class="text-xs font-bold text-yellow-800 uppercase mb-1">Order Note</p>
                    <p class="text-sm text-gray-700 italic">"${order.notes}"</p>
                </div>` : ''}

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="p-4 bg-gray-50 rounded border border-gray-200">
                        <h4 class="text-xs font-bold text-gray-400 uppercase mb-3">Customer Details</h4>
                        <div class="space-y-2 text-sm">
                            <div class="flex"><span class="w-20 text-gray-500">Name:</span> <span class="font-medium">${order.customerName}</span></div>
                            <div class="flex"><span class="w-20 text-gray-500">Email:</span> <span>${order.email}</span></div>
                            <div class="flex"><span class="w-20 text-gray-500">Phone:</span> <span class="font-mono">${order.phone}</span></div>
                        </div>
                    </div>
                    ${shippingSectionHtml}
                </div>

                ${order.return ? `
                <div class="bg-orange-50 border-l-4 border-orange-400 p-4 rounded shadow-sm">
                    <h4 class="text-sm font-bold text-orange-800 uppercase mb-2">Return Request</h4>
                    <div class="grid grid-cols-2 gap-4 text-xs text-gray-700">
                        <p><strong>Status:</strong> <span class="uppercase font-bold">${order.returnStatus}</span></p>
                        <p><strong>Type:</strong> ${order.returnType}</p>
                        <p class="col-span-2 mt-1"><strong>Reason:</strong> ${order.returnReason}</p>
                    </div>
                </div>` : ''}
                
                <div class="border border-gray-200 rounded overflow-hidden">
                    <table class="w-full text-sm">
                        <thead class="bg-gray-100 text-xs uppercase text-gray-500 font-bold">
                            <tr>
                                <th class="px-4 py-2 text-left">Product</th>
                                <th class="px-4 py-2 text-center">SKU</th>
                                <th class="px-4 py-2 text-center">Qty</th>
                                <th class="px-4 py-2 text-right">Price</th>
                                <th class="px-4 py-2 text-right">Total</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-gray-100 bg-white">
                            ${order.items.map(i => `
    <tr class="${i.returned ? 'bg-red-50' : ''}"> 
        <td class="px-4 py-3 font-medium text-gray-800">
            ${i.name}
            ${i.subtext ? `<div class="text-[10px] text-gray-500 font-normal mt-1">${i.subtext}</div>` : ''}
            ${i.returned ? `
                <div class="mt-1">
                    <span class="bg-red-600 text-white text-[9px] px-2 py-0.5 rounded font-bold uppercase tracking-tighter">
                        <i class="fas fa-undo mr-1"></i> Returned / Warranty Claimed
                    </span>
                </div>
            ` : ''}
        </td>
        <td class="px-4 py-3 text-center font-mono text-xs text-gray-500">${i.sku}</td>
        <td class="px-4 py-3 text-center">${i.qty}</td>
        <td class="px-4 py-3 text-right">LKR ${i.price.toLocaleString()}</td>
        <td class="px-4 py-3 text-right font-bold">LKR ${(i.price * i.qty).toLocaleString()}</td>
    </tr>
`).join('')}
                        </tbody>
                        <tfoot class="bg-gray-50">
                            <tr>
                                <td colspan="4" class="px-4 py-2 text-right text-xs text-gray-500 border-t border-gray-200">Subtotal</td>
                                <td class="px-4 py-2 text-right font-bold text-gray-700 border-t border-gray-200">LKR ${(order.subTotal || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                            </tr>
                            <tr>
                                <td colspan="4" class="px-4 py-2 text-right text-xs text-gray-500">Shipping</td>
                                <td class="px-4 py-2 text-right font-bold text-gray-700">LKR ${(order.shipping || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                            </tr>

                            ${order.discount > 0 ? `
                            <tr>
                                <td colspan="4" class="px-4 py-2 text-right text-xs text-green-600">Discount</td>
                                <td class="px-4 py-2 text-right font-bold text-green-600">- LKR ${(order.discount || 0).toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                            </tr>
                            ` : ''}
                            <tr>
                                <td colspan="4" class="px-4 py-3 text-right font-bold uppercase text-xs text-gray-500 border-t border-gray-300">Final Total</td>
                                <td class="px-4 py-3 text-right font-bold text-lg text-gold-600 border-t border-gray-300">LKR ${order.total.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                            </tr>
                        </tfoot>
                    </table>
                </div>

                <div class="flex justify-end gap-3 pt-2">
                    ${actionButtons}
                    <button onclick="closeOrderDetails()" class="bg-gray-200 text-gray-700 py-2 px-6 font-bold uppercase text-xs hover:bg-gray-300 rounded">Close</button>
                </div>
            </div>
        `;

        document.getElementById('order-details-content').innerHTML = content;
        document.getElementById('order-details-modal').classList.remove('hidden');
        document.getElementById('order-details-modal').classList.add('flex');

    } catch (e) {
        console.error("Error viewing order details:", e);
    }
}
async function handleReturnAction(orderId, action) {
    const doAction = async () => {
        try {
            const res = await fetch(`/api/admin/orders/${orderId}/return/action`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ action })
            });
            if(res.ok) {
                if(window.showToast) window.showToast('Return Processed Successfully', 'success');
                closeOrderDetails();
                loadOrderData();
            } else {
                if(window.showToast) window.showToast('Failed to process return', 'error');
            }
        } catch(e) {
            console.error(e);
            if(window.showToast) window.showToast('Network error', 'error');
        }
    };

    if(window.showConfirm) {
        window.showConfirm(`Confirm ${action} for ${orderId}?`, doAction);
    } else if(confirm(`Confirm ${action} for ${orderId}?`)) {
        doAction();
    }
}

function closeOrderDetails() {
    document.getElementById('order-details-modal').classList.add('hidden');
    document.getElementById('order-details-modal').classList.remove('flex');
}

function closeOrderDetailsOnOutside(event) {
    if (event.target.id === 'order-details-modal') closeOrderDetails();
}

async function markOrderAsPaid(orderId) {
    try {
        const res = await fetch(`/api/admin/orders/${encodeURIComponent(orderId)}/payment-complete`, {
            method: 'POST'
        });

        if (res.ok) {
            if (window.showToast) window.showToast('Payment Marked as COMPLETED', 'success');
            closeOrderDetails();
            loadOrderData();
        } else {
            const txt = await res.text();
            if (window.showToast) window.showToast(txt || 'Failed to update payment', 'error');
        }
    } catch (e) {
        console.error(e);
        if (window.showToast) window.showToast('Server error', 'error');
    }
}
function exportOrdersCSV() {
    if (!cachedOrders.length) return;
    const headers = ['Order ID', 'Customer', 'Email', 'Date', 'Total', 'Payment', 'Delivery'];
    const rows = cachedOrders.map(o => [
        o.orderId, o.customerName, o.customerEmail, o.date, o.total, o.paymentStatus, o.deliveryStatus
    ]);
    const csvContent = [headers, ...rows].map(e => e.join(",")).join("\n");
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `orders.csv`; a.click();
}

document.addEventListener('DOMContentLoaded', loadOrderData);