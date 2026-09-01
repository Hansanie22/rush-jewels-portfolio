import Notification from './notification.js';

const COLLECTIONS_API = "/api/collections";
const ITEMS_PER_PAGE = 12;
const SHEET_ANIMATION_MS = 280;

let allCollections = [];
let filteredCollections = [];
let currentPage = 1;
let currentSort = 'popularity';
let activeSheet = null;
let filterSheetTimeout = null;
let sortSheetTimeout = null;

// Track "Sale Only" state
let showOnlySale = false;

// Initialize notification globally
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
    const sortSheet = document.getElementById('mobile-sort-sheet');
    if (!sortSheet) return;
    clearTimeout(sortSheetTimeout);
    sortSheet.classList.remove('is-visible');
    sortSheetTimeout = setTimeout(() => {
        if (activeSheet === 'sort') {
            activeSheet = null;
            hideOverlay();
            enableBodyScroll();
        }
    }, SHEET_ANIMATION_MS);
}

function openSortSheet() {
    const sortSheet = document.getElementById('mobile-sort-sheet');
    if (!sortSheet || window.innerWidth >= 768 || activeSheet === 'sort') return;
    clearTimeout(sortSheetTimeout);
    requestAnimationFrame(() => sortSheet.classList.add('is-visible'));
    activeSheet = 'sort';
    showOverlay();
    disableBodyScroll();
}

function closeActiveSheet() {
    if (activeSheet === 'filters') closeFilterSheet();
    if (activeSheet === 'sort') closeSortSheet();
}

// -------------------- Sorting Sheet Logic --------------------
function getSortLabel(value) {
    const sortSelectDesktop = document.getElementById('sort-select-desktop');
    const sortSelectMobile = document.getElementById('sort-select');
    const select = sortSelectDesktop || sortSelectMobile;

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
    const sortSelectDesktop = document.getElementById('sort-select-desktop');

    if (!mobileSortOptions || !sortSelectDesktop) return;

    mobileSortOptions.innerHTML = '';
    Array.from(sortSelectDesktop.options).forEach(option => {
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
            if(window.closeSortSheet) window.closeSortSheet();
        });

        li.appendChild(btn);
        mobileSortOptions.appendChild(li);
    });
    updateSortOptionsState(currentSort);
}

// ✅ ලෝඩරය දෙපාරක් පෙනීම වැළැක්වූ නව fetchCollections ශ්‍රිතය
window.fetchCollections = async function() {
    // ❌ මෙහි තිබූ window.loader.show() ඉවත් කරන ලදී

    try {
        const grid = document.getElementById("collections-grid");
        if (grid) {
            grid.className = "grid grid-cols-2 lg:grid-cols-3 gap-3 md:gap-4 lg:gap-6";
            grid.innerHTML = '<div class="col-span-full text-center py-12"><i class="fas fa-spinner fa-spin text-3xl text-gold"></i></div>';
        }

        const res = await fetch(COLLECTIONS_API);
        const data = await res.json();

        allCollections = (data.collections || []).map(c => ({
            id: c.id,
            name: c.name,
            title: c.title || c.name,
            description: c.description,
            price: parseFloat(c.price) || 0,
            regularPrice: parseFloat(c.regularPrice) || parseFloat(c.price) || 0,
            discountPercentage: parseFloat(c.discountPercentage) || 0,
            stockStatus: c.status || (c.stockLimit > 0 ? "In Stock" : "Out Of Stock"),
            currentStockQty: c.stockLimit || 0,
            averageRating: c.averageRating || 0,
            reviewCount: c.reviewCount || 0,
            image: c.image || "/images/default-collection.png",
            date: c.createdAt ? new Date(c.createdAt) : new Date(0)
        }));

        setupEventListeners();
        syncSort(currentSort, null);

        return true;

    } catch (err) {
        console.error("Error loading collections:", err);
        const grid = document.getElementById("collections-grid");
        if (grid) grid.innerHTML = `<div class="col-span-full text-center py-12"><p class="text-red-600">Failed to load collections</p></div>`;
        return false;
    }
};
// -------------------- Filtering & Sorting --------------------

function applyFiltersAndSort() {
    const priceRange = document.getElementById('price-range');
    const searchInput = document.getElementById('collection-search-input');
    const priceValue = document.getElementById('price-value');

    const searchTerm = searchInput ? searchInput.value.toLowerCase() : '';
    const maxPrice = priceRange ? parseFloat(priceRange.value) : Infinity;

    if (priceRange && priceValue) {
        priceValue.textContent = formatPriceLKR(parseInt(priceRange.value, 10));
    }

    filteredCollections = allCollections.filter(c => {
        const matchesSearch = c.name.toLowerCase().includes(searchTerm) || (c.title && c.title.toLowerCase().includes(searchTerm));
        const matchesPrice = c.price <= maxPrice;

        // --- SALE FILTER: Only show items with discount > 0 if showOnlySale is true ---
        const matchesSale = !showOnlySale || c.discountPercentage > 0;

        return matchesSearch && matchesPrice && matchesSale;
    });

    sortCollections(currentSort);
    currentPage = 1;
    displayCollections();
}

function sortCollections(sortBy) {
    switch (sortBy) {
        case 'price-low':
            filteredCollections.sort((a, b) => a.price - b.price);
            break;
        case 'price-high':
            filteredCollections.sort((a, b) => b.price - a.price);
            break;
        case 'newest':
            filteredCollections.sort((a, b) => b.date - a.date);
            break;
        case 'popularity':
        default:
            filteredCollections.sort((a, b) => a.id - b.id);
            break;
    }
}

function syncSort(value, source) {
    currentSort = value;
    const sortSelectMobile = document.getElementById('sort-select');
    const sortSelectDesktop = document.getElementById('sort-select-desktop');
    const sortToggleLabel = document.getElementById('sort-toggle-label');

    if (sortSelectMobile && source !== sortSelectMobile) sortSelectMobile.value = value;
    if (sortSelectDesktop && source !== sortSelectDesktop) sortSelectDesktop.value = value;

    if (sortToggleLabel && sortSelectDesktop) {
        const selectedOption = Array.from(sortSelectDesktop.options).find(opt => opt.value === value);
        if (selectedOption) sortToggleLabel.textContent = selectedOption.textContent;
    }

    updateSortOptionsState(value);
    applyFiltersAndSort();
}

// -------------------- Design Logic Helpers --------------------

function getCollectionState(collection) {
    const isNew = collection.stockStatus === "In Stock" && collection.discountPercentage === 0;
    const isOut = collection.stockStatus === "Out Of Stock" || collection.stockStatus === "Out of Stock";

    let badgeHtml = '';
    if (isOut) {
        badgeHtml = '<span class="badge bg-dark text-white text-xs font-bold px-3 py-1 uppercase">SOLD OUT</span>';
    } else if (collection.discountPercentage > 0) {
        badgeHtml = `<span class="badge bg-red-600 text-white text-xs font-bold px-3 py-1 uppercase">-${Math.round(collection.discountPercentage)}%</span>`;
    } else if (isNew) {
        badgeHtml = '<span class="badge bg-gold text-dark text-xs font-bold px-3 py-1 uppercase">New</span>';
    }

    const btnState = isOut ? 'disabled' : '';
    const btnText = isOut ? 'Sold Out' : 'Add To Cart';

    let stockStatusHtml = '';
    if (isOut) {
        stockStatusHtml = `<i class="fas fa-circle text-red-500" style="font-size: 6px;"></i><span>Out of stock</span>`;
    } else if (collection.currentStockQty < 5 && collection.currentStockQty > 0) {
        stockStatusHtml = `<i class="fas fa-circle text-orange-500" style="font-size: 6px;"></i><span>Only ${collection.currentStockQty} left in stock</span>`;
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

// -------------------- Display & Rendering --------------------

function displayCollections() {
    const totalFiltered = filteredCollections.length;
    const totalPages = Math.ceil(totalFiltered / ITEMS_PER_PAGE);

    if (totalPages === 0) currentPage = 0;
    else if (currentPage > totalPages) currentPage = totalPages;
    else if (currentPage < 1) currentPage = 1;

    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    const endIndex = startIndex + ITEMS_PER_PAGE;
    const itemsToRender = filteredCollections.slice(startIndex, endIndex);

    renderCollections(itemsToRender, totalFiltered);
    updateCount(totalFiltered, totalFiltered > 0 ? startIndex + 1 : 0, Math.min(totalFiltered, endIndex));
    setupPagination(totalFiltered);
}

function renderCollections(collections, totalFiltered) {
    const grid = document.getElementById("collections-grid");
    const noResults = document.getElementById("no-results");

    if (!grid) return;
    grid.innerHTML = "";

    if (totalFiltered === 0) {
        if (noResults) noResults.classList.remove('hidden');
        return;
    } else {
        if (noResults) noResults.classList.add('hidden');
    }

    collections.forEach(c => {
        const formattedPrice = formatPriceLKR(c.price);
        const { badgeHtml, btnState, btnText, stockStatusHtml } = getCollectionState(c);
        const ratingHtml = generateRatingHtml(c.averageRating, c.reviewCount);
        const displayTitle = c.title || c.name;

        const imgUrl = c.image && c.image !== "/images/default-collection.png" ? c.image : 'https://placehold.co/600x600?text=No+Image';

        const card = document.createElement("div");
        // w-full added to ensure full width within grid cell
        card.className = "product-card w-full shadow-sm group animate__animated animate__fadeInUp";
        card.dataset.productId = c.id;

        card.onclick = () => window.location.href = `collection-details.html?id=${c.id}`;

        // UPDATED HTML STRUCTURE FOR MOBILE OPTIMIZATION
        card.innerHTML = `
            ${badgeHtml}
            
            <div class="product-image h-48 md:h-64 relative overflow-hidden">
                <img src="${imgUrl}" alt="${c.name}" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" loading="lazy">
                <button class="view-product-btn" onclick="event.stopPropagation(); window.location.href='collection-details.html?id=${c.id}'">View Collection</button>
            </div>

            <div class="product-info p-3">
                <p class="text-[10px] md:text-xs text-gray-400 uppercase tracking-wider font-semibold mb-1">RUSH JEWELS</p>
                
                <h3 class="product-title line-clamp-2 text-sm md:text-base font-medium mb-1" title="${displayTitle}">${displayTitle}</h3>
                
                <div class="mb-1">
                    ${ratingHtml}
                </div>

                <p class="product-price text-sm md:text-lg font-bold text-gray-900 mb-2">${formattedPrice}</p>
                
                <div class="product-availability mb-3 text-[10px] md:text-xs">
                    ${stockStatusHtml}
                </div>

                <button class="add-to-cart-btn w-full flex items-center justify-center gap-2 whitespace-nowrap uppercase tracking-widest font-bold text-[10px] md:text-xs py-1.5 md:py-2 transition-all hover:bg-gray-800 hover:text-white border border-gray-800 rounded-sm"
                        data-collection-id="${c.id}"
                        data-product-name="${c.name} (Collection)" 
                        data-product-price="${c.price}" 
                        data-product-image="${imgUrl}" 
                        data-stock-status="${c.stockStatus}"
                        data-product-quantity="1"
                        ${btnState}>
                    <i class="fas fa-shopping-bag"></i> ${btnText}
                </button>
            </div>
        `;

        grid.appendChild(card);
    });

    if (typeof window.attachCartFunctionality === 'function') {
        window.attachCartFunctionality();
    }
}
// -------------------- Pagination --------------------

// -------------------- Pagination --------------------

function setupPagination(totalItems) {
    const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
    const paginationNav = document.getElementById("pagination-nav");
    const paginationWrapper = document.getElementById('pagination-wrapper');

    if (!paginationNav) return;
    paginationNav.innerHTML = "";

    if (paginationWrapper) paginationWrapper.classList.toggle('hidden', totalPages <= 1);
    if (totalPages <= 1) return;

    // Grid එක ලඟට Scroll කිරීමේ logic එක
    const scrollToGrid = () => {
        const grid = document.getElementById('collections-grid');
        if (grid) window.scrollTo({top: grid.offsetTop - 100, behavior: 'smooth'});
    };

    // ඔබ ලබා දුන් හරියටම සමාන Button design එක
    const createButton = (label, onClick, disabled, isPage = false, pageNum = null) => {
        const a = document.createElement("a");
        a.href = "#";
        // Square border design classes
        a.className = `px-3 py-2 border border-gray-300 rounded-none hover:bg-gold hover:text-white hover:border-gold transition-colors ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`;
        a.innerHTML = label;

        if (isPage) {
            a.textContent = pageNum;
            a.className = `px-4 py-2 border border-gray-300 rounded-none hover:bg-gold hover:text-white hover:border-gold transition-colors`;
            // Active page එක gold පාට කිරීමට
            if (pageNum === currentPage) {
                a.classList.add('bg-gold', 'text-white', 'border-gold');
            }
        }

        if (!disabled) {
            a.addEventListener('click', e => {
                e.preventDefault();
                onClick();
                scrollToGrid();
            });
        }
        return a;
    };

    // Previous Arrow
    paginationNav.appendChild(createButton('<i class="fas fa-chevron-left"></i>', () => {
        currentPage--;
        displayCollections();
    }, currentPage === 1));

    // Page Numbers
    for (let i = 1; i <= totalPages; i++) {
        paginationNav.appendChild(createButton('', () => {
            currentPage = i;
            displayCollections();
        }, false, true, i));
    }

    // Next Arrow
    paginationNav.appendChild(createButton('<i class="fas fa-chevron-right"></i>', () => {
        currentPage++;
        displayCollections();
    }, currentPage === totalPages));
}

function updateCount(total, start, end) {
    const countEl = document.getElementById("collection-count");
    if (countEl) {
        countEl.textContent = total === 0
            ? `Showing 0 of ${allCollections.length} collections`
            : `Showing ${start}-${end} of ${total} collections`;
    }
}

// -------------------- Event Listeners --------------------

function setupEventListeners() {
    const searchInput = document.getElementById('collection-search-input');
    const priceRange = document.getElementById('price-range');
    const sortSelectDesktop = document.getElementById('sort-select-desktop');
    const sortSelectMobile = document.getElementById('sort-select');
    const applyFiltersBtn = document.getElementById('apply-filters');
    const clearFiltersBtn = document.getElementById('clear-filters');
    const filterToggle = document.getElementById('filter-toggle');
    const mobileFilterClose = document.getElementById('mobile-filter-close');

    let timeout;
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            clearTimeout(timeout);
            timeout = setTimeout(applyFiltersAndSort, 300);
        });
    }

    if (priceRange) {
        priceRange.addEventListener('input', () => {
            const priceValue = document.getElementById('price-value');
            if(priceValue) priceValue.textContent = formatPriceLKR(parseInt(priceRange.value, 10));
        });
        priceRange.addEventListener('change', applyFiltersAndSort);
    }

    if (sortSelectDesktop) {
        sortSelectDesktop.addEventListener('change', (e) => syncSort(e.target.value, sortSelectDesktop));
    }

    if (sortSelectMobile) {
        sortSelectMobile.addEventListener('change', (e) => syncSort(e.target.value, sortSelectMobile));
    }

    if (applyFiltersBtn) {
        applyFiltersBtn.addEventListener('click', () => {
            applyFiltersAndSort();
            if (window.innerWidth < 768) closeFilterSheet();
        });
    }

    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener('click', () => {
            if (searchInput) searchInput.value = '';
            if (priceRange) {
                const maxVal = priceRange.getAttribute('max') || '2000000';
                priceRange.value = maxVal;
                document.getElementById('price-value').textContent = formatPriceLKR(parseInt(maxVal));
            }

            // --- RESET SALE FILTER ---
            showOnlySale = false;

            applyFiltersAndSort();
            if (window.innerWidth < 768) closeFilterSheet();
        });
    }

    if (filterToggle) filterToggle.addEventListener('click', openFilterSheet);
    if (mobileFilterClose) mobileFilterClose.addEventListener('click', closeFilterSheet);

    const sortToggle = document.getElementById('sort-toggle');
    const mobileSortClose = document.getElementById('mobile-sort-close');
    if (sortToggle) sortToggle.addEventListener('click', openSortSheet);
    if (mobileSortClose) mobileSortClose.addEventListener('click', closeSortSheet);

    const overlay = document.getElementById('mobile-sheet-overlay');
    if (overlay) overlay.addEventListener('click', closeActiveSheet);

    window.addEventListener('resize', () => {
        const filtersSidebar = document.getElementById('filters-sidebar');
        if (window.innerWidth >= 768) {
            if (filtersSidebar) filtersSidebar.classList.remove('mobile-sheet-panel', 'is-visible', 'hidden');
            activeSheet = null;
            hideOverlay();
            enableBodyScroll();
        } else {
            if (filtersSidebar && activeSheet !== 'filters') {
                filtersSidebar.classList.add('hidden');
            }
        }
    });
}

// -------------------- Initialization --------------------

window.syncSort = syncSort;
window.applyFiltersAndSort = applyFiltersAndSort;
window.openFilterSheet = openFilterSheet;
window.closeFilterSheet = closeFilterSheet;
window.openSortSheet = openSortSheet;
window.closeSortSheet = closeSortSheet;
window.closeActiveSheet = closeActiveSheet;
window.renderSortOptions = renderSortOptions;

document.addEventListener("DOMContentLoaded", () => {
    // Master Loader එක HTML එකේ නැතිනම් පමණක් මෙය fallback එකක් ලෙස වැඩ කරයි
    if (!window.loader || !document.getElementById('main-content')) {
        window.fetchCollections();
    }
});