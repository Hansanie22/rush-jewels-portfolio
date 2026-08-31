// ==========================================
// REVIEW MANAGEMENT LOGIC
// ==========================================

let cachedReviews = [];
// 1. Load Reviews Table
async function loadReviews() {
    try {
        const response = await fetch('/api/admin/reviews');
        if (!response.ok) throw new Error('Failed to fetch reviews');

        const reviews = await response.json();
        cachedReviews = reviews;

        renderReviewTable(reviews);

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading reviews', 'error');
    }
}

// 2. Render Table
function renderReviewTable(data) {
    const tbody = document.getElementById('reviews-body');

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="px-6 py-4 text-center text-gray-500">No reviews found.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(r => {
        // Status Badge Logic
        let badgeClass = 'bg-gray-100 text-gray-800';
        if (r.statusName === 'Approved') badgeClass = 'bg-green-100 text-green-800';
        else if (r.statusName === 'Rejected') badgeClass = 'bg-red-100 text-red-800';
        else if (r.statusName === 'Pending') badgeClass = 'bg-yellow-100 text-yellow-800';

        // Star Display
        const stars = '<span class="text-gold-500">' + '★'.repeat(r.rating) + '</span>' +
            '<span class="text-gray-300">' + '☆'.repeat(5 - r.rating) + '</span>';

        // Author Logic
        const authorDisplay = r.adminId ?
            '<span class="font-bold text-blue-600">Manual (Admin)</span>' :
            (r.customerName || '<span class="text-gray-400 italic">Guest</span>');

        // ✅ FIX: Date Format (Asia/Colombo)
        const dateStr = new Date(r.createdAt).toLocaleString('en-US', {
            timeZone: 'Asia/Colombo',
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit', hour12: true
        });

        // ✅ FIX: Button Visibility Logic
        let actionButtons = '';

        // Show Approve if NOT Approved yet
        if (r.statusName !== 'Approved') {
            actionButtons += `<button onclick="updateReviewStatus(${r.id}, 2)" class="text-green-600 hover:text-green-800 text-xs font-bold uppercase mr-2" title="Approve">Approve</button>`;
        }

        // Show Reject if NOT Rejected and NOT Approved (Once approved, cannot be simply rejected, must be pending/new)
        // Wait, requirement says "dont show reject if already approved".
        // It implies: Approved -> No buttons (or maybe just Remove?). Rejected -> Show Approve. Pending -> Show Both.
        if (r.statusName !== 'Approved' && r.statusName !== 'Rejected') {
            actionButtons += `<button onclick="updateReviewStatus(${r.id}, 3)" class="text-red-600 hover:text-red-800 text-xs font-bold uppercase" title="Reject">Reject</button>`;
        } else if (r.statusName === 'Rejected') {
            // If rejected, maybe allow re-approve? Left visible above.
            // Hide reject button for rejected items.
        }

        return `
            <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                <td class="px-6 py-4 font-medium text-gray-800 text-xs">
                    ${r.varianceName || 'General Review'}
                </td>
                <td class="px-6 py-4 text-xs text-gray-700">
                    ${authorDisplay}
                </td>
                <td class="px-6 py-4 text-sm tracking-widest">${stars}</td>
                <td class="px-6 py-4">
                    <div class="text-xs text-gray-600 truncate w-64" title="${r.comment || ''}">
                        ${r.comment || '-'}
                    </div>
                </td>
                <td class="px-6 py-4 text-xs text-gray-500 font-mono">${dateStr}</td>
                <td class="px-6 py-4">
                    <span class="px-2 py-1 text-[10px] uppercase font-bold rounded ${badgeClass}">${r.statusName}</span>
                </td>
                <td class="px-6 py-4 text-right">
                    ${actionButtons}
                </td>
            </tr>
        `;
    }).join('');
}

async function loadProductVarianceDropdown() {
    try {
        const response = await fetch('/api/admin/variances');
        if(!response.ok) throw new Error("Failed");
        const variances = await response.json();

        const select = document.getElementById('review-product-variance');
        let options = '<option value="">General Review</option>';

        options += variances.map(v =>
            `<option value="${v.id}">${v.productName} (${v.sizeName || '-'}/${v.colorName || '-'})</option>`
        ).join('');

        select.innerHTML = options;
    } catch(e) { console.error(e); }
}

let selectedRating = 0; // Default no rating selected

// 4. Handle Star Click
function setRating(rating) {
    selectedRating = rating;
    document.getElementById('review-rating').value = rating;

    const stars = document.querySelectorAll('#review-rating-stars button');
    stars.forEach((btn, index) => {
        if (index < rating) {
            btn.classList.remove('text-gray-300');
            btn.classList.add('text-gold-500');
        } else {
            btn.classList.remove('text-gold-500');
            btn.classList.add('text-gray-300');
        }
    });
}

// 3. Add Review Modal
async function openAddReviewModal() {
    document.getElementById('review-edit-id').value = '';
    document.getElementById('review-product-variance').value = '';
    document.getElementById('review-content').value = '';
    document.getElementById('review-status').value = '2';

    selectedRating = 0; // Reset rating
    document.getElementById('review-rating').value = 0;

    // Reset stars UI
    const stars = document.querySelectorAll('#review-rating-stars button');
    stars.forEach(btn => {
        btn.classList.remove('text-gold-500');
        btn.classList.add('text-gray-300');
    });

    if(document.getElementById('review-customer')) document.getElementById('review-customer').value = '';
    if(document.getElementById('review-email')) document.getElementById('review-email').value = '';

    await loadProductVarianceDropdown();
    showSection('add-review');
}

// 5. Submit Review with Validation
async function handleReviewSubmit(e) {
    e.preventDefault();

    const varIdVal = document.getElementById('review-product-variance').value;
    const comment = document.getElementById('review-content').value.trim();

    // Custom validation: At least rating OR comment
    if (selectedRating === 0 && !comment) {
        if(window.showToast) showToast('Please provide a rating or comment', 'error');
        return;
    }

    // Grab optional customer fields
    const custNameEl = document.getElementById('review-customer');
    const custEmailEl = document.getElementById('review-email');
    const custName = custNameEl && custNameEl.value ? custNameEl.value : null;
    const custEmail = custEmailEl && custEmailEl.value ? custEmailEl.value : null;

    const payload = {
        varianceId: varIdVal ? parseInt(varIdVal) : null,
        adminId: 1,
        rating: selectedRating,
        comment: comment || null,
        customerName: custName,
        customerEmail: custEmail,
        statusId: parseInt(document.getElementById('review-status').value)
    };

    try {
        const response = await fetch('/api/admin/reviews', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error('Failed to save');

        if(window.showToast) showToast('Review saved successfully', 'success');
        showSection('reviews');
        loadReviews();
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error adding review', 'error');
    }
}


// 6. Update Status
async function updateReviewStatus(id, statusId) {
    try {
        const response = await fetch(`/api/admin/reviews/${id}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ statusId: statusId })
        });

        if (!response.ok) throw new Error('Update failed');

        const action = statusId === 2 ? 'Approved' : 'Rejected';
        if(window.showToast) showToast(`Review ${action}`, 'success');
        loadReviews();
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error updating status', 'error');
    }
}

// 7. Filter Logic
function filterReviews() {
    const search = document.getElementById('review-search').value.toLowerCase();
    const statusFilter = document.getElementById('review-status-filter').value;

    const filtered = cachedReviews.filter(r => {
        const matchesSearch = (r.varianceName || '').toLowerCase().includes(search) ||
            (r.comment || '').toLowerCase().includes(search);

        let matchesStatus = true;
        if (statusFilter !== 'all') {
            matchesStatus = (r.statusName || '').toLowerCase() === statusFilter.toLowerCase();
        }

        return matchesSearch && matchesStatus;
    });

    renderReviewTable(filtered);
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadReviews();
    window.openAddReviewModal = openAddReviewModal;
});