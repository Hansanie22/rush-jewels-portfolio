import Notification from './notification.js';

const notify = Notification({
    position: "top-right",
    duration: 3500,
    hidePrevious: false,
    maxVisible: 4
});

const API_BASE = '/api';
let currentOrder = null;

function getOrderIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('order');
}

// ✅ 1. ශ්‍රිතය window object එකට සම්බන්ධ කර ලෝඩරය එක් කිරීම
window.loadOrderConfirmation = async function() {
    const orderId = getOrderIdFromUrl();

    if (!orderId) {
        showError('No order ID provided in URL.');
        return false;
    }

    try {
        const response = await fetch(`${API_BASE}/order-confirmation/${orderId}`, {
            method: 'GET',
            credentials: 'include'
        });

        const result = await response.json();

        if (!result?.status) {
            showError(result?.message || 'Failed to load order details.');
            return false;
        }

        currentOrder = result.data;
        displayOrderConfirmation(result.data);
        return true;

    } catch (err) {
        console.error("Order Confirmation Load Error:", err);
        showError('Failed to load order details. Please try again later.');
        return false;
    }
};
/**
 * පිටුව පෙන්වන සහ ලෝඩරය අයින් කරන Helper Function එක
 */
function revealContent() {
    const mainContent = document.getElementById('main-content');
    if (mainContent) {
        mainContent.style.display = 'block';
        mainContent.classList.add('animate__animated', 'animate__fadeIn');
    }

    if (window.loader) {
        // ඇණවුමේ සියලු විස්තර පිළිවෙළට පෙනීමට තත්පර 0.5ක සහනයක් ලබා දෙන්න
        setTimeout(() => {
            window.loader.hide();
        }, 500);
    }
}
/* Display main order data */
function displayOrderConfirmation(data) {
    const elOrderNum = document.getElementById('order-number');
    if (elOrderNum) elOrderNum.textContent = data.orderNumber;

    const orderDate = new Date(data.orderDate);
    const elOrderDate = document.getElementById('order-placed-date');
    if (elOrderDate) {
        elOrderDate.textContent = orderDate.toLocaleDateString('en-US', {
            month: 'long',
            day: 'numeric',
            year: 'numeric'
        });
    }

    const elDelivery = document.getElementById('expected-delivery');
    const elExpectedLabel = document.getElementById('expected-label');
    
    if (elDelivery) elDelivery.textContent = data.expectedDelivery;
    
    const isPickup = (data.shippingMethod || '').toUpperCase().includes('PICKUP') || data.shippingMethod === 'STORE_PICKUP';

    if (isPickup) {
        if (elExpectedLabel) elExpectedLabel.style.display = 'none';
        if (elDelivery) elDelivery.style.display = 'none';
    } else {
        if (elExpectedLabel) {
            elExpectedLabel.style.display = 'inline';
            elExpectedLabel.textContent = 'Expected delivery: ';
        }
        if (elDelivery) {
            elDelivery.style.display = 'inline';
            elDelivery.textContent = data.expectedDelivery;
        }
    }

    const elEmail = document.getElementById('customer-email');
    if (elEmail) elEmail.textContent = data.customerEmail;

    displayOrderItems(data.items);
    displayTotals(data);

    if (data.shippingAddress) displayShippingAddress(data.shippingAddress, data);
    if (data.payment) displayPaymentMethod(data.payment);

    displayOrderTimeline(data);
}

/* Timeline */
function displayOrderTimeline(data) {
    const container = document.getElementById('order-timeline-container');
    if (!container) return;

    const isPickup = (data.shippingMethod || '').toUpperCase().includes('PICKUP');
    
    // Define steps
    let steps = [];
    if (isPickup) {
        steps = [
            { id: 'Order Placed', label: 'Order Placed', desc: 'We have received your order' },
            { id: 'Ready', label: 'Ready', desc: 'Your order is being prepared' },
            { id: 'Picked Up', label: 'Picked Up', desc: 'Your order has been picked up' }
        ];
    } else {
        steps = [
            { id: 'Order Placed', label: 'Order Placed', desc: 'We have received your order' },
            { id: 'Ready', label: 'Ready', desc: 'Your order is being prepared' },
            { id: 'Shipped', label: 'Shipped', desc: 'Your order has been dispatched' },
            { id: 'Delivered', label: 'Delivered', desc: 'Your order has been delivered' }
        ];
    }

    const orderStatus = data.orderStatus || 'Order Placed';
    const statusIndexMap = isPickup 
        ? { 'Order Placed': 0, 'Ready': 1, 'Picked Up': 2, 'Completed': 2 }
        : { 'Order Placed': 0, 'Ready': 1, 'Shipped': 2, 'Delivered': 3, 'Completed': 3 };

    let currentNorm = orderStatus;
    if (isPickup && (orderStatus === 'Shipped' || orderStatus === 'Delivered')) currentNorm = 'Picked Up';
    
    const currentIndex = statusIndexMap[currentNorm] ?? 0;
    
    // Format date for the active step (usually the Order Placed date for step 0)
    const orderDateFormatted = new Date(data.orderDate).toLocaleDateString('en-US', {
        month: 'long', day: 'numeric', year: 'numeric'
    });

    let html = '';
    steps.forEach((step, idx) => {
        const isActive = idx <= currentIndex;
        const isCurrent = idx === currentIndex;
        
        let dateOrPending = 'Pending';
        if (idx === 0) dateOrPending = orderDateFormatted; // Always show date for first step
        else if (isActive) dateOrPending = 'Completed'; // For past steps

        html += `
            <div class="timeline-item ${isActive ? '' : 'inactive'}">
                <div class="font-semibold ${isActive ? 'text-dark' : 'text-gray-600'}">${step.label}</div>
                <div class="text-sm ${isActive ? 'text-gray-600' : 'text-gray-400'}">${dateOrPending}</div>
                <p class="text-sm ${isActive ? 'text-gray-500' : 'text-gray-400'} mt-1">${step.desc}</p>
            </div>
        `;
    });

    container.innerHTML = html;
}

/* Items list */
function displayOrderItems(items) {
    const container = document.getElementById('order-items-container');
    if (!container) return;

    if (!items || items.length === 0) {
        container.innerHTML = '<p class="text-gray-500 text-center py-4 text-sm">No items found</p>';
        return;
    }

    // Generate the HTML
    container.innerHTML = items.map(item => {
        // Handle property names flexibly (supports both displayName and productName)
        const name = item.displayName || item.productName || 'Product Name';
        const image = item.image || 'assets/images/placeholder.png';

        // Calculate total if not provided
        const quantity = item.quantity || 1;
        const price = item.price || 0;
        const total = item.total || (price * quantity);

        return `
        <div class="flex gap-3 sm:gap-4 py-4 border-b border-gray-100 last:border-0 items-start hover:bg-gray-50/50 transition-colors">
            
            <!-- PRODUCT IMAGE -->
            <div class="w-20 h-20 sm:w-24 sm:h-24 flex-shrink-0 bg-white border border-gray-200 overflow-hidden relative group">
                <img src="${image}" alt="${name}" 
                     class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" 
                     onerror="this.src='assets/images/placeholder.png'" loading="lazy">
            </div>

            <!-- PRODUCT DETAILS -->
            <div class="flex-1 min-w-0 flex flex-col justify-between min-h-[5rem] sm:min-h-[6rem]">
                <div>
                    <!-- Name: Given full width to prevent squashing on mobile -->
                    <h4 class="font-playfair font-bold text-sm text-gray-900 leading-tight mb-1.5">
                        ${name}
                    </h4>

                    <!-- Variants: Cleanly stacked with correct spacing -->
                    <div class="text-[11px] sm:text-xs text-gray-500 font-medium space-y-0.5 mb-2">
                        ${item.variant ? `<p class="truncate text-gray-600">${item.variant}</p>` : ''}
                        ${item.size ? `<p class="uppercase tracking-wide">Size: ${item.size}</p>` : ''}
                        ${item.color ? `<p class="uppercase tracking-wide">Color: ${item.color}</p>` : ''}
                        ${item.gemstone ? `<p class="uppercase tracking-wide">Gemstone: ${item.gemstone}</p>` : ''}
                        ${item.material ? `<p class="uppercase tracking-wide">Material: ${item.material}</p>` : ''}
                    </div>
                </div>

                <!-- Bottom Row: Quantity Left, Price Right -->
                <div class="flex justify-between items-end mt-auto pt-2">
                    <span class="text-[10px] sm:text-xs text-gray-500 font-semibold bg-gray-50 border border-gray-100 px-2 py-0.5 rounded uppercase tracking-wider">
                        Qty: ${quantity}
                    </span>
                    <span class="font-bold text-sm text-gold whitespace-nowrap">
                        LKR ${total.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                    </span>
                </div>
            </div>
        </div>
    `}).join('');
}

/* Totals */
function displayTotals(data) {
    const elSub = document.getElementById('order-subtotal');
    if(elSub) elSub.textContent = `LKR ${formatCurrency(data.subtotal)}`;

    const elShip = document.getElementById('order-shipping');
    if(elShip) elShip.textContent = `LKR ${formatCurrency(data.shippingCost)}`;

    const elTax = document.getElementById('order-tax');

    const elTotal = document.getElementById('order-total');
    if(elTotal) elTotal.textContent = `LKR ${formatCurrency(data.total)}`;

    if (data.discountAmount > 0) {
        const row = document.getElementById('discount-row');
        if(row) row.style.display = 'flex';
        const elDisc = document.getElementById('order-discount');
        if(elDisc) elDisc.textContent = `- LKR ${formatCurrency(data.discountAmount)}`;
    }
}

/* Shipping */
function displayShippingAddress(address, data) {
    const container = document.getElementById('shipping-address');
    if (!container) return;

    const isPickup = (data && data.shippingMethod && data.shippingMethod.toUpperCase().includes('PICKUP'));

    const parentTitle = container.previousElementSibling;
    if (isPickup && parentTitle) {
        parentTitle.innerHTML = `<i class="fas fa-store text-gold mr-2"></i>Pickup Details`;
    }

    if (isPickup) {
        container.innerHTML = `
            <p class="font-semibold text-dark text-lg font-playfair">Rush Jewels (Kandy Store)</p>
            <p class="text-gray-600">454/5 Daulagala Road</p>
            <p class="text-gray-600">Pilimathalawa, Sri Lanka</p>
            <p class="mt-2 text-gold"><i class="fas fa-phone mr-2"></i>075 483 2960</p>
            <hr class="my-3 border-gray-200">
            <p class="text-gray-500 text-xs uppercase tracking-wide">Customer Details</p>
            <p class="font-medium text-dark">${address.firstName} ${address.lastName}</p>
            <p class="text-gray-600 text-sm"><i class="fas fa-phone mr-1"></i>${address.phone}</p>
        `;
    } else {
        const addrLine2 = address.addressLine2 ? `<p class="text-gray-600">${address.addressLine2}</p>` : '';
        const cityStr = address.city && !address.city.includes('Select') ? address.city : '';
        const stateStr = address.state && !address.state.includes('Select') ? address.state : '';
        const zipStr = address.postalCode ? address.postalCode : '';
        const csz = [cityStr, stateStr, zipStr].filter(Boolean).join(', ');

        container.innerHTML = `
            <p class="font-semibold text-dark text-lg font-playfair">${address.firstName} ${address.lastName}</p>
            <p class="text-gray-600">${address.addressLine1}</p>
            ${addrLine2}
            ${csz ? `<p class="text-gray-600">${csz}</p>` : ''}
            <p class="text-gray-600">${address.country || 'Sri Lanka'}</p>
            <p class="mt-2 text-gold"><i class="fas fa-phone mr-2"></i>${address.phone}</p>
        `;
    }
}

/* Payment */
function displayPaymentMethod(payment) {
    const container = document.getElementById('payment-method');
    if (!container) return;

    const completed = payment.status === 'COMPLETED';
    const classColor = completed ? 'text-green-600' : 'text-orange-600';
    const icon = completed ? 'check-circle' : 'clock';

    let html = `
        <p class="font-semibold text-dark">${payment.methodDisplay}</p>
    `;

    if (payment.method === 'card') {
        html += `<p class="text-gray-600">**** **** **** ${payment.lastFour || '****'}</p>`;
    }

    html += `
        <p class="text-sm ${classColor} mt-2 flex items-center">
            <i class="fas fa-${icon} mr-2"></i>
            ${completed ? "Payment Successful" : "Payment " + payment.status}
        </p>
    `;

    container.innerHTML = html;
}

function showError(message) {
    // ✅ නිවැරදි කළා: නව Loader පද්ධතියට අනුව
    if(window.loader) window.loader.hide();

    const error = document.getElementById('error-state');
    if(error) error.style.display = 'block';

    notify.error(message);
}

function formatCurrency(amount) {
    if (!amount && amount !== 0) return "0.00";
    return Number(amount).toLocaleString("en-US", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function loadFooter() {
    const footerContainer = document.getElementById('footer-container');
    if (!footerContainer) return;

    if (window.componentLoader) {
        window.componentLoader.loadFooter('#footer-container').catch(() => fallbackFooter(footerContainer));
    } else {
        fallbackFooter(footerContainer);
    }
}

function fallbackFooter(container) {
    container.innerHTML = `
        <footer class="bg-dark text-white py-8 mt-12 text-center">
            <p>&copy; 2025 Rush Jewels. All rights reserved.</p>
        </footer>
    `;
}

// ------------------------------------------------
// PRINT INVOICE LOGIC (Updated Design)
// ------------------------------------------------
function printOrder() {
    if (!currentOrder) return;

    const printWindow = window.open('', '_blank', 'width=1000,height=800');

    // Data from Current Order
    const orderNumber = currentOrder.orderNumber;
    const orderDate = new Date(currentOrder.orderDate).toLocaleDateString('en-US', { month: 'short', day: '2-digit', year: 'numeric' });
    const subtotal = formatCurrency(currentOrder.subtotal);
    const shipping = formatCurrency(currentOrder.shippingCost);
    const tax = formatCurrency(currentOrder.taxAmount);
    const total = formatCurrency(currentOrder.total);
    const discount = currentOrder.discountAmount > 0 ? formatCurrency(currentOrder.discountAmount) : null;

    // Split Address Logic for Billed/Shipped
    const addr = currentOrder.shippingAddress;

    // Billed To: Name, Email, Phone
    const billedToHtml = `
        <strong>${addr.firstName} ${addr.lastName}</strong><br>
        ${currentOrder.customerEmail}<br>
        ${addr.phone}
    `;

    // Shipped To: Address Lines Only
    const shippedToHtml = `
        ${addr.addressLine1}<br>
        ${addr.addressLine2 ? addr.addressLine2 + '<br>' : ''}
        ${addr.city}, ${addr.state}<br>
        ${addr.country}
    `;

    // Payment Status Logic
    const isCod = currentOrder.payment.method === 'cod' ||
        (currentOrder.payment.methodDisplay && currentOrder.payment.methodDisplay.toLowerCase().includes('cash'));

    const statusText = isCod ? 'Pending' : 'Paid';
    const statusColor = isCod ? '#e67e22' : '#C5A059'; // Orange if Pending, Gold if Paid

    // Generate Items
    let itemsHtml = '';
    currentOrder.items.forEach(item => {
        let details = [];
        if(item.size) details.push(`Size: ${item.size}`);
        if(item.color) details.push(`Color: ${item.color}`);
        if(item.gemstone) details.push(`Gem: ${item.gemstone}`);

        itemsHtml += `
            <tr>
                <td>
                    <div class="item-desc">${item.displayName}</div>
                    ${details.length ? `<div class="item-sub">${details.join(' • ')}</div>` : ''}
                </td>
                <td style="text-align: center;">${item.quantity}</td>
                <td style="text-align: right;">${formatCurrency(item.price)}</td>
                <td style="text-align: right;">${formatCurrency(item.total)}</td>
            </tr>
        `;
    });

    // Write Invoice HTML
    printWindow.document.write(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Invoice ${orderNumber}</title>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Bodoni+Moda:ital,wght@0,400;0,700;1,400&family=Lato:wght@300;400;700&family=Libre+Barcode+39+Text&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Lato', sans-serif; line-height: 1.6; color: #1a1a1a; background-color: #fff; margin: 0; padding: 20px; -webkit-font-smoothing: antialiased; }
        .container { width: 100%; max-width: 800px; background: #ffffff; margin: 0 auto; border: 1px solid #dcdcdc; }
        
        /* Brand Colors */
        :root { --brand-gold: #C5A059; --brand-dark: #121212; --text-muted: #666; --border-color: #e0e0e0; }

        .header { background: var(--brand-dark); color: var(--brand-gold); padding: 40px; display: flex; justify-content: space-between; align-items: center; border-bottom: 4px solid var(--brand-gold); }
        .header-logo { font-family: 'Playfair Display', serif; font-size: 32px; font-weight: 700; margin: 0; text-transform: uppercase; line-height: 1; }
        .header-meta { text-align: right; font-size: 10px; text-transform: uppercase; letter-spacing: 2px; color: #fff; opacity: 0.8; }

        .content { padding: 40px; }

        .invoice-hero { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 40px; border-bottom: 1px solid var(--brand-dark); padding-bottom: 15px; }
        .invoice-title { font-family: 'Bodoni Moda', serif; font-size: 36px; color: var(--brand-dark); margin: 0; font-style: italic; font-weight: 700; line-height: 1; }
        .invoice-number { font-family: 'Playfair Display', serif; font-size: 14px; color: var(--text-muted); font-weight: 600; }

        .info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 40px; }
        .info-col h3 { font-size: 9px; text-transform: uppercase; letter-spacing: 1.5px; color: var(--text-muted); margin: 0 0 8px 0; border-bottom: 1px solid var(--border-color); padding-bottom: 4px; }
        .info-col p, .info-col div { font-size: 12px; margin: 0; line-height: 1.6; color: var(--brand-dark); }
        .info-col strong { font-weight: 700; }

        .items-table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }
        .items-table th { text-align: left; font-size: 9px; text-transform: uppercase; color: var(--brand-dark); letter-spacing: 1.5px; padding: 10px 0; border-bottom: 2px solid var(--brand-dark); font-weight: 700; }
        .items-table th:last-child { text-align: right; }
        .items-table td { padding: 15px 0; border-bottom: 1px solid var(--border-color); font-size: 13px; vertical-align: top; }
        .items-table td:last-child { text-align: right; }
        .item-desc { font-family: 'Playfair Display', serif; font-weight: 700; font-size: 14px; color: var(--brand-dark); }
        .item-sub { font-size: 11px; color: var(--text-muted); margin-top: 4px; }

        .totals-wrapper { display: flex; justify-content: flex-end; }
        .totals-box { width: 250px; }
        .total-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 12px; color: var(--text-muted); text-transform: uppercase; }
        .total-row.final { font-family: 'Playfair Display', serif; font-size: 18px; color: var(--brand-dark); font-weight: 700; border-top: 2px solid var(--brand-dark); border-bottom: 2px solid var(--brand-dark); padding: 10px 0; margin-top: 15px; text-transform: none; align-items: center; }

        .footer { background: #111; color: #666; padding: 30px; text-align: center; font-size: 10px; text-transform: uppercase; letter-spacing: 1px; }
        .barcode { font-family: 'Libre Barcode 39 Text', cursive; font-size: 32px; color: #fff; opacity: 0.4; margin-top: 15px; display: block; transform: scaleY(1.4); }

        .contact-strip { margin-top: 15px; color: #888; }
        .contact-strip span { display: inline-block; }
        .contact-strip .sep { margin: 0 5px; }

        /* --- MOBILE RESPONSIVENESS START --- */
        @media only screen and (max-width: 600px) {
            body { padding: 0; }
            .container { width: 100% !important; border: none; }
            .content { padding: 20px; }
            
            /* Header Stacking */
            .header { flex-direction: column; text-align: center; gap: 10px; padding: 30px 20px; }
            .header-meta { text-align: center; }

            /* Hero Stacking */
            .invoice-hero { flex-direction: column; align-items: center; text-align: center; margin-bottom: 30px; }
            .invoice-title { font-size: 30px; margin-bottom: 5px; }

            /* Info Grid Stacking */
            .info-grid { grid-template-columns: 1fr; gap: 20px; text-align: center; }
            .info-col { border-bottom: 1px solid #eee; padding-bottom: 15px; }
            .info-col:last-child { border-bottom: none; }

            /* Table Adjustments */
            .items-table th { font-size: 8px; }
            .item-desc { font-size: 12px; }
            
            /* Totals Centering */
            .totals-wrapper { justify-content: center; margin-top: 20px; }
            .totals-box { width: 100%; }

            /* Rush Jewels Contact Line Breaking */
            .contact-strip span { display: block; margin: 4px 0; }
            .contact-strip .sep { display: none; }
        }
        /* --- MOBILE RESPONSIVENESS END --- */
        
        @media print { body { padding: 0; background: #fff; } .container { border: none; width: 100%; max-width: 100%; } }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 class="header-logo">Rush Jewels</h1>
            <div class="header-meta">Est. 2025<br>Kandy, Sri Lanka</div>
        </div>
        <div class="content">
            <div class="invoice-hero">
                <h1 class="invoice-title">Invoice</h1>
                <div class="invoice-number">#${orderNumber}</div>
            </div>
            <div class="info-grid">
                <div class="info-col">
                    <h3>Billed To</h3>
                    <div>${billedToHtml}</div>
                </div>
                <div class="info-col">
                    <h3>Shipped To</h3>
                    <div>${shippedToHtml}</div>
                </div>
                <div class="info-col">
                    <h3>Details</h3>
                    <p>
                        <strong>Issued:</strong> ${orderDate}<br>
                        <strong>Due:</strong> Upon Receipt<br>
                        <strong>Status:</strong> <span style="color: ${statusColor}; font-weight:bold;">${statusText}</span>
                    </p>
                </div>
            </div>
            <table class="items-table">
                <thead>
                    <tr>
                        <th width="50%">Description</th>
                        <th width="15%" style="text-align: center;">Qty</th>
                        <th width="15%" style="text-align: right;">Unit</th>
                        <th width="20%" style="text-align: right;">Total</th>
                    </tr>
                </thead>
                <tbody>
                    ${itemsHtml}
                </tbody>
            </table>
            <div class="totals-wrapper">
                <div class="totals-box">
                    <div class="total-row"><span>Subtotal</span><span>LKR ${subtotal}</span></div>
                    <div class="total-row"><span>Shipping</span><span>LKR ${shipping}</span></div>
                    <div class="total-row"><span>Tax (VAT)</span><span>LKR ${tax}</span></div>
                    ${discount ? `<div class="total-row" style="color: #e74c3c;"><span>Discount</span><span>${discount}</span></div>` : ''}
                    <div class="total-row final"><span>Total Paid</span><span>LKR ${total}</span></div>
                </div>
            </div>
        </div>
        <div class="footer">
            <p style="margin: 0; opacity: 0.7;">Thank you for your business. Elegance in every detail.</p>
            <div class="contact-strip">
                <span>rushjewelsofficial@gmail.com</span><span class="sep"> | </span><span>+94 75 483 2960</span>
            </div>
            <div class="barcode">RJ-${orderNumber}-${statusText.toUpperCase()}</div>
        </div>
    </div>
    <script>window.onload = function() { window.print(); };</script>
</body>
</html>
    `);

    printWindow.document.close();
}

// ✅ මෙය printOrder ශ්‍රිතයට පහළින් ඇතුළත් කරන්න
function initializePrintButton() {
    const btn = document.getElementById('print-order-btn');
    if (btn) {
        btn.addEventListener('click', printOrder);
    }
}

// ✅ යාවත්කාලීන කළා: Footer Categories එනතෙක් බලා සිටීම සඳහා (Initialization logic)
document.addEventListener('DOMContentLoaded', async () => {
    // HTML එකේ Master Script එක තිබේ නම්, Coordination එක එය බලාගනු ඇත.
    // එබැවින් මෙහිදී Master Script එක නැතිනම් පමණක් ක්‍රියාත්මක වන Fallback එකක් යොදමු.
    const isMasterScriptPresent = window.loader && document.getElementById('main-content');

    if (!isMasterScriptPresent) {
        if (window.loader) window.loader.show();
        try {
            // Footer (Categories සහිතව) සහ ඇණවුමේ දත්ත ලැබෙන තෙක් බලා සිටීම
            await Promise.all([
                window.loadOrderConfirmation(),
                window.componentLoader ? window.componentLoader.loadFooter() : Promise.resolve()
            ]);
            initializePrintButton();
        } catch (e) {
            console.error(e);
        } finally {
            revealContent();
        }
    } else {
        // Master script තිබේ නම්, එය Coordination එක බලාගන්නා අතර අප බොත්තම පමණක් සක්‍රීය කරමු
        initializePrintButton();
    }
});