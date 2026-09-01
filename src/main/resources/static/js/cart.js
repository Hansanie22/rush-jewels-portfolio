import Notification from './notification.js';
import { redirectToLogin } from './auth-redirect.js';

const notify = Notification({
    position: 'bottom-right',
    duration: 3000,
    hidePrevious: true,
    maxVisible: 5,
});

/**
 * ROBUST CART CONTROLLER (UPDATED)
 * Handles: Cart Data, API Calls, Side Panel UI
 * Change: Total in panel now reflects Subtotal (Taxes/Shipping deferred to checkout)
 */

let isControllerInitialized = false;

// Helper to get element safely
const getEl = (id) => document.getElementById(id);

// ==========================================
// 1. INJECT NEW DESIGN (HTML & CSS)
// ==========================================
function injectCartPanel() {
    // 1. Remove existing cart elements if they exist to prevent duplicates
    const existingOverlay = document.getElementById('cart-overlay');
    const existingPanel = document.getElementById('cart-panel');
    if (existingOverlay) existingOverlay.remove();
    if (existingPanel) existingPanel.remove();

    // 2. Inject CSS Styles
    const styleId = 'velora-cart-styles';
    if (!document.getElementById(styleId)) {
        const style = document.createElement('style');
        style.id = styleId;
        style.textContent = `
            /* Strict No-Rounding Policy */
            #cart-panel *, #cart-panel *::before, #cart-panel *::after { border-radius: 0 !important; }
            
            /* Custom Scrollbar */
            .sharp-scroll::-webkit-scrollbar { width: 4px; }
            .sharp-scroll::-webkit-scrollbar-track { background: #f1f1f1; }
            .sharp-scroll::-webkit-scrollbar-thumb { background: #111; }

            /* Safe Area */
            .pb-safe { padding-bottom: env(safe-area-inset-bottom); }

            /* GOOEY BUTTON CSS */
            .c-button {
                color: #111; font-weight: 700; font-size: 13px; text-decoration: none;
                padding: 1.2em 1em; cursor: pointer; display: block; width: 100%;
                vertical-align: middle; position: relative; z-index: 1;
                background: transparent; text-align: center;
                font-family: 'Cinzel', serif; letter-spacing: 0.15em;
                transition: all 0.3s ease;
            }
            .c-button--gooey {
                color: #111; text-transform: uppercase; border: 2px solid #111;
                position: relative; transition: all 700ms ease; overflow: hidden;
            }
            .c-button--gooey .c-button__blobs {
                height: 100%; filter: url(#goo); overflow: hidden;
                position: absolute; top: 0; left: 0; bottom: -3px; right: -1px; z-index: -1;
            }
            .c-button--gooey .c-button__blobs div {
                background-color: #C5A059; width: 34%; height: 100%; border-radius: 100% !important;
                position: absolute; transform: scale(1.4) translateY(125%) translateZ(0);
                transition: all 700ms ease;
            }
            .c-button--gooey .c-button__blobs div:nth-child(1) { left: -5%; }
            .c-button--gooey .c-button__blobs div:nth-child(2) { left: 30%; transition-delay: 60ms; }
            .c-button--gooey .c-button__blobs div:nth-child(3) { left: 66%; transition-delay: 25ms; }
            
            /* Hover States (Only when not disabled) */
            .c-button--gooey:not(:disabled):hover { color: #fff; border-color: #C5A059; }
            .c-button--gooey:not(:disabled):hover .c-button__blobs div { transform: scale(1.4) translateY(0) translateZ(0); }

            /* BLACK VARIANT */
            .c-button--black .c-button__blobs div { background-color: #111111; }
            .c-button--black:not(:disabled):hover { border-color: #111111; }

            /* DISABLED STATE (Locked Buttons) */
            button:disabled, .c-button:disabled {
                opacity: 0.4 !important;
                cursor: not-allowed !important;
                pointer-events: none !important;
                filter: grayscale(100%) !important;
                border-color: #ccc !important;
                color: #999 !important;
                background: #f5f5f5 !important;
            }

            /* ICON BUTTON VARIANT */
            .c-button--icon {
                width: 34px !important; height: 34px !important; padding: 0 !important;
                display: flex !important; align-items: center; justify-content: center;
                font-size: 14px; border-width: 1px !important;
            }
            
            /* Mobile adjustments for Icon Button */
            @media (max-width: 768px) {
                .c-button--icon {
                    width: 30px !important; height: 30px !important;
                }
            }
        `;
        document.head.appendChild(style);
    }

    // 3. Inject SVG Filter for Gooey Effect
    if (!document.getElementById('goo-filter')) {
        const svgDiv = document.createElement('div');
        svgDiv.id = 'goo-filter';
        svgDiv.innerHTML = `
            <svg xmlns="http://www.w3.org/2000/svg" version="1.1" style="display: block; height: 0; width: 0;">
              <defs>
                <filter id="goo">
                  <feGaussianBlur in="SourceGraphic" stdDeviation="10" result="blur"></feGaussianBlur>
                  <feColorMatrix in="blur" mode="matrix" values="1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 18 -7" result="goo"></feColorMatrix>
                  <feBlend in="SourceGraphic" in2="goo"></feBlend>
                </filter>
              </defs>
            </svg>
        `;
        document.body.appendChild(svgDiv);
    }

    // 4. Inject HTML Structure
    const cartHTML = `
    <div id="cart-overlay" class="fixed inset-0 bg-black/60 z-[140] hidden opacity-0 transition-opacity duration-300 backdrop-blur-sm" onclick="window.closeCartPanel()"></div>

    <div id="cart-panel" class="fixed top-0 right-0 h-full w-full md:w-[480px] bg-white z-[150] transform translate-x-full transition-transform duration-500 ease-[cubic-bezier(0.25,1,0.5,1)] flex flex-col shadow-2xl border-l border-gray-200">
        
        <!-- Header -->
        <header class="flex-none bg-white px-4 py-4 md:px-6 md:py-6 border-b border-gray-100 flex justify-between items-start">
            <div>
                <h2 class="font-serif text-lg md:text-2xl text-gray-900 font-semibold tracking-wider">YOUR BAG</h2>
                <p class="text-[10px] md:text-xs text-gray-500 mt-1 uppercase tracking-widest" id="cart-item-count-header">0 Items</p>
            </div>
            <button id="close-cart-panel" class="w-8 h-8 md:w-10 md:h-10 flex items-center justify-center border border-transparent hover:border-black transition-colors group">
                <i class="fas fa-times text-lg md:text-xl text-gray-900 group-hover:rotate-90 transition-transform duration-300"></i>
            </button>
        </header>

        <!-- Body -->
        <div class="flex-1 overflow-y-auto sharp-scroll bg-white p-4 md:p-6 relative">
            <div id="empty-cart" class="flex flex-col items-center justify-center text-center p-4">
                <div class="w-20 h-20 md:w-24 md:h-24 border border-gray-200 flex items-center justify-center mb-6">
                    <i class="fas fa-shopping-bag text-3xl md:text-4xl text-gray-300"></i>
                </div>
                <h3 class="font-serif text-xl md:text-2xl mb-2">Your Bag is Empty</h3>
                <p class="text-sm md:text-base text-gray-500 mb-8 max-w-[240px]">Timeless pieces are waiting for you.</p>
                <button id="continue-shopping"
                   onclick="window.location.href='shop.html'"
                   class="border-b border-black pb-1 uppercase text-xs md:text-sm tracking-widest hover:text-gold hover:border-gold transition-colors">
                   Continue Shopping
                </button>
            </div>

            <!-- Items Container -->
            <div id="cart-items-container" class="space-y-4 md:space-y-8"></div>
        </div>

        <!-- Footer -->
        <div class="flex-none bg-[#F9F9F9] p-4 md:p-6 border-t border-gray-200 pb-safe">
            <div class="space-y-2 md:space-y-3 mb-4 md:mb-6 text-xs md:text-sm">
                <div class="flex justify-between text-gray-600">
                    <span>Subtotal</span>
                    <span class="font-bold text-gray-900" id="cart-subtotal">LKR 0.00</span>
                </div>
                <div class="flex justify-between text-gray-500 text-[10px] md:text-xs italic">
                    <span>Shipping</span>
                    <span>Calculated at checkout</span>
                </div>
                <div class="pt-3 md:pt-4 mt-3 md:mt-4 border-t border-gray-300 flex justify-between items-center">
                    <span class="font-serif text-base md:text-lg font-semibold">Total</span>
                    <span class="font-serif text-lg md:text-xl font-bold" id="cart-total">LKR 0.00</span>
                </div>
            </div>

            <div class="grid grid-cols-1 gap-3">
                <button id="checkout-btn" class="c-button c-button--gooey" disabled>
                    Checkout Now
                    <div class="c-button__blobs"><div></div><div></div><div></div></div>
                </button>
                <button id="clear-cart-btn" class="c-button c-button--gooey c-button--black" disabled>
                    Clear Cart
                    <div class="c-button__blobs"><div></div><div></div><div></div></div>
                </button>
            </div>

            <div class="mt-6 flex justify-center gap-4 text-gray-400 opacity-60">
                <i class="fab fa-cc-visa text-lg md:text-xl"></i>
                <i class="fab fa-cc-mastercard text-lg md:text-xl"></i>
                <i class="fab fa-cc-amex text-lg md:text-xl"></i>
                <i class="fas fa-lock text-xs md:text-sm flex items-center"><span class="ml-1 font-sans text-[10px]">Secure</span></i>
            </div>
        </div>
    </div>`;

    document.body.insertAdjacentHTML('beforeend', cartHTML);
}

// ==========================================
// 2. MAIN LOGIC
// ==========================================

function initCartController() {
    if (isControllerInitialized) return;

    // Inject the design
    injectCartPanel();

    isControllerInitialized = true;

    let userLoggedIn = false;

    // --- Helpers ---
    function formatCurrency(amount) {
        const safeAmount = typeof amount === 'number' && !isNaN(amount) ? amount : 0;
        return `LKR ${safeAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }

    async function checkUserSession() {
        if (userLoggedIn) return true;
        try {
            const res = await fetch('/api/auth/session-check');
            if (!res.ok) throw new Error('Not logged in');
            const data = await res.json();
            if (!data.loggedIn) throw new Error('Not logged in');
            userLoggedIn = true;
            return true;
        } catch (err) {
            redirectToLogin({
                message: 'Please login to your account.',
                stateFlags: { openCartAfterLogin: true },
                delay: 1500
            });
            userLoggedIn = false;
            return false;
        }
    }

    async function initializeCart() {
        try {
            const res = await fetch('/api/auth/session-check');
            const data = await res.json();
            userLoggedIn = (res.ok && data.loggedIn);
        } catch (err) { console.warn('Session check init failed'); }

        if (userLoggedIn) {
            await loadCartData();
        } else {
            renderCartUI({ totalItems: 0, subtotal: 0, cartItems: [] });
        }
    }

    async function apiPostRequest(url, formData) {
        if (!userLoggedIn) return null;
        try {
            const res = await fetch(url, { method: 'POST', body: formData });
            const text = await res.text();
            let data;
            try { data = JSON.parse(text); } catch { return null; }
            if (!res.ok) throw new Error(data?.message || 'Error');
            return data;
        } catch (err) {
            notify.error(err.message);
            return null;
        }
    }

    // --- Panel Controls ---
    function closeCartPanelFunc() {
        const cartPanel = getEl('cart-panel');
        const cartOverlay = getEl('cart-overlay');

        if (cartPanel) {
            cartPanel.classList.remove('translate-x-0');
            cartPanel.classList.add('translate-x-full');
        }

        if (cartOverlay) {
            cartOverlay.classList.remove('opacity-100');
            cartOverlay.classList.add('opacity-0');
            setTimeout(() => {
                if (cartOverlay) cartOverlay.classList.add('hidden');
            }, 300);
        }

        document.body.style.overflow = '';
        document.documentElement.style.overflow = '';
    }

    async function openCartPanel() {
        if (!(await checkUserSession())) return;

        const cartPanel = getEl('cart-panel');
        const cartOverlay = getEl('cart-overlay');

        if (cartPanel && cartOverlay) {
            cartOverlay.classList.remove('hidden');
            void cartOverlay.offsetWidth;
            cartOverlay.classList.remove('opacity-0');
            cartOverlay.classList.add('opacity-100');

            cartPanel.classList.remove('translate-x-full');
            cartPanel.classList.add('translate-x-0');

            document.body.style.overflow = 'hidden';
            document.documentElement.style.overflow = 'hidden';
            await loadCartData();
        }
    }

    // --- Load & Render Cart ---
    async function loadCartData() {
        if (!userLoggedIn) return;
        try {
            const res = await fetch('/api/cart');
            const data = await res.json();
            if (data.success) renderCartUI(data);
        } catch (err) {
            console.error(err);
        }
    }

    /**
     * UPDATED RENDER UI:
     * We explicitly use data.subtotal for both "Subtotal" and "Total" fields
     * because shipping/tax are deferred to the checkout page.
     */
    function renderCartUI(data) {
        const itemCount = data.totalItems || 0;
        const isCartEmpty = itemCount === 0;
        const subtotalValue = data.subtotal || 0;

        const globalCartCounts = document.querySelectorAll('#cart-count, .cart-count-badge');
        globalCartCounts.forEach(el => {
            el.textContent = itemCount > 0 ? itemCount : '0';
            if(itemCount > 0) {
                el.classList.remove('hidden');
                el.classList.remove('cart-blink');
                void el.offsetWidth;
                el.classList.add('cart-blink');
            } else {
                el.classList.add('hidden');
            }
        });

        const cartItemCountHeader = getEl('cart-item-count-header');
        const cartSubtotal = getEl('cart-subtotal');
        const cartTotal = getEl('cart-total');
        const cartItemsContainer = getEl('cart-items-container');
        const emptyCart = getEl('empty-cart');
        const clearCartBtn = getEl('clear-cart-btn');
        const checkoutBtn = getEl('checkout-btn');

        if (cartItemCountHeader) cartItemCountHeader.textContent = `${itemCount} Items`;

        // Use subtotal for both to ignore tax/shipping calculation at this stage
        if (cartSubtotal) cartSubtotal.textContent = formatCurrency(subtotalValue);
        if (cartTotal) cartTotal.textContent = formatCurrency(subtotalValue);

        if (isCartEmpty) {
            if (cartItemsContainer) {
                cartItemsContainer.innerHTML = '';
                cartItemsContainer.classList.add('hidden');
            }
            if (emptyCart) emptyCart.classList.remove('hidden');
        } else {
            if (cartItemsContainer) {
                cartItemsContainer.classList.remove('hidden');
                cartItemsContainer.innerHTML = renderCartItemsList(data.cartItems);
            }
            if (emptyCart) emptyCart.classList.add('hidden');
        }

        if (clearCartBtn) clearCartBtn.disabled = isCartEmpty;
        if (checkoutBtn) checkoutBtn.disabled = isCartEmpty;
    }

    function renderCartItemsList(items) {
        return items.map(item => {
            const isMaxStock = item.quantity >= item.availableStock;
            return `
            <div class="flex gap-3 md:gap-4 group">
                <div class="relative w-20 h-20 md:w-24 md:h-24 flex-none bg-gray-100 overflow-hidden border border-gray-100">
                    <img src="${item.image}" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" alt="${item.name}" onerror="this.src='images/placeholder-collection.jpg'" loading="lazy">
                </div>
                
                <div class="flex-1 flex flex-col justify-between py-0.5">
                    <div>
                        <div class="flex justify-between items-start mb-1">
                            <h4 class="font-serif font-semibold text-gray-900 text-xs md:text-sm leading-tight uppercase tracking-wide">${item.name}</h4>
                            <span class="text-xs md:text-sm font-bold ml-2 whitespace-nowrap">${formatCurrency(item.finalPrice * item.quantity)}</span>
                        </div>
                        <p class="text-[9px] md:text-[10px] text-gray-400 mt-0">Unit: ${formatCurrency(item.finalPrice)}</p>
                    </div>

                    <div class="flex justify-between items-end mt-2">
                        <div class="flex items-center border border-gray-300 h-7 w-20 md:h-8 md:w-24 bg-white">
                            <button class="h-full w-7 md:w-8 flex items-center justify-center hover:bg-gray-50 text-gray-600 transition-colors qty-btn decrease-qty border-r border-gray-200" data-item-id="${item.cartId}">
                                <i class="fas fa-minus text-[8px] md:text-[10px]"></i>
                            </button>
                            <span class="flex-1 h-full flex items-center justify-center text-[10px] md:text-xs font-bold text-gray-900 cursor-default select-none">${item.quantity}</span>
                            <button class="h-full w-7 md:w-8 flex items-center justify-center hover:bg-gray-50 text-gray-600 transition-colors qty-btn increase-qty border-l border-gray-200" data-item-id="${item.cartId}" ${isMaxStock ? 'disabled' : ''}>
                                <i class="fas fa-plus text-[8px] md:text-[10px]"></i>
                            </button>
                        </div>
                        
                        <button class="c-button c-button--gooey c-button--black c-button--icon remove-item" data-item-id="${item.cartId}" aria-label="Remove Item">
                            <i class="fas fa-trash text-xs"></i>
                            <div class="c-button__blobs"><div></div><div></div><div></div></div>
                        </button>
                    </div>
                </div>
            </div>`;
        }).join('');
    }

    async function handleUpdateQty(cartId, action) {
        if (!await checkUserSession()) return;
        const formData = new FormData();
        formData.append('cartId', cartId);
        formData.append('action', action);
        const data = await apiPostRequest('/api/update-cart-quantity', formData);
        if (data?.success) await loadCartData();
    }

    async function handleRemoveItem(cartId) {
        if (!await checkUserSession()) return;
        const formData = new FormData();
        formData.append('cartId', cartId);
        const data = await apiPostRequest('/api/remove-from-cart', formData);

        if (data?.success) {
            notify.success(data.message || 'Item removed.');
            await loadCartData();
        }
    }

    async function handleClearCart() {
        if (!await checkUserSession()) return;
        const btn = getEl('clear-cart-btn');
        if (btn && btn.disabled) return;

        notify.confirm('Remove all items from cart?', async () => {
            const data = await apiPostRequest('/api/clear-cart', null);
            if (data?.success) {
                notify.success(data.message);
                await loadCartData();
            }
        });
    }

    function bindGlobalListeners() {
        document.addEventListener('click', (e) => {
            const target = e.target;

            if (target.closest('#cart-icon, #mobile-cart-icon')) {
                e.preventDefault();
                openCartPanel();
                const mobileMenu = getEl('mobile-menu');
                if (mobileMenu && !mobileMenu.classList.contains('invisible')) {
                    const closeBtn = getEl('close-mobile-menu');
                    if (closeBtn) closeBtn.click();
                }
                return;
            }

            if (target.closest('#close-cart-panel')) {
                closeCartPanelFunc();
                return;
            }

            if (target.closest('#continue-shopping')) {
                closeCartPanelFunc();
                window.location.href = 'shop.html';
                return;
            }

            const btn = target.closest('button');
            if (btn && getEl('cart-items-container')?.contains(btn)) {
                const cartId = btn.dataset.itemId;
                if (!cartId) return;

                if (btn.classList.contains('increase-qty')) {
                    handleUpdateQty(cartId, 'increase');
                } else if (btn.classList.contains('decrease-qty')) {
                    handleUpdateQty(cartId, 'decrease');
                } else if (btn.classList.contains('remove-item')) {
                    handleRemoveItem(cartId);
                }
                return;
            }

            if (target.closest('#clear-cart-btn')) {
                const clearBtn = target.closest('#clear-cart-btn');
                if (!clearBtn.disabled) handleClearCart();
                return;
            }

            if (target.closest('#checkout-btn')) {
                const checkoutBtn = target.closest('#checkout-btn');
                if (!checkoutBtn.disabled) window.location.href = 'checkout.html';
                return;
            }
        });
    }

    window.enhancedAddToCart = async (itemId, name, price, image, quantity, isCollection) => {
        if (!await checkUserSession()) return;

        const formData = new FormData();
        formData.append(isCollection ? 'collectionId' : 'varianceId', itemId);
        formData.append('qty', quantity);

        const data = await apiPostRequest('/api/add-to-cart', formData);
        if (data?.success) {
            notify.success(data.message || 'Added to cart!');
            await loadCartData();
        }
    };

    window.openCartPanel = openCartPanel;
    window.closeCartPanel = closeCartPanelFunc;

    bindGlobalListeners();
    initializeCart();
}

document.addEventListener('DOMContentLoaded', initCartController);
document.addEventListener('navbar-loaded', initCartController);
document.addEventListener('nav-component-loaded', initCartController);