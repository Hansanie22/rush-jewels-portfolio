let editingSaleId = null;
let selectedItem = null;
let debounceTimer;

async function loadSeasonalSales() {
    const tbody = document.getElementById('seasonal-sales-body');
    if (tbody) {
        tbody.innerHTML = '<tr><td colspan="6" class="px-6 py-4 text-center text-gray-500">Loading sales...</td></tr>';
    }

    try {
        const res = await fetch('/api/admin/seasonal-sales');
        if (!res.ok) throw new Error('Failed to fetch');
        const sales = await res.json();
        renderSeasonalTable(sales);
    } catch (err) {
        console.error(err);
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="6" class="px-6 py-4 text-center text-red-500">Error loading data.</td></tr>';
        }
        if (window.showToast) showToast('Error loading seasonal sales', 'error');
    }
}

function renderSeasonalTable(data) {
    const tbody = document.getElementById('seasonal-sales-body');
    if (!tbody) return;

    if (!data || !data.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="px-6 py-4 text-center text-gray-500">No active seasonal sales found.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(item => {
        const isActive = item.isActive === true || item.active === true;
        const statusBadge = isActive
            ? '<span class="bg-green-100 text-green-800 text-xs px-2 py-1 rounded font-bold uppercase">Active</span>'
            : '<span class="bg-gray-100 text-gray-800 text-xs px-2 py-1 rounded font-bold uppercase">Inactive</span>';

        const imgUrl = item.itemImage || '/favicon.jpg';
        // Escape quotes for safe HTML attribute injection
        const safeItem = JSON.stringify(item).replace(/"/g, '&quot;');

        return `
        <tr class="hover:bg-gray-50 border-b border-gray-100">
            <td class="px-6 py-4 flex items-center gap-3">
                <img src="${imgUrl}" onerror="this.src='/favicon.jpg'" class="w-10 h-10 object-cover border border-gray-200" loading="lazy">
                <div>
                    <div class="font-bold text-gray-900">${item.itemName || 'Unknown Item'}</div>
                    <div class="text-xs text-gray-500">${item.itemSku || '-'}</div>
                </div>
            </td>
            <td class="px-6 py-4 text-gray-600 font-medium text-xs">${item.description || ''}</td>
            <td class="px-6 py-4 text-xs">${item.startDate || '-'}</td>
            <td class="px-6 py-4 text-xs">${item.endDate || '-'}</td>
            <td class="px-6 py-4">${statusBadge}</td>
            <td class="px-6 py-4 text-right">
                <button onclick="openEditSeasonalSale(${safeItem})" class="text-gold-600 hover:text-gold-800 text-xs font-bold uppercase mr-3">Edit</button>
                <button onclick="deleteSeasonalSale(${item.id})" class="text-red-600 hover:text-red-800 text-xs font-bold uppercase">Remove</button>
            </td>
        </tr>`;
    }).join('');
}

// 3. Open Modal: Create
window.openAddSeasonalSaleModal = function() {
    editingSaleId = null;
    selectedItem = null;

    document.getElementById('seasonal-edit-id').value = '';
    document.getElementById('seasonal-product-search').value = '';
    document.getElementById('seasonal-description').value = '';
    document.getElementById('seasonal-status').checked = true;

    document.getElementById('seasonal-start-date').value = '';
    document.getElementById('seasonal-end-date').value = '';

    document.getElementById('seasonal-form-title').innerText = 'Add Seasonal Sale';
    document.getElementById('seasonal-submit-text').innerText = 'Save Sale';

    document.getElementById('product-suggestions').classList.add('hidden');
    if (typeof showSection === 'function') showSection('add-seasonal-sale');
};

// 4. Open Modal: Edit
window.openEditSeasonalSale = function(saleData) {
    editingSaleId = saleData.id;

    selectedItem = {
        id: saleData.type === 'PRODUCT' ? saleData.productVarianceId : saleData.collectionId,
        name: saleData.itemName,
        type: saleData.type
    };

    document.getElementById('seasonal-product-search').value = saleData.itemName;
    document.getElementById('seasonal-description').value = saleData.description;
    document.getElementById('seasonal-status').checked = saleData.isActive;

    document.getElementById('seasonal-start-date').value = saleData.startDate || '';
    document.getElementById('seasonal-end-date').value = saleData.endDate || '';

    document.getElementById('seasonal-form-title').innerText = 'Edit Seasonal Sale';
    document.getElementById('seasonal-submit-text').innerText = 'Update Sale';

    if (typeof showSection === 'function') showSection('add-seasonal-sale');
}

// 5. Setup Search Listener (Autocomplete with Debounce)
document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('seasonal-product-search');
    const suggestionsBox = document.getElementById('product-suggestions');

    if (searchInput) {
        searchInput.addEventListener('keyup', (e) => {
            clearTimeout(debounceTimer);
            const query = e.target.value;

            if (query.length < 2) {
                suggestionsBox.classList.add('hidden');
                return;
            }

            // Debounce the API call (wait 300ms after last keystroke)
            debounceTimer = setTimeout(() => fetchSuggestions(query), 300);
        });
    }

    // Hide suggestions when clicking outside
    document.addEventListener('click', (e) => {
        if (searchInput && suggestionsBox && !searchInput.contains(e.target) && !suggestionsBox.contains(e.target)) {
            suggestionsBox.classList.add('hidden');
        }
    });

    // Initial Load
    if(document.getElementById('seasonal-sales-body')) {
        loadSeasonalSales();
    }
});

// 6. Fetch Suggestions from API
async function fetchSuggestions(query) {
    const suggestionsBox = document.getElementById('product-suggestions');

    try {
        const response = await fetch(`/api/admin/seasonal-sales/search?q=${encodeURIComponent(query)}`);
        if(!response.ok) throw new Error("Search failed");

        const items = await response.json();

        if (items.length > 0) {
            suggestionsBox.innerHTML = items.map(item => `
                <div class="p-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 flex items-center gap-2" 
                     onclick="selectItem('${item.id}', '${item.name.replace(/'/g, "\\'")}', '${item.type}')">
                    <img src="${item.image}" onerror="this.src='/favicon.jpg'" class="w-8 h-8 object-cover rounded-sm" loading="lazy">
                    <div>
                        <div class="text-sm font-bold text-gray-800">${item.name}</div>
                        <div class="text-xs text-gray-500">${item.sku} <span class="text-gold-600 font-bold">(${item.type})</span></div>
                    </div>
                </div>
            `).join('');
            suggestionsBox.classList.remove('hidden');
        } else {
            suggestionsBox.innerHTML = '<div class="p-3 text-xs text-gray-500">No active products or collections found</div>';
            suggestionsBox.classList.remove('hidden');
        }
    } catch (err) {
        console.error(err);
    }
}

// 7. Handle Selection from Dropdown
window.selectItem = function(id, name, type) {
    selectedItem = { id: parseInt(id), name, type };
    document.getElementById('seasonal-product-search').value = name;
    document.getElementById('product-suggestions').classList.add('hidden');
};

// 8. Submit Form (Create or Update)
window.handleSeasonalSaleSubmit = async function(e) {
    e.preventDefault();

    const description = document.getElementById('seasonal-description').value.trim();
    const startDate = document.getElementById('seasonal-start-date').value;
    const endDate = document.getElementById('seasonal-end-date').value;
    const status = document.getElementById('seasonal-status').checked;

    if (!selectedItem) {
        if (window.showToast) showToast('Please search and select a product/collection', 'error');
        return;
    }
    if (!description) {
        if (window.showToast) showToast('Description is required', 'error');
        return;
    }
    if (!startDate) {
        if (window.showToast) showToast('Start date is required', 'error');
        return;
    }
    if (!endDate) {
        if (window.showToast) showToast('End date is required', 'error');
        return;
    }

    if (new Date(endDate) < new Date(startDate)) {
        if (window.showToast) showToast('End date cannot be before start date', 'error');
        return;
    }

    const payload = {
        id: editingSaleId ? Number(editingSaleId) : 0,
        description,
        startDate,
        endDate,
        isActive: status,
        type: selectedItem.type,
        productVarianceId: selectedItem.type === 'PRODUCT' ? selectedItem.id : null,
        collectionId: selectedItem.type === 'COLLECTION' ? selectedItem.id : null
    };

    const url = editingSaleId
        ? `/api/admin/seasonal-sales/${editingSaleId}`
        : `/api/admin/seasonal-sales`;

    const method = 'POST';

    try {
        const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error('Action failed');

        if (window.showToast) showToast('Saved successfully', 'success');
        if (typeof showSection === 'function') showSection('seasonal-sales');
        loadSeasonalSales();

    } catch (err) {
        console.error(err);
        if (window.showToast) showToast('Error saving sale', 'error');
    }
};

// 9. Delete (Deactivate/Remove)
window.deleteSeasonalSale = function(id) {
    if(typeof window.showConfirm === 'function') {
        window.showConfirm(
            "Are you sure you want to remove this sale? It will be marked inactive.",
            () => performDelete(id)
        );
    } else if(confirm("Remove this sale?")) {
        performDelete(id);
    }
};

async function performDelete(id) {
    try {
        const response = await fetch(`/api/admin/seasonal-sales/${id}/delete`, { method: 'POST' });
        if (!response.ok) throw new Error('Failed to delete');

        if(window.showToast) showToast('Sale removed successfully', 'success');
        loadSeasonalSales();
    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error deleting sale', 'error');
    }
}

// 10. Filter Table (FIXED)
// This function is now robust against null elements
window.filterTable = function(tableId, columnIndex) {
    const input = document.getElementById('seasonal-search');

    // Guard Clause: If input doesn't exist, stop.
    if (!input) return;

    const filter = input.value.toLowerCase();

    // Find table: Try the passed ID first, FALLBACK to 'seasonal-sales-body' if not found
    const table = document.getElementById(tableId) || document.getElementById('seasonal-sales-body');

    // Guard Clause: If still no table, stop to prevent error
    if (!table) return;

    const rows = table.getElementsByTagName('tr');

    for (let i = 0; i < rows.length; i++) {
        // Skip header rows if present
        if (rows[i].getElementsByTagName('th').length > 0) continue;

        // Try getting specific column, otherwise fallback to row text
        const td = rows[i].getElementsByTagName('td')[columnIndex];
        if (td) {
            const text = td.textContent || td.innerText;
            rows[i].style.display = text.toLowerCase().includes(filter) ? '' : 'none';
        } else {
            // Safe fallback: search entire row
            rows[i].style.display = rows[i].innerText.toLowerCase().includes(filter) ? '' : 'none';
        }
    }
}