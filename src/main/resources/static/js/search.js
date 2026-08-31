import Notification from './notification.js';

// === Configuration ===
const PRODUCTS_API_SEARCH = 'api/search/products';
const SUGGESTIONS_API = 'api/search/suggestions';
const DEBOUNCE_DELAY_MS = 300;
const FALLBACK_PRODUCT_IMAGE = 'https://images.unsplash.com/photo-1520962918287-7448c2878f65?auto=format&fit=crop&w=987&q=80';

// Initialize notification
const notify = Notification({
    position: 'bottom-right',
    duration: 3000,
    hidePrevious: true,
    maxVisible: 5,
});
window.notify = notify;

/**
 * MAIN INITIALIZATION FUNCTION
 * Called when 'navbar-loaded' event fires.
 */
function initSearchController() {
    // === DOM Elements ===
    const searchBtn = document.getElementById('search-btn');
    const mobileSearchBtn = document.getElementById('mobile-search-btn');
    const searchPanel = document.getElementById('search-panel');
    const searchOverlay = document.getElementById('search-overlay');
    const closeSearchPanel = document.getElementById('close-search-panel');
    const navSearchInput = document.getElementById('search-input');
    const searchResultsContainer = document.getElementById('search-results-container');
    const searchEmpty = document.getElementById('search-empty');
    const resultsWrapper = document.querySelector('.search-results-wrapper');
    const suggestionsSection = document.querySelector('.search-suggestions');
    const typeaheadContainer = document.getElementById('search-typeahead');
    const searchChipGroup = document.querySelector('.search-chip-group');

    if (!searchPanel) return;

    let searchTimeout = null;
    let isSearchOpen = false;

    // --- Utility Functions ---
    function formatCurrency(amount) {
        const safeAmount = typeof amount === 'number' && !isNaN(amount) ? amount : 0;
        return `Rs ${safeAmount.toLocaleString('en-IN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })}`;
    }

    function escapeRegExp(value) {
        if (!value) return '';
        return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    function capitalizeWords(value) {
        if (!value) return '';
        return value.split(' ').filter(Boolean).map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
    }

    function formatProductMeta(product) {
        if (product.type === 'COLLECTION') {
            // Responsive font size for collection tag
            return '<span class="text-gold font-medium tracking-wide text-[9px] md:text-[10px] uppercase">Collection</span>';
        }
        const parts = [product.gemstone, product.size, product.color]
            .filter(Boolean)
            .map(capitalizeWords);
        return parts.join(' • ');
    }

    // --- Search Panel Controls ---
    function openSearchPanel() {
        if (!searchPanel || !searchOverlay || isSearchOpen) return;
        isSearchOpen = true;
        searchPanel.classList.add('open');
        searchPanel.setAttribute('aria-hidden', 'false');
        searchOverlay.classList.add('show');
        document.body.style.overflow = 'hidden';
        document.documentElement.style.overflow = 'hidden';

        if (navSearchInput) {
            // Load default suggestions (Best Sellers)
            fetchAndRenderSuggestions('');
            setTimeout(() => navSearchInput.focus(), 400);
        }
        if (suggestionsSection) suggestionsSection.classList.add('is-active');
    }

    function closeSearchPanelFunc() {
        if (!searchPanel || !searchOverlay || !isSearchOpen) return;
        isSearchOpen = false;
        searchPanel.classList.remove('open');
        searchPanel.setAttribute('aria-hidden', 'true');
        searchOverlay.classList.remove('show');
        document.body.style.overflow = '';
        document.documentElement.style.overflow = '';

        if (navSearchInput) navSearchInput.value = '';
        if (searchResultsContainer) searchResultsContainer.innerHTML = '';
        if (searchEmpty) searchEmpty.style.display = 'none';
        if (resultsWrapper) resultsWrapper.classList.remove('is-active');
        if (suggestionsSection) suggestionsSection.classList.remove('is-active');

        if (typeaheadContainer) {
            typeaheadContainer.innerHTML = '';
            typeaheadContainer.classList.remove('active');
        }
    }

    // --- Fetch and Render Suggestions (Chips or Typeahead) ---
    function fetchAndRenderSuggestions(query = '') {
        const url = query
            ? `${SUGGESTIONS_API}?q=${encodeURIComponent(query)}`
            : SUGGESTIONS_API;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                const isTypeahead = query.length > 0;
                if (isTypeahead) {
                    renderTypeahead(data, query);
                } else {
                    renderChips(data);
                }
            })
            .catch(error => console.error("Error fetching suggestions:", error));
    }

    function renderChips(data) {
        if (!searchChipGroup) return;
        searchChipGroup.innerHTML = '';

        let allSuggestions = [];
        let seenNames = new Set();
        
        if (data && typeof data === 'object') {
            Object.values(data).forEach(list => {
                if (Array.isArray(list)) {
                    list.forEach(item => {
                        // Support both legacy string array and new object array
                        const name = typeof item === 'string' ? item : item.name;
                        if (!seenNames.has(name)) {
                            seenNames.add(name);
                            allSuggestions.push(typeof item === 'string' ? { name: item, type: 'PRODUCT_NAME' } : item);
                        }
                    });
                }
            });
        }

        const topSuggestions = allSuggestions.slice(0, 10); // User requested 10

        topSuggestions.forEach(suggestion => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'search-chip text-xs md:text-sm';
            button.setAttribute('data-search-suggestion', suggestion.name);
            button.textContent = capitalizeWords(suggestion.name);
            
            button.addEventListener('click', function () {
                if (suggestion.type === 'PRODUCT') {
                    window.location.href = `/product-detail.html?id=${suggestion.productId}`;
                } else if (suggestion.type === 'COLLECTION') {
                    window.location.href = `/collection-details.html?id=${suggestion.productId}`;
                } else {
                    // Fallback to searching the string
                    navSearchInput.value = suggestion.name;
                    navSearchInput.focus();
                    performSearch(suggestion.name);
                }
            });
            searchChipGroup.appendChild(button);
        });
    }

    function renderTypeahead(data, query) {
        if (!typeaheadContainer) return;
        typeaheadContainer.innerHTML = '';

        let hasResults = false;

        if (data && typeof data === 'object') {
            Object.entries(data).forEach(([category, items]) => {
                if (Array.isArray(items) && items.length > 0) {
                    hasResults = true;
                    const header = document.createElement('div');
                    header.className = 'text-[10px] md:text-xs text-gray-400 font-bold uppercase tracking-wider px-3 mt-2 mb-1';
                    header.textContent = category;
                    typeaheadContainer.appendChild(header);

                    items.forEach(item => {
                        const name = typeof item === 'string' ? item : item.name;
                        const div = document.createElement('div');
                        div.className = 'p-2 md:p-3 hover:bg-gray-100 cursor-pointer text-sm text-gray-700';
                        
                        const regex = new RegExp(`(${escapeRegExp(query)})`, 'gi');
                        div.innerHTML = name.replace(regex, '<strong>$1</strong>');

                        div.addEventListener('click', () => {
                            if (typeof item === 'object' && item.type === 'PRODUCT') {
                                window.location.href = `/product-detail.html?id=${item.productId}`;
                            } else if (typeof item === 'object' && item.type === 'COLLECTION') {
                                window.location.href = `/collection-details.html?id=${item.productId}`;
                            } else {
                                navSearchInput.value = name;
                                performSearch(name);
                            }
                            typeaheadContainer.classList.remove('active');
                        });
                        typeaheadContainer.appendChild(div);
                    });
                }
            });
        }

        if (hasResults) {
            typeaheadContainer.classList.add('active');
        } else {
            typeaheadContainer.classList.remove('active');
        }
    }

    // --- Perform Search (Main Logic) ---
    function performSearch(rawQuery) {
        if (!searchResultsContainer || !searchEmpty) return;
        const query = (rawQuery || '').trim();

        if (typeaheadContainer) typeaheadContainer.classList.remove('active');

        if (!query) {
            searchResultsContainer.innerHTML = '';
            searchEmpty.style.display = 'block';
            resultsWrapper.classList.remove('is-active');
            suggestionsSection.classList.add('is-active');
            fetchAndRenderSuggestions('');
            return;
        }

        searchEmpty.style.display = 'none';
        resultsWrapper.classList.add('is-active');
        suggestionsSection.classList.remove('is-active');

        // Responsive loading icon and text
        searchResultsContainer.innerHTML = `<div class="search-no-results py-8"><i class="fas fa-spinner fa-spin text-2xl md:text-3xl text-gold mb-3"></i><p class="text-xs md:text-sm text-gray-500">Searching...</p></div>`;

        const searchUrl = `${PRODUCTS_API_SEARCH}?q=${encodeURIComponent(query)}`;
        fetch(searchUrl)
            .then(r => r.json())
            .then(results => {
                if (results && results.error) {
                    searchResultsContainer.innerHTML = `<div class="search-no-results py-8"><p class="text-xs md:text-sm text-red-500">Search Error. Please try again.</p></div>`;
                    return;
                }

                const currentSearchResults = Array.isArray(results) ? results : [];

                if (currentSearchResults.length === 0) {
                    // Responsive no results state
                    searchResultsContainer.innerHTML = `
                        <div class="search-no-results py-12 text-center">
                            <i class="fas fa-search text-3xl md:text-4xl text-gray-200 mb-4"></i>
                            <h3 class="text-gray-800 text-sm md:text-base font-medium mb-1">No results found</h3>
                            <p class="text-xs md:text-sm text-gray-500">Try checking your spelling or using different keywords.</p>
                        </div>`;
                    return;
                }

                const highlightRegex = new RegExp(`(${escapeRegExp(query)})`, 'gi');
                const highlight = val => val ? val.replace(highlightRegex, '<mark class="bg-yellow-100 text-gray-900">$1</mark>') : '';

                // Generate Cleaner, Cart-free List
                searchResultsContainer.innerHTML = currentSearchResults.map(item => {
                    const isCollection = item.type === 'COLLECTION';
                    const image = item.image || FALLBACK_PRODUCT_IMAGE;
                    const meta = formatProductMeta(item);
                    const cleanName = (item.name || '').replace(/\s*\([^)]*\)/g, '').replace(/\s*-\s*[^-]+$/g, '').trim();
                    const highlightedName = highlight(cleanName);

                    // Unified link for the entire row
                    const linkHref = isCollection ? `collection-details.html?id=${item.productId}` : `product-detail.html?id=${item.varianceId}`;

                    // Elegant layout: Image - Info - Price (no buttons)
                    // Updated classes for responsiveness:
                    // 1. Padding: p-2 (mobile) -> md:p-3 (desktop)
                    // 2. Image: h-12 w-12 (mobile) -> md:h-14 md:w-14 (desktop)
                    // 3. Name: text-sm (mobile) -> md:text-base (desktop)
                    // 4. Meta: text-[10px] (mobile) -> md:text-xs (desktop)
                    // 5. Price: text-sm (mobile) -> md:text-base (desktop)
                    return `
                        <a href="${linkHref}" class="group block border-b border-gray-100 last:border-0 hover:bg-gray-50 transition-colors duration-200">
                            <div class="flex items-center p-2 md:p-3">
                                <!-- Thumb -->
                                <div class="h-12 w-12 md:h-14 md:w-14 flex-shrink-0 overflow-hidden border border-gray-100 bg-white">
                                    <img src="${image}" alt="${cleanName}" class="h-full w-full object-cover object-center group-hover:opacity-90 transition-opacity" loading="lazy">
                                </div>
                                
                                <!-- Details -->
                                <div class="ml-3 md:ml-4 flex-1 min-w-0 pr-2 md:pr-4">
                                    <p class="font-serif text-sm md:text-base text-gray-900 truncate group-hover:text-gold transition-colors">${highlightedName}</p>
                                    ${meta ? `<p class="mt-0.5 text-[10px] md:text-xs text-gray-500 truncate">${isCollection ? meta : highlight(meta)}</p>` : ''}
                                </div>
                                
                                <!-- Price -->
                                <div class="flex-shrink-0 text-right">
                                    <span class="block font-serif text-sm md:text-base font-bold text-gray-900">${formatCurrency(item.price)}</span>
                                </div>
                            </div>
                        </a>`;
                }).join('');
            })
            .catch(err => {
                console.error(err);
                searchResultsContainer.innerHTML = `<div class="search-no-results py-8"><p class="text-xs md:text-sm text-red-500">An error occurred.</p></div>`;
            });
    }

    // --- Event Listeners ---
    if (searchBtn) searchBtn.addEventListener('click', openSearchPanel);
    if (mobileSearchBtn) mobileSearchBtn.addEventListener('click', openSearchPanel);
    if (closeSearchPanel) closeSearchPanel.addEventListener('click', closeSearchPanelFunc);
    if (searchOverlay) searchOverlay.addEventListener('click', closeSearchPanelFunc);

    if (navSearchInput) {
        navSearchInput.addEventListener('input', function (event) {
            clearTimeout(searchTimeout);
            const query = event.target.value;

            // 1. Trigger Suggestions immediately (One letter typing)
            fetchAndRenderSuggestions(query);

            // 2. Trigger Full Search (Debounced)
            if (query.length > 0) {
                searchTimeout = setTimeout(() => performSearch(query), DEBOUNCE_DELAY_MS);
            } else {
                searchResultsContainer.innerHTML = '';
                searchEmpty.style.display = 'block';
                // If empty, show default suggestions again
                fetchAndRenderSuggestions('');
            }
        });

        navSearchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                clearTimeout(searchTimeout);
                performSearch(navSearchInput.value);
                if(typeaheadContainer) typeaheadContainer.classList.remove('active');
            }
        });
    }

    if (!window.formatCurrency) window.formatCurrency = formatCurrency;

    // Initial fetch for default suggestions
    fetchAndRenderSuggestions('');

    window.closeSearchPanelFunc = closeSearchPanelFunc;
    window.openSearchPanel = openSearchPanel;
}

document.addEventListener('navbar-loaded', initSearchController);
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('search-panel')) {
        initSearchController();
    }
});