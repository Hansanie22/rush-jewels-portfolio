// --- Globals ---
let currentCollection = null;
let userLoggedIn = false;

// Elements
const stockStatusContainer = document.querySelector('.inventory-status')?.parentElement;
const addToCartBtn = document.querySelector('[data-add-to-cart]');
const buyNowBtn = document.querySelector('.buy-now');
const mainDescriptionContainer = document.querySelector('[data-product-description]');
const specsEl = document.querySelector('[data-product-specs]');
const summaryEl = document.querySelector('[data-product-summary]');
const productDetailsBox = document.querySelector('.brand-panel-heading')?.nextElementSibling;
const relatedSection = document.getElementById('related-collections-container')?.parentElement?.parentElement;

// Image Elements
const mainImage = document.querySelector('[data-product-image]');
const mainImageContainer = document.querySelector('[data-product-image-container]');
const galleryThumbContainer = document.getElementById('gallery-thumbs');
const titleEl = document.querySelector('[data-product-title]');

// Quantity Elements
const decreaseBtn = document.querySelector('[data-quantity-decrease]');
const increaseBtn = document.querySelector('[data-quantity-increase]');
const quantityInput = document.querySelector('[data-quantity-input]');

document.addEventListener('DOMContentLoaded', () => {
    // 1. Force Show Loader immediately
    if (window.loader) window.loader.show();

    const params = new URLSearchParams(window.location.search);
    const collectionId = params.get('id');

    if (collectionId) {
        loadCollectionDetails(collectionId);
    } else {
        window.location.href = 'index.html';
    }

    // Init Interactions
    initZoom();
    initQuantityListeners();
    initViewAllButtons();
    initBuyNowListener();
});

// ✅ 1. ශ්‍රිතය window object එකට සම්බන්ධ කර ලෝඩරය එක් කිරීම
window.loadCollectionDetails = async function(id) {
    if (window.loader) window.loader.show();

    try {
        const response = await fetch(`/api/collections/details?id=${id}`);
        const data = await response.json();

        if (data.status && data.collection) {
            currentCollection = data.collection;
            renderPage(data);
            return true; // සාර්ථක බව හඟවයි
        } else {
            const mainContent = document.getElementById('main-content');
            if (mainContent) {
                mainContent.innerHTML = '<div class="text-center py-20"><h3>Collection not found</h3></div>';
            }
            return false;
        }
    } catch (error) {
        console.error("Error fetching collection details:", error);
        return false;
    } finally {
        // ✅ සියල්ල අවසන් වූ පසු පිටුව පෙන්වීම
        revealCollectionPage();
    }
};

/**
 * පිටුව පෙන්වන සහ ලෝඩරය අයින් කරන Helper Function එක
 */
function revealCollectionPage() {
    const mainContent = document.getElementById('main-content');
    if (mainContent) {
        mainContent.style.display = 'block';
        mainContent.classList.add('animate__animated', 'animate__fadeIn');
    }
    if (window.loader) {
        // පින්තූර Render වීමට තත්පර 0.5ක සහනයක් ලබා දෙන්න
        setTimeout(() => {
            window.loader.hide();
        }, 500);
    }
}

// ⚠️ Fallback Initialization
document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const collectionId = params.get('id');

    // Master Script එක නැතිනම් පමණක් මෙය ක්‍රියාත්මක වේ
    const isMasterScriptPresent = window.loader && document.getElementById('main-content');
    if (!isMasterScriptPresent && collectionId) {
        window.loadCollectionDetails(collectionId);
    }
});

function renderPage(data) {
    const c = data.collection;

    // 1. Basic Info
    setText('[data-product-title]', c.title);
    setText('[data-product-price]', formatCurrency(c.price));

    // Koko/Mint removed based on user request

    // 2. Images & Gallery
    if (c.images && c.images.length > 0) {
        populateGallery(c.images);
    } else {
        populateGallery([c.image]);
    }

    // 3. Stock Status
    updateStock(c);

    // 4. Description
    populateDescription(c.description);

    // 5. Specs & Details
    updateProductDetails(c);

    // 6. Box Contents
    if (c.collectionItems && c.collectionItems.length > 0) {
        renderBoxItems(c.collectionItems);
    } else {
        const boxSection = document.getElementById('box-contents-section');
        if(boxSection) boxSection.style.display = 'none';
    }

    // 7. Related Collections
    if (data.relatedCollections) {
        renderRelated(data.relatedCollections);
    }

    initViewAllButtons();
}

// --- Helper: Check Session ---
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
        if(window.notify) window.notify.warning('Please login to your account.');
        sessionStorage.setItem('returnUrl', window.location.href);
        setTimeout(() => window.location.href = 'auth.html', 1500);
        userLoggedIn = false;
        return false;
    }
}

// --- Image Logic ---

function populateGallery(images) {
    if (!images?.length || !mainImage || !galleryThumbContainer) return;

    galleryThumbContainer.innerHTML = '';

    mainImage.src = images[0];
    mainImage.alt = titleEl?.textContent || 'Collection Image';

    images.forEach((src, i) => {
        const btn = document.createElement('button');
        btn.className = 'gallery-thumb border transition-all duration-200 focus:outline-none';
        btn.innerHTML = `<img src="${src}" alt="Gallery image ${i + 1}" class="h-20 w-20 object-cover" loading="lazy">`;

        btn.addEventListener('click', () => {
            mainImage.src = src;
            document.querySelectorAll('.gallery-thumb').forEach(b => b.classList.remove('is-active'));
            btn.classList.add('is-active');
        });

        galleryThumbContainer.appendChild(btn);
    });

    if (galleryThumbContainer.firstChild) {
        galleryThumbContainer.firstChild.classList.add('is-active');
    }
}

function initZoom() {
    if (!mainImage || !mainImageContainer) return;
    const scale = 2.2;
    let active = false;

    mainImageContainer.addEventListener('mouseenter', (e) => {
        active = true;
        mainImage.style.transition = 'transform 0.2s ease';
        updateZoom(e);
    });

    mainImageContainer.addEventListener('mousemove', updateZoom);

    mainImageContainer.addEventListener('mouseleave', () => {
        active = false;
        mainImage.style.transform = '';
        mainImage.style.transformOrigin = 'center center';
    });

    function updateZoom(e) {
        if (!active) return;
        const rect = mainImageContainer.getBoundingClientRect();
        const x = ((e.clientX - rect.left) / rect.width) * 100;
        const y = ((e.clientY - rect.top) / rect.height) * 100;
        mainImage.style.transformOrigin = `${x}% ${y}%`;
        mainImage.style.transform = `scale(${scale})`;
    }
}

// --- Quantity & Cart Logic ---

function initQuantityListeners() {
    decreaseBtn?.addEventListener('click', () => {
        const current = parseInt(quantityInput.value) || 1;
        quantityInput.value = Math.max(1, current - 1);
        updateAddToCartButtonState();
    });

    increaseBtn?.addEventListener('click', () => {
        const current = parseInt(quantityInput.value) || 1;
        const max = currentCollection ? currentCollection.stockLimit : 99;

        if (current < max) {
            quantityInput.value = current + 1;
        }
        updateAddToCartButtonState();
    });

    quantityInput?.addEventListener('change', () => {
        const current = parseInt(quantityInput.value) || 1;
        const max = currentCollection ? currentCollection.stockLimit : 99;

        if (current < 1) quantityInput.value = 1;
        if (current > max) quantityInput.value = max;

        updateAddToCartButtonState();
    });

    if (addToCartBtn) {
        addToCartBtn.setAttribute('data-cart-attached', 'true');
    }

    addToCartBtn?.addEventListener('click', (e) => {
        e.preventDefault();
        if (!currentCollection) return;
        if (addToCartBtn.disabled) return;

        const qty = parseInt(quantityInput.value) || 1;
        if (qty > currentCollection.stockLimit) return;

        const img = (currentCollection.images && currentCollection.images.length > 0)
            ? currentCollection.images[0]
            : currentCollection.image;

        if (typeof window.enhancedAddToCart === 'function') {
            window.enhancedAddToCart(
                currentCollection.id,
                currentCollection.title,
                currentCollection.price,
                img,
                qty,
                true
            );

            if (typeof window.createFloatingCartAnimation === 'function') {
                window.createFloatingCartAnimation(addToCartBtn);
            }
        } else {
            console.error("Cart functionality (cart.js) is not loaded.");
        }
    });
}

// --- Buy Now Logic ---
function initBuyNowListener() {
    buyNowBtn?.addEventListener('click', async (e) => {
        e.preventDefault();

        if (!currentCollection) return;

        const loggedIn = await checkUserSession();
        if (!loggedIn) return;

        const requestedQty = parseInt(quantityInput.value) || 1;
        const availableQty = currentCollection.stockLimit || 0;

        if (availableQty <= 0) {
            if(window.notify) window.notify.warning('This collection is currently out of stock.');
            return;
        }

        if (requestedQty > availableQty) {
            if(window.notify) window.notify.warning(`Only ${availableQty} item(s) available.`);
            return;
        }

        try {
            const res = await fetch('/api/order/buy-now-session', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    collectionId: currentCollection.id,
                    quantity: requestedQty
                })
            });

            const result = await res.json();
            if (!result.status) {
                if(window.notify) window.notify.error(result.message || 'Cannot proceed to checkout.');
                return;
            }

            window.location.href = '/checkout.html?mode=buynow';

        } catch (err) {
            console.error(err);
            if(window.notify) window.notify.error('Something went wrong.');
        }
    });
}

function updateAddToCartButtonState() {
    const qty = parseInt(quantityInput.value) || 0;
    const max = currentCollection ? currentCollection.stockLimit : 0;

    if (addToCartBtn) {
        if (max === 0) {
            addToCartBtn.disabled = true;
            addToCartBtn.textContent = 'Sold Out';
            addToCartBtn.classList.add('opacity-50', 'cursor-not-allowed');
        } else {
            addToCartBtn.disabled = false;
            if (addToCartBtn.textContent === 'Sold Out') {
                addToCartBtn.innerHTML = '<i class="fas fa-shopping-bag text-base"></i> Add to Cart';
                addToCartBtn.classList.remove('opacity-50', 'cursor-not-allowed');
            }
        }
    }

    if (buyNowBtn) {
        if (max === 0) {
            buyNowBtn.disabled = true;
            buyNowBtn.classList.add('opacity-50', 'cursor-not-allowed');
        } else {
            buyNowBtn.disabled = false;
            buyNowBtn.classList.remove('opacity-50', 'cursor-not-allowed');
        }
    }
}


// --- View All Logic ---

function initViewAllButtons() {
    const relatedSection = document.querySelector('.mt-20');
    if (relatedSection) {
        relatedSection.querySelectorAll('button').forEach(button => {
            if (button.textContent.trim().toLowerCase().includes('view all')) {
                const link = document.createElement('a');
                link.href = 'collection.html';
                link.className = button.className;
                link.innerHTML = button.innerHTML;
                button.parentNode.replaceChild(link, button);
            }
        });
    }
}


// --- Content Rendering Helpers ---

function updateStock(collection) {
    if (!collection || !stockStatusContainer) return;

    if (collection.stockLimit > 0) {
        stockStatusContainer.innerHTML = `
        <span class="inline-flex h-6 w-6 items-center justify-center rounded-full inventory-status">
            <i class="fas fa-check text-xs inventory-status-icon"></i>
        </span>
        <span class="text-sm font-medium brand-accent">
            In stock (${collection.stockLimit} available)
        </span>
        `;
        quantityInput.max = collection.stockLimit;
        if(quantityInput.value > collection.stockLimit) quantityInput.value = collection.stockLimit;
        updateAddToCartButtonState();
    } else {
        stockStatusContainer.innerHTML = `
        <span class="inline-flex h-6 w-6 items-center justify-center rounded-full bg-red-100 text-red-600">
            <i class="fas fa-times text-xs"></i>
        </span>
        <span class="text-sm font-medium text-red-600">Out of Stock</span>
        `;
        quantityInput.value = 0;
        updateAddToCartButtonState();
    }
}

function populateDescription(descriptionString) {
    if (!mainDescriptionContainer || !descriptionString) return;

    let descriptionData;
    try {
        descriptionData = JSON.parse(descriptionString);
    } catch (e) {
        mainDescriptionContainer.innerHTML = `<h2 class="text-lg font-semibold text-dark">Description</h2><p class="mt-2">${descriptionString}</p>`;
        return;
    }

    let html = '<h2 class="text-lg font-semibold text-dark">Description</h2>';

    if (descriptionData.main) {
        if (Array.isArray(descriptionData.main)) {
            descriptionData.main.forEach(p => html += `<p class="mt-2">${p}</p>`);
        } else {
            html += `<p class="mt-2">${descriptionData.main}</p>`;
        }
    }

    if (descriptionData.styling_tips?.length > 0) {
        html += `
        <div class="space-y-3 mt-6">
            <h3 class="font-semibold text-dark">Styling Tips</h3>
            <ul class="list-disc space-y-2 pl-5">
                ${descriptionData.styling_tips.map(tip => `<li>${tip}</li>`).join('')}
            </ul>
        </div>`;
    }

    if (descriptionData.care_details?.length > 0) {
        html += `
        <div class="space-y-3 mt-6">
            <h3 class="font-semibold text-dark">Care Details</h3>
            <ul class="list-disc space-y-2 pl-5">
                ${descriptionData.care_details.map(detail => `<li>${detail}</li>`).join('')}
            </ul>
        </div>`;
    }
    mainDescriptionContainer.innerHTML = html;
}

function updateProductDetails(collection) {
    if (specsEl) {
        let specs = [];
        if (Array.isArray(collection.specifications)) {
            specs = collection.specifications;
        } else if (typeof collection.specifications === 'string') {
            specs = collection.specifications.split(/\r?\n|•|,/).map(s => s.trim()).filter(Boolean);
        }
        if (specs.length > 0) {
            specsEl.innerHTML = specs.map(s => `<li>${s}</li>`).join('');
            specsEl.parentElement.style.display = 'block';
        } else {
            specsEl.parentElement.style.display = 'none';
        }
    }

    if (summaryEl && collection.title) {
        summaryEl.textContent = collection.title;
    }

    if (productDetailsBox) {
        const details = [];
        const totalPieces = collection.collectionItems ? collection.collectionItems.length : 0;
        if (totalPieces > 0) details.push({label: 'Total Pieces', value: `${totalPieces} Items`});
        if (collection.material) details.push({label: 'Material', value: collection.material});
        if (collection.warranty) details.push({label: 'Warranty', value: collection.warranty});
        productDetailsBox.innerHTML = details.length ? details.map((d, i) => `
            <div class="flex items-center justify-between ${i < details.length - 1 ? 'border-b border-gray-100 pb-3' : ''}">
                <span>${d.label}</span>
                <span class="font-medium text-dark">${d.value}</span>
            </div>
        `).join('') : '<p>No specs available.</p>';
    }
}

function renderBoxItems(items) {
    const container = document.getElementById('box-contents-list');
    if (!container) return;
    container.innerHTML = items.map(item => `
        <div class="flex items-start gap-4 bg-white p-3 shadow-sm brand-panel">
            <div class="h-16 w-16 flex-shrink-0 overflow-hidden rounded-sm bg-gray-100">
                <img src="${item.image}" class="h-full w-full object-cover" alt="${item.productTitle}" onerror="this.src='images/placeholder-jewelry.jpg'" loading="lazy">
            </div>
            <div>
                <h4 class="text-sm font-semibold text-dark">${item.qty}x ${item.productTitle}</h4>
                <p class="text-xs text-gray-500 mt-1">${item.variantName}</p>
                <p class="text-xs mt-1">${item.productSubtext}</p>
            </div>
        </div>
    `).join('');
}

// --- NEW HELPER FUNCTIONS FOR CARD DESIGN ---

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
    const btnText = isOut ? 'Sold Out' : 'Add Collection';

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

function renderRelated(collections) {
    const container = document.getElementById('related-collections-container');
    if (!container) return;

    if (!collections || collections.length === 0) {
        container.closest('section.mt-20')?.remove();
        return;
    }

    container.innerHTML = collections.map(col => {
        const formattedPrice = formatCurrency(col.price);
        const { badgeHtml, btnState, btnText, stockStatusHtml } = getCollectionState(col);
        const ratingHtml = generateRatingHtml(col.averageRating, col.reviewCount);
        const imgUrl = col.image || 'images/placeholder-collection.jpg';

        return `
        <div class="product-card shadow-sm group flex-none w-72 md:w-full snap-start" onclick="window.location.href='collection-details.html?id=${col.id}'">
            ${badgeHtml}
            
            <div class="product-image h-64">
                <img src="${imgUrl}" alt="${col.name}" loading="lazy">
                <button class="view-product-btn" onclick="event.stopPropagation(); window.location.href='collection-details.html?id=${col.id}'">View Collection</button>
            </div>

            <div class="product-info">
                <p class="text-xs text-gray-400 uppercase tracking-wider font-semibold">RUSH JEWELS</p>
                <h3 class="product-title line-clamp-2" title="${col.title}">${col.title}</h3>
                
                ${ratingHtml}

                <p class="product-price">${formattedPrice}</p>
                
                <div class="product-availability">
                    ${stockStatusHtml}
                </div>

                <button class="add-to-cart-btn"
                        data-collection-id="${col.id}"
                        data-product-name="${col.title} (Collection)" 
                        data-product-price="${col.price}" 
                        data-product-image="${imgUrl}" 
                        data-stock-status="${col.stockStatus}"
                        data-product-quantity="1"
                        ${btnState}>
                    <i class="fas fa-shopping-bag mr-2"></i> ${btnText}
                </button>
            </div>
        </div>
        `;
    }).join('');

    const parentPanel = container.parentElement;
    if (parentPanel && !parentPanel.querySelector('.mobile-view-all-btn')) {
        const mobileBtn = document.createElement('a');
        mobileBtn.href = 'collection.html';
        mobileBtn.className = 'mobile-view-all-btn mt-8 inline-flex items-center gap-2 text-sm font-medium text-dark transition-colors hover:text-gold md:hidden';
        mobileBtn.innerHTML = 'View all <i class="fas fa-arrow-right text-xs"></i>';
        parentPanel.appendChild(mobileBtn);
    }

    if (typeof window.attachCartFunctionality === 'function') {
        window.attachCartFunctionality();
    }
}

function setText(selector, text) { const el = document.querySelector(selector); if (el) el.textContent = text; }
function formatCurrency(amount) { return 'LKR ' + amount.toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }