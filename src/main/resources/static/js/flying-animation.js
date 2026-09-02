/**
 * file: js/flying-animation.js
 * Description: Floating Cart Animation
 * FIXED: Restored original logic for existing pages to fix "flow upset",
 * while adding simple animation support for the new product page.
 */

(function() {
    // 1. Inject Animation CSS
    function injectAnimationStyles() {
        const styleId = 'flying-cart-styles';
        if (document.getElementById(styleId)) return;

        const styleEl = document.createElement('style');
        styleEl.id = styleId;
        styleEl.textContent = `
            .flying-item {
                position: fixed;
                z-index: 9999999;
                pointer-events: none;
                width: 50px;
                height: 50px;
                border-radius: 4px; 
                display: flex;
                align-items: center;
                justify-content: center;
                background: #111111; 
                border: 1px solid #D4AF37; 
                color: #D4AF37; 
                box-shadow: 0 0 20px rgba(212, 175, 55, 0.5);
                transition: transform 0.6s ease-out, left 1.5s cubic-bezier(0.25, 1, 0.5, 1), top 1.5s cubic-bezier(0.25, 1, 0.5, 1), opacity 0.5s ease;
            }
            .flying-item i { font-size: 22px; }
            .cart-blink { animation: cart-bump 0.5s ease-in-out; }
            @keyframes cart-bump {
                0% { transform: scale(1); color: #111; }
                50% { transform: scale(1.3); color: #D4AF37; }
                100% { transform: scale(1); color: #111; }
            }
        `;
        document.head.appendChild(styleEl);
    }

    // 2. The Animation Function
    function createFloatingCartAnimation(button) {
        const desktopIcon = document.getElementById('cart-icon');
        const mobileIcon = document.getElementById('mobile-cart-icon');

        // Find visible icon
        let cartIcon = null;
        if (desktopIcon && desktopIcon.offsetParent !== null) cartIcon = desktopIcon;
        else if (mobileIcon && mobileIcon.offsetParent !== null) cartIcon = mobileIcon;
        else cartIcon = document.querySelector('.fa-shopping-bag, .fa-shopping-cart');

        if (!cartIcon) return;

        const startRect = button.getBoundingClientRect();
        const targetRect = cartIcon.getBoundingClientRect();

        const flyer = document.createElement('div');
        flyer.className = 'flying-item';
        flyer.innerHTML = '<i class="fas fa-shopping-bag"></i>';
        document.body.appendChild(flyer);

        Object.assign(flyer.style, {
            left: `${startRect.left + startRect.width / 2}px`,
            top: `${startRect.top + startRect.height / 2}px`,
            opacity: '0',
            transform: 'translate(-50%, -50%) scale(0.5)'
        });

        setTimeout(() => {
            Object.assign(flyer.style, {
                opacity: '1',
                transform: 'translate(-50%, -50%) scale(1.3)'
            });
        }, 50);

        setTimeout(() => {
            Object.assign(flyer.style, {
                left: `${targetRect.left + targetRect.width / 2}px`,
                top: `${targetRect.top + targetRect.height / 2}px`,
                transform: 'translate(-50%, -50%) scale(0.2) rotate(180deg)',
                opacity: '0.5'
            });
        }, 850);

        setTimeout(() => {
            if (flyer.parentNode) flyer.parentNode.removeChild(flyer);
            cartIcon.classList.add('cart-blink');
            setTimeout(() => cartIcon.classList.remove('cart-blink'), 500);
        }, 2400);
    }

    // 3. Attach Listeners (Separated Logic)
    function attachCartFunctionality() {

        // --- GROUP A: OLD BUTTONS (Restore Original Logic) ---
        const oldButtons = document.querySelectorAll('.add-to-cart-btn:not([data-animation-attached])');
        oldButtons.forEach(btn => {
            btn.setAttribute('data-animation-attached', 'true');
            btn.addEventListener('click', async (e) => {
                // Keep original preventions for old pages
                e.preventDefault();
                e.stopPropagation();

                createFloatingCartAnimation(btn);

                // --- RESTORED ORIGINAL CART LOGIC ---
                let id = btn.dataset.productId || btn.dataset.collectionId;
                let isCollection = !!btn.dataset.collectionId;
                let name = btn.dataset.productName || '';
                let qty = parseInt(btn.dataset.qty || '1') || 1;
                let imageSrc = btn.dataset.productImage || '';

                // Extract actual price from data attribute
                let price = parseFloat(btn.dataset.productPrice || btn.dataset.price || '0') || 0;

                if (!imageSrc) {
                    const container = btn.closest('.product-card') || btn.closest('.group');
                    if (container) {
                        const img = container.querySelector('img');
                        if (img) imageSrc = img.src;
                        // Try to get name from card title if not set
                        if (!name) {
                            const titleEl = container.querySelector('.product-title, h3, h4');
                            if (titleEl) name = titleEl.textContent.trim();
                        }
                    }
                }

                if (!id) {
                    const urlParams = new URLSearchParams(window.location.search);
                    id = urlParams.get('id');
                }

                if (id && typeof window.enhancedAddToCart === 'function') {
                    await window.enhancedAddToCart(id, name, price, imageSrc, qty, isCollection);
                }
            });
        });

        // --- GROUP B: NEW BUTTONS (Animation Only) ---
        // For the new product page, we ONLY want animation.
        // We let your other scripts (product-details.js) handle the cart logic.
        const newButtons = document.querySelectorAll('[data-add-to-cart]:not([data-animation-attached])');
        newButtons.forEach(btn => {
            btn.setAttribute('data-animation-attached', 'true');
            btn.addEventListener('click', (e) => {
                // Do NOT prevent default here, so product-details.js can run
                createFloatingCartAnimation(btn);
            });
        });
    }

    injectAnimationStyles();
    window.attachFlyingAnimation = attachCartFunctionality;
    window.attachCartFunctionality = attachCartFunctionality; // alias for home-data-loader.js

    document.addEventListener('DOMContentLoaded', attachCartFunctionality);
    document.addEventListener('navbar-loaded', attachCartFunctionality);

    // Safety check loop
    let attempts = 0;
    const interval = setInterval(() => {
        attachCartFunctionality();
        attempts++;
        if (attempts > 5) clearInterval(interval);
    }, 1000);

})();