// ==========================================================================
// MAIN APPLICATION CONTROLLER (app.js)
// Handles Navigation, Sidebar, and Global UI Events
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Default View (Dashboard)
    const dashboardLink = document.querySelector("a[onclick*=\"'dashboard'\"]");
    if (dashboardLink) {
        showSection('dashboard', dashboardLink);
    } else {
        showSection('dashboard', null);
    }

    // 2. Setup Global Listeners (Mobile Menu, Notifications)
    setupGlobalListeners();

    // 3. Initialize Data
    initializeApp();
});

// Initialize All Modules
function initializeApp() {
    if (typeof window.loadOrderData === 'function') window.loadOrderData();
    if (typeof window.loadDashboardData === 'function') window.loadDashboardData();
    if (typeof window.loadNotifications === 'function') window.loadNotifications();
    // Check for unread support messages to show sidebar indicator
    if (typeof window.checkUnreadSupportMessages === 'function') window.checkUnreadSupportMessages();
}

// ==========================================
// NAVIGATION LOGIC
// ==========================================

window.showSection = function(sectionId, element) {
    // 1. Hide All Sections
    const allSections = document.querySelectorAll('.section');
    allSections.forEach(sec => {
        sec.classList.add('hidden');
        sec.classList.remove('fade-in');
    });

    // 2. Show Target Section
    // Handle 'analytics' specifically if its ID is 'analytics-container'
    let targetId = `${sectionId}-section`;
    if (sectionId === 'analytics') targetId = 'analytics-container';

    const targetSection = document.getElementById(targetId);

    if (targetSection) {
        targetSection.classList.remove('hidden');
        setTimeout(() => targetSection.classList.add('fade-in'), 10);
    } else {
        console.warn(`Section ID '${targetId}' not found in HTML.`);
    }

    // 3. Update Header Title
    const pageTitle = document.getElementById('page-title');
    if (pageTitle) {
        const formattedTitle = sectionId.replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
        pageTitle.innerText = formattedTitle;
    }

    // 4. Update Sidebar Active State
    if (element) {
        document.querySelectorAll('.sidebar-link').forEach(link => {
            link.classList.remove('active');
            link.classList.remove('text-gold-400', 'bg-gray-900');
        });
        element.classList.add('active');
        element.classList.add('text-gold-400', 'bg-gray-900');
    }

    // 5. Trigger Data Refresh for the Module
    refreshModuleData(sectionId);
};

window.showProductSection = function(sectionId, element) {
    showSection(sectionId, element);
};

// NOTE: window.loadAnalytics is REMOVED from here.
// It is now fully handled in analytics-management.js to prevent scope conflicts.

window.toggleSubmenu = function(submenuId, element) {
    const submenu = document.getElementById(submenuId);
    const arrow = element.querySelector('.fa-chevron-down');

    if (submenu) {
        submenu.classList.toggle('open');
        if (submenu.classList.contains('open')) {
            submenu.style.maxHeight = "500px";
        } else {
            submenu.style.maxHeight = "0";
        }
    }

    if (arrow) {
        arrow.style.transform = submenu.classList.contains('open') ? 'rotate(180deg)' : 'rotate(0deg)';
        arrow.style.transition = 'transform 0.2s';
    }
};

// ==========================================
// MODULE REFRESHER (BRIDGE)
// ==========================================

function refreshModuleData(sectionId) {
    // Always refresh notifications
    if (typeof window.loadNotifications === 'function') window.loadNotifications();

    switch (sectionId) {
        case 'dashboard':
            if (typeof window.loadDashboardData === 'function') window.loadDashboardData();
            break;

        case 'orders':
            if (typeof window.loadOrderData === 'function') window.loadOrderData();
            break;

        case 'analytics':
            // Logic handled by analytics-management.js
            // If we navigated here via sidebar without clicking a specific tab, load default
            if (typeof window.fetchAnalyticsData === 'function') {
                // If the container is visible but no data is loaded, this ensures data loads
                window.fetchAnalyticsData();
            }
            break;

        case 'products':
        case 'add-product':
            if (typeof window.loadProducts === 'function') window.loadProducts();
            break;

        case 'product-variants':
        case 'add-variant':
            if (typeof window.loadAllVariants === 'function') window.loadAllVariants();
            break;

        case 'inventory':
        case 'stock-adjustment':
            if (typeof window.loadInventory === 'function') window.loadInventory();
            break;

        case 'warehouse':
            if (typeof window.loadWarehouseData === 'function') window.loadWarehouseData();
            break;

        case 'customers':
            if (typeof window.loadCustomers === 'function') window.loadCustomers();
            break;

        case 'marketing':
            if (typeof window.loadMarketingDashboard === 'function') window.loadMarketingDashboard();
            break;

        case 'coupons':
        case 'add-coupon':
            if (typeof window.loadCoupons === 'function') window.loadCoupons();
            break;

        // --- NEW SEASONAL SALES SECTION ---
        case 'seasonal-sales':
        case 'add-seasonal-sale':
            // Checks if the logic function exists (in seasonal_logic.js) and calls it
            if (typeof window.loadSeasonalSales === 'function') window.loadSeasonalSales();
            break;
        // ----------------------------------

        case 'reviews':
        case 'add-review':
            if (typeof window.loadReviews === 'function') window.loadReviews();
            break;

        case 'logistics':
        case 'add-shipment':
            if (typeof window.loadShipments === 'function') window.loadShipments();
            break;

        case 'staff':
            if (typeof window.loadStaff === 'function') window.loadStaff();
            break;

        case 'integrations':
            if (typeof window.loadIntegrations === 'function') window.loadIntegrations();
            break;

        case 'courier-settings':
            if (typeof window.loadCourierSettings === 'function') window.loadCourierSettings();
            break;

        case 'finance-settings':
            if (typeof window.loadFinanceData === 'function') window.loadFinanceData();
            break;

        case 'cms':
            if (typeof window.loadCmsData === 'function') window.loadCmsData();
            break;

        case 'blog':
            if (typeof window.loadBlogPosts === 'function') window.loadBlogPosts();
            break;

        case 'support':
            if (typeof window.loadSupportTickets === 'function') window.loadSupportTickets();
            break;

        case 'categories':
        case 'add-category':
            if (typeof window.loadCategories === 'function') window.loadCategories();
            break;

        case 'product-attributes':
            if(typeof window.loadSizesByCategory === 'function') window.loadSizesByCategory();
            if(typeof window.loadMetalsAttribute === 'function') window.loadMetalsAttribute();
            if(typeof window.loadGemstonesAttribute === 'function') window.loadGemstonesAttribute();
            break;

        case 'collections':
        case 'add-collection':
            if(typeof window.loadCollections === 'function') window.loadCollections();
            break;

        case 'collection-sets':
        case 'add-collection-set':
            if(typeof window.loadCollectionSets === 'function') window.loadCollectionSets();
            break;
    }
}

// ==========================================
// GLOBAL UI LISTENERS
// ==========================================

function setupGlobalListeners() {

    // 1. Notifications Toggle
    window.toggleNotifications = function() {
        const dropdown = document.getElementById('notif-dropdown');
        if (dropdown) dropdown.classList.toggle('hidden');
    };

    window.clearNotifications = function() {
        const list = document.getElementById('notif-list');
        const badge = document.getElementById('notif-badge');
        if (list) list.innerHTML = '<div class="p-3 text-xs text-gray-400 text-center">No new notifications</div>';
        if (badge) badge.classList.add('hidden');
    };

    // 2. Logout Logic (UPDATED)
    window.handleAdminLogout = function() {
        // Use the custom confirmation modal
        if (typeof showConfirm === 'function') {
            showConfirm('Are you sure you want to logout?', async () => {
                try {
                    const response = await fetch('/api/admin/logout', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' }
                    });

                    if (response.ok) {
                        if (window.showToast) showToast('Logged out successfully', 'success');
                        // Small delay to let the toast show before redirecting
                        setTimeout(() => {
                            window.location.href = '/admin-login.html';
                        }, 1000);
                    } else {
                        if (window.showToast) showToast('Logout failed', 'error');
                    }
                } catch (err) {
                    console.error(err);
                    if (window.showToast) showToast('Error logging out', 'error');
                }
            });
        } else if (confirm('Are you sure you want to logout?')) {
            // Fallback if custom modal not loaded
            window.location.href = '/admin-login.html';
        }
    };

    // 3. Close dropdowns when clicking outside
    document.addEventListener('click', (e) => {
        const notifBtn = document.querySelector('button[onclick="toggleNotifications()"]');
        const dropdown = document.getElementById('notif-dropdown');

        if (notifBtn && dropdown && !notifBtn.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.add('hidden');
        }
    });

    // 4. Mobile Sidebar Logic
    const sidebar = document.getElementById('sidebar');
    const menuBtn = document.querySelector('.fa-bars');

    if (menuBtn && sidebar) {
        const btn = menuBtn.parentElement;
        btn.onclick = (e) => {
            e.stopPropagation();
            sidebar.classList.toggle('hidden');
            sidebar.classList.toggle('absolute');
            sidebar.classList.toggle('h-full');
            sidebar.classList.toggle('z-50');
        };

        document.addEventListener('click', (e) => {
            if (window.innerWidth < 768 &&
                !sidebar.classList.contains('hidden') &&
                !sidebar.contains(e.target) &&
                !btn.contains(e.target)) {
                sidebar.classList.add('hidden');
            }
        });
    }
}