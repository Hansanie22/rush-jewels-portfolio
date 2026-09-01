    // --- DOM Elements ---
    const mainImage = document.querySelector('[data-product-image]');
    const mainImageContainer = document.querySelector('[data-product-image-container]');
    const galleryThumbContainer = document.querySelectorAll('[data-gallery-thumb]')[0]?.parentElement || null;
    const titleEl = document.querySelector('[data-product-title]');
    const priceEl = document.querySelector('[data-product-price]');
    const categoryEl = document.querySelector('.brand-accent');
    const quantityInput = document.querySelector('[data-quantity-input]');
    const decreaseBtn = document.querySelector('[data-quantity-decrease]');
    const increaseBtn = document.querySelector('[data-quantity-increase]');
    const addToCartBtn = document.querySelector('[data-add-to-cart]');
    const buyNowBtn = document.querySelector('.buy-now');
    const descriptionContainer = document.querySelector('[data-product-description]');
    const summaryEl = descriptionContainer?.querySelector('[data-product-summary]');
    const specsEl = document.querySelector('[data-product-specs]');

    // --- Variant / Options Elements ---
    const dynamicOptionsWrapper = document.getElementById('dynamic-options-wrapper');

    const stockStatusContainer = document.querySelector('.inventory-status')?.parentElement || null;
    const mainDescriptionContainer = document.querySelector('.mt-16 .leading-relaxed');
    const productDetailsBox = document.querySelector('.brand-panel.p-6 .mt-6.space-y-4');

    // Select the Frequently Bought Together container using its classes
    const relatedProductsContainer = document.querySelector('.mt-8.flex.gap-6.overflow-x-auto.pb-4');

    // --- State ---
    let allVariancesData = [];
    let currentVariance = {};
    let currentProduct = {};
    let baseProductName = '';
    let userLoggedIn = false;

    // --- Helper Functions ---
    function formatCurrency(amount) {
        return `LKR ${Number(amount).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
    }

    // --- Helper Functions for Card Design (Shared Logic) ---
    function getProductState(product) {
        const stockQty = product.stockQty !== undefined ? product.stockQty : (product.currentStockQty || 0);
        const discountPercentage = product.discountPercentage || 0;
        const tags = product.tags || [];
        const stockStatus = product.stockStatus || (stockQty > 0 ? 'In Stock' : 'Out Of Stock');

        const isNew = tags.includes('New Arrival');
        const isOut = stockStatus === 'Out Of Stock' || stockStatus === 'Out of Stock' || stockQty <= 0;

        let badgeHtml = '';
        if (isOut) {
            badgeHtml = '<span class="badge bg-dark text-white text-xs font-bold px-3 py-1 uppercase">SOLD OUT</span>';
        } else if (discountPercentage > 0) {
            badgeHtml = `<span class="badge bg-red-600 text-white text-xs font-bold px-3 py-1 uppercase">-${Math.round(discountPercentage)}%</span>`;
        } else if (isNew) {
            badgeHtml = '<span class="badge bg-gold text-dark text-xs font-bold px-3 py-1 uppercase">New</span>';
        }

        const btnState = isOut ? 'disabled' : '';
        const btnText = isOut ? 'Sold Out' : 'Add to Cart';

        let stockStatusHtml = '';
        if (isOut) {
            stockStatusHtml = `<i class="fas fa-circle text-red-500" style="font-size: 6px;"></i><span>Out of stock</span>`;
        } else if (stockQty < 5 && stockQty > 0) {
            stockStatusHtml = `<i class="fas fa-circle text-orange-500" style="font-size: 6px;"></i><span>Only ${stockQty} left in stock</span>`;
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

    // --- UI Update Functions ---
    function updatePrice(variance) {
        if (!variance || !priceEl) return;
        priceEl.textContent = formatCurrency(variance.price);
    }

    function updateStock(variance) {
        if (!variance || !stockStatusContainer) return;

        if (variance.stockLimit > 0) {
            stockStatusContainer.innerHTML = `
            <span class="inline-flex h-6 w-6 items-center justify-center rounded-full inventory-status">
                <i class="fas fa-check text-xs inventory-status-icon"></i>
            </span>
            <span class="text-sm font-medium brand-accent">
                In stock (${variance.stockLimit} available)
            </span>
        `;
            if (addToCartBtn) {
                addToCartBtn.disabled = false;
                addToCartBtn.classList.remove('opacity-50', 'cursor-not-allowed');
                if (addToCartBtn.textContent === 'Sold Out') {
                    addToCartBtn.innerHTML = '<i class="fas fa-shopping-bag text-base"></i> Add to cart';
                }
            }
            if (buyNowBtn) {
                buyNowBtn.disabled = false;
                buyNowBtn.classList.remove('opacity-50', 'cursor-not-allowed');
            }
            if (quantityInput) quantityInput.disabled = false;
            if (decreaseBtn) decreaseBtn.disabled = false;
            if (increaseBtn) increaseBtn.disabled = false;
        } else {
            stockStatusContainer.innerHTML = `
            <span class="inline-flex h-6 w-6 items-center justify-center rounded-full bg-red-100 text-red-600">
                <i class="fas fa-times text-xs"></i>
            </span>
            <span class="text-sm font-medium text-red-600">Out of Stock</span>
        `;
            if (addToCartBtn) {
                addToCartBtn.disabled = true;
                addToCartBtn.textContent = 'Sold Out';
                addToCartBtn.classList.add('opacity-50', 'cursor-not-allowed');
            }
            if (buyNowBtn) {
                buyNowBtn.disabled = true;
                buyNowBtn.classList.add('opacity-50', 'cursor-not-allowed');
            }
            if (quantityInput) quantityInput.disabled = true;
            if (decreaseBtn) decreaseBtn.disabled = true;
            if (increaseBtn) increaseBtn.disabled = true;
        }
    }

    function updateAddToCartButton(variance, product) {
        if (!addToCartBtn || !variance) return;

        const quantity = parseInt(quantityInput.value) || 1;
        const stockLimit = variance.stockLimit || 0;

        // Update data attributes for potential debugging or future use
        addToCartBtn.dataset.productId = variance.id;
        addToCartBtn.dataset.productName = baseProductName;
        addToCartBtn.dataset.productPrice = variance.price;
        addToCartBtn.dataset.productImage = mainImage?.src || '';
        addToCartBtn.dataset.productQuantity = quantity;

        if (stockLimit === 0) {
            addToCartBtn.disabled = true;
            addToCartBtn.textContent = 'Sold Out';
            addToCartBtn.classList.add('opacity-50', 'cursor-not-allowed');
        } else {
            addToCartBtn.disabled = false;
            addToCartBtn.classList.remove('opacity-50', 'cursor-not-allowed');
            if (addToCartBtn.textContent === 'Sold Out') {
                addToCartBtn.innerHTML = '<i class="fas fa-shopping-bag text-base"></i> Add to cart';
            }
        }
    }

    function updateProductDetails(product, variance) {
        if (specsEl) {
            specsEl.innerHTML = '';
            const specs = [];
            if (product.specifications && Array.isArray(product.specifications)) {
                specs.push(...product.specifications);
            } else if (typeof product.specifications === 'string' && product.specifications.trim() !== '') {
                specs.push(...product.specifications.split(/\r?\n|•|,/).map(s => s.trim()).filter(Boolean));
            }

            if (specs.length > 0) {
                specsEl.style.display = 'block';
                specsEl.classList.add('list-disc', 'pl-5', 'space-y-1', 'text-sm');
                specsEl.innerHTML = specs.map(s => `<li>${s}</li>`).join('');
            } else {
                specsEl.style.display = 'none';
            }
        }

        if (summaryEl && product.title) {
            summaryEl.textContent = product.title;
        }

        if (productDetailsBox) {
            productDetailsBox.innerHTML = '';
            const uniqueColors = [...new Set(allVariancesData.map(v => v.color).filter(Boolean))];
            const uniqueSizes = [...new Set(allVariancesData.map(v => v.size).filter(Boolean))];
            const uniqueGemstones = [...new Set(allVariancesData.map(v => v.gemstone).filter(Boolean))];
            const details = [];
            if (product.category) details.push({label: 'Category', value: product.category});
            if (uniqueColors.length > 0) details.push({label: 'Finish Options', value: uniqueColors.join(' / ')});
            if (uniqueSizes.length > 0) {
                const label = (product.name.toLowerCase().includes('necklace') || product.name.toLowerCase().includes('chain'))
                    ? 'Chain Lengths'
                    : 'Available Sizes';
                details.push({label: label, value: uniqueSizes.join(' / ')});
            }
            if (uniqueGemstones.length > 0) details.push({label: 'Gemstones', value: uniqueGemstones.join(' / ')});
            if (product.warranty) details.push({label: 'Warranty', value: product.warranty});

            const detailsToRender = details.slice(0, 4);
            let html = '';
            detailsToRender.forEach((detail, index) => {
                const borderClass = (index === detailsToRender.length - 1) ? '' : ' border-b border-gray-100 pb-3';
                html += `
                <div class="flex items-center justify-between${borderClass}">
                    <span>${detail.label}</span>
                    <span class="font-medium text-dark">${detail.value}</span>
                </div>
            `;
            });
            if (html === '') html = '<p>No additional details available.</p>';
            productDetailsBox.innerHTML = html;
        }
    }

    function populateGallery(images) {
        if (!images?.length || !mainImage || !galleryThumbContainer) return;
        galleryThumbContainer.innerHTML = '';
        mainImage.src = images[0];
        mainImage.alt = titleEl?.textContent || 'Product image';
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

    function renderAllOptions(uniqueColors, uniqueSizes, uniqueGemstones) {
        if (!dynamicOptionsWrapper) return;
        dynamicOptionsWrapper.innerHTML = '';
        
        function createSection(attribute, options, selectedValue) {
            const section = document.createElement('div');
            section.className = 'space-y-3';
            
            const label = document.createElement('p');
            label.className = 'text-xs uppercase tracking-[0.35em] brand-accent';
            label.textContent = attribute.charAt(0).toUpperCase() + attribute.slice(1);
            
            const container = document.createElement('div');
            container.className = 'flex gap-3 flex-wrap';
            
            options.forEach(optionValue => {
                const btn = document.createElement('button');
                btn.className = 'product-color-option px-4 py-2 text-sm font-medium';
                btn.dataset.option = optionValue;
                btn.textContent = optionValue;
                if (optionValue === selectedValue) btn.classList.add('is-active');
                
                btn.addEventListener('click', () => {
                    // Instant UI feedback
                    container.querySelectorAll('button').forEach(b => b.classList.remove('is-active'));
                    btn.classList.add('is-active');

                    // Try to find a variant that matches current selection but with the new attribute value
                    let match = allVariancesData.find(v => {
                        let ok = true;
                        if (attribute === 'color') ok = ok && v.color === optionValue;
                        else if (uniqueColors.length > 0) ok = ok && v.color === currentVariance.color;
                        
                        if (attribute === 'size') ok = ok && v.size === optionValue;
                        else if (uniqueSizes.length > 0) ok = ok && v.size === currentVariance.size;
                        
                        if (attribute === 'gemstone') ok = ok && v.gemstone === optionValue;
                        else if (uniqueGemstones.length > 0) ok = ok && v.gemstone === currentVariance.gemstone;
                        
                        return ok;
                    });
                    
                    // If no exact match found, fallback to the first variant that has the clicked option
                    if (!match) match = allVariancesData.find(v => v[attribute] === optionValue);
                    
                    if (!match) return;
                    const url = new URL(window.location);
                    url.searchParams.set('id', match.id);
                    
                    // SPA approach: Update URL without reloading the page
                    history.pushState(null, '', url.href);
                    
                    // Fetch new data and update DOM smoothly
                    if (typeof window.loadProductData === 'function') {
                        window.loadProductData();
                    }
                });
                container.appendChild(btn);
            });
            
            section.appendChild(label);
            section.appendChild(container);
            dynamicOptionsWrapper.appendChild(section);
        }

        if (uniqueColors.length > 0) createSection('color', uniqueColors, currentVariance.color);
        if (uniqueSizes.length > 0) createSection('size', uniqueSizes, currentVariance.size);
        if (uniqueGemstones.length > 0) createSection('gemstone', uniqueGemstones, currentVariance.gemstone);
    }

    // --- POPULATE RELATED PRODUCTS (FREQUENTLY BOUGHT TOGETHER) ---
    function populateRelated(products) {
        if (!relatedProductsContainer) return;

        if (!products || products.length === 0) {
            relatedProductsContainer.closest('section.mt-20')?.remove();
            return;
        }

        relatedProductsContainer.innerHTML = ''; // Clear existing content

        products.forEach(p => {
            const formattedPrice = formatCurrency(p.price);

            // Get styled HTML components
            const { badgeHtml, btnState, btnText, stockStatusHtml } = getProductState(p);
            const ratingHtml = generateRatingHtml(p.averageRating || 0, p.reviewCount || 0);

            const imgUrl = p.image || 'https://placehold.co/600x600?text=No+Image';

            // GENERATE CARD HTML (Matches Featured Products Design)
            const cardHtml = `
            <div class="product-card shadow-sm group flex-none w-72 md:w-full snap-start" onclick="window.location.href='product-detail.html?id=${p.varianceId}'">
                ${badgeHtml}
                
                <div class="product-image h-64">
                    <img src="${imgUrl}" alt="${p.name}" loading="lazy">
                    <button class="view-product-btn" onclick="event.stopPropagation(); window.location.href='product-detail.html?id=${p.varianceId}'">View Product</button>
                </div>

                <div class="product-info">
                    <p class="text-xs text-gray-400 uppercase tracking-wider font-semibold">VELORA FINE JEWELLERY</p>
                    <h3 class="product-title line-clamp-2" title="${p.name}">${p.name}</h3>
                    
                    ${ratingHtml}

                    <p class="product-price">${formattedPrice}</p>
                    
                    <div class="product-availability">
                        ${stockStatusHtml}
                    </div>

                    <button class="add-to-cart-btn"
                            data-product-id="${p.varianceId}"
                            data-product-name="${p.name}"
                            data-product-price="${p.price}"
                            data-product-image="${imgUrl}"
                            data-stock-status="${p.stockStatus || (p.stockQty > 0 ? 'In Stock' : 'Out Of Stock')}"
                            data-stock-qty="${p.stockQty}"
                            ${btnState}>
                        <i class="fas fa-shopping-bag mr-2"></i> ${btnText}
                    </button>
                </div>
            </div>`;

            relatedProductsContainer.innerHTML += cardHtml;
        });

        // Re-attach cart event listeners to the newly added buttons
        if (typeof window.attachCartFunctionality === 'function') {
            window.attachCartFunctionality();
        }
    }

    function populateDescription(descriptionString) {
        if (!mainDescriptionContainer || !descriptionString) return;
        let descriptionData;
        try {
            descriptionData = JSON.parse(descriptionString);
        } catch (e) {
            mainDescriptionContainer.innerHTML = `<h2 class="text-lg font-semibold text-dark">Description</h2><p>${descriptionString}</p>`;
            return;
        }
        let html = '<h2 class="text-lg font-semibold text-dark">Description</h2>';
        if (descriptionData.main && Array.isArray(descriptionData.main)) {
            descriptionData.main.forEach(p => {
                html += `<p>${p}</p>`;
            });
        }
        if (descriptionData.styling_tips && Array.isArray(descriptionData.styling_tips) && descriptionData.styling_tips.length > 0) {
            html += `
            <div class="space-y-3">
                <h3 class="font-semibold text-dark">Styling Tips</h3>
                <ul class="list-disc space-y-2 pl-5">
                    ${descriptionData.styling_tips.map(tip => `<li>${tip}</li>`).join('')}
                </ul>
            </div>
        `;
        }
        if (descriptionData.care_details && Array.isArray(descriptionData.care_details) && descriptionData.care_details.length > 0) {
            html += `
            <div class="space-y-3">
                <h3 class="font-semibold text-dark">Care Details</h3>
                <ul class="list-disc space-y-2 pl-5">
                    ${descriptionData.care_details.map(detail => `<li>${detail}</li>`).join('')}
                </ul>
            </div>
        `;
        }
        mainDescriptionContainer.innerHTML = html;
    }

    // --- Main Add to Cart Button Logic ---
    addToCartBtn?.addEventListener('click', (e) => {
        // Prevent default simply to handle logic here, though usually a button type=button
        e.preventDefault();

        // 1. Check if Cart System is loaded
        if (typeof window.enhancedAddToCart !== 'function') {
            if(window.notify) window.notify.error("Cart system not initialized.");
            return;
        }

        // 2. Validate Selection (if necessary)
        if (!currentVariance || !currentVariance.id) {
            if(window.notify) window.notify.error("Please select a valid product option.");
            return;
        }

        // 3. Get Data
        const variantId = currentVariance.id;
        let name = currentProduct.name;
        let attrs = [];
        if (currentVariance.size) attrs.push(`Size: ${currentVariance.size}`);
        if (currentVariance.color) attrs.push(`Color: ${currentVariance.color}`);
        if (currentVariance.gemstone) attrs.push(`Gemstone: ${currentVariance.gemstone}`);
        if (attrs.length > 0) {
            name += ` (${attrs.join(', ')})`;
        }
        const price = currentVariance.price;
        const image = mainImage?.src || '';
        const quantity = parseInt(quantityInput?.value) || 1;

        // 4. Call Global Cart Function
        // Using 'false' for isCollection because this is a product page
        window.enhancedAddToCart(variantId, name, price, image, quantity, false);

        // 5. Trigger Animation
        if (typeof window.createFloatingCartAnimation === 'function') {
            window.createFloatingCartAnimation(addToCartBtn);
        }
    });

    decreaseBtn?.addEventListener('click', () => {
        if (currentVariance.stockLimit <= 0) return;
        const current = parseInt(quantityInput.value) || 1;
        quantityInput.value = Math.max(1, current - 1);
        updateAddToCartButton(currentVariance, currentProduct);
    });

    increaseBtn?.addEventListener('click', () => {
        if (currentVariance.stockLimit <= 0) return;
        const current = parseInt(quantityInput.value) || 1;
        const max = currentVariance.stockLimit || 99;
        quantityInput.value = Math.min(max, current + 1);
        updateAddToCartButton(currentVariance, currentProduct);
    });

    quantityInput?.addEventListener('change', () => {
        if (currentVariance.stockLimit <= 0) {
            quantityInput.value = 0;
            return;
        }
        const current = parseInt(quantityInput.value) || 1;
        const max = currentVariance.stockLimit || 99;
        if (current < 1) quantityInput.value = 1;
        if (current > max) quantityInput.value = max;
        updateAddToCartButton(currentVariance, currentProduct);
    });

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

    function initViewAllButtons() {
        const relatedSection = document.querySelector('.mt-20');
        if (relatedSection) {
            relatedSection.querySelectorAll('button').forEach(button => {
                if (button.textContent.trim().startsWith('View all')) {
                    const link = document.createElement('a');
                    link.href = 'shop.html';
                    link.className = button.className;
                    link.innerHTML = button.innerHTML;
                    button.parentNode.replaceChild(link, button);
                }
            });
        }
    }

    // ✅ වෙනස් කළ යුතු ශ්‍රිතය 1: revealProductPage
    function revealProductPage() {
        const main = document.getElementById('main-content');
        if (main) {
            main.style.display = 'block';
            main.classList.add('animate__animated', 'animate__fadeIn');
        }

        if (window.loader) {
            // සියලුම දත්ත පිරවී අවසන් වීමට තත්පර 0.5ක සහනයක් ලබා දෙයි
            setTimeout(() => {
                window.loader.hide();
            }, 500);
        }
    }

    // ✅ වෙනස් කළ යුතු ශ්‍රිතය 2: loadProductData (window object එකට සම්බන්ධ කළා)
    window.loadProductData = async function loadProductData() {
        const params = new URLSearchParams(window.location.search);
        const idParam = params.get('id');

        if (!idParam) {
            if (typeof titleEl !== 'undefined' && titleEl) titleEl.textContent = 'Product Not Found';
            revealProductPage();
            return;
        }

        try {
            const res = await fetch(`/api/products/details?id=${idParam}`);
            if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);

            const data = await res.json();
            if (!data.status) throw new Error(data.message || 'Invalid response from server');

            // --- ඔබේ පවතින Logic එක (කිසිදු වෙනසක් නොකර මෙහි තබන්න) ---
            currentProduct = data.product;
            allVariancesData = data.allVariances;
            currentVariance = data.selectedVariance;
            baseProductName = currentProduct.name;

            document.title = `${currentProduct.name} - Velora Fine Jewellery`;
            if (typeof categoryEl !== 'undefined' && categoryEl) categoryEl.textContent = currentProduct.category;
            if (typeof titleEl !== 'undefined' && titleEl) titleEl.textContent = baseProductName;

            updateProductDetails(currentProduct, currentVariance);
            populateDescription(currentProduct.description);
            updatePrice(currentVariance);
            updateStock(currentVariance);
            updateAddToCartButton(currentVariance, currentProduct);
            populateGallery(data.images);

            const uniqueColors = [...new Set(allVariancesData.map(v => v.color).filter(Boolean))];
            const uniqueSizes = [...new Set(allVariancesData.map(v => v.size).filter(Boolean))];
            const uniqueGemstones = [...new Set(allVariancesData.map(v => v.gemstone).filter(Boolean))];

            if (dynamicOptionsWrapper) {
                renderAllOptions(uniqueColors, uniqueSizes, uniqueGemstones);
            }

            populateRelated(data.relatedProducts);
            if (data.reviews) populateReviews(data.reviews);
            if (typeof initZoom === 'function') initZoom();
            if (typeof initViewAllButtons === 'function') initViewAllButtons();
            // -------------------------------------------------------

        } catch (err) {
            console.error("Error loading product data:", err);
        } finally {
            // ✅ සාර්ථක වුවත් නැතත් අවසානයේ එක් වරක් පමණක් ලෝඩරය අයින් කරයි
            revealProductPage();
        }
    }

    function populateReviews(reviews) {
        const reviewsSection = document.getElementById('reviews-section');
        const reviewsContainer = document.getElementById('reviews-container');
        const starsContainer = document.getElementById('average-rating-stars');
        const avgText = document.getElementById('average-rating-text');
        const totalCount = document.getElementById('total-reviews-count');

        if (!reviewsSection || !reviewsContainer || !reviews || reviews.length === 0) {
            if (reviewsSection) reviewsSection.style.display = 'none';
            return;
        }

        reviewsSection.style.display = 'block';
        reviewsContainer.innerHTML = '';

        let totalStars = 0;
        
        reviews.forEach(r => {
            totalStars += r.rating;
            
            const reviewCard = document.createElement('div');
            reviewCard.className = 'bg-white p-6 shadow-sm border border-gray-100 rounded-sm';
            
            let starsHtml = '';
            for(let i = 1; i <= 5; i++) {
                if (i <= r.rating) {
                    starsHtml += '<i class="fas fa-star"></i>';
                } else {
                    starsHtml += '<i class="far fa-star"></i>';
                }
            }
            
            reviewCard.innerHTML = `
                <div class="flex items-center justify-between mb-4">
                    <div class="flex gap-2 items-center">
                        <div class="h-10 w-10 bg-gold/10 text-gold rounded-full flex items-center justify-center font-bold font-serif text-lg">
                            ${r.reviewerName ? r.reviewerName.charAt(0).toUpperCase() : 'A'}
                        </div>
                        <div>
                            <h4 class="font-semibold text-sm text-dark">${r.reviewerName || 'Anonymous'}</h4>
                            <p class="text-xs text-gray-500">${r.date || ''}</p>
                        </div>
                    </div>
                    <div class="flex text-gold text-xs">${starsHtml}</div>
                </div>
                <p class="text-sm text-gray-600 italic">"${r.comment || ''}"</p>
            `;
            reviewsContainer.appendChild(reviewCard);
        });

        const avgRating = (totalStars / reviews.length).toFixed(1);
        
        if (avgText) avgText.textContent = avgRating;
        if (totalCount) totalCount.textContent = `(${reviews.length} Review${reviews.length > 1 ? 's' : ''})`;
        
        if (starsContainer) {
            let avgStarsHtml = '';
            const fullStars = Math.floor(avgRating);
            const hasHalfStar = (avgRating - fullStars) >= 0.5;
            
            for(let i = 1; i <= 5; i++) {
                if (i <= fullStars) {
                    avgStarsHtml += '<i class="fas fa-star"></i>';
                } else if (i === fullStars + 1 && hasHalfStar) {
                    avgStarsHtml += '<i class="fas fa-star-half-alt"></i>';
                } else {
                    avgStarsHtml += '<i class="far fa-star"></i>';
                }
            }
            starsContainer.innerHTML = avgStarsHtml;
        }
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
            if(window.notify) window.notify.warning('Please login to your account.');
            sessionStorage.setItem('returnUrl', window.location.href);
            setTimeout(() => window.location.href = 'auth.html', 1500);
            userLoggedIn = false;
            return false;
        }
    }

    buyNowBtn?.addEventListener('click', async () => {
        const loggedIn = await checkUserSession();
        if (!loggedIn) return;

        const requestedQty = parseInt(quantityInput.value) || 1;
        const availableQty = currentVariance?.stockLimit || 0;

        if (availableQty <= 0) {
            if(window.notify) window.notify.warning('This product is currently out of stock.');
            return;
        }

        if (requestedQty > availableQty) {
            if(window.notify) window.notify.warning(`Only ${availableQty} item(s) available in stock.`);
            return;
        }

        try {
            const res = await fetch('/api/order/buy-now-session', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    productVariantId: currentVariance.id,
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
