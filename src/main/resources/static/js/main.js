/**
 * Loads a component HTML into a div and executes scripts
 * @param {string} componentPath - Path to html file
 * @param {string} targetId - ID of container div
 */
async function loadComponent(componentPath, targetId) {
    try {
        const response = await fetch(componentPath);
        if (!response.ok) throw new Error(`Failed to fetch ${componentPath}`);
        const html = await response.text();

        const container = document.getElementById(targetId);
        if (!container) return;

        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');

        // 1. Inject Styles
        doc.querySelectorAll('style').forEach(style => {
            document.head.appendChild(style.cloneNode(true));
        });

        // 2. Inject Content
        const content = doc.body.firstElementChild;
        if (content) {
            container.replaceWith(content);

            // 3. Execute Scripts found in the component
            const script = doc.querySelector('script');
            if (script) {
                const newScript = document.createElement('script');
                newScript.textContent = script.textContent;
                document.body.appendChild(newScript);
            }
        }
    } catch (error) {
        console.error(`Error loading ${componentPath}:`, error);
    }
}

/**
 * Checks for login state flags in sessionStorage
 * This runs AFTER components (like Footer/HelpDesk) are loaded.
 */
function checkLoginStateFlags() {
    // 1. Help Desk Restoration
    const shouldOpenHelpDesk = sessionStorage.getItem('openHelpDeskAfterLogin');

    if (shouldOpenHelpDesk === 'true') {
        // Clear flag so it doesn't open on every refresh
        sessionStorage.removeItem('openHelpDeskAfterLogin');

        // Small delay to ensure the help-desk.js script has initialized
        setTimeout(() => {
            if (typeof window.openHelpDesk === 'function') {
                window.openHelpDesk();
            } else {
                console.warn("openHelpDesk function not found. Ensure help-desk.js is loaded.");
            }
        }, 500);
    }

    // 2. Cart Restoration
    const shouldOpenCart = sessionStorage.getItem('openCartAfterLogin');
    if (shouldOpenCart === 'true') {
        sessionStorage.removeItem('openCartAfterLogin');
        setTimeout(() => {
            // Try clicking the icon first (best for UI consistency)
            const cartIcon = document.getElementById('cart-icon');
            if (cartIcon) {
                cartIcon.click();
            } else if (typeof window.openCartPanel === 'function') {
                window.openCartPanel();
            }
        }, 500);
    }
}


document.addEventListener('DOMContentLoaded', async function () {

    // (Components are now loaded globally by js/components.js to avoid duplicate loads and missing API calls)


    // --- 2. Check Flags ---
    // Now that HTML is injected, we can safely check if we need to open modals
    checkLoginStateFlags();


    // --- 3. Initialize Standard UI Events ---

    // Mobile Menu Toggle
    const mobileMenuButton = document.getElementById('mobile-menu-button');
    const mobileMenu = document.getElementById('mobile-menu');
    if (mobileMenuButton && mobileMenu) {
        mobileMenuButton.addEventListener('click', () => {
            mobileMenu.classList.toggle('hidden');
        });
    }

    // Product Thumbnails (Gallery)
    const thumbnails = document.querySelectorAll('.thumbnail');
    const mainImage = document.getElementById('main-image');
    if (mainImage) {
        thumbnails.forEach(t => {
            t.addEventListener('click', function () {
                document.querySelectorAll('.thumbnail').forEach(th => {
                    th.parentElement.classList.remove('border-gold');
                    th.parentElement.classList.add('border-transparent');
                });
                this.parentElement.classList.remove('border-transparent');
                this.parentElement.classList.add('border-gold');
                mainImage.src = this.src;
            });
        });
    }

    // Quantity Selectors (Global)
    document.querySelectorAll('.increase-qty').forEach((btn, i) => {
        btn.addEventListener('click', () => {
            const inputs = document.querySelectorAll('.qty-input');
            if(inputs[i]) {
                inputs[i].value = parseInt(inputs[i].value) + 1;
                inputs[i].dispatchEvent(new Event('change'));
            }
        });
    });

    document.querySelectorAll('.decrease-qty').forEach((btn, i) => {
        btn.addEventListener('click', () => {
            const inputs = document.querySelectorAll('.qty-input');
            if(inputs[i] && parseInt(inputs[i].value) > 1) {
                inputs[i].value = parseInt(inputs[i].value) - 1;
                inputs[i].dispatchEvent(new Event('change'));
            }
        });
    });

    // Tab Navigation
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    tabButtons.forEach(button => {
        button.addEventListener('click', function () {
            const tabId = this.getAttribute('data-tab');

            // Reset state
            tabButtons.forEach(btn => {
                btn.classList.remove('border-gold', 'text-gold');
                btn.classList.add('border-transparent', 'text-gray-500');
            });
            tabContents.forEach(c => c.classList.add('hidden'));

            // Activate clicked
            this.classList.remove('border-transparent', 'text-gray-500');
            this.classList.add('border-gold', 'text-gold');

            const target = document.getElementById(tabId);
            if(target) target.classList.remove('hidden');
        });
    });

    // Password Visibility Toggle
    document.querySelectorAll('.toggle-password').forEach(btn => {
        btn.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            const input = document.getElementById(targetId);
            if(input) {
                if (input.type === 'password') {
                    input.type = 'text';
                    this.innerHTML = '<i class="fas fa-eye-slash"></i>';
                } else {
                    input.type = 'password';
                    this.innerHTML = '<i class="fas fa-eye"></i>';
                }
            }
        });
    });
});

// --- Global Helpers ---

/**
 * Updates the file name display in Help Desk file input
 * Called via onchange="updateFileName(this)" in HTML
 */
window.updateFileName = function(input) {
    const display = document.getElementById('file-name-display');
    if (display) {
        if (input.files && input.files.length > 0) {
            display.textContent = input.files[0].name;
            display.classList.add('text-gold'); // Optional: highlight color
        } else {
            display.textContent = 'Upload a photo...';
            display.classList.remove('text-gold');
        }
    }
};

// --- Dynamic Public Integrations ---
async function initializePublicIntegrations() {
    // Don't show on admin/POS pages
    const path = window.location.pathname.toLowerCase();
    if (path.includes('admin') || path.includes('pos')) return;

    try {
        const response = await fetch('/api/public/integrations/active');
        if (!response.ok) return;
        
        const integrations = await response.json();
        
        integrations.forEach(integration => {
            const name = integration.name.toLowerCase();
            const key = integration.apiKey;
            
            if (!key) return; // Skip if no tracking ID/key provided

            if (name.includes('google')) {
                injectGoogleAnalytics(key);
            } else if (name.includes('meta') || name.includes('facebook')) {
                injectMetaPixel(key);
            } else if (name.includes('whatsapp')) {
                injectWhatsAppWidget(key);
            }
        });
    } catch (e) {
        console.error("Failed to load integrations", e);
    }
}

function injectGoogleAnalytics(measurementId) {
    if (document.getElementById('ga-script')) return;

    const script = document.createElement('script');
    script.async = true;
    script.src = `https://www.googletagmanager.com/gtag/js?id=${measurementId}`;
    script.id = 'ga-script';
    document.head.appendChild(script);

    const inlineScript = document.createElement('script');
    inlineScript.innerHTML = `
        window.dataLayer = window.dataLayer || [];
        function gtag(){dataLayer.push(arguments);}
        gtag('js', new Date());
        gtag('config', '${measurementId}');
    `;
    document.head.appendChild(inlineScript);
    console.log("✅ Google Analytics injected");
}

function injectMetaPixel(pixelId) {
    if (document.getElementById('meta-pixel-script')) return;

    const script = document.createElement('script');
    script.id = 'meta-pixel-script';
    script.innerHTML = `
        !function(f,b,e,v,n,t,s)
        {if(f.fbq)return;n=f.fbq=function(){n.callMethod?
        n.callMethod.apply(n,arguments):n.queue.push(arguments)};
        if(!f._fbq)f._fbq=n;n.push=n;n.loaded=!0;n.version='2.0';
        n.queue=[];t=b.createElement(e);t.async=!0;
        t.src=v;s=b.getElementsByTagName(e)[0];
        s.parentNode.insertBefore(t,s)}(window, document,'script',
        'https://connect.facebook.net/en_US/fbevents.js');
        fbq('init', '${pixelId}');
        fbq('track', 'PageView');
    `;
    document.head.appendChild(script);
    console.log("✅ Meta Pixel injected");
}

function injectWhatsAppWidget(phoneNumber) {
    if (document.getElementById('wa-floating-btn')) return;

    const cleanNumber = phoneNumber.replace(/[^0-9]/g, '');

    const waWidget = document.createElement('a');
    waWidget.id = "wa-floating-btn";
    waWidget.href = `https://wa.me/${cleanNumber}`;
    waWidget.target = "_blank";
    waWidget.rel = "noopener noreferrer";
    waWidget.setAttribute('aria-label', 'Chat with us on WhatsApp');
    waWidget.className = "fixed bottom-24 right-6 bg-[#25D366] text-white rounded-full w-14 h-14 flex items-center justify-center shadow-lg hover:bg-[#128C7E] transition-transform duration-300 hover:scale-110 z-50 animate__animated animate__bounceIn";
    waWidget.innerHTML = '<i class="fab fa-whatsapp text-3xl"></i>';
    document.body.appendChild(waWidget);
    console.log("✅ WhatsApp Widget injected");
}

// Call on load
document.addEventListener("DOMContentLoaded", () => {
    initializePublicIntegrations();
});