import Notification from './notification.js';

// Initialize Notification System
const notify = Notification({
    position: 'bottom-right',
    duration: 3000,
    hidePrevious: true,
    maxVisible: 5,
});

const API_BASE_URL = '/api/orders';

/**
 * 1. ඇණවුම් ලැයිස්තුව පූරණය කිරීම (Master Loader සමඟ)
 */
window.loadOrderHistory = async function loadOrderHistory() {
    const ordersContainer = document.getElementById('orders-container');
    if (!ordersContainer) return;


    try {
        const response = await fetch(API_BASE_URL, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            credentials: 'include'
        });

        if (response.status === 401 || response.status === 403) {
            sessionStorage.setItem('returnUrl', window.location.href);
            window.location.href = 'auth.html';
            return;
        }

        const result = await response.json();
        const orders = result.status ? result.orders : (Array.isArray(result) ? result : (result.orders || []));

        displayOrders(orders);

    } catch (error) {
        console.error("Order Load Error:", error);
        ordersContainer.innerHTML = `<p class="text-center py-10 text-red-500">Unable to load orders.</p>`;
    } finally {
        revealContent();
    }
};

function revealContent() {
    const main = document.getElementById('main-content');
    if (main) {
        main.style.display = 'block';
        main.classList.add('animate__animated', 'animate__fadeIn');
    }
    if (window.loader) {
        setTimeout(() => { window.loader.hide(); }, 400);
    }
}

/**
 * ඇණවුම් ලැයිස්තුව පෙන්වීම
 */
function displayOrders(orders) {
    const ordersContainer = document.getElementById('orders-container');
    if (!ordersContainer) return;

    if (!orders || orders.length === 0) {
        ordersContainer.innerHTML = `
            <div class="text-center py-24 bg-gray-50 border border-gray-100 p-4">
                <div class="mb-6 opacity-50"><i class="fas fa-shopping-bag text-5xl text-gray-300"></i></div>
                <h3 class="text-xl font-playfair text-dark mb-2">Your collection is empty</h3>
                <p class="text-gray-500 text-xs uppercase tracking-wide mb-8">You haven't placed any orders yet.</p>
                <a href="shop.html" class="inline-block px-10 py-4 bg-dark text-white text-xs font-bold uppercase tracking-widest hover:bg-gold transition-colors shadow-sm">Discover Collection</a>
            </div>`;
        return;
    }

    let html = '<div class="space-y-4 md:space-y-6">';
    orders.forEach(o => {
        const statusColor = getStatusColor(o.orderStatus);
        const returnStatusColor = o.hasReturn ? getStatusColor(o.returnStatus) : null;

        let returnBadge = '';
        if (o.hasReturn) {
            const isWarranty = o.returnType && o.returnType.toUpperCase() === 'WARRANTY';
            const badgeLabel = isWarranty ? 'WARRANTY CLAIM' : 'RETURNED';
            const badgeIcon = isWarranty ? 'fa-shield-alt' : 'fa-history';
            returnBadge = `<span class="px-3 py-1 text-[10px] font-bold uppercase tracking-widest ${returnStatusColor.bg} ${returnStatusColor.text} flex items-center gap-1 border border-current opacity-90"><i class="fas ${badgeIcon} text-[8px]"></i> ${badgeLabel}: ${o.returnStatus.replace(/_/g, ' ')}</span>`;
        }

        html += `
            <div class="group bg-white border border-gray-100 hover:border-gold hover:shadow-[0_4px_20px_rgba(0,0,0,0.03)] transition-all duration-300">
                <div class="p-4 md:p-6 border-b border-gray-100 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 bg-white relative">
                    <div class="w-full md:w-auto">
                        <div class="flex justify-between md:justify-start items-center gap-3 mb-2">
                            <span class="text-lg font-playfair font-bold text-dark">#${o.orderId}</span>
                            <div class="flex flex-wrap gap-2">
                                <span class="px-3 py-1 text-[10px] font-bold uppercase tracking-widest ${statusColor.bg} ${statusColor.text}">${o.orderStatus}</span>
                                ${returnBadge}
                            </div>
                        </div>
                        <p class="text-[11px] text-gray-400 font-medium uppercase tracking-wide">Placed on ${o.orderDate}</p>
                    </div>
                    <div class="text-right">
                         <p class="text-lg font-playfair font-bold text-gold">LKR ${(o.totalAmount||0).toLocaleString()}</p>
                    </div>
                </div>
                <div class="p-4 md:p-6 flex flex-col md:flex-row justify-between items-center gap-4 bg-white">
                    <div class="flex items-center gap-3 text-xs text-gray-500 w-full md:w-auto"><i class="fas fa-box text-gold"></i><span class="font-medium uppercase tracking-wide">${o.totalItems} Items</span></div>
                    <div class="flex flex-col md:flex-row gap-3 w-full md:w-auto">
                        ${(o.canCancel && o.orderStatus !== 'Ready' && !o.hasReturn) ? `<button onclick="window.cancelOrder('${o.orderId}')" class="w-full md:w-auto flex-1 md:flex-none px-6 py-3 bg-white text-red-500 text-[10px] font-bold uppercase tracking-widest hover:bg-red-50 transition border border-gray-200">Cancel</button>` : ''}
                        <button onclick="window.viewOrderDetails('${o.orderId}')" class="w-full md:w-auto flex-1 md:flex-none px-8 py-3 bg-dark text-white text-[10px] font-bold uppercase tracking-widest hover:bg-gold transition-colors flex items-center justify-center gap-2 shadow-sm"><span>View Details</span> <i class="fas fa-arrow-right"></i></button>
                    </div>
                </div>
            </div>`;
    });
    ordersContainer.innerHTML = html + '</div>';
}

/**
 * 2. ORDER DETAILS MODAL (INTEGRATED WITH GLOBAL LOADER)
 */
window.viewOrderDetails = async function(orderId) {
    try {
        document.body.style.overflow = 'hidden';
        const res = await fetch(`${API_BASE_URL}/${orderId}`, { credentials: 'include' });
        if (!res.ok) throw new Error('Failed');
        const result = await res.json();

        if (result.status && result.order) {
            renderDetailsModal(result.order);
        }
    } catch (e) {
        notify.error('Error loading details.');
        document.body.style.overflow = '';
    }
};

function renderDetailsModal(order) {
    const modalId = 'order-details-modal';
    document.getElementById(modalId)?.remove();

    const isDelivered = (order.orderStatus === 'Delivered');
    const hasReturn = order.hasReturn === true;
    const isPickup = (order.shippingMethod || '').toUpperCase().includes('PICKUP');
    const returnStatusColor = hasReturn ? getStatusColor(order.returnStatus) : null;
    const statusStepper = generateStepper(order.orderStatus, order.hasReturn ? order.returnStatus : null, order.shippingMethod);
    const hideTracking = ['Order Placed', 'Ready', 'Cancelled'].includes(order.orderStatus) || hasReturn || isPickup;

    const itemsHtml = order.items.map(item => {
        const isCollection = item.productName.startsWith('Collection:');
        const safeName = item.productName.replace(/'/g, "\\'");
        return `
            <div class="flex gap-4 py-4 border-b border-gray-100 last:border-0 items-start md:items-center group">
                <div class="w-16 h-16 md:w-20 md:h-20 flex-shrink-0 bg-gray-50 border border-gray-100 overflow-hidden">
                    <img src="${item.image}" alt="Product" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700" onerror="this.src='assets/images/placeholder.png'" loading="lazy">
                </div>
                <div class="flex-1 min-w-0">
                    <h4 class="font-playfair font-bold text-sm md:text-base text-dark leading-tight md:leading-normal mb-1 md:mb-0 line-clamp-2">${item.productName}</h4>
                    <p class="text-[10px] text-gray-400 font-bold uppercase tracking-wide">Qty: ${item.quantity} <span class="mx-1 md:mx-2 text-gray-300">|</span> LKR ${item.price.toLocaleString()}</p>
                </div>
                <div class="text-right flex flex-col items-end gap-2 pl-2">
                    <p class="font-bold text-xs md:text-sm text-dark font-playfair whitespace-nowrap">LKR ${(item.price * item.quantity).toLocaleString()}</p>
                    ${(isDelivered && !hasReturn) ? `<button onclick="window.openReviewModal('${item.variantId}', ${!isCollection}, '${safeName}')" class="text-[9px] bg-gold text-white px-3 py-1.5 hover:bg-dark transition-colors uppercase font-bold tracking-widest">Review</button>` : ''}
                </div>
            </div>`;
    }).join('');

    const html = `
    <div id="${modalId}" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 animate-fadeIn">
        <div class="absolute inset-0 bg-white/90 backdrop-blur-sm transition-opacity" onclick="window.closeModal('${modalId}')"></div>
        <div class="relative bg-white w-full max-w-4xl shadow-[0_0_50px_rgba(0,0,0,0.05)] border border-gray-100 flex flex-col h-[100dvh] sm:h-[85vh] animate-slideUp">
            <div class="px-5 py-4 sm:px-8 sm:py-6 border-b border-gray-100 flex justify-between items-center bg-white z-20 shrink-0">
                <div><h2 class="text-xl sm:text-2xl font-playfair font-bold text-dark">Order Details</h2><p class="text-[10px] text-gray-400 mt-1 font-bold uppercase tracking-widest">ID: #${order.orderId} <span class="mx-2 text-gray-300">|</span> ${order.orderDate}</p></div>
                <button onclick="window.closeModal('${modalId}')" class="w-10 h-10 flex items-center justify-center hover:bg-black hover:text-white transition-colors text-dark border border-gray-100"><i class="fas fa-times"></i></button>
            </div>
            <div class="p-5 sm:p-8 overflow-y-auto flex-1 custom-scrollbar bg-white overscroll-contain">
                <div class="mb-8">${!hideTracking ? `<div class="mb-6 text-center"><a href="track-order.html?orderId=${order.orderId}" class="inline-flex items-center justify-center gap-3 px-8 py-3 bg-dark text-white font-bold uppercase text-[10px] tracking-[0.2em] hover:bg-gold transition-colors w-full sm:w-auto shadow-sm">Track Shipment <i class="fas fa-arrow-right"></i></a></div>` : ''}<div class="py-2 overflow-x-auto">${statusStepper}</div></div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-8 mb-8">
                    <div class="bg-gray-50 p-5 border border-gray-100"><h3 class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-3 border-b border-gray-200 pb-2">${isPickup ? 'Pickup Details' : 'Delivery Address'}</h3><p class="text-xs sm:text-sm text-gray-600 leading-relaxed font-medium font-montserrat">${isPickup ? 'Store Pickup (In-Store)' : (order.deliveryAddress || 'N/A')}</p></div>
                    <div class="bg-gray-50 p-5 border border-gray-100"><h3 class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-3 border-b border-gray-200 pb-2">Payment Summary</h3><div class="space-y-2"><div class="flex justify-between items-center text-xs"><span class="text-gray-500 uppercase">Status</span><span class="px-2 py-0.5 text-[9px] font-bold uppercase ${order.paymentStatus === 'COMPLETED' ? 'bg-green-50 text-green-700' : 'bg-yellow-50 text-yellow-700'}">${order.paymentStatus || 'N/A'}</span></div><div class="flex justify-between items-center text-xs pt-1"><span class="text-gray-500 uppercase">Method</span><span class="text-dark font-medium">${isPickup && (order.paymentMethod || '').includes('Cash on Delivery') ? 'Pay at Store' : (order.paymentMethod || 'N/A')}</span></div></div></div>
                </div>
                <div class="border border-gray-100 mb-8">
                    <div class="bg-gray-50 px-5 py-3 border-b border-gray-100 flex justify-between items-center"><h3 class="text-[10px] font-bold text-dark uppercase tracking-widest">Items Ordered</h3>${isDelivered && !hasReturn ? `<button onclick="window.openReturnModal('${order.orderId}', ${JSON.stringify(order.items).replace(/"/g, '&quot;')})" class="text-[9px] font-bold text-dark hover:text-gold uppercase tracking-wider transition-colors"><i class="fas fa-undo"></i> Return</button>` : ''}${hasReturn ? `<div class="text-[9px] font-bold ${returnStatusColor.text} uppercase tracking-widest flex items-center gap-2 ml-auto"><i class="fas fa-history"></i> ${order.returnType}: ${order.returnStatus.replace(/_/g, ' ')}</div>` : ''}</div>
                    <div class="px-5 bg-white">${itemsHtml}</div>
                    <div class="bg-gray-50 px-5 py-5 border-t border-gray-100">
                        <div class="space-y-2 max-w-xs ml-auto">
                            <div class="flex justify-between text-xs"><span class="text-gray-500 uppercase tracking-wide">Subtotal</span><span class="text-dark font-medium">LKR ${(order.subTotal || 0).toLocaleString()}</span></div>
                            ${(order.discount && order.discount > 0) ? `<div class="flex justify-between text-xs text-gold"><span class="uppercase tracking-wide">Discount</span><span>- LKR ${order.discount.toLocaleString()}</span></div>` : ''}
                            <div class="flex justify-between text-xs"><span class="text-gray-500 uppercase tracking-wide">Shipping</span><span class="text-dark font-medium">LKR ${(order.shippingCost || 0).toLocaleString()}</span></div>
                            ${(order.tax && order.tax > 0) ? `<div class="flex justify-between text-xs"><span class="text-gray-500 uppercase tracking-wide">Tax (VAT)</span><span class="text-dark font-medium">LKR ${order.tax.toLocaleString()}</span></div>` : ''}
                            <div class="flex justify-between items-center pt-3 border-t border-gray-200 mt-3"><span class="text-xs sm:text-sm font-bold text-dark uppercase tracking-widest">Total</span><span class="text-lg sm:text-xl font-playfair font-bold text-gold">LKR ${(order.totalAmount).toLocaleString()}</span></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="px-5 py-4 border-t border-gray-100 bg-white flex justify-end gap-3 z-20 shrink-0 pb-safe">
                ${(order.canCancel && order.orderStatus !== 'Ready' && !hasReturn) ? `<button onclick="window.cancelOrder('${order.orderId}'); window.closeModal('${modalId}')" class="px-8 py-3 bg-red-50 text-red-600 font-bold hover:bg-red-600 hover:text-white transition text-[10px] uppercase tracking-widest border border-red-100">Cancel Order</button>` : ''}
                <button onclick="window.closeModal('${modalId}')" class="px-8 py-3 bg-white border border-gray-200 text-dark font-bold hover:bg-gray-50 transition text-[10px] uppercase tracking-widest">Close</button>
            </div>
        </div>
    </div>`;
    document.body.insertAdjacentHTML('beforeend', html);
}

/**
 * 3. REVIEW MODAL
 */
window.openReviewModal = function(id, isProduct, name) {
    const modalId = 'review-modal';
    document.getElementById(modalId)?.remove();
    document.body.style.overflow = 'hidden';

    const html = `
        <div id="${modalId}" class="fixed inset-0 bg-white/95 z-[70] flex items-center justify-center p-4">
            <div class="relative bg-white w-full max-w-lg p-6 sm:p-10 shadow-[0_0_60px_rgba(0,0,0,0.1)] border border-gray-100 text-center animate-slideUp">
                <button onclick="window.closeModal('${modalId}')" class="absolute top-4 right-4 text-gray-400 hover:text-dark p-2"><i class="fas fa-times text-lg"></i></button>
                <h3 class="text-xl sm:text-2xl font-playfair font-bold text-dark mb-2">Write a Review</h3>
                <div class="h-0.5 w-10 bg-gold mx-auto mb-4"></div>
                <p class="text-[10px] sm:text-xs text-gray-500 mb-6 uppercase tracking-widest line-clamp-2">${name}</p>
                <div class="flex justify-center gap-3 sm:gap-4 mb-8" id="star-container">
                    ${[1,2,3,4,5].map(i => `<i class="fas fa-star text-2xl cursor-pointer text-gray-200 hover:text-gold transition-colors" data-val="${i}"></i>`).join('')}
                </div>
                <input type="hidden" id="review-rating" value="0">
                <div class="mb-8"><textarea id="review-comment" class="w-full border border-gray-200 p-4 text-sm outline-none resize-none font-montserrat" rows="4" placeholder="Share your thoughts..."></textarea></div>
                <div class="flex gap-4">
                    <button onclick="window.closeModal('${modalId}')" class="w-full py-3 border border-gray-200 text-gray-500 font-bold text-[10px] uppercase tracking-widest">Cancel</button>
                    <button onclick="window.submitReview('${id}', ${isProduct})" class="w-full py-3 bg-dark text-white text-[10px] font-bold hover:bg-gold transition uppercase tracking-widest">Submit</button>
                </div>
            </div>
        </div>`;
    document.body.insertAdjacentHTML('beforeend', html);

    const stars = document.querySelectorAll('#star-container i');
    stars.forEach(star => {
        star.addEventListener('click', function() {
            const val = this.dataset.val;
            document.getElementById('review-rating').value = val;
            stars.forEach(s => {
                s.className = s.dataset.val <= val ? "fas fa-star text-2xl cursor-pointer text-gold transition-colors" : "fas fa-star text-2xl cursor-pointer text-gray-200 transition-colors";
            });
        });
    });
};

window.submitReview = async function(id, isProduct) {
    const rating = document.getElementById('review-rating').value;
    const comment = document.getElementById('review-comment').value;
    if(rating == 0) { notify.error('Please select a star rating.'); return; }
    const payload = { rating: parseInt(rating), comment: comment };
    if(isProduct) payload.productVariantId = parseInt(id);
    else payload.collectionId = parseInt(id);
    try {
        const res = await fetch(`${API_BASE_URL}/review`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload), credentials: 'include' });
        if(res.ok) { notify.success('Review submitted successfully.'); window.closeModal('review-modal'); }
    } catch(e) { notify.error('Error submitting review.'); }
};

/**
 * 4. RETURN MODAL
 */
window.openReturnModal = function(orderId, items) {
    const modalId = 'return-modal';
    document.getElementById(modalId)?.remove();
    document.body.style.overflow = 'hidden';

    const itemChecks = items.map(i => `
        <label class="flex items-center gap-4 p-4 border border-gray-100 cursor-pointer hover:border-gold transition-colors bg-white">
            <input type="checkbox" value="${i.productName}" class="return-check w-5 h-5 appearance-none border border-gray-300 checked:bg-black checked:border-black transition-colors">
            <div class="w-12 h-12 bg-gray-50 border border-gray-100 shrink-0"><img src="${i.image}" class="w-full h-full object-cover" loading="lazy"></div>
            <div class="flex-1"><span class="text-sm font-bold text-dark block font-playfair line-clamp-1">${i.productName}</span><span class="text-[10px] text-gray-400 uppercase tracking-wide">Qty: ${i.quantity}</span></div>
        </label>`).join('');

    const html = `
        <div id="${modalId}" class="fixed inset-0 bg-white/95 z-[60] flex items-center justify-center p-4">
            <div class="relative bg-white w-full max-w-lg p-6 sm:p-10 shadow-[0_0_60px_rgba(0,0,0,0.1)] border border-gray-100 flex flex-col max-h-[90vh] animate-slideUp">
                <button onclick="window.closeModal('${modalId}')" class="absolute top-4 right-4 text-gray-400 hover:text-dark p-2"><i class="fas fa-times text-lg"></i></button>
                <div class="text-center mb-6"><h3 class="text-xl sm:text-2xl font-playfair font-bold text-dark">Return Items</h3></div>
                <div class="overflow-y-auto flex-1 mb-6 space-y-3 pr-2">${itemChecks}</div>
                <div class="mb-6"><label class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-3 block">Reason</label><textarea id="return-reason" class="w-full border border-gray-200 p-4 text-sm outline-none resize-none" rows="3" placeholder="Describe the issue..."></textarea></div>
                <button onclick="window.submitReturn('${orderId}')" class="w-full py-4 bg-dark text-white text-[10px] font-bold hover:bg-gold transition uppercase tracking-widest">Submit Request</button>
            </div>
        </div>`;
    document.body.insertAdjacentHTML('beforeend', html);
};

window.submitReturn = async function(orderId) {
    const selected = Array.from(document.querySelectorAll('.return-check:checked')).map(c => c.value);
    const reason = document.getElementById('return-reason').value;
    if(selected.length === 0 || !reason) { notify.error('Please complete all fields.'); return; }
    try {
        const res = await fetch(`${API_BASE_URL}/${orderId}/return`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ selectedItemNames: selected, reason: reason }),
            credentials: 'include'
        });
        if(res.ok) {
            notify.success('Return request initiated.');
            window.closeModal('return-modal');

            // ✅ UPDATED: Refresh both list and details modal immediately
            await window.loadOrderHistory();
            window.viewOrderDetails(orderId);
        }
    } catch(e) { notify.error('Network error.'); }
};

/**
 * 5. CANCEL ORDER
 */
window.cancelOrder = function(orderId) {
    notify.confirm('Are you sure you want to cancel this order?', () => {
        fetch(`${API_BASE_URL}/${orderId}/cancel/update`, { method: 'POST', credentials: 'include' })
            .then(r => r.json())
            .then(res => { if(res.status) { notify.success('Order Cancelled'); window.loadOrderHistory(); } })
            .catch(() => notify.error('Error processing cancellation'));
    });
};

/**
 * UTILITIES (Status colors & Stepper)
 */
function getStatusColor(status) {
    const s = (status || '').toUpperCase();
    if (s.includes('PLACED')) return { bg: 'bg-gray-100', text: 'text-gray-600' };
    if (s.includes('READY')) return { bg: 'bg-yellow-50', text: 'text-yellow-800' };
    if (s.includes('SHIPPED')) return { bg: 'bg-blue-50', text: 'text-blue-800' };
    if (s.includes('DELIVERED')) return { bg: 'bg-green-50', text: 'text-green-800' };
    if (s.includes('CANCELLED')) return { bg: 'bg-red-50', text: 'text-red-800' };
    if (s === 'RETURN_REQUESTED') return { bg: 'bg-amber-50', text: 'text-amber-700' };
    if (s === 'APPROVED') return { bg: 'bg-blue-50', text: 'text-blue-700' };
    if (s === 'REJECTED') return { bg: 'bg-red-50', text: 'text-red-800' };
    return { bg: 'bg-gray-50', text: 'text-gray-600' };
}

function generateStepper(orderStatus, returnStatus, shippingMethod) {
    if (returnStatus) {
        const normalizedStatus = returnStatus.toUpperCase().replace(/\s+/g, '_');
        if (normalizedStatus === 'REJECTED') return `<div class="bg-red-50 border border-red-100 p-4 text-center text-red-700 font-bold uppercase tracking-widest text-[10px]">RETURN REQUEST REJECTED</div>`;
        const steps = ['RETURN_REQUESTED', 'APPROVED', 'COMPLETED'];
        const currentIndex = steps.indexOf(normalizedStatus);
        let html = `<div class="relative flex w-full px-4 mt-6 mb-4 pb-8 min-w-[300px]"><div class="absolute left-0 top-1/2 -translate-y-1/2 w-full h-[1px] bg-gray-200 -z-10"></div><div class="absolute left-0 top-1/2 -translate-y-1/2 h-[1px] bg-gold -z-10 transition-all duration-700" style="width:${Math.max(0, currentIndex) / (steps.length - 1) * 100}%"></div>`;
        steps.forEach((step, idx) => {
            const isActive = idx <= currentIndex;
            html += `<div class="flex-1 flex flex-col items-center relative"><div class="w-3 h-3 rotate-45 border ${isActive ? 'bg-gold border-gold' : 'bg-white border-gray-300'} transition-all duration-500 z-10 shadow-sm"></div><div class="absolute top-6 text-center w-full"><p class="text-[9px] uppercase tracking-widest ${isActive ? 'text-gold font-bold' : 'text-gray-400 font-medium'}">${step.replace(/_/g, ' ')}</p></div></div>`;
        });
        return html + `</div>`;
    }

    const isPickup = (shippingMethod || '').toUpperCase().includes('PICKUP');
    const steps = isPickup 
        ? ['Order Placed', 'Ready', 'Picked Up'] 
        : ['Order Placed', 'Ready', 'Shipped', 'Delivered'];
        
    const statusIndex = isPickup 
        ? { 'Order Placed': 0, 'Ready': 1, 'Picked Up': 2, 'Completed': 2 }
        : { 'Order Placed': 0, 'Ready': 1, 'Shipped': 2, 'Delivered': 3, 'Completed': 3 };

    if (orderStatus === 'Cancelled') return `<div class="bg-red-50 border border-red-100 p-4 text-center text-red-700 font-bold uppercase tracking-widest text-[10px]">ORDER CANCELLED</div>`;
    
    // Fallback normalization in case of DB mismatch
    let currentNorm = orderStatus;
    if (isPickup && (orderStatus === 'Shipped' || orderStatus === 'Delivered')) currentNorm = 'Picked Up';
    
    const currentIndex = statusIndex[currentNorm] ?? 0;
    let html = `<div class="relative flex w-full px-4 mt-6 mb-4 pb-8 min-w-[300px]"><div class="absolute left-0 top-1/2 -translate-y-1/2 w-full h-[1px] bg-gray-200 -z-10"></div><div class="absolute left-0 top-1/2 -translate-y-1/2 h-[1px] bg-gold -z-10 transition-all duration-700" style="width:${currentIndex / (steps.length - 1) * 100}%"></div>`;
    steps.forEach((step, idx) => {
        const isActive = idx <= currentIndex;
        html += `<div class="flex-1 flex flex-col items-center relative"><div class="w-3 h-3 rotate-45 border ${isActive ? 'bg-gold border-gold' : 'bg-white border-gray-300'} transition-all duration-500 z-10 shadow-sm"></div><div class="absolute top-6 text-center w-full"><p class="text-[9px] uppercase tracking-widest ${isActive ? 'text-gold font-bold' : 'text-gray-400 font-medium'}">${step}</p></div></div>`;
    });
    return html + `</div>`;
}

// --------------------------------------------------------
// INITIALIZATION
// --------------------------------------------------------

document.addEventListener('DOMContentLoaded', () => {
    window.closeModal = function(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('opacity-0');
            setTimeout(() => modal.remove(), 300);
            document.body.style.overflow = '';
        }
    };

    const isMasterScriptPresent = window.loader && document.getElementById('main-content');
    if (!isMasterScriptPresent) {
        window.loadOrderHistory();
    }
});

// Animations CSS injection
const style = document.createElement('style');
style.textContent = `
    @keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
    .animate-slideUp { animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
    .animate-fadeIn { animation: fadeIn 0.3s ease-out forwards; }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    .custom-scrollbar::-webkit-scrollbar { width: 4px; }
    .custom-scrollbar::-webkit-scrollbar-thumb { background: #E5E7EB; border-radius: 2px; }
`;
document.head.appendChild(style);