window.loadBlogPost = async function() {
    // පටන් ගන්නා විට ලෝඩරය පෙන්වීම
    if (window.loader) window.loader.show();

    const urlParams = new URLSearchParams(window.location.search);
    const postId = urlParams.get('id');

    const errorState = document.getElementById('error-state');
    const contentContainer = document.getElementById('blog-content');

    if (!postId) {
        showError(errorState);
        if (window.loader) window.loader.hide();
        return false;
    }

    try {
        const response = await fetch(`/api/public/post/details?id=${postId}`);

        if (!response.ok) throw new Error('Post not found');

        const post = await response.json();

        // --- Standard Blog Content Mapping ---
        document.title = `${post.title} | Velora Fine Jewellery`;
        document.getElementById('post-title').textContent = post.title;
        document.getElementById('breadcrumb-title').textContent = post.title.length > 30 ? post.title.substring(0, 30) + '...' : post.title;
        document.getElementById('post-date').textContent = post.date;
        document.getElementById('post-readtime').textContent = post.readTime;
        document.getElementById('post-category').textContent = post.category || 'Jewelry';

        const imgEl = document.getElementById('post-image');
        imgEl.src = post.imagePath;
        imgEl.onerror = function() {
            this.src = 'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=1200&q=80';
        };

        const tagsContainer = document.getElementById('tags-container');
        if (post.tags && post.tags.length > 0) {
            tagsContainer.innerHTML = post.tags.map(tag =>
                `<span class="bg-gray-100 text-gray-600 px-3 py-1 text-xs uppercase tracking-wider font-semibold">${tag}</span>`
            ).join('');
        }

        const bodyContainer = document.getElementById('post-body');
        if (post.content && post.content.includes('<p>')) {
            bodyContainer.innerHTML = post.content;
        } else if (post.content) {
            bodyContainer.innerHTML = post.content.split('\n').map(paragraph => {
                if (paragraph.trim() === '') return '';
                return `<p>${paragraph}</p>`;
            }).join('');
        }

        // --- SHOP THE LOOK / RELATED ITEMS ---
        if (post.relatedItems && post.relatedItems.length > 0) {
            renderRelatedItems(post.relatedItems);
        }

        return true; // සාර්ථක බව හඟවයි

    } catch (error) {
        console.error('Error loading blog post:', error);
        showError(errorState);
        return false;
    } finally {
        // ✅ 2. සියල්ල අවසන් වූ පසු පිටුව පෙන්වා ලෝඩරය අයින් කිරීම
        revealContent();
    }
};

/**
 * පිටුව පෙන්වන සහ ලෝඩරය අයින් කරන Helper Function එක
 */
function revealContent() {
    const main = document.getElementById('main-content');
    const contentContainer = document.getElementById('blog-content');

    if (main) {
        main.style.display = 'block';
        main.classList.add('animate__animated', 'animate__fadeIn');
    }

    if (contentContainer) {
        contentContainer.classList.remove('opacity-0');
        contentContainer.classList.add('opacity-100');
    }

    if (window.loader) {
        // පින්තූර සම්පූර්ණයෙන් Render වීමට තත්පර 0.5ක සහනයක් ලබා දෙන්න
        setTimeout(() => {
            window.loader.hide();
        }, 500);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (!window.loader || !document.getElementById('main-content')) {
        window.loadBlogPost();
    }
});

function renderRelatedItems(items) {
    const section = document.getElementById('shop-the-look-container');
    const grid = document.getElementById('related-items-grid');

    if (!items || items.length === 0) return;

    section.classList.remove('hidden');
    grid.innerHTML = '';

    items.forEach(item => {
        const p = {
            varianceId: item.id,
            title: item.title,
            name: item.name || item.title,
            price: item.price,
            regularPrice: item.regularPrice,
            discountPercentage: item.discountPercentage || 0,
            currentStockQty: item.currentStockQty || 0,
            stockStatus: item.stockStatus || (item.currentStockQty > 0 ? "In Stock" : "Out of Stock"),
            image: item.imagePath,
            averageRating: item.averageRating || 0,
            reviewCount: item.reviewCount || 0,
            type: item.type,
            tags: []
        };

        const formattedPrice = formatPriceLKR(p.price);
        const { badgeHtml, btnState, btnText, stockStatusHtml } = getProductState(p);
        const ratingHtml = generateRatingHtml(p.averageRating, p.reviewCount);
        const displayTitle = p.title || p.name;
        const imgUrl = p.image && p.image !== "/images/default.png" ? p.image : 'https://placehold.co/600x600?text=No+Image';

        let detailPage = p.type === 'COLLECTION' ? 'collection-details.html' : 'product-detail.html';
        const itemUrl = `${detailPage}?id=${p.varianceId}`;

        const productCard = document.createElement("div");
        productCard.className = "product-card w-full shadow-sm group animate__animated animate__fadeInUp";
        productCard.dataset.productId = p.varianceId;
        productCard.onclick = () => window.location.href = itemUrl;

        productCard.innerHTML = `
            ${badgeHtml}
            <div class="product-image h-64">
                <img src="${imgUrl}" alt="${p.name}" loading="lazy">
                <button class="view-product-btn" onclick="event.stopPropagation(); window.location.href='${itemUrl}'">View Product</button>
            </div>
            <div class="product-info">
                <p class="text-xs text-gray-400 uppercase tracking-wider font-semibold">VELORA FINE JEWELLERY</p>
                <h3 class="product-title line-clamp-2" title="${displayTitle}">${displayTitle}</h3>
                ${ratingHtml}
                <p class="product-price">${formattedPrice}</p>
                <div class="product-availability">${stockStatusHtml}</div>
                <button class="add-to-cart-btn"
                        data-product-id="${p.varianceId}"
                        data-product-name="${p.name}"
                        data-product-price="${p.price}"
                        data-product-image="${imgUrl}"
                        data-stock-status="${p.stockStatus}"
                        data-stock-qty="${p.currentStockQty}"
                        ${btnState}>
                    <i class="fas fa-shopping-bag mr-2"></i> ${btnText}
                </button>
            </div>
        `;
        grid.appendChild(productCard);
    });

    if (typeof window.attachCartFunctionality === 'function') {
        window.attachCartFunctionality();
    }
}

function showError(errorDiv) {
    if (errorDiv) errorDiv.classList.remove('hidden');
}

// --- HELPER FUNCTIONS (Design Intact) ---

function formatPriceLKR(amount) {
    const safeAmount = typeof amount === 'number' && !isNaN(amount) ? amount : parseFloat(String(amount).replace(/[^0-9.]/g, '')) || 0;
    return "LKR " + safeAmount.toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
}

function getProductState(product) {
    const isNew = product.tags?.includes('New Arrival');
    const statusLower = (product.stockStatus || '').toLowerCase();
    const isOut = statusLower === 'out of stock' || product.currentStockQty <= 0;

    let badgeHtml = '';
    if (isOut) badgeHtml = '<span class="badge bg-dark text-white text-xs font-bold px-3 py-1 uppercase">SOLD OUT</span>';
    else if (product.discountPercentage > 0) badgeHtml = `<span class="badge bg-red-600 text-white text-xs font-bold px-3 py-1 uppercase">-${Math.round(product.discountPercentage)}%</span>`;
    else if (isNew) badgeHtml = '<span class="badge bg-gold text-dark text-xs font-bold px-3 py-1 uppercase">New</span>';

    const btnState = isOut ? 'disabled' : '';
    const btnText = isOut ? 'Sold Out' : 'Add to Cart';

    let stockStatusHtml = isOut ? `<i class="fas fa-circle text-red-500" style="font-size: 6px;"></i><span>Out of stock</span>` : (product.currentStockQty < 5 && product.currentStockQty > 0 ? `<i class="fas fa-circle text-orange-500" style="font-size: 6px;"></i><span>Only ${product.currentStockQty} left in stock</span>` : `<i class="fas fa-circle text-green-500" style="font-size: 6px;"></i><span>In stock & ready to ship</span>`);

    return { badgeHtml, btnState, btnText, stockStatusHtml };
}

function generateRatingHtml(rating, count) {
    let avgRating = parseFloat(rating) || 0;
    const reviewCount = parseInt(count) || 0;
    if (reviewCount === 0) avgRating = 0;

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