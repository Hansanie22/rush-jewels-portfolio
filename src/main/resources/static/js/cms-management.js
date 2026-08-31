// ==========================================
// CMS / BANNER MANAGEMENT LOGIC
// ==========================================

async function loadCmsData() {
    loadBanners();
}

// 1. Load Banners
async function loadBanners() {
    const container = document.getElementById('cms-banners-grid');

    try {
        if (!container.innerHTML.trim()) {
            container.innerHTML = '<p class="text-center text-gray-400 py-8 col-span-2">Loading banners...</p>';
        }

        const response = await fetch('/api/admin/banners');
        if (!response.ok) throw new Error('Failed to fetch banners');

        const banners = await response.json();
        renderBannersGrid(banners);

        // Placeholder එක (Upload Button) හැමවෙලේම අන්තිමට පෙන්නන්න
        addBannerPlaceholder();

    } catch (err) {
        console.error(err);
        container.innerHTML = '<p class="text-center text-red-500 py-8 col-span-2">Failed to load banners.</p>';
        if(window.showToast) showToast('Error loading banners', 'error');
    }
}

// 2. Render Grid
function renderBannersGrid(banners) {
    const container = document.getElementById('cms-banners-grid');
    container.innerHTML = ''; // Clear existing content

    const timestamp = new Date().getTime();

    banners.forEach(b => {
        // Cloudinary URL එක Cache නොවී අලුත් වෙන්න timestamp එකක් එකතු කරනවා
        const url = b.url.includes('?') ? `${b.url}&t=${timestamp}` : `${b.url}?t=${timestamp}`;

        // Media elements logic
        // පසුබිම (Blurred) සහ ඉදිරිපස (Clear) ස්ථර දෙකම හදනවා
        const backgroundLayer = b.type === 'VIDEO'
            ? `<video src="${url}" class="absolute inset-0 w-full h-full object-cover blur-md opacity-30 scale-110" muted></video>`
            : `<img src="${url}" class="absolute inset-0 w-full h-full object-cover blur-md opacity-30 scale-110" loading="lazy">`;

        const foregroundLayer = b.type === 'VIDEO'
            ? `<video src="${url}" class="relative w-full h-full object-contain" autoplay loop muted playsinline></video>`
            : `<img src="${url}" class="relative w-full h-full object-contain" loading="lazy">`;

        const deleteButton = !isNaN(b.id) ? `
            <button onclick="deleteBanner(${b.id})" class="absolute top-2 right-2 bg-red-600 text-white w-8 h-8 flex items-center justify-center rounded-full shadow-md hover:bg-red-700 transition z-20" title="Remove">
                <i class="fas fa-times"></i>
            </button>
        ` : '';

        const html = `
            <div class="relative border border-gray-200 bg-black group h-48 shadow-sm overflow-hidden flex items-center justify-center">
                ${backgroundLayer}
                
                ${foregroundLayer}

                ${deleteButton}
                
                <div class="absolute bottom-2 left-2 bg-black bg-opacity-75 text-white text-[10px] font-bold px-2 py-1 uppercase rounded z-10">
                    ${b.type}
                </div>
            </div>
        `;

        container.insertAdjacentHTML('beforeend', html);
    });
}

// 3. Add Placeholder Logic
function addBannerPlaceholder() {
    const container = document.getElementById('cms-banners-grid');

    // Duplicate නොවෙන්න ID එකෙන් Check කරනවා
    if(document.getElementById('upload-placeholder-btn')) return;

    const tempId = 'upload-placeholder-btn';

    const placeholderHtml = `
        <div id="${tempId}" onclick="triggerBannerUpload()" 
             class="border-2 border-dashed border-gray-300 bg-gray-50 h-48 flex flex-col items-center justify-center text-gray-400 cursor-pointer hover:border-gold-400 hover:text-gold-500 hover:bg-white transition animate-pulse">
            <i class="fas fa-cloud-upload-alt text-3xl mb-2"></i>
            <span class="text-xs font-bold uppercase">Click to Upload</span>
            <span class="text-[10px] mt-1">(Image or Video)</span>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', placeholderHtml);
}

// 4. Trigger Upload
function triggerBannerUpload() {
    // HTML එකේ hidden input එකක් තියෙන්න ඕන ID එක 'cms-hidden-file-input' විදිහට
    let input = document.getElementById('cms-hidden-file-input');

    // නැත්නම් ඒක හදනවා (Safety Check)
    if (!input) {
        input = document.createElement('input');
        input.type = 'file';
        input.id = 'cms-hidden-file-input';
        input.style.display = 'none';
        document.body.appendChild(input);
    }

    input.value = ''; // පරණ අගයන් අයින් කරනවා
    input.onchange = (e) => handleBannerUpload(e.target);
    input.click();
}

// 5. Handle File Upload (Updated with Size Validation)
async function handleBannerUpload(input) {
    if (!input.files || !input.files[0]) return;

    const file = input.files[0];

    // ✅ VALIDATION: File Size Limit (100MB)
    const MAX_SIZE = 100 * 1024 * 1024; // 100MB
    if (file.size > MAX_SIZE) {
        if(window.showToast) showToast('File is too large! Max limit is 100MB.', 'error');
        else alert('File is too large! Max limit is 100MB.');

        input.value = ''; // Clear input
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    if(window.showToast) showToast('Uploading media... This may take a moment.', 'info');

    try {
        const response = await fetch('/api/admin/banners', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            // Server Error එක කියවන්න උත්සාහ කරනවා
            const errorText = await response.text();
            throw new Error(errorText || 'Upload failed');
        }

        if(window.showToast) showToast('Banner added successfully', 'success');

        // Grid එක Refresh කරනවා
        loadBanners();

    } catch (err) {
        console.error(err);

        let msg = 'Error uploading banner';
        if (err.message.includes('400') || err.message.includes('Size')) {
            msg = 'Upload failed: File size might be too large for the server.';
        }

        if(window.showToast) showToast(msg, 'error');
    } finally {
        input.value = ''; // Input එක හිස් කරනවා
    }
}

// 6. Delete Banner
function deleteBanner(id) {
    if (isNaN(id)) {
        console.warn('Cannot delete placeholder banner:', id);
        return;
    }

    // Custom confirm dialog එක තිබේ නම් භාවිතා කරයි, නැත්නම් default එක
    const confirmAction = async () => {
        try {
            const response = await fetch(`/api/admin/banners/${id}/delete`, { method: 'POST' });
            if (!response.ok) throw new Error('Delete failed');
            if(window.showToast) showToast('Banner removed', 'success');
            loadBanners();

        } catch (err) {
            console.error(err);
            if(window.showToast) showToast('Error deleting banner', 'error');
        }
    };

    if (typeof showConfirmDialog === 'function') {
        showConfirmDialog("Are you sure you want to remove this banner?", confirmAction);
    } else {
        if (confirm("Are you sure you want to remove this banner?")) {
            confirmAction();
        }
    }
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    // මේ Page එකේ Grid එක තියෙනවා නම් විතරක් Load කරන්න
    if(document.getElementById('cms-banners-grid')) {
        loadBanners();
    }
});