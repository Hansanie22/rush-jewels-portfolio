// Import the redirect utility
import { redirectToLogin } from './auth-redirect.js';

/**
 * NAVBAR CONTROLLER
 * Handles loading navbar, mobile menu interactions, and dynamic auth UI.
 */

const NAVBAR_PATH = '/components/nav.html';
const NAVBAR_CONTAINER_ID = 'navbar-container';
const NAVIGATION_API = "/api/navigation/init";

let navbarInitialized = false;
let navbarLoaded = false;
let isMenuOpen = false;

document.addEventListener("DOMContentLoaded", async () => {
    if (navbarInitialized) return;
    navbarInitialized = true;

    const existingNav = document.querySelector('nav');
    if (existingNav && !document.getElementById(NAVBAR_CONTAINER_ID)) {
        await initializeMobileMenu();
        await fetchNavigationData();
        updateMobileAuthUI(); // Check auth state on init
        setupAccountInterceptor(); // Init interceptor for existing nav
        return;
    }

    await loadNavbar();
    await initializeMobileMenu();
    await fetchNavigationData();
    updateMobileAuthUI(); // Check auth state after load
});

// --- PART 1: LOAD NAVBAR HTML ---
async function loadNavbar() {
    if (navbarLoaded) return;
    const container = document.getElementById(NAVBAR_CONTAINER_ID);
    if (!container) return;

    try {
        const response = await fetch(NAVBAR_PATH);
        if (!response.ok) throw new Error(`Status: ${response.status}`);
        const html = await response.text();
        container.innerHTML = html;
        navbarLoaded = true;

        // Initialize the interceptor after HTML is injected
        setupAccountInterceptor();

        document.dispatchEvent(new Event('navbar-loaded'));
    } catch (error) {
        console.error('❌ Error loading navbar:', error);
    }
}

// --- NEW PART: ACCOUNT LINK INTERCEPTOR ---
function setupAccountInterceptor() {
    const accountLink = document.getElementById('account-link');
    if (!accountLink) return;

    // Remove old listeners (if any) by cloning node, or just add logic to prevent default
    // Using a simple event listener approach
    accountLink.addEventListener('click', (e) => {
        // Standard local storage check
        const user = localStorage.getItem('user') || localStorage.getItem('currentUser') || sessionStorage.getItem('user');

        if (!user) {
            e.preventDefault(); // Stop navigation to account.html

            // Use the provided redirect utility
            redirectToLogin({
                message: 'Please log in to access your account.',
                stateFlags: { openProfileAfterLogin: true },
                delay: 100
            });
        }
        // If user exists, let the default link behavior (href="account.html") proceed
    });
}

// --- PART 2: MOBILE MENU ANIMATION & LOGIC ---
async function initializeMobileMenu() {
    await new Promise(resolve => setTimeout(resolve, 100));

    const mobileMenuBtn = document.getElementById('mobile-menu-button');
    const mobileMenu = document.getElementById('mobile-menu');
    const iconBars = document.getElementById('icon-bars');
    const iconClose = document.getElementById('icon-close');

    // Internal elements
    const mobileSearchBtn = document.getElementById('mobile-search-btn');
    const mobileCatBtn = document.getElementById('mobile-cat-btn');
    const mobileCatList = document.getElementById('mobile-category-list');
    const mobileCatIcon = document.getElementById('mobile-cat-icon');

    // Mobile Cart Button
    const mobileHomeCartBtn = document.getElementById('mobile-home-cart-btn');

    // 1. Toggle Menu Function (Sharp Square Logic)
    window.toggleBubbleMenu = function(show) {
        isMenuOpen = show;
        if (!mobileMenu || !mobileMenuBtn) return;

        if (show) {
            updateMobileAuthUI(); // Refresh auth state when opening menu

            const btnRect = mobileMenuBtn.getBoundingClientRect();
            const centerX = btnRect.left + btnRect.width / 2;
            const centerY = btnRect.top + btnRect.height / 2;

            mobileMenu.style.setProperty('--btn-x', `${centerX}px`);
            mobileMenu.style.setProperty('--btn-y', `${centerY}px`);

            mobileMenu.classList.add('menu-open');
            document.body.style.overflow = 'hidden';

            if(iconBars) {
                iconBars.classList.remove('opacity-100', 'rotate-0', 'scale-100');
                iconBars.classList.add('opacity-0', 'rotate-90', 'scale-0');
            }
            if(iconClose) {
                iconClose.classList.remove('opacity-0', '-rotate-90', 'scale-50');
                iconClose.classList.add('opacity-100', 'rotate-0', 'scale-100');
            }

            mobileMenuBtn.classList.remove('text-dark');
            mobileMenuBtn.classList.add('text-white');

            setTimeout(() => {
                const activeBtn = document.querySelector('.pill-btn-active');
                if (activeBtn) movePillTo(activeBtn);
            }, 300);

        } else {
            mobileMenu.classList.remove('menu-open');
            document.body.style.overflow = '';

            if(iconBars) {
                iconBars.classList.remove('opacity-0', 'rotate-90', 'scale-0');
                iconBars.classList.add('opacity-100', 'rotate-0', 'scale-100');
            }
            if(iconClose) {
                iconClose.classList.remove('opacity-100', 'rotate-0', 'scale-100');
                iconClose.classList.add('opacity-0', '-rotate-90', 'scale-50');
            }

            mobileMenuBtn.classList.remove('text-white');
            mobileMenuBtn.classList.add('text-dark');
        }
    };

    // 2. Event Listeners
    if (mobileMenuBtn) {
        mobileMenuBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            window.toggleBubbleMenu(!isMenuOpen);
        });
    }

    if (mobileCatBtn && mobileCatList) {
        mobileCatBtn.addEventListener('click', () => {
            mobileCatList.classList.toggle('hidden');
            if (mobileCatIcon) {
                mobileCatIcon.style.transform = mobileCatList.classList.contains('hidden') ? 'rotate(0deg)' : 'rotate(180deg)';
            }
        });
    }

    if (mobileSearchBtn) {
        mobileSearchBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            window.toggleBubbleMenu(false);
            setTimeout(() => {
                if (typeof window.openSearchPanel === 'function') {
                    window.openSearchPanel();
                } else {
                    const desktopSearch = document.getElementById('search-btn');
                    if(desktopSearch) desktopSearch.click();
                }
            }, 400);
        });
    }

    if (mobileHomeCartBtn) {
        mobileHomeCartBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            window.toggleBubbleMenu(false);
            setTimeout(() => {
                const desktopCartIcon = document.getElementById('cart-icon');
                if (desktopCartIcon) {
                    desktopCartIcon.click();
                }
            }, 400);
        });
    }

    setupPillToggle();
}

// --- PART 3: PILL TOGGLE LOGIC ---
function setupPillToggle() {
    const toggleButtons = document.querySelectorAll('.toggle-pill-btn');
    const pillBg = document.getElementById('pill-bg');

    window.movePillTo = function(btn) {
        if (!btn || !pillBg) return;
        const left = btn.offsetLeft;
        const width = btn.offsetWidth;
        pillBg.style.transform = `translateX(${left - 6}px)`;
        pillBg.style.width = `${width}px`;
    }

    toggleButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            toggleButtons.forEach(b => {
                b.classList.remove('pill-btn-active');
                b.classList.add('pill-btn-inactive');
                const icon = b.querySelector('i');
                if(icon) {
                    icon.classList.remove('text-white');
                    icon.classList.add('text-gray-600');
                }
            });

            this.classList.remove('pill-btn-inactive');
            this.classList.add('pill-btn-active');
            const activeIcon = this.querySelector('i');
            if(activeIcon) {
                activeIcon.classList.remove('text-gray-600');
                activeIcon.classList.add('text-white');
            }

            movePillTo(this);

            const target = this.getAttribute('data-target');
            ['home', 'shop', 'profile'].forEach(sec => {
                const el = document.getElementById(`section-${sec}`);
                if(el) {
                    el.classList.add('hidden');
                    el.classList.remove('flex');
                }
            });
            const activeSection = document.getElementById(`section-${target}`);
            if(activeSection) {
                activeSection.classList.remove('hidden');
                activeSection.classList.add('flex');
            }
        });
    });
}

// --- PART 4: FETCH NAVIGATION DATA ---
async function fetchNavigationData() {
    const fallbackData = {
        categories: ["Rings", "Necklaces", "Earrings", "Bracelets", "Sets", "Pendants"],
    };

    try {
        let data = fallbackData;
        try {
            const response = await fetch(NAVIGATION_API);
            if (response.ok) data = await response.json();
        } catch (e) { console.warn('Using fallback nav data'); }

        const navCategoriesContainer = document.getElementById('nav-categories-container');
        const mobileCategoryList = document.getElementById('mobile-category-list');

        renderCategories(data.categories, navCategoriesContainer, mobileCategoryList);

    } catch (error) {
        console.error("Nav error:", error);
    }
}

// --- PART 5: RENDER CATEGORIES ---
function renderCategories(categories, desktopContainer, mobileContainer) {
    if (!categories || !Array.isArray(categories)) return;

    const getIcon = (catName) => {
        const lower = catName.toLowerCase();
        if (lower.includes('ring')) return 'fa-ring';
        if (lower.includes('neck')) return 'fa-gem';
        if (lower.includes('ear')) return 'fa-circle';
        return 'fa-gem';
    };

    if (desktopContainer) {
        desktopContainer.innerHTML = '';
        categories.forEach(cat => {
            const link = document.createElement('a');
            link.href = `shop.html?category=${encodeURIComponent(cat)}`;
            // ADDED: cute-dropdown-item class
            link.className = "cute-dropdown-item block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 hover:text-gold transition-colors flex items-center";
            link.innerHTML = `<i class="fas ${getIcon(cat)} text-gold mr-2 w-5 text-center"></i>${cat}`;
            desktopContainer.appendChild(link);
        });
    }

    if (mobileContainer) {
        mobileContainer.innerHTML = '';
        categories.forEach(cat => {
            const link = document.createElement('a');
            link.href = `shop.html?category=${encodeURIComponent(cat)}`;
            // ADDED: cute-mobile-item class (reusing animation logic)
            link.className = "cute-mobile-item block text-white/80 hover:text-gold transition-colors py-2 flex items-center";
            link.innerHTML = `<i class="fas ${getIcon(cat)} text-gold mr-3 w-5 text-center"></i>${cat}`;
            mobileContainer.appendChild(link);
        });
    }
}

// --- PART 6: AUTH UI LOGIC (ENHANCED FOR OAUTH2) ---
async function updateMobileAuthUI() {
    const authSection = document.getElementById('mobile-auth-section');
    if (!authSection) return;

    // First check local/session storage (fast, for regular login)
    let user = localStorage.getItem('user') || sessionStorage.getItem('user');
    let isLoggedIn = !!user;

    // If not found in storage, check server session (for OAuth2 login)
    if (!isLoggedIn) {
        try {
            const response = await fetch('/api/auth/session-check', {
                credentials: 'include',
                headers: {
                    'Accept': 'application/json'
                }
            });

            // Handle 404 gracefully
            if (response.status === 404) {
                console.error('❌ /api/auth/session-check endpoint not found');
                isLoggedIn = false;
            } else if (response.ok) {
                const data = await response.json();
                isLoggedIn = data.loggedIn;

                // If server says logged in, fetch and cache user data
                if (isLoggedIn && !user) {
                    const userResponse = await fetch('/api/auth/me', {
                        credentials: 'include',
                        headers: {
                            'Accept': 'application/json'
                        }
                    });

                    if (userResponse.status === 404) {
                        console.error('❌ /api/auth/me endpoint not found');
                    } else if (userResponse.ok) {
                        const userData = await userResponse.json();
                        if (userData.status && userData.user) {
                            sessionStorage.setItem('user', JSON.stringify(userData.user));
                            user = JSON.stringify(userData.user);
                        }
                    }
                }
            }
        } catch (error) {
            console.error('❌ Error checking auth state:', error.message);
            isLoggedIn = false;
        }
    }

    if (isLoggedIn) {
        // User is Logged In -> Show Go to Profile
        authSection.innerHTML = `
            <div class="text-center w-full">
                     <i class="far fa-user-circle text-6xl text-gold/50 mb-6"></i>
                <h3 class="text-white font-playfair text-xl mb-6">Welcome Back</h3>
                <a href="account.html" class="block w-full max-w-xs mx-auto bg-white text-dark py-3 font-bold uppercase tracking-widest hover:bg-gold hover:text-white transition-colors">
                    Go to Profile
                </a>
            </div>
        `;
    } else {
        // User is Logged Out -> Show Log In / Create Account
        // Uses hash #login and #register to trigger specific tabs in auth.html
        authSection.innerHTML = `
            <div class="text-center w-full">
                <i class="far fa-user-circle text-6xl text-gold/50 mb-6"></i>
                <a href="auth.html#login" class="block w-full max-w-xs mx-auto bg-gold text-dark py-3 font-bold uppercase tracking-widest hover:bg-white transition-colors mb-4">
                    Log In
                </a>
                <a href="auth.html#register" class="block text-white hover:text-gold uppercase tracking-widest text-sm">
                    Create Account
                </a>
            </div>
        `;
    }
}

// Export function for external use (OAuth2 success handler)
if (typeof window !== 'undefined') {
    window.updateMobileAuthUI = updateMobileAuthUI;
}

if (typeof window !== 'undefined') {
    window.navbarController = {
        loadNavbar,
        initializeMobileMenu,
        fetchNavigationData
    };
}