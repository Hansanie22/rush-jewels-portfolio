// ==========================================
// ADMIN LOGIN LOGIC
// ==========================================

document.addEventListener('DOMContentLoaded', () => {

    // 1. Load "Remember Me"
    const savedEmail = localStorage.getItem('velora_admin_email');
    if (savedEmail) {
        const emailInput = document.getElementById('email');
        const rememberBox = document.getElementById('remember-me');
        if (emailInput) emailInput.value = savedEmail;
        if (rememberBox) rememberBox.checked = true;
    }

    // 2. Password Toggle Listener
    const toggleBtn = document.getElementById('toggle-password-btn');
    if (toggleBtn) toggleBtn.addEventListener('click', togglePassword);

    // 3. Form Submit Listener
    const form = document.getElementById('admin-login-form');
    if (form) {
        form.addEventListener('submit', handleAdminLogin);
    }

    // 4. Session & RBAC Check (Only on admin dashboard pages)
    if (!form) { // If it's not the login page
        checkAdminSession();
    }
});

let currentAdminRole = null;

async function checkAdminSession() {
    try {
        const response = await fetch('/api/admin/validate-session');
        const data = await response.json();
        
        if (response.ok && data.success) {
            currentAdminRole = data.role;
            
            // Update Top Bar UI
            const nameEl = document.getElementById('header-admin-name');
            const roleEl = document.getElementById('header-admin-role');
            const imgEl = document.getElementById('header-admin-img');
            
            if (nameEl) nameEl.textContent = data.name;
            if (roleEl) roleEl.textContent = data.role;
            if (imgEl && data.imagePath) imgEl.src = data.imagePath;
            
            applyRBAC(data.role);
        } else {
            // Redirect to login if not authenticated
            window.location.href = '/admin-login.html';
        }
    } catch (error) {
        console.error('Session validation error:', error);
        window.location.href = '/admin-login.html';
    }
}

function applyRBAC(role) {
    if (role === 'CASHIER') {
        // Hide menus for Cashier
        const hideSections = [
            'marketing', 'coupons', 'seasonal-sales', 
            'sales', 'product', 'finance', 
            'logistics', 'warehouse', 
            'cms', 'blog', 
            'staff', 'courier-settings', 'integrations', 'finance-settings', 'logs'
        ];
        
        const sidebarLinks = document.querySelectorAll('.sidebar-link');
        sidebarLinks.forEach(link => {
            hideSections.forEach(sec => {
                if (link.getAttribute('onclick') && link.getAttribute('onclick').includes(`showSection('${sec}'`) || 
                    link.getAttribute('onclick') && link.getAttribute('onclick').includes(`loadAnalytics('${sec}'`)) {
                    link.style.display = 'none';
                }
            });
        });

        // Hide sidebar headers that don't have visible links
        document.querySelectorAll('.sidebar-header').forEach(header => {
            if (header.innerText.includes('Marketing') || 
                header.innerText.includes('Analytics') || 
                header.innerText.includes('Logistics') || 
                header.innerText.includes('CMS') || 
                header.innerText.includes('System Settings')) {
                header.style.display = 'none';
            }
        });
    }
}

function togglePassword() {
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('eye-icon');

    const isHidden = passwordInput.type === 'password';
    passwordInput.type = isHidden ? 'text' : 'password';

    eyeIcon.classList.toggle('fa-eye', !isHidden);
    eyeIcon.classList.toggle('fa-eye-slash', isHidden);
}

// ----------------------------
// Handle Admin Login
// ----------------------------
async function handleAdminLogin(event) {
    event.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('remember-me').checked;
    const submitBtn = document.getElementById('submit-btn');
    const errorAlert = document.getElementById('error-alert');

    // 1. Validation
    if (!email) {
        showToast("Please enter your email address", "error");
        return;
    }
    if (!password) {
        showToast("Please enter your password", "error");
        return;
    }

    // UI Loading
    if (errorAlert) errorAlert.classList.add('hidden');
    const originalBtnText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Authenticating...';

    try {
        const response = await fetch('/api/admin/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            // ✅ Handle Remember Me
            if (rememberMe) {
                localStorage.setItem('velora_admin_email', email);
            } else {
                localStorage.removeItem('velora_admin_email');
            }

            showToast("Login successful! Redirecting...", "success");
            submitBtn.innerHTML = '<i class="fas fa-check mr-2"></i>Success!';
            submitBtn.classList.remove('bg-gold');
            submitBtn.classList.add('bg-green-600');

            setTimeout(() => {
                window.location.href = '/admin.html';
            }, 1000);
        } else {
            // Show Error Toast AND Alert
            const msg = data.message || 'Invalid credentials';
            showToast(msg, 'error');
            if(errorAlert) {
                errorAlert.classList.remove('hidden');
                errorAlert.querySelector('p').textContent = msg;
            }
            resetButton(submitBtn, originalBtnText);
        }
    } catch (error) {
        console.error('Login error:', error);
        showToast('Server connection failed', 'error');
        resetButton(submitBtn, originalBtnText);
    }
}

function resetButton(btn, text) {
    btn.disabled = false;
    btn.innerHTML = text;
    // Optional: Reset color if it was changed
    btn.classList.add('bg-gold');
    btn.classList.remove('bg-green-600');
}

// ==========================================================================
// TOAST NOTIFICATION SYSTEM (Local Definition for Login Page)
// ==========================================================================

// Ensure container exists or create it
let toastContainer = document.getElementById('toast-container');
if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toast-container';
    // Inline styles in case CSS file failed
    toastContainer.style.cssText = "position: fixed; bottom: 20px; right: 20px; z-index: 9999; display: flex; flex-direction: column; gap: 10px;";
    document.body.appendChild(toastContainer);
}

function showToast(message, type = 'success', id = null, duration = 3000) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');

    // Base Classes
    let classes = "toast px-4 py-3 rounded shadow-lg border-l-4 flex items-center gap-3 bg-white mb-2 z-50 transform transition-all duration-300 translate-x-full opacity-0";

    // Colors
    let iconHtml = '';
    if (type === 'success') {
        classes += " border-green-500 text-green-800";
        iconHtml = `<i class="fas fa-check-circle text-green-500 text-lg"></i>`;
    } else if (type === 'error') {
        classes += " border-red-500 text-red-800";
        iconHtml = `<i class="fas fa-exclamation-circle text-red-500 text-lg"></i>`;
    } else {
        classes += " border-blue-500 text-blue-800";
        iconHtml = `<i class="fas fa-info-circle text-blue-500 text-lg"></i>`;
    }

    toast.className = classes;

    toast.innerHTML = `
        <div>${iconHtml}</div>
        <div class="flex-1 text-sm font-medium">
            <p>${message}</p>
        </div>
    `;

    container.appendChild(toast);

    // Trigger Animation
    requestAnimationFrame(() => {
        toast.classList.remove('translate-x-full', 'opacity-0');
    });

    // Auto Dismiss
    setTimeout(() => {
        toast.classList.add('translate-x-full', 'opacity-0');
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

// Expose for potential external use
window.showToast = showToast;

// ==========================================
// SMART EMAIL CAMPAIGNS
// ==========================================
async function launchSmartCampaign(type) {
    let campaignName = type === 'new-arrivals' ? 'New Arrivals' : 'Hot Deals';
    
    // Add SweetAlert if available, fallback to regular confirm
    let confirmed = false;
    if (typeof Swal !== 'undefined') {
        const result = await Swal.fire({
            title: `Launch ${campaignName} Campaign?`,
            text: "This will automatically generate and send an email to all active subscribers.",
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#d4af37',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, Send it!'
        });
        confirmed = result.isConfirmed;
    } else {
        confirmed = confirm(`Are you sure you want to launch the ${campaignName} Campaign to all subscribers?`);
    }

    if (!confirmed) return;

    try {
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: 'Sending Campaign...',
                text: 'Please wait while we build and dispatch the emails.',
                allowOutsideClick: false,
                didOpen: () => Swal.showLoading()
            });
        }

        const response = await fetch(`/api/admin/marketing/smart-campaign/${type}`, {
            method: 'POST'
        });
        const data = await response.json();

        if (typeof Swal !== 'undefined') {
            if (response.ok && data.success) {
                Swal.fire('Success!', data.message, 'success');
            } else {
                Swal.fire('Error', data.message || 'Failed to send campaign', 'error');
            }
        } else {
            showToast(data.message, response.ok ? 'success' : 'error');
        }

    } catch (error) {
        console.error('Campaign Error:', error);
        if (typeof Swal !== 'undefined') {
            Swal.fire('Error', 'A network error occurred while launching the campaign.', 'error');
        } else {
            showToast('Network error occurred.', 'error');
        }
    }
}