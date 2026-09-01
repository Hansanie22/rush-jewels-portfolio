import Notification from './notification.js';

const PRODUCTS_API = "/api/products";
const PRODUCTS_PER_PAGE = 12;
const SHEET_ANIMATION_MS = 280;

let allProducts = [];
let filteredProducts = [];
let currentPage = 1;
let currentSort = 'popularity';
let activeSheet = null;
let filterSheetTimeout = null;
let sortSheetTimeout = null;

// Track "Sale Only" state
let showOnlySale = false;

// Initialize notification
const notify = Notification({
    position: 'bottom-right',
    duration: 3000,
    hidePrevious: true,
    maxVisible: 5,
});
window.notify = notify;

function formatPriceLKR(amount) {
    const safeAmount = typeof amount === 'number' && !isNaN(amount)
        ? amount
        : parseFloat(String(amount).replace(/[^0-9.]/g, '')) || 0;
    return "LKR " + safeAmount.toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
}

function getSelectedFilters(type) {
    const selected = [];
    const containerId = type === 'metal' ? 'colorList' : type + 'List';

    const containers = document.querySelectorAll(`[id="${containerId}"]`);
    let container = null;

    for (const c of containers) {
        if (c.querySelector('input[type="checkbox"]')) {
            container = c;
            break;
        }
    }

    if (!container && containers.length > 0) container = containers[0];

    if (container) {
        container.querySelectorAll('input[type="checkbox"]:checked').forEach(cb => {
            selected.push(cb.value.trim().toLowerCase());
        });
    }
    return selected;
}

// -------------------- Mobile Sheet Control --------------------
function disableBodyScroll() {
    document.body.style.overflow = 'hidden';
    document.body.style.touchAction = 'none';
    document.documentElement.style.overflow = 'hidden';
}

function enableBodyScroll() {
    document.body.style.overflow = '';
    document.body.style.touchAction = '';
    document.documentElement.style.overflow = '';
}

function showOverlay() {
    const mobileSheetOverlay = document.getElementById('mobile-sheet-overlay');
    if (mobileSheetOverlay) mobileSheetOverlay.classList.add('show');
}

function hideOverlay() {
    const mobileSheetOverlay = document.getElementById('mobile-sheet-overlay');
    if (mobileSheetOverlay) mobileSheetOverlay.classList.remove('show');
}

function closeFilterSheet() {
    const filtersSidebar = document.getElementById('filters-sidebar');
    if (!filtersSidebar) return;
    clearTimeout(filterSheetTimeout);
    filtersSidebar.classList.remove('is-visible');
    filterSheetTimeout = setTimeout(() => {
        if (window.innerWidth < 768) filtersSidebar.classList.add('hidden');
        filtersSidebar.classList.remove('mobile-sheet-panel');
        if (activeSheet === 'filters') {
            activeSheet = null;
            hideOverlay();
            enableBodyScroll();
        }
    }, SHEET_ANIMATION_MS);
}

function openFilterSheet() {
    const filtersSidebar = document.getElementById('filters-sidebar');
    if (!filtersSidebar || window.innerWidth >= 768 || activeSheet === 'filters') return;
    clearTimeout(filterSheetTimeout);
    filtersSidebar.classList.remove('hidden');
    filtersSidebar.classList.add('mobile-sheet-panel');
    requestAnimationFrame(() => filtersSidebar.classList.add('is-visible'));
    activeSheet = 'filters';
    showOverlay();
    disableBodyScroll();
}

function closeSortSheet() {
    const mobileSortSheet = document.getElementById('mobile-sort-sheet');
    if (!mobileSortSheet) return;
    clearTimeout(sortSheetTimeout);
    mobileSortSheet.classList.remove('is-visible');
    sortSheetTimeout = setTimeout(() => {
        if (activeSheet === 'sort') {
            activeSheet = null;
            hideOverlay();
            enableBodyScroll();
        }
    }, SHEET_ANIMATION_MS);
}

function openSortSheet() {
    const mobileSortSheet = document.getElementById('mobile-sort-sheet');
    if (!mobileSortSheet || activeSheet === 'sort' || window.innerWidth >= 768) return;
    clearTimeout(sortSheetTimeout);
    activeSheet = 'sort';
    renderSortOptions();
    mobileSortSheet.classList.add('is-visible');
    showOverlay();
    disableBodyScroll();
}

function closeActiveSheet() {
    if (activeSheet === 'filters') closeFilterSheet();
    else if (activeSheet === 'sort') closeSortSheet();
}

// -------------------- Sort & Filter Helpers --------------------
function getSortLabel(value) {
    const sortSelectMobile = document.getElementById('sort-select');
    const sortSelectDesktop = document.getElementById('sort-select-desktop');
    const select = sortSelectMobile || sortSelectDesktop;
    if (select) {
        const option = Array.from(select.options).find(opt => opt.value === value);
        if (option) return option.textContent;
    }
    return 'Sort';
}

function updateSortOptionsState(value) {
    const mobileSortOptions = document.getElementById('mobile-sort-options');
    if (!mobileSortOptions) return;
    const buttons = mobileSortOptions.querySelectorAll('.sheet-option-button');
    buttons.forEach(button => {
        if (button.dataset.value === value) {
            button.classList.add('is-active');
            button.setAttribute('aria-pressed', 'true');
            button.querySelector('.sort-option-indicator').style.display = 'inline';
        } else {
            button.classList.remove('is-active');
            button.setAttribute('aria-pressed', 'false');
            button.querySelector('.sort-option-indicator').style.display = 'none';
        }
    });
}

function renderSortOptions() {
    const mobileSortOptions = document.getElementById('mobile-sort-options');
    const sortSelectMobile = document.getElementById('sort-select');
    if (!mobileSortOptions || !sortSelectMobile) return;
    mobileSortOptions.innerHTML = '';
    Array.from(sortSelectMobile.options).forEach(option => {
        const li = document.createElement('li');
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'sheet-option-button px-4 py-3 text-left w-full block transition-colors' + (option.value === currentSort ? ' is-active bg-gray-100 text-gold' : ' hover:bg-gray-50');
        btn.dataset.value = option.value;
        btn.setAttribute('aria-label', `Sort by ${option.textContent}`);
        btn.setAttribute('aria-pressed', option.value === currentSort ? 'true' : 'false');

        const spanLabel = document.createElement('span');
        spanLabel.className = 'sort-option-label';
        spanLabel.textContent = option.textContent;

        const spanIndicator = document.createElement('span');
        spanIndicator.className = 'sort-option-indicator float-right text-gold';
        spanIndicator.setAttribute('aria-hidden', 'true');
        spanIndicator.innerHTML = '<i class="fas fa-check"></i>';
        spanIndicator.style.display = option.value === currentSort ? 'inline' : 'none';

        btn.appendChild(spanLabel);
        btn.appendChild(spanIndicator);

        btn.addEventListener('click', () => {
            syncSort(option.value, btn);
            closeSortSheet();
        });

        li.appendChild(btn);
        mobileSortOptions.appendChild(li);
    });
    updateSortOptionsState(currentSort);
}

// -------------------- Product Fetching --------------------
// -------------------- Product Fetching (Updated for Master Loader) --------------------

window.fetchProducts = async function() {
    // 1. පටන් ගන්නා විට ලෝඩරය පෙන්වීම
    if (window.loader) window.loader.show();

    try {
        const grid = document.getElementById("products-grid");
        if (grid) {
            // Mobile සහ Desktop වලට අවශ්‍ය Column ගණන සැකසීම
            grid.className = "grid grid-cols-2 lg:grid-cols-3 gap-3 md:gap-4 lg:gap-6";
        }

        const res = await fetch(PRODUCTS_API);
        const data = await res.json();

        // දත්ත Map කිරීම (ඔබේ මුල් Logic එකම වේ)
        let rawProducts = (data.products || []).map(p => ({
            productId: p.productId,
            varianceId: p.varianceId,
            name: p.name,
            title: p.title || p.name,
            brand: "Velora Fine Jewellery",
            price: parseFloat(p.price) || 0,
            regularPrice: parseFloat(p.regularPrice) || parseFloat(p.price) || 0,
            discountPercentage: parseFloat(p.discountPercentage) || 0,
            stockStatus: p.stockStatus || "Out Of Stock",
            currentStockQty: p.currentStockQty || 0,
            averageRating: p.averageRating || 0,
            reviewCount: p.reviewCount || 0,
            category: p.category ? String(p.category).trim().toLowerCase() : "",
            gemstone: p.gemstone ? String(p.gemstone).trim().toLowerCase() : "",
            metal: p.color ? String(p.color).trim().toLowerCase() : "",
            image: p.image || "/images/default.png",
            tags: Array.isArray(p.tags) ? p.tags : [],
            date: p.createdAt ? new Date(p.createdAt) : new Date(0)
        }));

        // GROUP BY PRODUCT ID (ENTERPRISE STANDARD)
        const productMap = new Map();
        rawProducts.forEach(p => {
            if (!productMap.has(p.productId)) {
                productMap.set(p.productId, {
                    ...p,
                    variances: [],
                    minPrice: p.price,
                    maxPrice: p.price,
                    metals: new Set([p.metal]),
                    gemstones: new Set([p.gemstone])
                });
            }
            const grouped = productMap.get(p.productId);
            grouped.variances.push(p);
            if (p.price < grouped.minPrice) grouped.minPrice = p.price;
            if (p.price > grouped.maxPrice) grouped.maxPrice = p.price;
            grouped.currentStockQty += p.currentStockQty;
            if (p.currentStockQty > 0) grouped.stockStatus = "In Stock"; // If any in stock
            grouped.metals.add(p.metal);
            grouped.gemstones.add(p.gemstone);
        });

        allProducts = Array.from(productMap.values());


        setupEventListeners();
        initializeFiltersFromURL();

        // URL Parameters පරීක්ෂා කිරීම
        const params = new URLSearchParams(window.location.search);
        if (params.get('sale') === 'true') {
            showOnlySale = true;
            if (window.notify) notify.info("Showing Seasonal Sales");
        }

        const urlSort = params.get('sort');
        const sortSelectDesktop = document.getElementById('sort-select-desktop');

        if (urlSort) {
            currentSort = urlSort;
        } else if (sortSelectDesktop) {
            currentSort = sortSelectDesktop.value;
        }

        // Sorting සහ Display එක ආරම්භ කිරීම
        syncSort(currentSort, null);

        return true; // සාර්ථක බව හඟවයි

    } catch (err) {
        console.error("Error loading products:", err);
        const grid = document.getElementById("products-grid");
        if (grid) grid.innerHTML = `<div class="col-span-full text-center py-12"><p class="text-red-600">Failed to load products</p></div>`;
        setupEventListeners();
        displayProducts();
        return false;
    } finally {
        // 2. ✅ සියල්ල අවසන් වූ පසු පිටුව පෙන්වා ලෝඩරය අයින් කිරීම
        revealShopContent();
    }
};

/**
 * පිටුව පෙන්වන සහ ලෝඩරය අයින් කරන Helper Function එක
 */
function revealShopContent() {
    const main = document.getElementById('main-content');
    if (main) {
        main.style.display = 'block';
        main.classList.add('animate__animated', 'animate__fadeIn');
    }

    if (window.loader) {
        // පින්තූර Render වීමට සුළු ප්‍රමාදයක් (500ms) ලබා දෙන්න
        setTimeout(() => {
            window.loader.hide();
        }, 500);
    }
}
// Handle URL Filters
function initializeFiltersFromURL() {
    const params = new URLSearchParams(window.location.search);
    const categoryParam = params.get('category');

    if (categoryParam) {
        const categoryValue = categoryParam.trim().toLowerCase();
        const containers = document.querySelectorAll('[id="categoryList"]');
        let categoryList = null;
        for (const c of containers) {
            if (c.querySelector('input[type="checkbox"]')) {
                categoryList = c;
                break;
            }
        }

        if (categoryList) {
            const checkboxes = categoryList.querySelectorAll('input[type="checkbox"]');
            checkboxes.forEach(cb => {
                if (cb.value.trim().toLowerCase() === categoryValue) {
                    cb.checked = true;
                }
            });
        }
    }
}

// -------------------- Filtering & Sorting --------------------
function applyFiltersAndSort() {
    const priceRange = document.getElementById('price-range');
    const searchInput = document.getElementById('shop-search-input');
    const searchTerm = searchInput ? searchInput.value.toLowerCase().trim() : '';
    const maxPrice = priceRange ? parseFloat(priceRange.value) : Infinity;
    const selectedCategories = getSelectedFilters('category');
    const selectedGemstones = getSelectedFilters('gemstone');
    const selectedMetals = getSelectedFilters('metal');

    filteredProducts = allProducts.filter(p => {
        const matchesSearch = p.name.toLowerCase().includes(searchTerm) ||
            p.category.includes(searchTerm) ||
            p.tags.some(t => t.toLowerCase().includes(searchTerm));

        const matchesPrice = p.price <= maxPrice;

        const matchesCategory = !selectedCategories.length || selectedCategories.includes(p.category);
        const matchesGemstone = !selectedGemstones.length || selectedGemstones.includes(p.gemstone);
        const matchesMetal = !selectedMetals.length || selectedMetals.includes(p.metal);

        // --- SALE FILTER LOGIC ---
        const matchesSale = !showOnlySale || p.discountPercentage > 0;

        return matchesSearch && matchesPrice && matchesCategory && matchesGemstone && matchesMetal && matchesSale;
    });

    sortProducts(currentSort);
    currentPage = 1;
    displayProducts();
}

function sortProducts(sortBy) {
    switch (sortBy) {
        case 'price-low':
            filteredProducts.sort((a, b) => a.price - b.price);
            break;
        case 'price-high':
            filteredProducts.sort((a, b) => b.price - b.price);
            break;
        case 'newest':
            filteredProducts.sort((a, b) => b.date - a.date);
            break;
        case 'popularity':
            break;
        default:
            break;
    }
}

// -------------------- Design Logic Helpers --------------------

function getProductState(product) {
    const isNew = product.tags?.includes('New Arrival');
    const isOut = product.stockStatus === 'Out Of Stock' || product.stockStatus === 'Out of Stock';

    let badgeHtml = '';
    if (isOut) {
        badgeHtml = '<span class="badge bg-dark text-white text-xs font-bold px-3 py-1 uppercase">SOLD OUT</span>';
    } else if (product.discountPercentage > 0) {
        badgeHtml = `<span class="badge bg-red-600 text-white text-xs font-bold px-3 py-1 uppercase">-${Math.round(product.discountPercentage)}%</span>`;
    } else if (isNew) {
        badgeHtml = '<span class="badge bg-gold text-dark text-xs font-bold px-3 py-1 uppercase">New</span>';
    }

    const btnState = isOut ? 'disabled' : '';
    const btnText = isOut ? 'Sold Out' : 'Add to Cart';

    let stockStatusHtml = '';
    if (isOut) {
        stockStatusHtml = `<i class="fas fa-circle text-red-500" style="font-size: 6px;"></i><span>Out of stock</span>`;
    } else if (product.currentStockQty < 5 && product.currentStockQty > 0) {
        stockStatusHtml = `<i class="fas fa-circle text-orange-500" style="font-size: 6px;"></i><span>Only ${product.currentStockQty} left in stock</span>`;
    } else {
        stockStatusHtml = `<i class="fas fa-circle text-green-500" style="font-size: 6px;"></i><span>In stock & ready to ship</span>`;
    }

    return { badgeHtml, btnState, btnText, stockStatusHtml };
}

function generateRatingHtml(rating, count) {
    const avgRating = parseFloat(rating) || 0;
    const reviewCount = parseInt(count) || 0;

    let starsHtml = '<div class="stars">';
    for (let i = 1; i <= 5; i++) {
        if (avgRating >= i) {
            starsHtml += '<i class="fas fa-star star"></i>';
        } else if (avgRating >= i - 0.5) {
            starsHtml += '<i class="fas fa-star-half-alt star"></i>';
        } else {
            starsHtml += '<i class="far fa-star star" style="color:#d1d5db;"></i>';
        }
    }
    starsHtml += '</div>';

    const countHtml = reviewCount > 0
        ? `<span class="review-count">(${reviewCount})</span>`
        : `<span class="review-count text-xs" style="margin-left:5px;"></span>`;

    return `
        <div class="product-rating">
            ${starsHtml}
            ${countHtml}
        </div>
    `;
}

// -------------------- Product Display --------------------
function displayProducts() {
    const totalFiltered = filteredProducts.length;
    const totalPages = Math.ceil(totalFiltered / PRODUCTS_PER_PAGE);
    if (totalPages === 0) currentPage = 0;
    else if (currentPage > totalPages) currentPage = totalPages;
    else if (currentPage < 1) currentPage = 1;

    const startIndex = (currentPage - 1) * PRODUCTS_PER_PAGE;
    const endIndex = startIndex + PRODUCTS_PER_PAGE;
    const productsToRender = filteredProducts.slice(startIndex, endIndex);

    renderProducts(productsToRender, totalFiltered);
    updateProductCount(totalFiltered, totalFiltered > 0 ? startIndex + 1 : 0, Math.min(totalFiltered, endIndex));
    setupPagination(totalFiltered);
}

function renderProducts(products, totalFiltered) {
    const grid = document.getElementById("products-grid");
    if (!grid) return;
    grid.innerHTML = "";

    if (totalFiltered === 0) {
        grid.innerHTML = `<div id="no-products-message" class="col-span-full text-center py-12">
            <p class="text-xl text-gray-600">No products found</p>
        </div>`;
        return;
    }

    products.forEach(p => {
        let formattedPrice = '';
        if (p.minPrice && p.maxPrice && p.minPrice !== p.maxPrice) {
            formattedPrice = `LKR ${p.minPrice.toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')} - LKR ${p.maxPrice.toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')}`;
        } else {
            formattedPrice = formatPriceLKR(p.price);
        }

        const { badgeHtml, btnState, stockStatusHtml } = getProductState(p);
        const ratingHtml = generateRatingHtml(p.averageRating, p.reviewCount);
        const displayTitle = p.title || p.name;
        const imgUrl = p.image && p.image !== "/images/default.png" ? p.image : 'https://placehold.co/600x600?text=No+Image';

        const isGrouped = p.variances && p.variances.length > 1;
        const finalBtnText = btnState === 'disabled' ? 'Sold Out' : (isGrouped ? 'Select Options' : 'Add to Cart');
        const iconHtml = btnState === 'disabled' ? '<i class="fas fa-lock"></i>' : (isGrouped ? '<i class="fas fa-list-ul"></i>' : '<i class="fas fa-shopping-bag"></i>');

        const productCard = document.createElement("div");
        productCard.className = "product-card w-full min-w-0 shadow-sm group animate__animated animate__fadeInUp";
        productCard.dataset.productId = p.varianceId;
        productCard.onclick = () => window.location.href = `product-detail.html?id=${p.varianceId}`;

        let actionButtonHtml = '';
        if (isGrouped || btnState === 'disabled') {
            actionButtonHtml = `<button class="w-full flex items-center justify-center gap-2 whitespace-nowrap uppercase tracking-widest font-bold text-[10px] md:text-xs py-1.5 md:py-2 transition-all hover:bg-gray-800 hover:text-white border border-gray-800 rounded-sm ${btnState === 'disabled' ? 'opacity-50 cursor-not-allowed' : ''}" onclick="event.stopPropagation(); window.location.href='product-detail.html?id=${p.varianceId}'" ${btnState}>${iconHtml} ${finalBtnText}</button>`;
        } else {
            actionButtonHtml = `<button class="add-to-cart-btn w-full flex items-center justify-center gap-2 whitespace-nowrap uppercase tracking-widest font-bold text-[10px] md:text-xs py-1.5 md:py-2 transition-all hover:bg-gray-800 hover:text-white border border-gray-800 rounded-sm" data-product-id="${p.varianceId}" data-product-name="${p.name}" data-product-price="${p.price}" data-product-image="${imgUrl}" data-stock-status="${p.stockStatus}" data-stock-qty="${p.currentStockQty}"><i class="fas fa-shopping-bag"></i> Add to Cart</button>`;
        }

        productCard.innerHTML = `
            ${badgeHtml}
            <div class="product-image h-48 md:h-64 relative overflow-hidden">
                <img src="${imgUrl}" alt="${p.name}" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" loading="lazy">
                <button class="view-product-btn" onclick="event.stopPropagation(); window.location.href='product-detail.html?id=${p.varianceId}'">View Product</button>
            </div>
            <div class="product-info p-3">
                <p class="text-[10px] md:text-xs text-gray-400 uppercase tracking-wider font-semibold mb-1">VELORA FINE JEWELLERY</p>
                <h3 class="product-title line-clamp-2 text-sm md:text-base font-medium mb-1" title="${displayTitle}">${displayTitle}</h3>
                <div class="mb-1">${ratingHtml}</div>
                <p class="product-price text-sm md:text-lg font-bold text-gray-900 mb-2">${formattedPrice}</p>
                <div class="product-availability mb-3 text-[10px] md:text-xs">${stockStatusHtml}</div>
                ${actionButtonHtml}
            </div>
        `;

        grid.appendChild(productCard);
    });

    // Important: Re-attach listeners via cart.js since we just created new buttons
    if (typeof window.attachCartFunctionality === 'function') {
        window.attachCartFunctionality();
    }
}

function setupQuickViewListeners() {
}

function handleCategoryLinkClick(e) {
    e.preventDefault();
    const category = e.currentTarget.dataset.category;

    if (!category) return;

    const containers = document.querySelectorAll('[id="categoryList"]');
    let categoryList = null;
    for (const c of containers) {
        if (c.querySelector('input[type="checkbox"]')) {
            categoryList = c;
            break;
        }
    }

    if (!categoryList) {
        notify.warning("Category filters not found on this page.");
        return;
    }

    const categoryValue = category.toLowerCase();
    let targetCheckbox = null;
    let isAlreadyChecked = false;

    categoryList.querySelectorAll('input[type="checkbox"]').forEach(cb => {
        if (cb.value.toLowerCase() === categoryValue) {
            targetCheckbox = cb;
            isAlreadyChecked = cb.checked;
        } else {
            cb.checked = false;
        }
    });

    if (targetCheckbox) {
        targetCheckbox.checked = !isAlreadyChecked;
    }

    applyFiltersAndSort();

    const grid = document.getElementById('products-grid');
    if (grid) window.scrollTo({top: grid.offsetTop - 100, behavior: 'smooth'});

    notify.info(`Filtered by: ${targetCheckbox?.checked ? category : 'All Categories'}`);
}

function updateProductCount(totalMatching, start, end) {
    const countEl = document.getElementById("product-count");
    if (!countEl) return;
    const totalAvailable = allProducts.length;
    countEl.textContent = totalMatching === 0
        ? `Showing 0 of ${totalAvailable} products`
        : `Showing ${start}-${end} of ${totalMatching} products`;
}

function setupPagination(totalProducts) {
    const totalPages = Math.ceil(totalProducts / PRODUCTS_PER_PAGE);
    const paginationNav = document.getElementById("pagination-nav");
    const paginationWrapper = document.getElementById('pagination-wrapper');
    if (!paginationNav) return;
    paginationNav.innerHTML = "";
    if (paginationWrapper) paginationWrapper.classList.toggle('hidden', totalPages <= 1);
    if (totalPages <= 1) return;

    const scrollToGrid = () => {
        const grid = document.getElementById('products-grid');
        if (grid) window.scrollTo({top: grid.offsetTop - 100, behavior: 'smooth'});
    };

    const createButton = (label, onClick, disabled, isPage = false, pageNum = null) => {
        const a = document.createElement("a");
        a.href = "#";
        a.className = `px-3 py-2 border border-gray-300 rounded-none hover:bg-gold hover:text-white hover:border-gold transition-colors ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`;
        a.innerHTML = label;
        if (isPage) {
            a.textContent = pageNum;
            a.className = `px-4 py-2 border border-gray-300 rounded-none hover:bg-gold hover:text-white hover:border-gold transition-colors`;
            if (pageNum === currentPage) a.classList.add('bg-gold', 'text-white', 'border-gold');
        }
        if (!disabled) a.addEventListener('click', e => {
            e.preventDefault();
            onClick();
            scrollToGrid();
        });
        return a;
    };

    paginationNav.appendChild(createButton('<i class="fas fa-chevron-left"></i>', () => {
        currentPage--;
        displayProducts();
    }, currentPage === 1));
    for (let i = 1; i <= totalPages; i++) paginationNav.appendChild(createButton('', () => {
        currentPage = i;
        displayProducts();
    }, false, true, i));
    paginationNav.appendChild(createButton('<i class="fas fa-chevron-right"></i>', () => {
        currentPage++;
        displayProducts();
    }, currentPage === totalPages));
}

function syncSort(value, source) {
    const sortSelectMobile = document.getElementById('sort-select');
    const sortSelectDesktop = document.getElementById('sort-select-desktop');
    const sortToggleLabel = document.getElementById('sort-toggle-label');
    currentSort = value;
    if (sortSelectMobile && source !== sortSelectMobile) sortSelectMobile.value = value;
    if (sortSelectDesktop && source !== sortSelectDesktop) sortSelectDesktop.value = value;
    if (sortToggleLabel) sortToggleLabel.textContent = getSortLabel(value);
    updateSortOptionsState(value);
    applyFiltersAndSort();
}

function setupEventListeners() {
    const searchInput = document.getElementById('shop-search-input');
    const sortSelectMobile = document.getElementById('sort-select');
    const sortSelectDesktop = document.getElementById('sort-select-desktop');
    const priceRange = document.getElementById('price-range');
    const priceValue = document.getElementById('price-value');
    const applyFiltersBtn = document.getElementById('apply-filters');
    const clearFiltersBtn = document.getElementById('clear-filters');
    const gemstoneList = document.getElementById('gemstoneList');
    const colorList = document.getElementById('colorList');

    const catContainers = document.querySelectorAll('[id="categoryList"]');
    let categoryList = null;
    for (const c of catContainers) {
        if (c.querySelector('input[type="checkbox"]')) {
            categoryList = c;
            break;
        }
    }

    const navCategoryLinks = document.querySelectorAll('.category-link');

    window.addEventListener('resize', () => {
        const filtersSidebar = document.getElementById('filters-sidebar');
        if (window.innerWidth >= 768) {
            if (filtersSidebar) filtersSidebar.classList.remove('mobile-sheet-panel', 'is-visible', 'hidden');
            const mobileSortSheet = document.getElementById('mobile-sort-sheet');
            if (mobileSortSheet) mobileSortSheet.classList.remove('is-visible');
            activeSheet = null;
            hideOverlay();
            enableBodyScroll();
        } else if (filtersSidebar && !filtersSidebar.classList.contains('is-visible')) filtersSidebar.classList.add('hidden');
    });

    if (searchInput) searchInput.addEventListener('input', applyFiltersAndSort);
    if (sortSelectMobile) sortSelectMobile.addEventListener('change', () => syncSort(sortSelectMobile.value, sortSelectMobile));
    if (sortSelectDesktop) sortSelectDesktop.addEventListener('change', () => syncSort(sortSelectDesktop.value, sortSelectDesktop));

    if (priceRange && priceValue) {
        priceRange.addEventListener('input', () => {
            priceValue.textContent = formatPriceLKR(parseInt(priceRange.value, 10));
        });
        priceRange.addEventListener('change', applyFiltersAndSort);
        const initVal = parseInt(priceRange.value, 10);
        if (!isNaN(initVal)) priceValue.textContent = formatPriceLKR(initVal);
    }

    [categoryList, gemstoneList, colorList].forEach(container => {
        if (container) {
            container.addEventListener('change', e => {
                if (e.target.type === 'checkbox') applyFiltersAndSort();
            });
        }
    });

    if (applyFiltersBtn) applyFiltersBtn.addEventListener('click', () => {
        applyFiltersAndSort();
        if (window.innerWidth < 768) closeFilterSheet();
    });

    if (clearFiltersBtn) clearFiltersBtn.addEventListener('click', () => {
        if (searchInput) searchInput.value = '';
        if (priceRange) {
            const maxVal = priceRange.getAttribute('max') || '2000000';
            priceRange.value = maxVal;
            if (priceValue) priceValue.textContent = formatPriceLKR(parseInt(maxVal, 10));
        }
        document.querySelectorAll('.filters-panel input[type="checkbox"]').forEach(cb => cb.checked = false);

        // --- RESET SALE FILTER ---
        showOnlySale = false;

        applyFiltersAndSort();
        if (window.innerWidth < 768) closeFilterSheet();
    });

    if (navCategoryLinks.length > 0) {
        navCategoryLinks.forEach(link => {
            link.addEventListener('click', handleCategoryLinkClick);
        });
    }

    setupQuickViewListeners();
}

// Exports
window.syncSort = syncSort;
window.openSortSheet = openSortSheet;
window.closeSortSheet = closeSortSheet;
window.updateSortOptionsState = updateSortOptionsState;
window.applyFiltersAndSort = applyFiltersAndSort;
window.openFilterSheet = openFilterSheet;
window.closeFilterSheet = closeFilterSheet;
window.closeActiveSheet = closeActiveSheet;
