// ==========================================================================
// 1. CONFIRMATION MODAL SYSTEM (Reusable)
// ==========================================================================

window.confirmStore = window.confirmStore || new Map();

window.showConfirm = function (message, onConfirm, onCancel = null, id = null) {
    let container = document.getElementById("confirm-container");

    if (!container) {
        container = document.createElement('div');
        container.id = 'confirm-container';
        document.body.appendChild(container);
    }

    if (id && window.confirmStore.has(id)) {
        const existingModal = window.confirmStore.get(id);
        existingModal.querySelector(".confirm-message").textContent = message;
        existingModal.onConfirmCallback = onConfirm;
        existingModal.onCancelCallback = onCancel;
        return;
    }

    const modal = document.createElement("div");
    modal.className = `
        fixed inset-0 bg-black bg-opacity-40 flex justify-center items-center
        opacity-0 transition-opacity duration-300 z-[60]
    `;

    modal.innerHTML = `
        <div class="bg-white p-5 rounded-lg shadow-xl w-72 transform scale-95 transition-all duration-300 border-t-4 border-gold-500">
            <h4 class="text-xs font-bold uppercase text-gray-500 mb-2">Confirmation</h4>
            <p class="confirm-message text-gray-900 text-sm font-medium mb-4">${message}</p>

            <div class="flex justify-end gap-2">
                <button class="cancel-btn px-3 py-1.5 text-xs font-bold uppercase text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded transition">Cancel</button>
                <button class="confirm-btn px-3 py-1.5 text-xs font-bold uppercase bg-black text-gold-400 hover:bg-gray-800 rounded shadow-sm transition">Confirm</button>
            </div>
        </div>
    `;

    modal.onConfirmCallback = onConfirm;
    modal.onCancelCallback = onCancel;

    container.appendChild(modal);

    requestAnimationFrame(() => {
        modal.style.opacity = "1";
        modal.querySelector("div").style.transform = "scale(1)";
    });

    modal.querySelector(".confirm-btn").addEventListener("click", () => {
        hideConfirm(modal, id);
        if (typeof modal.onConfirmCallback === "function") modal.onConfirmCallback();
    });

    modal.querySelector(".cancel-btn").addEventListener("click", () => {
        hideConfirm(modal, id);
        if (typeof modal.onCancelCallback === "function") modal.onCancelCallback();
    });

    if (id) window.confirmStore.set(id, modal);
};

window.hideConfirm = function (modal, id = null) {
    modal.style.opacity = "0";
    modal.querySelector("div").style.transform = "scale(0.95)";

    setTimeout(() => {
        if (modal && modal.parentNode) modal.remove();
        if (id) window.confirmStore.delete(id);
    }, 250);
};


// ==========================================================================
// 2. AI TAG GENERATION LOGIC
// ==========================================================================

const MAX_RETRIES = 5;

async function retryFetchWithExponentialBackoff(url, options) {
    for (let i = 0; i < MAX_RETRIES; i++) {
        try {
            const response = await fetch(url, options);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response;
        } catch (error) {
            if (i === MAX_RETRIES - 1) {
                throw error;
            }
            const delay = Math.pow(2, i) * 1000 + Math.random() * 1000;
            await new Promise(resolve => setTimeout(resolve, delay));
        }
    }
}

function updateBlogTags(tags, isChecked = true) {
    const container = document.getElementById('blog-tags-container');
    if (!container) return; // Guard clause

    container.innerHTML = '';

    if (!tags || tags.length === 0) {
        container.innerHTML = '<span class="text-sm text-gray-500">Start typing content and click \'Generate Tags\'.</span>';
        return;
    }

    const uniqueTags = Array.from(new Set(tags)).sort();

    uniqueTags.forEach(tag => {
        const tagId = 'tag-' + tag.replace(/\s+/g, '-').toLowerCase();
        const tagElement = `
            <label class="flex items-center space-x-2 text-sm text-gray-700 p-2 bg-white border rounded shadow-sm hover:bg-gray-50 transition duration-150">
                <input type="checkbox" name="blog-tag" value="${tag}" id="${tagId}" class="form-checkbox h-4 w-4 text-black border-gray-300 rounded focus:ring-black" ${isChecked ? 'checked' : ''}>
                <span class="font-medium">${tag}</span>
            </label>
        `;
        container.innerHTML += tagElement;
    });
}

async function generateTagsFromContent(content) {
    try {
        const response = await fetch('/api/admin/blog/generate-tags', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content })
        });

        if (!response.ok) throw new Error('Failed to generate tags');

        const result = await response.json();
        return result.tags || [];
    } catch (error) {
        console.error("Server-side Tag Generation Error:", error);
        throw new Error("Failed to generate tags using the AI service.");
    }
}

window.handleGenerateTags = async function() {
    const content = document.getElementById('blog-content').value.trim();
    const loader = document.getElementById('tag-generation-loader');

    if (content.length < 50) {
        if(window.showToast) showToast("Please write at least 50 characters of blog content before generating tags.", 'info');
        return;
    }

    if(loader) loader.classList.remove('hidden');

    try {
        const tags = await generateTagsFromContent(content);
        updateBlogTags(tags, true);
        if(window.showToast) showToast(`Successfully generated ${tags.length} tags.`, 'success');

    } catch (error) {
        console.error(error);
        const container = document.getElementById('blog-tags-container');
        if(container) container.innerHTML = '<span class="text-sm text-red-500">Error generating tags. Check console.</span>';
        if(window.showToast) showToast("AI Error: Failed to generate tags.", 'error');
    } finally {
        if(loader) loader.classList.add('hidden');
    }
}


// ==========================================
// 3. BLOG MANAGEMENT LOGIC
// ==========================================

let editingBlogId = null;

// 1. Load Blog Posts
window.loadBlogPosts = async function() {
    try {
        const response = await fetch('/api/admin/blog');
        if (!response.ok) throw new Error('Failed to fetch posts');

        const posts = await response.json();
        renderBlogTable(posts);

        const countElement = document.getElementById('blog-count');
        if (countElement) countElement.textContent = posts.length.toString();

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading blog posts', 'error');
    }
}

// Load Product Variances
async function loadProductVariances() {
    try {
        const response = await fetch('/api/admin/blog/product-variances');
        if (!response.ok) throw new Error('Failed to fetch products');
        return await response.json();
    } catch (err) {
        console.error('Error loading products:', err);
        return [];
    }
}

// ----------------------------------------------------------------------
// 🔥 RENAMED THIS FUNCTION TO AVOID CONFLICT WITH COLLECTIONS PAGE
// ----------------------------------------------------------------------
async function fetchCollectionsForBlog() {
    try {
        const response = await fetch('/api/admin/blog/collections');
        if (!response.ok) throw new Error('Failed to fetch collections');
        return await response.json();
    } catch (err) {
        console.error('Error loading collections:', err);
        return [];
    }
}

let associatedItems = [];

async function loadAssociatedItems() {
    try {
        // 🔥 Updated the function call here
        const [products, collections] = await Promise.all([
            loadProductVariances(),
            fetchCollectionsForBlog()
        ]);

        associatedItems = [...products, ...collections];
        populateAssociatedDropdown();
    } catch (err) {
        console.error('Error loading associated items:', err);
    }
}

function populateAssociatedDropdown() {
    const select = document.getElementById('blog-products');
    if (!select) return;

    const sortedItems = associatedItems.sort((a, b) => {
        if (a.type === b.type) {
            return a.name.localeCompare(b.name);
        }
        return a.type === 'product' ? -1 : 1;
    });

    select.innerHTML = sortedItems.map(item => {
        const prefix = item.type === 'product' ? '🔷' : '📦';
        const displayName = `${prefix} ${item.name} (${item.slug})`;
        return `<option value="${item.type}-${item.id}" data-type="${item.type}" data-id="${item.id}">${displayName}</option>`;
    }).join('');
}

function renderBlogTable(data) {
    const tbody = document.getElementById('blog-body');

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="px-6 py-8 text-center text-gray-500">No blog posts found.</td></tr>';
        return;
    }

    const timestamp = new Date().getTime();

    tbody.innerHTML = data.map(p => {
        const imgUrl = p.image ? `${p.image}?t=${timestamp}` : 'https://placehold.co/48x48/F3F4F6/9CA3AF?text=N%2FA';

        return `
        <tr class="hover:bg-gray-50 transition border-b border-gray-100">
            <td class="px-6 py-4">
                <div class="w-12 h-12 bg-gray-200 rounded overflow-hidden border border-gray-300">
                    <img src="${imgUrl}" class="w-full h-full object-cover" onerror="this.src='https://placehold.co/48x48/F3F4F6/9CA3AF?text=N%2FA'" loading="lazy">
                </div>
            </td>
            <td class="px-6 py-4 font-bold text-gray-800 text-sm">${p.title}</td>
            <td class="px-6 py-4 text-xs text-gray-500">
                ${new Date(p.date).toLocaleDateString()}
            </td>
            <td class="px-6 py-4 text-right">
                <button onclick="openEditBlog(${p.id})" class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase mr-3">Edit</button>
                <button onclick="deleteBlogPost(${p.id})" class="text-red-600 hover:text-red-800 text-xs font-bold uppercase">Delete</button>
            </td>
        </tr>
    `}).join('');
}

window.openAddBlogModal = function() {
    editingBlogId = null;
    document.getElementById('blog-edit-id').value = '';
    document.getElementById('blog-title').value = '';
    document.getElementById('blog-category').value = '';
    document.getElementById('blog-snippet').value = '';
    document.getElementById('blog-content').value = '';
    document.getElementById('blog-read-time').value = '';
    document.getElementById('blog-image').value = '';
    document.getElementById('blog-is-published').checked = true;

    document.getElementById('blog-image-preview').classList.add('hidden');
    document.getElementById('blog-upload-placeholder').classList.remove('hidden');

    updateBlogTags(null);

    const select = document.getElementById('blog-products');
    if (select) {
        Array.from(select.options).forEach(option => option.selected = false);
    }

    document.getElementById('blog-form-title').innerText = 'Create New Blog Post';
    document.getElementById('blog-submit-text').innerText = 'Publish Post';

    if(typeof showSection === 'function') showSection('add-blog');
}

window.openEditBlog = async function(id) {
    try {
        const response = await fetch(`/api/admin/blog/${id}`);
        if(!response.ok) throw new Error("Failed");
        const post = await response.json();

        editingBlogId = id;
        document.getElementById('blog-edit-id').value = post.id;
        document.getElementById('blog-title').value = post.title || '';
        document.getElementById('blog-category').value = post.category || '';
        document.getElementById('blog-snippet').value = post.snippet || '';
        document.getElementById('blog-content').value = post.content || '';
        document.getElementById('blog-read-time').value = post.readTime || '';
        document.getElementById('blog-is-published').checked = post.isPublished !== false;
        document.getElementById('blog-image').value = '';

        const preview = document.getElementById('blog-image-preview');
        const placeholder = document.getElementById('blog-upload-placeholder');

        if (post.image) {
            preview.src = post.image + "?t=" + new Date().getTime();
            preview.classList.remove('hidden');
            placeholder.classList.add('hidden');
        } else {
            preview.classList.add('hidden');
            placeholder.classList.remove('hidden');
        }

        updateBlogTags(post.tags || [], true);

        const select = document.getElementById('blog-products');
        if (select) {
            const selectedProductIds = post.productVarianceIds || [];
            const selectedCollectionIds = post.collectionIds || [];

            Array.from(select.options).forEach(option => {
                const type = option.getAttribute('data-type');
                const id = parseInt(option.getAttribute('data-id'));

                if (type === 'product' && selectedProductIds.includes(id)) {
                    option.selected = true;
                } else if (type === 'collection' && selectedCollectionIds.includes(id)) {
                    option.selected = true;
                }
            });
        }

        document.getElementById('blog-form-title').innerText = 'Edit Blog Post';
        document.getElementById('blog-submit-text').innerText = 'Update Post';

        if(typeof showSection === 'function') showSection('add-blog');
    } catch (e) {
        console.error(e);
        if(window.showToast) showToast('Error loading blog post', 'error');
    }
}

window.previewBlogImage = function() {
    const input = document.getElementById('blog-image');
    const preview = document.getElementById('blog-image-preview');
    const placeholder = document.getElementById('blog-upload-placeholder');

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            preview.classList.remove('hidden');
            placeholder.classList.add('hidden');
        }
        reader.readAsDataURL(input.files[0]);
    }
}

window.handleBlogSubmit = async function(e) {
    e.preventDefault();

    const title = document.getElementById('blog-title').value.trim();
    const category = document.getElementById('blog-category').value.trim();
    const snippet = document.getElementById('blog-snippet').value.trim();
    const content = document.getElementById('blog-content').value.trim();
    const readTime = document.getElementById('blog-read-time').value.trim();
    const isPublished = document.getElementById('blog-is-published').checked;
    const fileInput = document.getElementById('blog-image');

    const slug = title.toLowerCase()
        .replace(/[^a-z0-9\s-]/g, '')
        .replace(/\s+/g, '-')
        .replace(/-+/g, '-')
        .substring(0, 200);

    const selectedTags = Array.from(document.querySelectorAll('#blog-tags-container input[name="blog-tag"]:checked'))
        .map(cb => cb.value);

    const select = document.getElementById('blog-products');
    const selectedProducts = [];
    const selectedCollections = [];

    if(select) {
        Array.from(select.selectedOptions).forEach(option => {
            const type = option.getAttribute('data-type');
            const id = option.getAttribute('data-id');

            if (type === 'product') {
                selectedProducts.push(id);
            } else if (type === 'collection') {
                selectedCollections.push(id);
            }
        });
    }

    if (!title) { if(window.showToast) showToast('Please enter a blog title', 'error'); return; }
    if (!category) { if(window.showToast) showToast('Please enter a category', 'error'); return; }
    if (!snippet) { if(window.showToast) showToast('Please enter a snippet', 'error'); return; }
    if (!content) { if(window.showToast) showToast('Please enter blog content', 'error'); return; }

    if (!editingBlogId && (!fileInput.files || !fileInput.files[0])) {
        if(window.showToast) showToast('Please select an image for the blog', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('slug', slug);
    formData.append('snippet', snippet);
    formData.append('content', content);
    formData.append('category', category);
    formData.append('readTime', readTime || '');
    formData.append('isPublished', isPublished);
    formData.append('tags', selectedTags.join(','));
    formData.append('productVarianceIds', selectedProducts.join(','));
    formData.append('collectionIds', selectedCollections.join(','));

    if (fileInput.files[0]) {
        formData.append('image', fileInput.files[0]);
    }

    try {
        const url = editingBlogId ? `/api/admin/blog/${editingBlogId}/update` : '/api/admin/blog';
        const method = 'POST';

        const response = await fetch(url, { method, body: formData });

        if (!response.ok) throw new Error('Failed to save');

        if(window.showToast) showToast(editingBlogId ? 'Post updated' : 'Post published', 'success');
        if(typeof showSection === 'function') showSection('blog');
        loadBlogPosts();
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error saving post', 'error');
    }
}

window.deleteBlogPost = function(id) {
    showConfirm(
        "Are you sure you want to delete this post permanently?",
        async () => {
            try {
                const response = await fetch(`/api/admin/blog/${id}/delete`, { method: 'POST' });
                if (!response.ok) throw new Error('Failed');

                if(window.showToast) showToast('Post deleted', 'success');
                loadBlogPosts();
            } catch (e) {
                console.error(e);
                if(window.showToast) showToast('Error deleting post', 'error');
            }
        }
    );
}

document.addEventListener('DOMContentLoaded', () => {
    loadBlogPosts();
    loadAssociatedItems();
});