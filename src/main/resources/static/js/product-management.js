// ==========================================================================
// 1. SHOW SECTION LOGIC
// ==========================================================================
function showProductSection(section) {
    document.querySelectorAll('.section').forEach(s => s.classList.add('hidden'));

    switch(section) {
        case 'products':
            document.getElementById('products-section').classList.remove('hidden');
            loadProducts();
            break;
        case 'add-product':
            document.getElementById('add-product-section').classList.remove('hidden');
            loadActiveCategories();
            break;
        case 'manage-variants':
            document.getElementById('manage-variants-section').classList.remove('hidden');
            break;
        case 'product-variants':
            document.getElementById('product-variants-section').classList.remove('hidden');
            loadAllVariants();
            break;
        case 'add-variant':
            document.getElementById('add-variant-section').classList.remove('hidden');
            loadAllVariants();
            setupPriceCalculation();
            break;
        case 'product-attributes':
            document.getElementById('product-attributes-section').classList.remove('hidden');
            loadSizesByCategory();
            loadMetalsAttribute();
            loadGemstonesAttribute();
            break;
        case 'collections':
            document.getElementById('collections-section').classList.remove('hidden');
            loadCollections();
            setupCollectionPriceCalc();
            break;
        case 'add-collection':
            document.getElementById('add-collection-section').classList.remove('hidden');
            loadCollections();
            setupCollectionPriceCalc();
            break;
        case 'collection-sets':
            document.getElementById('collection-sets-section').classList.remove('hidden');
            loadCollectionSets();
            break;
        case 'add-collection-set':
            document.getElementById('add-collection-set-section').classList.remove('hidden');
            loadCollectionSets();
            break;
    }
}

// ==========================================================================
// 2. PRODUCT MANAGEMENT LOGIC
// ==========================================================================

// Load active categories for add/edit form dropdown
async function loadActiveCategories() {
    try {
        // FIXED: Changed '/api/categories/getAllCategories' to '/api/categories'
        // This must match the @RequestMapping and @GetMapping in your Java Controller
        const response = await fetch('/api/categories');

        if (!response.ok) {
            throw new Error(`Server responded with status: ${response.status}`);
        }

        const categories = await response.json();
        const select = document.getElementById('product-category');

        if (select) {
            // Clear and populate the dropdown
            select.innerHTML = '<option value="">Select Category</option>' +
                categories.map(c => `<option value="${c.id}">${c.category_name || c.category}</option>`).join('');

        } else {
            console.warn('Dropdown element "product-category" not found in DOM.');
        }
    } catch (err) {
        console.error('Error loading categories:', err);
        // Using a safe check for showToast if it exists in your UI framework
        if (typeof window.showToast === 'function') {
            window.showToast('Failed to load active categories', 'error');
        }
    }
}
// Load products into the table
async function loadProducts() {
    try {
        const response = await fetch('/api/admin/products');
        if (!response.ok) throw new Error('Failed to fetch products');

        const products = await response.json();
        const tbody = document.getElementById('products-body');

        if (products.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="px-6 py-4 text-center text-gray-500">No products found.</td></tr>';
            return;
        }

        tbody.innerHTML = products.map(p => `
            <tr class="hover:bg-gray-50 transition border-b border-gray-50">
                <td class="px-6 py-4 font-medium flex items-center gap-3">
                    <div class="w-10 h-10 flex-shrink-0 bg-gray-100 rounded border border-gray-200 overflow-hidden">
                         <img src="${p.image}" onerror="this.src='/images/placeholder.png'" class="w-full h-full object-cover" loading="lazy">
                    </div>
                    <div>
                        <div class="font-bold text-gray-800 text-sm">${p.title}</div>
                        <div class="text-xs text-gray-500">${p.name}</div>
                    </div>
                </td>
                <td class="px-6 py-4 text-gray-600 text-xs">${p.category?.category || 'Uncategorized'}</td>
                <td class="px-6 py-4 text-center">
                    <span class="bg-gray-100 text-gray-800 text-xs font-medium px-2.5 py-0.5 rounded border border-gray-200">
                        ${p.variantCount || 0}
                    </span>
                </td>
                <td class="px-6 py-4">
                    <label class="inline-flex relative items-center cursor-pointer">
                        <input type="checkbox" class="sr-only peer" 
                               ${p.status?.id === 1 ? 'checked' : ''} 
                               onchange="toggleStatus(${p.id}, this.checked)">
                        <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-gold-300 rounded-full peer peer-checked:bg-gold-400 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border after:rounded-full after:h-4 after:w-4 after:transition-all"></div>
                    </label>
                </td>
                <td class="px-6 py-4 text-right">
                    <div class="flex justify-end gap-3">
                        <button onclick="editProduct(${p.id})" class="text-gray-400 hover:text-gold-600 text-xs font-bold uppercase transition" title="Edit Product">
                            Edit
                        </button>
                        <button onclick="viewVariants(${p.id}, '${p.title.replace(/'/g, "\\'")}')" class="text-gray-400 hover:text-blue-600 text-xs font-bold uppercase transition" title="View Variants">
                            View
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Failed to load product list', 'error');
    }
}

// --- View Variants Modal Function ---
async function viewVariants(productId, productName) {
    try {
        const subtitle = document.getElementById('variant-modal-subtitle');
        if (subtitle) subtitle.innerText = productName;

        const response = await fetch(`/api/admin/products/${productId}/variants`);
        if (!response.ok) throw new Error('Failed to fetch variants');

        const variants = await response.json();
        const listContainer = document.getElementById('product-variants-list');

        if (!listContainer) {
            console.error("List container 'product-variants-list' not found.");
            return;
        }

        if (variants.length === 0) {
            listContainer.innerHTML = '<li class="text-center text-gray-400 py-8 text-xs uppercase tracking-wide">No variants configured for this product.</li>';
        } else {
            listContainer.innerHTML = variants.map(v => {
                let attributes = [];

                if (v.size && v.size.size) {
                    attributes.push(`<span class="text-gray-400">Size:</span> <span class="font-semibold text-gray-800">${v.size.size}</span>`);
                }
                if (v.color && v.color.name) {
                    attributes.push(`<span class="text-gray-400">Color:</span> <span class="font-semibold text-gray-800">${v.color.name}</span>`);
                }
                if (v.gemstone && v.gemstone.name) {
                    attributes.push(`<span class="text-gray-400">Gem:</span> <span class="font-semibold text-gray-800">${v.gemstone.name}</span>`);
                }

                const attrString = attributes.length > 0
                    ? attributes.join('<span class="mx-2 text-gray-300">|</span>')
                    : '<span class="text-gray-400 italic">Standard / Base Variant</span>';

                const qty = v.stockQty !== undefined ? v.stockQty : 0;
                const stockClass = qty > 0 ? 'text-black' : 'text-red-500';
                const stockLabel = qty > 0 ? 'In Stock' : 'Out of Stock';

                return `
                <li class="flex justify-between items-center bg-white p-4 border-b border-gray-100 last:border-0 hover:bg-gray-50 transition duration-200">
                    <div class="flex flex-col">
                        <span class="text-[10px] font-bold uppercase text-gray-400 mb-1 tracking-wider">Variant #${v.id}</span>
                        <div class="text-sm flex flex-wrap items-center gap-y-1">
                            ${attrString}
                        </div>
                    </div>
                    <div class="text-right pl-4">
                        <span class="block text-xl font-bold ${stockClass}">${qty}</span>
                        <span class="text-[10px] uppercase font-bold text-gray-400 tracking-wide">${stockLabel}</span>
                    </div>
                </li>
                `;
            }).join('');
        }

        const modal = document.getElementById('product-variants-modal');
        if (modal) modal.classList.remove('hidden');

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading product variants', 'error');
    }
}

function closeVariantsModal() {
    document.getElementById('product-variants-modal').classList.add('hidden');
}

// Toggle product status (Active/Inactive)
async function toggleStatus(productId, active) {
    try {
        const response = await fetch(`/api/admin/products/${productId}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ active })
        });

        if(!response.ok) throw new Error('Status update failed');

        if(window.showToast) showToast(active ? 'Product activated' : 'Product deactivated', 'success');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error updating status', 'error');
        loadProducts();
    }
}

// Filter products table locally
function filterProducts() {
    const search = document.getElementById('product-search').value.toLowerCase();
    const rows = document.querySelectorAll('#products-body tr');
    rows.forEach(row => {
        const title = row.cells[0].textContent.toLowerCase();
        const category = row.cells[1].textContent.toLowerCase();
        row.style.display = title.includes(search) || category.includes(search) ? '' : 'none';
    });
}
// ==========================================================================
// 3. ADD / EDIT FORM LOGIC & BULK UPLOAD
// ==========================================================================
async function handleBulkUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    try {
        Swal.fire({
            title: 'Uploading...',
            text: 'Please wait while we process the Excel file.',
            allowOutsideClick: false,
            didOpen: () => { Swal.showLoading(); }
        });

        const res = await fetch('/api/admin/products/bulk/upload', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') },
            body: formData
        });

        const data = await res.json();
        
        // Reset file input
        event.target.value = '';

        if (res.ok) {
            Swal.fire('Success', 'Products uploaded successfully!', 'success');
            loadProducts();
        } else {
            Swal.fire('Error', data.error || 'Upload failed', 'error');
        }
    } catch (e) {
        Swal.fire('Error', 'Network error or server is down.', 'error');
        console.error(e);
    }
}

function openCreateProductModal() {
    document.getElementById('edit-product-id').value = '';

    document.getElementById('product-title').value = '';
    document.getElementById('product-name').value = '';
    document.getElementById('product-specs').value = '';
    document.getElementById('product-warranty').value = '';
    document.getElementById('product-category').value = '';

    document.getElementById('desc-main').value = '';
    document.getElementById('desc-styling').value = '';
    document.getElementById('desc-care').value = '';

    for(let i=1; i<=4; i++) {
        const container = document.getElementById(`preview-${i}`);
        const uploadBox = container.previousElementSibling; // The upload placeholder

        container.innerHTML = '';
        container.classList.add('hidden');
        if(uploadBox) uploadBox.classList.remove('hidden');

        document.getElementById(`image-${i}`).value = '';
    }

    document.getElementById('form-title').innerText = 'Create New Product';
    document.getElementById('submit-btn-text').innerText = 'Publish Product';

    loadActiveCategories();
    showProductSection('add-product');
}

async function editProduct(id) {
    try {
        const response = await fetch(`/api/admin/products/${id}`);
        if (!response.ok) throw new Error('Failed to fetch product');

        const product = await response.json();

        document.getElementById('edit-product-id').value = product.id;
        document.getElementById('product-title').value = product.title;
        document.getElementById('product-name').value = product.name;
        document.getElementById('product-specs').value = product.specifications;
        document.getElementById('product-warranty').value = product.warranty;

        if (typeof loadActiveCategories === 'function') {
            await loadActiveCategories();
        }

        if (product.category) {
            document.getElementById('product-category').value = product.category.id;
        }

        try {
            const descObj = JSON.parse(product.description);
            document.getElementById('desc-main').value = Array.isArray(descObj.main) ? descObj.main.join('\n') : descObj.main || '';
            document.getElementById('desc-styling').value = Array.isArray(descObj.styling_tips) ? descObj.styling_tips.join('\n') : descObj.styling_tips || '';
            document.getElementById('desc-care').value = Array.isArray(descObj.care_details) ? descObj.care_details.join('\n') : descObj.care_details || '';

        } catch (e) {
            document.getElementById('desc-main').value = product.description;
            document.getElementById('desc-styling').value = '';
            document.getElementById('desc-care').value = '';
        }

        // ✅ UPDATED: Cloudinary Image Loading Logic
        for (let i = 1; i <= 4; i++) {
            const previewContainer = document.getElementById(`preview-${i}`);
            const uploadBox = previewContainer.previousElementSibling;
            const fileInput = document.getElementById(`image-${i}`);

            fileInput.value = '';

            // ✅ Get URL directly from the product object (image1, image2, etc.)
            const imgUrl = product[`image${i}`];

            if (imgUrl) {
                // If Cloudinary URL exists, show it
                const img = new Image();
                img.src = imgUrl;
                img.className = 'w-full h-full object-cover cursor-pointer';
                img.title = "Click to replace image";
                img.onclick = function() { fileInput.click(); };

                img.onload = function() {
                    previewContainer.innerHTML = '';
                    previewContainer.appendChild(img);
                    previewContainer.className = 'w-full h-32 rounded border border-gray-200 overflow-hidden relative';
                    previewContainer.classList.remove('hidden');
                    if(uploadBox) uploadBox.classList.add('hidden');
                };
            } else {
                // No image found for this slot
                previewContainer.innerHTML = '';
                previewContainer.classList.add('hidden');
                if(uploadBox) uploadBox.classList.remove('hidden');
            }
        }

        document.getElementById('form-title').innerText = 'Edit Product #' + id;
        document.getElementById('submit-btn-text').innerText = 'Update Product';

        if (typeof showProductSection === 'function') {
            showProductSection('add-product');
        }
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading product details', 'error');
    }
}

// 4. HANDLE FORM SUBMIT (Create & Update)
async function handleProductSubmit(e) {
    e.preventDefault(); // disable default browser validation completely

    const id = document.getElementById('edit-product-id').value;

    // -----------------------------
    // 🔥 CUSTOM VALIDATION
    // -----------------------------
    const title = document.getElementById('product-title').value.trim();
    const name = document.getElementById('product-name').value.trim();
    const specs = document.getElementById('product-specs').value.trim();
    const warranty = document.getElementById('product-warranty').value.trim();
    const category = document.getElementById('product-category').value;

    if (!title) { showToast('Please enter product title', 'error'); return; }
    if (!name) { showToast('Please enter product name', 'error'); return; }
    if (!specs) { showToast('Please enter product specifications', 'error'); return; }
    if (!warranty) { showToast('Please enter product warranty', 'error'); return; }
    if (!category || category === "0" || category === "") { showToast('Please select a category', 'error'); return; }

    const rawMain = document.getElementById('desc-main').value.trim();
    if (!rawMain) { showToast('Please enter product description', 'error'); return; }

    // ✅ At least one image validation
    // MODIFIED: This check is skipped if 'id' exists (Editing mode).
    if (!id) {
        let imageSelected = false;
        for (let i = 1; i <= 4; i++) {
            const fileInput = document.getElementById(`image-${i}`);
            if (fileInput.files && fileInput.files[0]) {
                imageSelected = true;
                break;
            }
        }
        if (!imageSelected) {
            showToast('Please select at least 1 product image', 'error');
            return;
        }
    }

    // -----------------------------
    // 🔥 BUILD REQUEST PAYLOAD
    // -----------------------------
    const rawStyling = document.getElementById('desc-styling').value;
    const rawCare = document.getElementById('desc-care').value;

    const descJson = {
        main: rawMain.split('\n').map(s => s.trim()).filter(s => s),
        styling_tips: rawStyling.split('\n').map(s => s.trim()).filter(s => s),
        care_details: rawCare.split('\n').map(s => s.trim()).filter(s => s)
    };

    const productData = {
        title,
        name,
        description: JSON.stringify(descJson),
        specifications: specs,
        warranty,
        category: { id: parseInt(category) }
    };

    const formData = new FormData();
    formData.append('product', new Blob([JSON.stringify(productData)], { type: 'application/json' }));

    for (let i = 1; i <= 4; i++) {
        const fileInput = document.getElementById(`image-${i}`);
        if (fileInput.files[0]) {
            formData.append(`image${i}`, fileInput.files[0]);
        }
    }

    // -----------------------------
    // 🔥 API REQUEST
    // -----------------------------
    try {
        const url = id ? `/api/admin/products/${id}/update` : '/api/admin/products';
        const method = 'POST';

        const response = await fetch(url, { method, body: formData });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Unknown error');
        }

        showToast(id ? 'Product updated successfully!' : 'Product created successfully!', 'success');

        if (typeof showProductSection === 'function') {
            showProductSection('products');
        }

    } catch (err) {
        console.error(err);
        showToast('Error saving product: ' + err.message, 'error');
    }
}

// Helper: Image Preview on File Select
function previewImage(index) {
    const input = document.getElementById(`image-${index}`);
    const previewContainer = document.getElementById(`preview-${index}`);
    const uploadBox = previewContainer.previousElementSibling;

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            // Create img element dynamically to match editProduct structure
            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'w-full h-full object-cover cursor-pointer';
            img.title = "Click to change selection";

            // Allow clicking this preview to change file again
            img.onclick = function() { input.click(); };

            previewContainer.innerHTML = '';
            previewContainer.appendChild(img);

            // FORCE HEIGHT here too to match edit mode consistency
            previewContainer.className = 'w-full h-32 rounded border border-gray-200 overflow-hidden relative';

            previewContainer.classList.remove('hidden');
            if(uploadBox) uploadBox.classList.add('hidden');
        }
        reader.readAsDataURL(input.files[0]);
    } else {
        // If file selection cancelled, keep existing or show placeholder?
        // Usually safer to clear if no file selected, but if editing, this might clear pre-loaded image.
        // Simple logic: if file input is empty and no pre-loaded image exists (checked via childNodes), show placeholder.
        if(previewContainer.childNodes.length === 0) {
            previewContainer.classList.add('hidden');
            if(uploadBox) uploadBox.classList.remove('hidden');
        }
    }
}


// ==========================================
// GLOBAL STATE (To track editing IDs)
// ==========================================
let editingSizeId = null;
let editingMetalId = null;
let editingGemId = null;

// ==========================================
// 1. LOAD SIZES (With Category)
// ==========================================
async function loadSizesByCategory() {
    try {
        const response = await fetch('/api/admin/attributes/sizes');
        if (!response.ok) throw new Error('Failed to fetch sizes');

        const sizes = await response.json();
        const tbody = document.getElementById('sizes-category-body');

        if (sizes.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="px-4 py-3 text-center text-gray-500">No sizes found.</td></tr>';
            return;
        }

        tbody.innerHTML = sizes.map(s => `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-4 py-3 font-medium text-gray-800">${s.categoryName}</td>
                <td class="px-4 py-3 text-gray-600">${s.size}</td>
                <td class="px-4 py-3 text-right">
                    <button onclick="editSizeCategory(${s.id}, '${escapeStr(s.size)}', ${s.categoryId})" 
                            class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase transition">
                        Edit
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading sizes', 'error');
    }
}

// ==========================================
// 2. LOAD METALS (Mapped to Colors)
// ==========================================
async function loadMetalsAttribute() {
    try {
        const response = await fetch('/api/admin/attributes/colors');
        if (!response.ok) throw new Error('Failed to fetch metals');

        const metals = await response.json();
        const tbody = document.getElementById('metals-attribute-body');

        if (metals.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2" class="px-4 py-3 text-center text-gray-500">No metals found.</td></tr>';
            return;
        }

        tbody.innerHTML = metals.map(m => `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-4 py-3 font-medium text-gray-800">${m.color || m.name}</td>
                <td class="px-4 py-3 text-right">
                    <button onclick="editMetalAttribute(${m.id}, '${escapeStr(m.color || m.name)}')" 
                            class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase transition">
                        Edit
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading metals', 'error');
    }
}

// ==========================================
// 3. LOAD GEMSTONES
// ==========================================
async function loadGemstonesAttribute() {
    try {
        const response = await fetch('/api/admin/attributes/gemstones');
        if (!response.ok) throw new Error('Failed to fetch gemstones');

        const gemstones = await response.json();
        const tbody = document.getElementById('gemstones-attribute-body');

        if (gemstones.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2" class="px-4 py-3 text-center text-gray-500">No gemstones found.</td></tr>';
            return;
        }

        tbody.innerHTML = gemstones.map(g => `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-4 py-3 font-medium text-gray-800">${g.gemStone || g.name}</td>
                <td class="px-4 py-3 text-right">
                    <button onclick="editGemstoneAttribute(${g.id}, '${escapeStr(g.gemStone || g.name)}')" 
                            class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase transition">
                        Edit
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading gemstones', 'error');
    }
}

// Helper to escape strings in HTML attributes
function escapeStr(str) {
    if (!str) return '';
    return str.replace(/'/g, "\\'");
}

// ==========================================
// 4. SIZE CATEGORY ACTIONS
// ==========================================

// Open Modal for CREATE
function openAddSizeCategoryModal() {
    editingSizeId = null; // Reset ID for create mode

    // Reset Form
    document.getElementById('new-size-category').value = '';
    document.getElementById('new-size-category-name').value = '';

    // Update Title
    const modalTitle = document.querySelector('#size-category-modal h3');
    if(modalTitle) modalTitle.innerText = 'Add Size by Category';

    document.getElementById('size-category-modal').classList.remove('hidden');
    loadAttributeCategories();
}

// Open Modal for EDIT
function editSizeCategory(id, sizeName, categoryId) {
    editingSizeId = id; // Set ID for update mode

    // Populate Form
    document.getElementById('new-size-category').value = categoryId;
    document.getElementById('new-size-category-name').value = sizeName;

    // Update Title
    const modalTitle = document.querySelector('#size-category-modal h3');
    if(modalTitle) modalTitle.innerText = 'Edit Size';

    document.getElementById('size-category-modal').classList.remove('hidden');
    loadAttributeCategories();
}

function closeSizeCategoryModal() {
    document.getElementById('size-category-modal').classList.add('hidden');
    editingSizeId = null;
    document.getElementById('new-size-category').value = '';
    document.getElementById('new-size-category-name').value = '';
}

// Helper to load categories specifically for this select
async function loadAttributeCategories() {
    try {
        const response = await fetch('/api/admin/categories?status=active');
        if (!response.ok) return;
        const categories = await response.json();

        const select = document.getElementById('new-size-category');
        const currentValue = select.value; // Preserve current value if set

        select.innerHTML = '<option value="">Select Category</option>' +
            categories.map(c => `<option value="${c.id}">${c.category}</option>`).join('');

        if(currentValue) select.value = currentValue;
    } catch (e) { console.error(e); }
}

async function saveNewSizeCategory() {
    const categoryId = document.getElementById('new-size-category').value;
    const size = document.getElementById('new-size-category-name').value;

    if (!categoryId) {
        if (window.showToast) showToast('Please select a category', 'error');
        return;
    }

    if (!size) {
        if (window.showToast) showToast('Please enter a size', 'error');
        return;
    }

    const payload = {
        size: size,
        category: { id: parseInt(categoryId) }
    };

    try {
        const url = editingSizeId
            ? `/api/admin/attributes/sizes/${editingSizeId}`
            : '/api/admin/attributes/sizes';

        const method = 'POST';

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        // ✅ FIXED: Correctly read error message for duplicates
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to save size');
        }

        loadSizesByCategory();
        closeSizeCategoryModal();
        if(window.showToast) showToast(editingSizeId ? 'Size updated' : 'Size added', 'success');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast(err.message, 'error');
    }
}

// ==========================================
// 5. METAL (COLOR) ACTIONS
// ==========================================

// Open Modal for CREATE
function openAddMetalAttributeModal() {
    editingMetalId = null;
    document.getElementById('new-metal-attribute-name').value = '';

    const modalTitle = document.querySelector('#metal-attribute-modal h3');
    if(modalTitle) modalTitle.innerText = 'Add Metal';

    document.getElementById('metal-attribute-modal').classList.remove('hidden');
}

// Open Modal for EDIT
function editMetalAttribute(id, metalName) {
    editingMetalId = id;
    document.getElementById('new-metal-attribute-name').value = metalName;

    const modalTitle = document.querySelector('#metal-attribute-modal h3');
    if(modalTitle) modalTitle.innerText = 'Edit Metal';

    document.getElementById('metal-attribute-modal').classList.remove('hidden');
}

function closeMetalAttributeModal() {
    document.getElementById('metal-attribute-modal').classList.add('hidden');
    editingMetalId = null;
    document.getElementById('new-metal-attribute-name').value = '';
}

async function saveNewMetalAttribute() {
    const name = document.getElementById('new-metal-attribute-name').value;

    if (!name) {
        if(window.showToast) showToast('Please enter metal type', 'error');
        return;
    }

    const payload = { color: name };

    try {
        const url = editingMetalId
            ? `/api/admin/attributes/colors/${editingMetalId}`
            : '/api/admin/attributes/colors';

        const method = 'POST';

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to save metal');
        }

        loadMetalsAttribute();
        closeMetalAttributeModal();
        if(window.showToast) showToast(editingMetalId ? 'Metal updated' : 'Metal added', 'success');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast(err.message, 'error');
    }
}

// ==========================================
// 6. GEMSTONE ACTIONS
// ==========================================

// Open Modal for CREATE
function openAddGemstoneAttributeModal() {
    editingGemId = null;
    document.getElementById('new-gemstone-attribute-name').value = '';

    const modalTitle = document.querySelector('#gemstone-attribute-modal h3');
    if(modalTitle) modalTitle.innerText = 'Add Gemstone';

    document.getElementById('gemstone-attribute-modal').classList.remove('hidden');
}

// Open Modal for EDIT
function editGemstoneAttribute(id, gemName) {
    editingGemId = id;
    document.getElementById('new-gemstone-attribute-name').value = gemName;

    const modalTitle = document.querySelector('#gemstone-attribute-modal h3');
    if(modalTitle) modalTitle.innerText = 'Edit Gemstone';

    document.getElementById('gemstone-attribute-modal').classList.remove('hidden');
}

function closeGemstoneAttributeModal() {
    document.getElementById('gemstone-attribute-modal').classList.add('hidden');
    editingGemId = null;
    document.getElementById('new-gemstone-attribute-name').value = '';
}

async function saveNewGemstoneAttribute() {
    const name = document.getElementById('new-gemstone-attribute-name').value;

    if (!name) {
        if(window.showToast) showToast('Please enter gemstone name', 'error');
        return;
    }

    const payload = { gemStone: name };

    try {
        const url = editingGemId
            ? `/api/admin/attributes/gemstones/${editingGemId}`
            : '/api/admin/attributes/gemstones';

        const method = 'POST';

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to save gemstone');
        }

        loadGemstonesAttribute();
        closeGemstoneAttributeModal();
        if(window.showToast) showToast(editingGemId ? 'Gemstone updated' : 'Gemstone added', 'success');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast(err.message, 'error');
    }
}

let editingVarianceId = null;
// Cache data to support dynamic filtering
let cachedProducts = [];
let cachedSizes = [];

// 1. Load All Variants into Table
async function loadAllVariants() {
    try {
        const response = await fetch('/api/admin/variances');

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Failed to fetch variants: ${response.status} - ${errorText}`);
        }

        const variants = await response.json();
        const tbody = document.getElementById('all-variants-body');

        if (variants.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="px-6 py-4 text-center text-gray-500">No variants found.</td></tr>';
            return;
        }

        tbody.innerHTML = variants.map(v => `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-6 py-4 font-bold text-gray-800">${v.productName}</td>
                <td class="px-6 py-4 text-gray-600">${v.sizeName}</td>
                <td class="px-6 py-4 text-gray-600">${v.colorName}</td>
                <td class="px-6 py-4 text-gray-600">${v.gemstoneName}</td>
                <td class="px-6 py-4">
                    <div class="flex flex-col">
                        <span class="font-bold text-gray-800">LKR ${v.price.toFixed(2)}</span>
                        ${v.discountPercentage > 0 ? `<span class="text-xs text-red-500 line-through">LKR ${v.regularPrice.toFixed(2)}</span>` : ''}
                    </div>
                </td>
                <td class="px-6 py-4 font-bold text-gray-800">${v.stockLimit}</td>
                <td class="px-6 py-4">
                    <label class="inline-flex relative items-center cursor-pointer">
                        <input type="checkbox" class="sr-only peer" 
                               ${v.statusId === 1 ? 'checked' : ''} 
                               onchange="toggleVariantStatus(${v.id}, this.checked)">
                        <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-gold-300 rounded-full peer peer-checked:bg-gold-400 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border after:rounded-full after:h-4 after:w-4 after:transition-all"></div>
                    </label>
                </td>
                <td class="px-6 py-4 text-right">
                    <button onclick="openEditVariantModal(${v.id})" 
                            class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase transition">
                        Edit
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Full error:', err);
        if(window.showToast) showToast('Error loading variants', 'error');
    }
}

// 2. Open Modal for CREATE
async function openAddVariantModal() {
    editingVarianceId = null;

    // Reset Form
    document.getElementById('variant-product').value = '';
    document.getElementById('variant-size').value = '';
    document.getElementById('variant-metal').value = '';
    document.getElementById('variant-gemstone').value = '';
    document.getElementById('variant-regular-price').value = '';
    document.getElementById('variant-discount').value = '0';
    document.getElementById('variant-price').value = '';
    document.getElementById('variant-stock').value = '';

    // Update Titles
    document.querySelector('#add-variant-section h3').innerText = 'Add Product Variant';
    document.querySelector('#add-variant-section button[type="submit"]').innerText = 'Save Variant';

    await loadVariantDropdowns();
    showProductSection('add-variant');
}

// 3. Open Modal for EDIT (Populate Data)
async function openEditVariantModal(id) {
    try {
        editingVarianceId = id;

        const response = await fetch(`/api/admin/variances/${id}`);
        if(!response.ok) throw new Error("Failed to load variant details");
        const v = await response.json();

        // 1. Load Dropdowns (fetches data)
        await loadVariantDropdowns();

        // 2. Set Product ID
        document.getElementById('variant-product').value = v.productId;

        // 3. CRITICAL: Manually trigger filter so the correct sizes are loaded for this product
        filterSizesForSelectedProduct();

        // 4. Set remaining values
        // Note: Size must be set AFTER filterSizesForSelectedProduct runs
        document.getElementById('variant-size').value = v.sizeId || '';
        document.getElementById('variant-metal').value = v.colorId || '';
        document.getElementById('variant-gemstone').value = v.gemstoneId || '';
        document.getElementById('variant-regular-price').value = v.regularPrice;
        document.getElementById('variant-discount').value = v.discountPercentage;
        document.getElementById('variant-price').value = v.price;
        document.getElementById('variant-stock').value = v.stockLimit;

        document.querySelector('#add-variant-section h3').innerText = 'Edit Variant #' + id;
        document.querySelector('#add-variant-section button[type="submit"]').innerText = 'Update Variant';

        showProductSection('add-variant');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading variant details', 'error');
    }
}

// 4. Handle Form Submit
async function handleVariantSubmit(e) {
    e.preventDefault();

    const productId = document.getElementById('variant-product').value;
    const sizeId = document.getElementById('variant-size').value;
    const colorId = document.getElementById('variant-metal').value;
    const gemstoneId = document.getElementById('variant-gemstone').value;
    const regularPrice = document.getElementById('variant-regular-price').value;
    const discountPercentage = document.getElementById('variant-discount').value;
    const stockLimit = document.getElementById('variant-stock').value;

    // Validate each field individually
    if (!productId) {
        if (window.showToast) showToast('Please select a product', 'error');
        return;
    }

    if (!regularPrice || isNaN(regularPrice) || parseFloat(regularPrice) <= 0) {
        if (window.showToast) showToast('Please enter a valid regular price', 'error');
        return;
    }

    if (!stockLimit || isNaN(stockLimit) || parseInt(stockLimit) < 0) {
        if (window.showToast) showToast('Please enter a valid stock quantity', 'error');
        return;
    }

    const payload = {
        productId: parseInt(productId),
        sizeId: parseInt(sizeId),
        colorId: parseInt(colorId),
        gemstoneId: parseInt(gemstoneId),
        regularPrice: parseFloat(regularPrice),
        discountPercentage: parseFloat(discountPercentage) || 0,
        stockLimit: parseInt(stockLimit)
    };

    try {
        const url = editingVarianceId
            ? `/api/admin/variances/${editingVarianceId}`
            : '/api/admin/variances';
        const method = 'POST';

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to save variant');
        }

        if (window.showToast) showToast(editingVarianceId ? 'Variant Updated' : 'Variant Created', 'success');
        showProductSection('product-variants');

    } catch (err) {
        console.error(err);
        if (window.showToast) showToast(err.message, 'error');
    }
}


// 5. Helper: Load Dropdowns with Category Filtering Logic
async function loadVariantDropdowns() {
    try {
        // Fetch all necessary data in parallel
        const [products, sizes, colors, gemstones] = await Promise.all([
            fetch('/api/admin/products').then(res => res.json()),
            fetch('/api/admin/attributes/sizes').then(res => res.json()),
            fetch('/api/admin/attributes/colors').then(res => res.json()),
            fetch('/api/admin/attributes/gemstones').then(res => res.json())
        ]);

        // Store in global variables for filtering
        cachedProducts = products;
        cachedSizes = sizes;

        // --- 1. Populate Products ---
        const prodSelect = document.getElementById('variant-product');

        // Clone node to remove old event listeners to prevent duplicates
        const newProdSelect = prodSelect.cloneNode(true);
        prodSelect.parentNode.replaceChild(newProdSelect, prodSelect);

        newProdSelect.innerHTML = '<option value="">Choose Product</option>' +
            products.map(p => `<option value="${p.id}">${p.title}</option>`).join('');

        // --- 2. Populate Metals ---
        const metalSelect = document.getElementById('variant-metal');
        metalSelect.innerHTML = '<option value="">Choose Metal</option>' +
            colors.map(c => `<option value="${c.id}">${c.color || c.name}</option>`).join('');

        // --- 3. Populate Gemstones ---
        const gemSelect = document.getElementById('variant-gemstone');
        gemSelect.innerHTML = '<option value="">Choose Gemstone</option>' +
            gemstones.map(g => `<option value="${g.id}">${g.gemStone || g.name}</option>`).join('');

        // --- 4. Setup Listener for Product Change ---
        newProdSelect.addEventListener('change', () => {
            filterSizesForSelectedProduct();
        });

        // --- 5. Initial Size Load ---
        // Loads sizes based on the current product selection (empty initially)
        filterSizesForSelectedProduct();

    } catch (err) {
        console.error("Error loading dropdowns", err);
    }
}

// New Helper: Filter Sizes based on selected Product's Category
function filterSizesForSelectedProduct() {
    const prodSelect = document.getElementById('variant-product');
    const sizeSelect = document.getElementById('variant-size');

    const selectedProductId = prodSelect.value;
    const currentSizeId = sizeSelect.value; // Preserve selection if possible

    let filteredSizes = [];

    if (selectedProductId) {
        // Find the full product object to get its category ID
        const product = cachedProducts.find(p => p.id == selectedProductId);

        if (product && product.category) {
            const categoryId = product.category.id;
            // Filter sizes: Keep only those matching the category
            filteredSizes = cachedSizes.filter(s => s.categoryId === categoryId);
        } else {
            // If product has no category, maybe show all or none (Showing all for safety)
            filteredSizes = cachedSizes;
        }
    } else {
        // If no product selected, show ALL sizes (or empty if you prefer strict dependency)
        filteredSizes = cachedSizes;
    }

    // Render the filtered options
    sizeSelect.innerHTML = '<option value="">Choose Size</option>' +
        filteredSizes.map(s => `<option value="${s.id}">${s.size} (${s.categoryName})</option>`).join('');

    // Restore the previous size value if it exists in the new filtered list
    if (currentSizeId && filteredSizes.some(s => s.id == currentSizeId)) {
        sizeSelect.value = currentSizeId;
    } else {
        sizeSelect.value = "";
    }
}

// 6. Helper: Auto Calculate Final Price
function setupPriceCalculation() {
    const regularInput = document.getElementById('variant-regular-price');
    const discountInput = document.getElementById('variant-discount');
    const finalInput = document.getElementById('variant-price');

    // Ensure elements exist before attaching events
    if (!regularInput || !discountInput || !finalInput) {
        console.warn("Price inputs not found in DOM");
        return;
    }

    const calculate = () => {
        const regularPrice = parseFloat(regularInput.value);
        const discountPercent = parseFloat(discountInput.value);

        // If regular price is invalid or empty, clear final price
        if (isNaN(regularPrice)) {
            finalInput.value = '';
            return;
        }

        // Use 0 if discount is invalid/empty
        const discount = isNaN(discountPercent) ? 0 : discountPercent;

        // Formula: Final = Regular - (Regular * Discount / 100)
        const finalPrice = regularPrice - (regularPrice * (discount / 100));

        finalInput.value = finalPrice.toFixed(2);
    };

    // Attach listeners for various input types to ensure it triggers
    ['input', 'change', 'keyup'].forEach(event => {
        regularInput.addEventListener(event, calculate);
        discountInput.addEventListener(event, calculate);
    });
}

// 7. Toggle Status
async function toggleVariantStatus(id, active) {
    try {
        await fetch(`/api/admin/variances/${id}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ active })
        });
        if(window.showToast) showToast('Status Updated', 'success');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error updating status', 'error');
        loadAllVariants();
    }
}

// Search Filter
function filterVariants() {
    const search = document.getElementById('variants-search').value.toLowerCase();
    const rows = document.querySelectorAll('#all-variants-body tr');
    rows.forEach(row => {
        const text = row.innerText.toLowerCase();
        row.style.display = text.includes(search) ? '' : 'none';
    });
}
// ==========================================
// COLLECTION MANAGEMENT LOGIC
// ==========================================

let editingCollectionId = null;

// 1. Load Collections
async function loadCollections() {
    try {
        const response = await fetch('/api/admin/collections');
        if (!response.ok) throw new Error('Failed to fetch collections');

        const collections = await response.json();
        const tbody = document.getElementById('collections-body');

        if (!collections || collections.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="px-6 py-4 text-center text-gray-500">No collections found.</td></tr>';
            return;
        }

        tbody.innerHTML = collections.map(c => {
            // ✅ UPDATED: Use c.image (Cloudinary URL) directly
            const imgUrl = c.image || '/images/placeholder.png';

            const price = parseFloat(c.price || 0).toFixed(2);
            const regPrice = parseFloat(c.regularPrice || 0).toFixed(2);
            const discount = c.discountPercentage || 0;
            const isActive = (c.statusId === 1) || (c.status?.id === 1) || (c.active === true);

            return `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-6 py-4 font-medium flex items-center gap-3">
                    <div class="w-10 h-10 flex-shrink-0 bg-gray-100 rounded border border-gray-200 overflow-hidden">
                         <img src="${imgUrl}" onerror="this.src='/images/placeholder.png'" class="w-full h-full object-cover" loading="lazy">
                    </div>
                    <div>
                        <div class="font-bold text-gray-800 text-sm">${c.title || 'Untitled'}</div>
                        <div class="text-xs text-gray-500">${c.name || ''}</div>
                    </div>
                </td>
                <td class="px-6 py-4">
                    <div class="flex flex-col">
                        <span class="font-bold text-gray-800">LKR ${price}</span>
                        ${discount > 0 ? `<span class="text-xs text-red-500 line-through">LKR ${regPrice}</span>` : ''}
                    </div>
                </td>
                <td class="px-6 py-4 font-bold text-gray-800">${c.stockLimit || 0}</td>
                <td class="px-6 py-4">
                    <label class="inline-flex relative items-center cursor-pointer">
                        <input type="checkbox" class="sr-only peer" 
                               ${isActive ? 'checked' : ''} 
                               onchange="toggleCollectionStatus(${c.id}, this.checked)">
                        <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-gold-300 rounded-full peer peer-checked:bg-gold-400 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border after:rounded-full after:h-4 after:w-4 after:transition-all"></div>
                    </label>
                </td>
                <td class="px-6 py-4 text-right">
                    <button onclick="openEditCollection(${c.id})" 
                            class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase transition">
                        Edit
                    </button>
                </td>
            </tr>
        `}).join('');
    } catch (err) {
        console.error("Error in loadCollections:", err);
        if(window.showToast) showToast('Error loading collections', 'error');
    }
}

// 2. Open Modal: Create Mode
function openAddCollectionModal() {
    editingCollectionId = null;

    // Reset Standard Fields
    document.getElementById('edit-collection-id').value = '';
    document.getElementById('collection-title').value = '';
    document.getElementById('collection-name').value = '';
    document.getElementById('collection-specs').value = '';
    document.getElementById('collection-warranty').value = '';
    document.getElementById('collection-material').value = '';
    document.getElementById('collection-regular-price').value = '';
    document.getElementById('collection-discount').value = '0';
    document.getElementById('collection-price').value = '';
    document.getElementById('collection-stock').value = '';

    // Reset Split Description Fields
    document.getElementById('col-desc-main').value = '';
    document.getElementById('col-desc-styling').value = '';
    document.getElementById('col-desc-care').value = '';

    // Reset Images (Show Upload Box, Hide Preview)
    for(let i=1; i<=4; i++) {
        const prev = document.getElementById(`collection-preview-${i}`);
        const input = document.getElementById(`collection-image-${i}`);

        // Reset Value
        input.value = '';

        // Reset UI
        prev.innerHTML = '';
        prev.classList.add('hidden');

        // Remove fixed height class if it exists (reset to default state)
        prev.className = 'hidden';

        // Show the upload placeholder (previous sibling)
        const uploadBox = prev.previousElementSibling;
        if(uploadBox) uploadBox.classList.remove('hidden');
    }

    // Titles
    document.getElementById('collection-form-title').innerText = 'Create New Collection';
    document.getElementById('collection-submit-btn').innerText = 'Save Collection';

    if(typeof showProductSection === 'function') {
        showProductSection('add-collection');
    }
}

// 3. Open Modal: Edit Mode
async function openEditCollection(id) {
    try {
        editingCollectionId = id;
        const response = await fetch(`/api/admin/collections/${id}`);
        if(!response.ok) throw new Error("Fetch failed");
        const c = await response.json();

        document.getElementById('collection-title').value = c.title;
        document.getElementById('collection-name').value = c.name;
        document.getElementById('collection-specs').value = c.specifications;
        document.getElementById('collection-warranty').value = c.warranty;
        document.getElementById('collection-material').value = c.material || '';
        document.getElementById('collection-regular-price').value = c.regularPrice;
        document.getElementById('collection-discount').value = c.discountPercentage;
        document.getElementById('collection-price').value = c.price;
        document.getElementById('collection-stock').value = c.stockLimit;

        try {
            const descObj = JSON.parse(c.description);
            document.getElementById('col-desc-main').value = Array.isArray(descObj.main) ? descObj.main.join('\n') : descObj.main || '';
            document.getElementById('col-desc-styling').value = Array.isArray(descObj.styling_tips) ? descObj.styling_tips.join('\n') : descObj.styling_tips || '';
            document.getElementById('col-desc-care').value = Array.isArray(descObj.care_details) ? descObj.care_details.join('\n') : descObj.care_details || '';
        } catch (e) {
            document.getElementById('col-desc-main').value = c.description;
            document.getElementById('col-desc-styling').value = '';
            document.getElementById('col-desc-care').value = '';
        }

        // ✅ UPDATED: Cloudinary Collection Image Loading
        // Backend DTO returns 'images' list OR individual 'image1', 'image2' etc fields.
        // Assuming your DTO has a list called 'images' based on previous code.
        const imagesList = c.images || [];

        for(let i=1; i<=4; i++) {
            const prev = document.getElementById(`collection-preview-${i}`);
            const uploadBox = prev.previousElementSibling;
            const input = document.getElementById(`collection-image-${i}`);

            input.value = '';

            // Attempt to get URL from list (index i-1)
            const imgUrl = imagesList[i-1];

            if (imgUrl) {
                const img = new Image();
                img.src = imgUrl;
                img.className = 'w-full h-full object-cover rounded cursor-pointer';
                img.title = 'Click to replace';
                img.onclick = () => input.click();

                img.onload = () => {
                    prev.innerHTML = '';
                    prev.appendChild(img);
                    prev.className = 'w-full h-32 rounded border border-gray-200 overflow-hidden relative';
                    prev.classList.remove('hidden');
                    if(uploadBox) uploadBox.classList.add('hidden');
                };
            } else {
                prev.innerHTML = '';
                prev.classList.add('hidden');
                if(uploadBox) uploadBox.classList.remove('hidden');
            }
        }

        document.getElementById('collection-form-title').innerText = 'Edit Collection #' + id;
        document.getElementById('collection-submit-btn').innerText = 'Update Collection';

        if(typeof showProductSection === 'function') {
            showProductSection('add-collection');
        }
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading collection details', 'error');
    }
}

// 4. Submit Handler
async function handleCollectionSubmit(e) {
    e.preventDefault(); // Disable default browser validation completely

    // -----------------------------
    // 🔥 CUSTOM VALIDATION
    // -----------------------------
    const title = document.getElementById('collection-title').value.trim();
    const name = document.getElementById('collection-name').value.trim();
    const regularPrice = document.getElementById('collection-regular-price').value;
    const stock = document.getElementById('collection-stock').value;

    if (!title) {
        if(window.showToast) showToast('Please enter collection title', 'error');
        return;
    }

    if (!name) {
        if(window.showToast) showToast('Please enter collection name', 'error');
        return;
    }

    if (!regularPrice || isNaN(regularPrice) || parseFloat(regularPrice) <= 0) {
        if(window.showToast) showToast('Please enter a valid regular price', 'error');
        return;
    }

    if (!stock || isNaN(stock) || parseInt(stock) < 0) {
        if(window.showToast) showToast('Please enter valid stock quantity', 'error');
        return;
    }

    // ✅ At least one image validation
    // Skip if editing (editingCollectionId exists)
    if (!editingCollectionId) {
        let imageSelected = false;
        for (let i = 1; i <= 4; i++) {
            const fileInput = document.getElementById(`collection-image-${i}`);
            if (fileInput.files && fileInput.files[0]) {
                imageSelected = true;
                break;
            }
        }
        if (!imageSelected) {
            if(window.showToast) showToast('Please select at least 1 collection image', 'error');
            return;
        }
    }

    // -----------------------------
    // 🔥 BUILD PAYLOAD
    // -----------------------------
    const rawMain = document.getElementById('col-desc-main').value.trim();
    const rawStyling = document.getElementById('col-desc-styling').value.trim();
    const rawCare = document.getElementById('col-desc-care').value.trim();

    const descJson = {
        main: rawMain.split('\n').map(s => s.trim()).filter(s => s),
        styling_tips: rawStyling.split('\n').map(s => s.trim()).filter(s => s),
        care_details: rawCare.split('\n').map(s => s.trim()).filter(s => s)
    };

    const payload = {
        title,
        name,
        description: JSON.stringify(descJson),
        specifications: document.getElementById('collection-specs').value,
        warranty: document.getElementById('collection-warranty').value,
        material: document.getElementById('collection-material').value,
        regularPrice: parseFloat(regularPrice),
        discountPercentage: parseFloat(document.getElementById('collection-discount').value) || 0,
        stockLimit: parseInt(stock)
    };

    const formData = new FormData();
    formData.append('collection', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    // Append selected images
    for (let i = 1; i <= 4; i++) {
        const fileInput = document.getElementById(`collection-image-${i}`);
        if (fileInput.files && fileInput.files[0]) {
            formData.append(`image${i}`, fileInput.files[0]);
        }
    }

    // -----------------------------
    // 🔥 API REQUEST
    // -----------------------------
    try {
        const url = editingCollectionId ? `/api/admin/collections/${editingCollectionId}/update` : '/api/admin/collections';
        const method = 'POST';

        const response = await fetch(url, { method, body: formData });

        if (!response.ok) {
            const errText = await response.text();
            throw new Error(errText || 'Failed to save collection');
        }

        if(window.showToast) showToast(editingCollectionId ? 'Collection updated' : 'Collection created', 'success');

        if(typeof showProductSection === 'function') {
            showProductSection('collections');
        }

        // Ensure list is refreshed
        loadCollections();

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error saving collection: ' + err.message, 'error');
    }
}

// 5. Status Toggle
async function toggleCollectionStatus(id, active) {
    try {
        await fetch(`/api/admin/collections/${id}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ active })
        });
        if(window.showToast) showToast('Status updated', 'success');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error updating status', 'error');
        loadCollections();
    }
}

// 6. Price Calc
function setupCollectionPriceCalc() {
    const regular = document.getElementById('collection-regular-price');
    const discount = document.getElementById('collection-discount');
    const final = document.getElementById('collection-price');

    if(!regular || !discount || !final) return;

    const calc = () => {
        const r = parseFloat(regular.value) || 0;
        const d = parseFloat(discount.value) || 0;
        const f = r - (r * d / 100);
        final.value = f.toFixed(2);
    };

    ['input', 'change', 'keyup'].forEach(evt => {
        regular.addEventListener(evt, calc);
        discount.addEventListener(evt, calc);
    });
}

// 7. Image Preview
function previewCollectionImage(index) {
    const input = document.getElementById(`collection-image-${index}`);
    const prev = document.getElementById(`collection-preview-${index}`);
    const uploadBox = prev.previousElementSibling; // The dashed placeholder div

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = "w-full h-full object-cover rounded cursor-pointer";
            img.title = "Click to change";

            // Allow clicking preview to change file
            img.onclick = () => input.click();

            prev.innerHTML = '';
            prev.appendChild(img);

            // 🔥 FORCE HEIGHT for new uploads too
            prev.className = 'w-full h-32 rounded border border-gray-200 overflow-hidden relative';

            prev.classList.remove('hidden'); // Show Image
            if(uploadBox) uploadBox.classList.add('hidden'); // Hide Placeholder
        };
        reader.readAsDataURL(input.files[0]);
    } else {
        // If file input cleared and no children, show placeholder
        if(prev.childNodes.length === 0) {
            prev.classList.add('hidden');
            if(uploadBox) uploadBox.classList.remove('hidden');
        }
    }
}

// Search
function filterCollections() {
    const search = document.getElementById('collection-search').value.toLowerCase();
    const rows = document.querySelectorAll('#collections-body tr');
    rows.forEach(row => {
        const text = row.innerText.toLowerCase();
        row.style.display = text.includes(search) ? '' : 'none';
    });
}

// ==========================================
// COLLECTION SET LOGIC
// ==========================================

let cachedActiveCollections = [];
let cachedActiveVariants = [];
let setRowCounter = 0;
let currentSetData = []; // Store fetched sets for easy access

// 1. Load Sets Table
async function loadCollectionSets() {
    try {
        const response = await fetch('/api/admin/collection-sets');
        if (!response.ok) throw new Error('Failed to fetch sets');

        currentSetData = await response.json(); // Cache for View/Edit
        const tbody = document.getElementById('collection-sets-body');

        if (currentSetData.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="px-6 py-4 text-center text-gray-500">No sets configured.</td></tr>';
            return;
        }

        tbody.innerHTML = currentSetData.map(set => `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-6 py-4 font-medium text-gray-800">${set.collectionName}</td>
                <td class="px-6 py-4 text-gray-600">${set.totalItems} Items</td>
                <td class="px-6 py-4 text-right">
                    <button onclick="openViewSetModal(${set.collectionId})" 
                            class="text-blue-600 hover:text-blue-800 text-xs font-bold uppercase transition mr-3">
                        View
                    </button>
                    <button onclick="openEditCollectionSet(${set.collectionId})" 
                            class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase transition">
                        Edit
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading collection sets', 'error');
    }
}

// 2. Load Active Dropdown Data (Collections & Variants)
async function loadActiveSetData() {
    try {
        // Fetch Collections and All Variants in parallel
        const [collRes, varRes] = await Promise.all([
            fetch('/api/admin/categories?status=active'),
            fetch('/api/admin/variances')
        ]);

        const allCollections = await collRes.json();
        // Adjust based on your API needs
        const realCollRes = await fetch('/api/admin/collections');
        const allRealColls = await realCollRes.json();

        // Filter Active Collections
        cachedActiveCollections = allRealColls.filter(c => c.statusId === 1);

        // Filter Active Variants
        const allVars = await varRes.json();
        cachedActiveVariants = allVars.filter(v => v.statusId === 1);

        populateCollectionDropdown();

    } catch (err) {
        console.error("Error loading active data", err);
    }
}

function populateCollectionDropdown() {
    const select = document.getElementById('set-collection-id');
    select.innerHTML = '<option value="">Choose Collection</option>' +
        cachedActiveCollections.map(c => `<option value="${c.id}">${c.title}</option>`).join('');
}

// 3. Open CREATE Modal
async function openAddCollectionSetModal() {
    document.getElementById('set-form-title').innerText = 'Create Collection Set';
    document.getElementById('set-collection-id').value = '';
    document.getElementById('set-collection-id').disabled = false; // Enable selection
    document.getElementById('set-products-container').innerHTML = '';

    setRowCounter = 0;

    await loadActiveSetData();
    addProductToSetRow(); // Add one empty row by default
    showProductSection('add-collection-set');
}

// 4. Open EDIT Modal
async function openEditCollectionSet(collectionId) {
    const set = currentSetData.find(s => s.collectionId === collectionId);
    if (!set) return;

    document.getElementById('set-form-title').innerText = 'Edit Collection Set';

    await loadActiveSetData();

    const select = document.getElementById('set-collection-id');
    select.value = collectionId;
    select.disabled = true; // Lock collection in edit mode

    const container = document.getElementById('set-products-container');
    container.innerHTML = '';
    setRowCounter = 0;

    // Populate Rows
    set.items.forEach(item => {
        addProductToSetRow(item.varianceId, item.qty);
    });

    showProductSection('add-collection-set');
}

// 5. Open VIEW Modal
function openViewSetModal(collectionId) {
    const set = currentSetData.find(s => s.collectionId === collectionId);
    if (!set) return;

    document.getElementById('view-set-title').innerText = set.collectionName;
    const tbody = document.getElementById('view-set-body');

    tbody.innerHTML = set.items.map(item => `
        <tr>
            <td class="py-3 text-gray-700 font-medium">${item.varianceName}</td>
            <td class="py-3 text-right font-bold">${item.qty}</td>
        </tr>
    `).join('');

    document.getElementById('view-set-modal').classList.remove('hidden');
    document.getElementById('view-set-modal').classList.add('flex');
}

function closeViewSetModal() {
    const modal = document.getElementById('view-set-modal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
}

function closeSetModalOnBackdrop(e) {
    if (e.target.id === 'view-set-modal') closeViewSetModal();
}

// 6. Dynamic Rows Logic
function addProductToSetRow(selectedVarianceId = null, qty = 1) {
    const container = document.getElementById('set-products-container');

    // Generate Options
    const options = cachedActiveVariants.map(v => {
        // Build descriptive name: Name + Size + Color + Gem
        let desc = v.productName;
        let attrs = [];
        if(v.sizeName && v.sizeName !== '-') attrs.push(v.sizeName);
        if(v.colorName && v.colorName !== '-') attrs.push(v.colorName);
        if(v.gemstoneName && v.gemstoneName !== '-') attrs.push(v.gemstoneName);
        if(attrs.length > 0) desc += ` (${attrs.join(' / ')})`;

        const isSelected = (selectedVarianceId && v.id === selectedVarianceId) ? 'selected' : '';
        return `<option value="${v.id}" ${isSelected}>${desc}</option>`;
    }).join('');

    const html = `
        <div class="flex gap-4 items-center p-3 bg-gray-50 border border-gray-200 rounded" id="set-row-${setRowCounter}">
            <div class="flex-1">
                <select class="w-full p-2 border border-gray-300 text-sm outline-none bg-white focus:border-gold-400" data-product-select>
                    <option value="">Select Product Variant</option>
                    ${options}
                </select>
            </div>
            <div class="w-24">
                <input type="number" min="1" value="${qty}" class="w-full p-2 border border-gray-300 text-sm outline-none text-center focus:border-gold-400" data-product-qty>
            </div>
            <button type="button" onclick="removeSetRow(${setRowCounter})" class="text-gray-400 hover:text-red-600 transition">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', html);
    setRowCounter++;
}

function removeSetRow(id) {
    const row = document.getElementById(`set-row-${id}`);
    if (row) row.remove();
}

// 7. Save Logic
async function saveCollectionSet() {
    const collectionId = document.getElementById('set-collection-id').value;

    if (!collectionId) {
        if(window.showToast) showToast('Please select a collection', 'error');
        return;
    }

    const rows = document.querySelectorAll('#set-products-container > div');
    const items = [];

    rows.forEach(row => {
        const varId = row.querySelector('[data-product-select]').value;
        const qty = row.querySelector('[data-product-qty]').value;

        if (varId && qty > 0) {
            items.push({ varianceId: parseInt(varId), qty: parseInt(qty) });
        }
    });

    if (items.length === 0) {
        if(window.showToast) showToast('Please add at least one product', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/admin/collection-sets/${collectionId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(items)
        });

        if (!response.ok) throw new Error('Failed to save set');

        if(window.showToast) showToast('Collection Set saved successfully', 'success');
        showProductSection('collection-sets');

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast(err.message, 'error');
    }
}
