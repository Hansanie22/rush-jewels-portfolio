import Notification from './notification.js';

// Initialize global notification instance
const notify = Notification({
    position: 'bottom-right',
    duration: 3000,
    hidePrevious: true,
    maxVisible: 5,
});

// ✅ යාවත්කාලීන කළා: Master Loader Logic එක ඇතුළත් කරමින්
document.addEventListener('DOMContentLoaded', async () => {

    // 1. පිටුව පටන් ගත් සැණින් ලෝඩරය පෙන්වන්න
    if (window.loader) window.loader.show();

    try {
        // 2. සියලුම දත්ත එකවර ගෙන්වීම (Wait for all critical data)
        await Promise.all([
            checkOAuth2Success(),
            checkNewsletterVisibility(),
            fetchStorefrontData(),
            // Hero Carousel එක ලෝඩ් වන තෙක් බලා සිටීම
            typeof window.initHeroCarousel === 'function' ? window.initHeroCarousel() : Promise.resolve()
        ]);

        // Newsletter form එක setup කිරීම
        setupNewsletterForm();

    } catch (error) {
        console.error("Home Data Loading Error:", error);
    } finally {
        // 3. ✅ සියල්ල අවසන් වූ පසු පිටුව පෙන්වා ලෝඩරය අයින් කිරීම
        revealHomePage();
    }
});

// පිටුව පෙන්වන Helper Function එක
function revealHomePage() {
    const main = document.getElementById('main-content');
    if (main) {
        main.style.display = 'block';
        main.classList.add('animate__animated', 'animate__fadeIn');
    }
    if (window.loader) {
        // තත්පර 0.5ක සුළු ප්‍රමාදයක් තැබීමෙන් පින්තූර සහ Carousel එක හරියට render වීමට කාලය ලැබේ
        setTimeout(() => {
            window.loader.hide();
        }, 500);
    }
}

// =======================================================
// OAUTH2 SUCCESS CHECK
// =======================================================
async function checkOAuth2Success() {
    const urlParams = new URLSearchParams(window.location.search);
    const oauth = urlParams.get('oauth');

    if (oauth === 'success') {
        window.history.replaceState({}, document.title, window.location.pathname);
        try {
            const response = await fetch('/api/auth/me', {
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            });

            if (response.ok) {
                const data = await response.json();
                if (data.status && data.user) {
                    sessionStorage.setItem('user', JSON.stringify(data.user));
                    notify.success(`Welcome! You're now logged in via ${data.user.loginProvider}`);
                    updateNavigationForLoggedInUser();
                    if (typeof window.updateMobileAuthUI === 'function') {
                        window.updateMobileAuthUI();
                    }
                }
            }
        } catch (error) {
            console.error('❌ Error verifying OAuth2 login:', error.message);
        }
    }
    return true; // Promise එක resolve කිරීමට
}

// =======================================================
// UPDATE NAVIGATION FOR LOGGED IN USER
// =======================================================
function updateNavigationForLoggedInUser() {
    const accountLinks = document.querySelectorAll('a[href*="auth.html"], a[href*="account"]');
    accountLinks.forEach(link => {
        if (link.textContent.includes('Sign In') || link.textContent.includes('Login')) {
            link.href = 'account.html';
            link.textContent = 'Account';
        } else if (link.href.includes('auth.html')) {
            link.href = 'account.html';
        }
    });
}

// =======================================================
// NEWSLETTER VISIBILITY CHECK
// =======================================================
async function checkNewsletterVisibility() {
    const section = document.getElementById("newsletter-section");
    if (!section) return;

    try {
        const sessionRes = await fetch('/api/auth/session-check', { credentials: 'include' });
        if (!sessionRes.ok) {
            section.classList.remove("hidden");
            return;
        }

        const sessionData = await sessionRes.json();
        if (!sessionData.loggedIn) {
            section.classList.remove("hidden");
            return;
        }

        const userRes = await fetch('/api/auth/me', { credentials: 'include' });
        if (userRes.ok) {
            const userData = await userRes.json();
            if (userData.status && userData.user) {
                const user = userData.user;
                if (user.subscribed === true) section.classList.add("hidden");
                else section.classList.remove("hidden");

                const emailInput = document.getElementById("newsletter-email");
                if (emailInput && user.email) emailInput.value = user.email;
            }
        }
    } catch (e) {
        section.classList.remove("hidden");
    }
    return true;
}

// =======================================================
// NEWSLETTER FORM SUBMIT
// =======================================================
function setupNewsletterForm() {
    const form = document.getElementById("newsletter-form");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const emailInput = document.getElementById("newsletter-email");
        const email = emailInput?.value?.trim();

        if (!email) {
            notify.warning("Please enter your email.");
            return;
        }

        try {
            const res = await fetch('/api/auth/subscribe', {
                method: "POST",
                credentials: "include",
                headers: { "Accept": "application/json", "Content-Type": "application/json" },
                body: JSON.stringify({ email })
            });

            if (res.status === 401) {
                notify.warning("Please login to subscribe.");
                setTimeout(() => { window.location.href = 'auth.html'; }, 1500);
                return;
            }

            const data = await res.json();
            if (data.status) {
                notify.success("Subscribed successfully!");
                document.getElementById("newsletter-section").classList.add("hidden");
            } else {
                notify.error(data.message || "Subscription failed.");
            }
        } catch (err) {
            notify.error("Something went wrong.");
        }
    });
}

// =======================================================
// LOAD STOREFRONT DATA
// =======================================================
// ✅ යාවත්කාලීන කළා: window object එකට සම්බන්ධ කළා
window.fetchStorefrontData = async function() {
    try {
        const response = await fetch('/api/v1/storefront/home-content', {
            method: "GET",
            credentials: "include",
            headers: { "Accept": "application/json" }
        });

        if (!response.ok) throw new Error('Failed to load storefront data');

        const data = await response.json();

        // Helper function to group products by productId
        const groupProducts = (rawProducts) => {
            if (!rawProducts) return [];
            const productMap = new Map();
            rawProducts.forEach(p => {
                if (!productMap.has(p.productId)) {
                    productMap.set(p.productId, {
                        ...p,
                        variances: [],
                        minPrice: p.price,
                        maxPrice: p.price
                    });
                }
                const grouped = productMap.get(p.productId);
                grouped.variances.push(p);
                if (p.price < grouped.minPrice) grouped.minPrice = p.price;
                if (p.price > grouped.maxPrice) grouped.maxPrice = p.price;
                grouped.currentStockQty += p.currentStockQty;
                if (p.currentStockQty > 0) grouped.stockStatus = "In Stock";
            });
            return Array.from(productMap.values());
        };

        const groupedFeatured = groupProducts(data.featuredProducts);
        const groupedNew = groupProducts(data.newArrivals);

        // නිෂ්පාදන Render කිරීම
        renderSection('featured-products-wrapper', groupedFeatured, 'featured');
        renderSection('new-arrivals-wrapper', groupedNew, 'new-arrival');

        // Cart Functionality සම්බන්ධ කිරීම
        if (typeof window.attachCartFunctionality === 'function') {
            window.attachCartFunctionality();
        }

        return true;
    } catch (error) {
        console.error('Error loading storefront data:', error);
        return false;
    }
};

// ⚠️ Fallback Initialization
document.addEventListener('DOMContentLoaded', () => {
    // Master Script එක HTML එකේ නැතිනම් පමණක් මෙය ක්‍රියාත්මක වේ
    const isMasterScriptPresent = window.loader && document.getElementById('main-content');
    if (!isMasterScriptPresent) {
        window.fetchStorefrontData();
        checkOAuth2Success();
        checkNewsletterVisibility();
        setupNewsletterForm();
    }
});
// aliases for backward compatibility if needed
const fetchStorefrontData = window.fetchStorefrontData;

// =======================================================
// RENDER SECTIONS
// =======================================================
function renderSection(containerId, products, styleType) {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = '';

    if (!products || products.length === 0) {
        container.innerHTML = '<div class="w-full text-center py-8 text-gray-500">Coming Soon</div>';
        return;
    }

    products.forEach(product => {
        const html = buildFeaturedCardHtml(product);
        container.insertAdjacentHTML('beforeend', html);
    });
}

// =======================================================
// UTILITIES (Prices, Badges, Ratings - DESIGN PRESERVED)
// =======================================================
function getProductName(product) {
    return product.productName || product.name || product.title || 'Product';
}

function formatPrice(price) {
    return new Intl.NumberFormat('en-LK', {
        style: 'currency', currency: 'LKR', minimumFractionDigits: 0
    }).format(price);
}

function getProductState(product) {
    const isNew = product.tags?.includes('New Arrival');
    const isOut = product.stockStatus === 'Out Of Stock' || product.stockStatus === 'Out of Stock';
    let badgeHtml = '';
    if (isOut) badgeHtml = '<span class="badge bg-dark text-white text-xs font-bold px-3 py-1 uppercase">SOLD OUT</span>';
    else if (product.discountPercentage > 0) badgeHtml = `<span class="badge bg-red-600 text-white text-xs font-bold px-3 py-1 uppercase">-${product.discountPercentage}%</span>`;
    else if (isNew) badgeHtml = '<span class="badge bg-gold text-dark text-xs font-bold px-3 py-1 uppercase">New</span>';

    const btnState = isOut ? 'disabled' : '';
    const btnText = isOut ? 'Sold Out' : 'Add to Cart';
    let stockStatusHtml = isOut ? `<i class="fas fa-circle text-red-500" style="font-size: 6px;"></i><span>Out of stock</span>` : (product.currentStockQty < 5 && product.currentStockQty > 0 ? `<i class="fas fa-circle text-orange-500" style="font-size: 6px;"></i><span>Only ${product.currentStockQty} left in stock</span>` : `<i class="fas fa-circle text-green-500" style="font-size: 6px;"></i><span>In stock & ready to ship</span>`);

    return { badgeHtml, btnState, btnText, stockStatusHtml };
}

function generateRatingHtml(rating, count) {
    const avgRating = parseFloat(rating) || 0;
    const reviewCount = parseInt(count) || 0;
    let starsHtml = '<div class="stars">';
    for (let i = 1; i <= 5; i++) {
        if (avgRating >= i) starsHtml += '<i class="fas fa-star star"></i>';
        else if (avgRating >= i - 0.5) starsHtml += '<i class="fas fa-star-half-alt star"></i>';
        else starsHtml += '<i class="far fa-star star" style="color:#d1d5db;"></i>';
    }
    starsHtml += '</div>';
    const countHtml = reviewCount > 0 ? `<span class="review-count">(${reviewCount})</span>` : `<span class="review-count text-xs" style="margin-left:5px;"></span>`;
    return `<div class="product-rating">${starsHtml}${countHtml}</div>`;
}

function buildFeaturedCardHtml(product) {
    let formattedPrice = '';
    if (product.minPrice && product.maxPrice && product.minPrice !== product.maxPrice) {
        formattedPrice = `LKR ${product.minPrice.toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')} - LKR ${product.maxPrice.toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')}`;
    } else {
        formattedPrice = formatPrice(product.price);
    }

    const { badgeHtml, btnState, stockStatusHtml } = getProductState(product);
    const imgUrl = product.image || 'https://placehold.co/600x600?text=No+Image';
    const productName = getProductName(product);
    const displayTitle = product.title || productName;
    const ratingHtml = generateRatingHtml(product.averageRating, product.reviewCount);

    const isGrouped = product.variances && product.variances.length > 1;
    const finalBtnText = btnState === 'disabled' ? 'Sold Out' : (isGrouped ? 'Select Options' : 'Add to Cart');
    const iconHtml = btnState === 'disabled' ? '<i class="fas fa-lock mr-2"></i>' : (isGrouped ? '<i class="fas fa-list-ul mr-2"></i>' : '<i class="fas fa-shopping-bag mr-2"></i>');

    let actionButtonHtml = '';
    if (isGrouped || btnState === 'disabled') {
        actionButtonHtml = `<button class="add-to-cart-btn ${btnState === 'disabled' ? 'opacity-50 cursor-not-allowed' : ''}" onclick="event.stopPropagation(); window.location.href='product-detail.html?id=${product.varianceId}'" ${btnState}>
            ${iconHtml} ${finalBtnText}
        </button>`;
    } else {
        actionButtonHtml = `<button class="add-to-cart-btn" onclick="event.stopPropagation()" data-product-id="${product.varianceId}" data-product-name="${productName}" data-product-price="${product.price}" data-product-image="${imgUrl}" data-stock-status="${product.stockStatus}" data-stock-qty="${product.currentStockQty}">
            <i class="fas fa-shopping-bag mr-2"></i> Add to Cart
        </button>`;
    }

    return `
    <div class="flex-none w-64 sm:w-72 product-card shadow-sm group" onclick="window.location.href='product-detail.html?id=${product.varianceId}'">
        ${badgeHtml}
        <div class="product-image h-64 relative overflow-hidden">
            <img src="${imgUrl}" alt="${productName}" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" loading="lazy">
            <button class="view-product-btn" onclick="event.stopPropagation(); window.location.href='product-detail.html?id=${product.varianceId}'">View Product</button>
        </div>
        <div class="product-info p-3">
            <p class="text-[10px] md:text-xs text-gray-400 uppercase tracking-wider font-semibold mb-1">VELORA FINE JEWELLERY</p>
            <h3 class="product-title line-clamp-2 text-sm md:text-base font-medium mb-1" title="${displayTitle}">${displayTitle}</h3>
            <div class="mb-1">${ratingHtml}</div>
            <p class="product-price text-sm md:text-lg font-bold text-gray-900 mb-2">${formattedPrice}</p>
            <div class="product-availability mb-3 text-[10px] md:text-xs">${stockStatusHtml}</div>
            ${actionButtonHtml}
        </div>
    </div>`;
}