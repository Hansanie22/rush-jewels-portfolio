document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
    initializeModal();

    const form = document.getElementById('category-form');
    if (form) form.addEventListener('submit', handleCategorySubmit);

    const searchInput = document.getElementById('category-search');
    if (searchInput) searchInput.addEventListener('input', () => filterTable('category-table', 0));
});
// ---------------- Load Categories ----------------
function loadCategories() {
    // showToast('Loading categories...', 'info', 'loading-categories'); // Optional loading toast
    fetch('/api/admin/categories')
        .then(response => response.json())
        .then(data => {
            renderCategoryTable(data);
            // showToast('Categories loaded successfully', 'success', 'loading-categories');
        })
        .catch(err => {
            console.error('Error loading categories:', err);
            showToast('Failed to load categories', 'error', 'loading-categories');
        });
}

// ---------------- Render Category Table ----------------
function renderCategoryTable(categories) {
    const tbody = document.getElementById('categories-body');
    tbody.innerHTML = '';

    categories.forEach(cat => {
        const tr = document.createElement('tr');
        tr.className = "hover:bg-gray-50 border-b border-gray-100 transition";

        const statusToggle = `
            <label class="relative inline-flex items-center cursor-pointer">
                <input type="checkbox" 
                       class="sr-only peer" 
                       ${cat.statusId === 1 ? 'checked' : ''} 
                       onchange="toggleCategoryStatus(${cat.id})">
                <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-gold-300 rounded-full peer peer-checked:bg-gold-400 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border after:rounded-full after:h-4 after:w-4 after:transition-all">
                </div>
            </label>
        `;

        const safeCategoryName = (cat.category || '').replace(/'/g, "\\'");

        tr.innerHTML = `
            <td class="px-6 py-4 font-medium text-gray-800">${cat.category}</td>
            <td class="px-6 py-4 text-gray-600">${cat.productCount || 0}</td>
            <td class="px-6 py-4">${statusToggle}</td>
            <td class="px-6 py-4 text-right">
                <div class="flex justify-end gap-3">
                    <!-- UPDATED: Replaced Icons with Text Buttons -->
                    <button onclick="editCategory(${cat.id})" 
                            class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase transition" 
                            title="Edit">
                        Edit
                    </button>
                    <button onclick="viewCategoryProducts(${cat.id}, '${safeCategoryName}')" 
                            class="text-blue-600 hover:text-blue-800 text-xs font-bold uppercase transition" 
                            title="View Products">
                        View Products
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// ---------------- Toggle Category Status ----------------
function toggleCategoryStatus(id) {
    // showToast('Updating status...', 'info', `toggle-status-${id}`);
    fetch(`/api/admin/categories/${id}/status`, { method: 'POST' })
        .then(response => {
            if (!response.ok) throw new Error('Failed to toggle status');
            return response.json();
        })
        .then(data => {
            showToast('Status updated', 'success', `toggle-status-${id}`);
            // loadCategories(); // Optional: reload if you want to confirm server state
        })
        .catch(err => {
            console.error('Error toggling status:', err);
            showToast('Failed to update status', 'error', `toggle-status-${id}`);
            loadCategories(); // Revert checkbox on error
        });
}

// ---------------- Edit Category ----------------
function editCategory(id) {
    showSectionOnly('add-category');
    // showToast('Loading category...', 'info', `edit-category-${id}`);
    fetch(`/api/admin/categories/${id}/raw`)
        .then(res => {
            if (!res.ok) throw new Error('Failed to fetch category');
            return res.json();
        })
        .then(data => {
            document.getElementById('category-edit-id').value = data.id || '';
            document.getElementById('category-name').value = data.category || '';
            document.getElementById('category-status').checked = (data.statusId === 1);
            document.getElementById('category-form-title').textContent = 'Edit Category';
            document.getElementById('category-submit-text').textContent = 'Update Category';
            // showToast('Category loaded', 'success', `edit-category-${id}`);
        })
        .catch(err => {
            console.error(err);
            showToast('Failed to load category: ' + err.message, 'error', `edit-category-${id}`);
        });
}

// ---------------- Handle Category Form Submit ----------------
function handleCategorySubmit(event) {
    event.preventDefault();

    const editId = document.getElementById('category-edit-id').value;
    const categoryName = document.getElementById('category-name').value.trim();
    const statusId = document.getElementById('category-status').checked ? 1 : 2;

    if (!categoryName) {
        showToast('Category name cannot be empty', 'error');
        return;
    }

    const categoryData = { category: categoryName, statusId };
    if (editId) categoryData.id = parseInt(editId);

    const url = editId ? `/api/admin/categories/${editId}/update` : '/api/admin/categories';
    const method = 'POST';

    // showToast(editId ? 'Updating category...' : 'Creating category...', 'info', 'category-submit');

    fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(categoryData)
    })
        .then(async res => {
            if (!res.ok) {
                // Try to parse JSON error
                let errorMessage = 'Failed to save category';
                try {
                    const data = await res.json();
                    if (data.error) errorMessage = data.error;
                } catch (e) {
                    // fallback to text
                    const text = await res.text();
                    if (text) errorMessage = text;
                }
                throw new Error(errorMessage);
            }
            return res.json();
        })
        .then(data => {
            showToast(editId ? 'Category updated successfully!' : 'Category created successfully!', 'success', 'category-submit');
            resetCategoryForm();
            showSection('categories');
            loadCategories();
        })
        .catch(err => {
            showToast(err.message, 'error', 'category-submit');
            console.warn('Validation error:', err.message);
        });

}

// ---------------- Reset Form ----------------
function resetCategoryForm() {
    document.getElementById('category-edit-id').value = '';
    document.getElementById('category-name').value = '';
    document.getElementById('category-status').checked = true;
    document.getElementById('category-form-title').textContent = 'Create New Category';
    document.getElementById('category-submit-text').textContent = 'Create Category';
}

// ---------------- Section Management ----------------
function showSectionOnly(sectionName) {
    document.querySelectorAll('.section').forEach(s => s.classList.add('hidden'));
    const section = document.getElementById(sectionName + '-section');
    if (section) section.classList.remove('hidden');
}

function showSection(sectionName) {
    document.querySelectorAll('.section').forEach(s => s.classList.add('hidden'));
    const section = document.getElementById(sectionName + '-section');
    if (section) section.classList.remove('hidden');
    if (sectionName === 'add-category') resetCategoryForm();
}

// ---------------- Products Modal ----------------
function initializeModal() {
    if (!document.getElementById('products-modal')) {
        const modalHTML = `
            <div id="products-modal" class="fixed inset-0 bg-black bg-opacity-50 hidden items-center justify-center z-50" onclick="closeModalOnBackdrop(event)">
                <div class="bg-white w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col shadow-2xl" onclick="event.stopPropagation()">
                    <div class="p-6 border-b border-gray-200 bg-black text-white flex justify-between items-center">
                        <h3 class="font-bold uppercase text-sm tracking-wider" id="modal-category-title">Products in Category</h3>
                        <button onclick="closeProductsModal()" class="text-white hover:text-gold-400 text-2xl leading-none">&times;</button>
                    </div>
                    <div class="p-0 overflow-y-auto flex-1">
                        <div id="modal-products-content" class="text-center py-8">
                            <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-gold-500"></div>
                            <p class="mt-2 text-gray-500 text-sm">Loading products...</p>
                        </div>
                    </div>
                    <div class="p-4 border-t border-gray-200 bg-gray-50 flex justify-end">
                        <button onclick="closeProductsModal()" class="bg-black text-gold-400 px-6 py-2 text-xs font-bold uppercase hover:bg-gray-900 transition">Close</button>
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHTML);
    }
}

function viewCategoryProducts(categoryId, categoryName) {
    const modal = document.getElementById('products-modal');
    const modalTitle = document.getElementById('modal-category-title');
    const modalContent = document.getElementById('modal-products-content');

    modalTitle.textContent = `Products in "${categoryName}"`;
    modalContent.innerHTML = `<div class="text-center py-8"><div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-gold-500"></div><p class="mt-2 text-gray-500 text-sm">Loading products...</p></div>`;
    modal.classList.remove('hidden'); modal.classList.add('flex');

    // showToast('Loading products...', 'info', `category-products-${categoryId}`);

    fetch(`/api/admin/categories/${categoryId}/products`)
        .then(res => {
            if (!res.ok) throw new Error('Failed to load products');
            return res.json();
        })
        .then(products => {
            // hideToast(toastStore.get(`category-products-${categoryId}`));
            if (!products || products.length === 0) {
                modalContent.innerHTML = `<div class="text-center py-12"><p class="text-gray-500 text-sm">No products found in this category.</p></div>`;
                return;
            }
            modalContent.innerHTML = `
                <div class="bg-white">
                    <table class="w-full text-sm text-left">
                        <thead class="bg-gray-50 border-b border-gray-200 uppercase text-xs text-gray-500 sticky top-0">
                            <tr>
                                <th class="px-6 py-3">Product Name</th>
                                <th class="px-6 py-3">Title</th>
                                <th class="px-6 py-3">Status</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-gray-100">
                            ${products.map(p => `
                                <tr class="hover:bg-gray-50 transition">
                                    <td class="px-6 py-3 font-medium text-gray-800">${p.name || 'N/A'}</td>
                                    <td class="px-6 py-3 text-gray-600">${p.title || 'N/A'}</td>
                                    <td class="px-6 py-3">
                                        <span class="px-2 py-1 text-xs rounded border ${p.statusId === 1 ? 'bg-green-50 text-green-700 border-green-200' : 'bg-red-50 text-red-700 border-red-200'}">
                                            ${p.statusId === 1 ? 'Active' : 'Inactive'}
                                        </span>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>`;
        })
        .catch(err => {
            console.error(err);
            modalContent.innerHTML = `<div class="text-center py-12"><p class="text-red-500 text-sm font-bold">Failed to load products</p></div>`;
            showToast('Failed to load products', 'error', `category-products-${categoryId}`);
        });
}

function closeProductsModal() {
    const modal = document.getElementById('products-modal');
    if (modal) { modal.classList.add('hidden'); modal.classList.remove('flex'); }
}

function closeModalOnBackdrop(event) {
    if (event.target && event.target.id === 'products-modal') closeProductsModal();
}

// ---------------- Filter Table ----------------
function filterTable(tableId, columnIndex) {
    const input = document.getElementById('category-search');
    const filter = input.value.toUpperCase();
    const table = document.getElementById('categories-body'); // Changed to body ID for easier selection
    if (!table) return;
    const tr = table.getElementsByTagName('tr');

    for (let i = 0; i < tr.length; i++) {
        const td = tr[i].getElementsByTagName('td')[columnIndex];
        if (td) {
            const txtValue = td.textContent || td.innerText;
            tr[i].style.display = txtValue.toUpperCase().indexOf(filter) > -1 ? "" : "none";
        }
    }
}