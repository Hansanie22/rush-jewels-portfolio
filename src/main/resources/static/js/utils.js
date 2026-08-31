// ==========================================================================
// 1. TOAST NOTIFICATION SYSTEM
// ==========================================================================

window.toastStore = window.toastStore || new Map();
window.showToast = function(message, type = 'success', id = null, duration = 3000) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    // If ID exists, update existing toast instead of creating new one
    if (id && window.toastStore.has(id)) {
        const existingToast = window.toastStore.get(id);
        existingToast.querySelector('p').textContent = message;
        return;
    }

    const toast = document.createElement('div');
    toast.className =
        `toast px-4 py-2 rounded shadow-md border-l-4 flex items-center gap-2 
        opacity-0 transform translate-x-4 transition-all duration-300 bg-white mb-2 z-50 pointer-events-auto`;

    // Color coding based on type
    if (type === 'success') {
        toast.classList.add('border-gold-500');
    } else if (type === 'error') {
        toast.classList.add('border-red-500');
    } else {
        toast.classList.add('border-blue-500');
    }

    // Icon logic
    let typeLabel = type;
    let typeClass = type === 'success' ? 'text-gold-600' : (type === 'error' ? 'text-red-600' : 'text-blue-600');

    toast.innerHTML = `
        <div class="flex-1 text-xs text-gray-700">
            <strong class="uppercase font-bold ${typeClass}">${typeLabel}</strong>
            <p class="text-black leading-tight mt-1">${message}</p>
        </div>
        <button class="text-gray-400 hover:text-gray-600 text-sm font-bold p-1">&times;</button>
    `;

    // Close button logic
    toast.querySelector('button').addEventListener('click', () => hideToast(toast, id));
    container.appendChild(toast);

    // Animation In
    requestAnimationFrame(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateX(0)';
    });

    if (id) window.toastStore.set(id, toast);

    // Auto Dismiss
    setTimeout(() => hideToast(toast, id), duration);
};

window.hideToast = function(toast, id = null) {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    setTimeout(() => {
        if (toast && toast.parentNode) toast.remove();
        if (id) window.toastStore.delete(id);
    }, 300);
};

// ==========================================================================
// 2. CONFIRMATION MODAL SYSTEM
// ==========================================================================

window.confirmStore = window.confirmStore || new Map();

window.showConfirm = function (message, onConfirm, onCancel = null, id = null) {
    let container = document.getElementById("confirm-container");
    if (!container) return;

    // If modal with same ID exists, update message + callbacks
    if (id && window.confirmStore.has(id)) {
        const existingModal = window.confirmStore.get(id);
        existingModal.querySelector(".confirm-message").textContent = message;
        existingModal.onConfirmCallback = onConfirm;
        existingModal.onCancelCallback = onCancel;
        return;
    }

    // Create modal wrapper
    const modal = document.createElement("div");
    modal.className = `
        fixed inset-0 bg-black bg-opacity-40 flex justify-center items-center
        opacity-0 transition-opacity duration-300 z-50
    `;

    modal.innerHTML = `
        <div class="bg-white p-5 rounded-lg shadow-xl w-72 transform scale-95 transition-all duration-300">
            <p class="confirm-message text-gray-800 text-sm">${message}</p>

            <div class="flex justify-end gap-3 mt-4">
                <button class="cancel-btn px-3 py-1 text-gray-600 hover:text-gray-800">Cancel</button>
                <button class="confirm-btn px-3 py-1 bg-gold-500 text-white rounded hover:bg-gold-600">Confirm</button>
            </div>
        </div>
    `;

    // Store callbacks in modal element
    modal.onConfirmCallback = onConfirm;
    modal.onCancelCallback = onCancel;

    // Add to DOM
    container.appendChild(modal);

    // Animate in
    requestAnimationFrame(() => {
        modal.style.opacity = "1";
        modal.querySelector("div").style.transform = "scale(1)";
    });

    // Btn logic
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

// Close animation
window.hideConfirm = function (modal, id = null) {
    modal.style.opacity = "0";
    modal.querySelector("div").style.transform = "scale(0.95)";

    setTimeout(() => {
        if (modal && modal.parentNode) modal.remove();
        if (id) window.confirmStore.delete(id);
    }, 250);
};


// ---------------- Navigation & UI Logic ----------------

// 1. Toggle Submenus (e.g., Products Dropdown)
function toggleSubmenu(id, el) {
    const submenu = document.getElementById(id);
    if (!submenu) return;

    submenu.classList.toggle('open');
    const icon = el.querySelector('.fa-chevron-down');
    if (icon) {
        icon.style.transform = submenu.classList.contains('open')
            ? 'rotate(180deg)'
            : 'rotate(0deg)';
    }
}

// 2. Main Section Switcher
function showSection(id, el) {
    // A. Hide ALL sections first
    document.querySelectorAll('.section').forEach(s => s.classList.add('hidden'));

    // B. Show the specific target section
    const target = document.getElementById(id + '-section');
    if (target) {
        target.classList.remove('hidden');
        target.classList.add('fade-in');
    }

    // C. Update Header Title (CRITICAL FIX)
    const pageTitle = document.getElementById('page-title');
    if (pageTitle) {
        // Replaces hyphens with spaces (e.g. "add-product" -> "ADD PRODUCT")
        let titleText = id.replace(/-/g, ' ').toUpperCase();
        pageTitle.innerText = titleText;
    }

    // D. Update Sidebar Styling (Active State)
    if (el) {
        // Reset all sidebar links to default gray
        document.querySelectorAll('.sidebar-link').forEach(l => {
            l.classList.remove('active', 'text-gold-400', 'border-l-gold-400');
            l.classList.add('text-gray-400', 'border-transparent');
        });

        // Set clicked link to active gold
        el.classList.add('active', 'text-gold-400', 'border-l-gold-400');
        el.classList.remove('text-gray-400', 'border-transparent');
    }

    // E. Mobile Sidebar: Close sidebar after selection on mobile
    if (window.innerWidth < 768) {
        const sidebar = document.getElementById('sidebar');
        if (sidebar) sidebar.classList.add('hidden');
    }
}

// 3. Wrapper for Product Sub-sections
function showProductSection(id, el) {
    showSection(id, el);
}

// ---------------- Generic Table Filter ----------------
function filterTable(tableId, colIndex) {
    const input = event.target.value.toUpperCase();
    const table = document.getElementById(tableId);
    if (!table) return;

    Array.from(table.getElementsByTagName("tr"))
        .slice(1)
        .forEach(tr => {
            const td = tr.getElementsByTagName("td")[colIndex];
            tr.style.display = td && td.textContent.toUpperCase().includes(input) ? "" : "none";
        });
}

// ---------------- Notifications ----------------
function toggleNotifications() {
    const dropdown = document.getElementById('notif-dropdown');
    if (dropdown) dropdown.classList.toggle('hidden');

    const badge = document.getElementById('notif-badge');
    if (badge) badge.classList.add('hidden');
}

function clearNotifications() {
    const notifList = document.getElementById('notif-list');
    if (notifList) {
        // Save currently visible notification messages as cleared
        const msgElements = notifList.querySelectorAll('p.text-xs.font-bold.text-gray-800, p.text-xs.font-bold');
        let clearedNotifs = JSON.parse(localStorage.getItem('cleared_notifications') || '[]');
        msgElements.forEach(el => {
            if (!clearedNotifs.includes(el.innerText)) {
                clearedNotifs.push(el.innerText);
            }
        });
        localStorage.setItem('cleared_notifications', JSON.stringify(clearedNotifs));

        notifList.innerHTML =
            '<div class="p-3 text-[10px] text-gray-400 text-center">No new notifications</div>';
    }
    
    const badge = document.getElementById('notif-badge');
    if (badge) badge.classList.add('hidden');
}

// ---------------- Charts ----------------
function updateCharts(id = 'dash-chart') {
    const container = document.getElementById(id);
    if (!container) return;

    container.innerHTML = '';
    for (let i = 0; i < 30; i++) {
        const height = Math.floor(Math.random() * 80) + 10;
        const bar = document.createElement('div');
        bar.className = 'w-full bg-gold-400 hover:bg-black transition-all duration-300';
        bar.style.height = `${height}%`;
        container.appendChild(bar);
    }
}

function loadAnalytics(type, el) {
    showSection('analytics-container', el);
    const title = document.getElementById('analytics-title');
    if (title) {
        title.innerText = `${type.charAt(0).toUpperCase() + type.slice(1)} Reports`;
    }
    updateCharts('analytics-chart');
}

// ---------------- Helpers ----------------
function formatCurrency(amount) {
    if (typeof amount === 'string' && amount.includes('Rs.')) return amount;
    return `Rs. ${Number(amount).toLocaleString('en-LK')}`;
}

function parseCurrency(str) {
    return Number(str.replace(/[^0-9.-]+/g, ''));
}

function formatDate(date) {
    if (typeof date === 'string') return date;
    return new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function debounce(func, wait) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func(...args), wait);
    };
}

function generateId(prefix = 'ID') {
    return `${prefix}${Math.random().toString(36).substr(2, 9)}`;
}

function confirmAction(message, callback) {
    if (confirm(message)) callback();
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text)
        .then(() => showToast('Copied!', 'success'))
        .catch(() => showToast('Failed to copy', 'error'));
}

function handleAdminLogout() {
    confirmAction('Are you sure you want to logout?', async () => {
        showToast('Logging out...', 'info');
        try {
            await fetch('/api/admin/logout', { method: 'POST' });
        } catch (e) {}
        setTimeout(() => window.location.href = '/admin-login.html', 1000);
    });
}

function toggleMobileSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) sidebar.classList.toggle('hidden');
}

function initTooltips() {
    const elements = document.querySelectorAll('[data-tooltip]');
    elements.forEach(el => {
        el.addEventListener('mouseenter', function () {
            showToast(this.getAttribute('data-tooltip'), 'info');
        });
    });
}

// ---------------- Local Storage ----------------
function saveToLocalStorage(key, value) {
    try { localStorage.setItem(key, JSON.stringify(value)); return true; }
    catch { return false; }
}

function getFromLocalStorage(key, defaultValue = null) {
    try {
        const item = localStorage.getItem(key);
        return item ? JSON.parse(item) : defaultValue;
    } catch {
        return defaultValue;
    }
}

function removeFromLocalStorage(key) {
    try { localStorage.removeItem(key); return true; }
    catch { return false; }
}