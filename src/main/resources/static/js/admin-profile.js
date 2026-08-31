// ==========================================
// ADMIN PROFILE LOGIC
// ==========================================

async function loadAdminProfile() {
    try {
        const response = await fetch('/api/admin/staff/profile');
        if (!response.ok) throw new Error("Failed to load profile");

        const admin = await response.json();

        // Update Header Elements
        const nameEl = document.getElementById('header-admin-name');
        const roleEl = document.getElementById('header-admin-role');
        const imgEl = document.getElementById('header-admin-img');

        if (nameEl) nameEl.innerText = admin.name || "Admin";
        if (roleEl) roleEl.innerText = admin.role || "User";

        // Generate Avatar based on name
        if (imgEl) {
            const safeName = (admin.name || 'Admin').replace(/ /g, '+');
            imgEl.src = `https://ui-avatars.com/api/?name=${safeName}&background=000&color=d4af37&bold=true`;
        }

        // Return data for modal population
        return admin;

    } catch (err) {
        console.error("Profile Load Error:", err);
        return null;
    }
}

// --- Modal Logic ---

async function openProfileModal() {
    // Fetch latest data to populate modal
    const admin = await loadAdminProfile();
    if (!admin) return;

    document.getElementById('profile-name').value = admin.name;
    document.getElementById('profile-email').value = admin.email;
    document.getElementById('profile-new-password').value = '';
    document.getElementById('profile-current-password').value = '';

    // Set Modal Avatar
    const safeName = (admin.name || 'Admin').replace(/ /g, '+');
    document.getElementById('modal-profile-img').src = `https://ui-avatars.com/api/?name=${safeName}&background=000&color=d4af37&bold=true`;

    const modal = document.getElementById('profile-modal');
    modal.classList.remove('hidden');
    modal.classList.add('flex');
}

function closeProfileModal() {
    const modal = document.getElementById('profile-modal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
}

async function handleProfileUpdate(e) {
    e.preventDefault();

    const payload = {
        name: document.getElementById('profile-name').value,
        email: document.getElementById('profile-email').value,
        currentPassword: document.getElementById('profile-current-password').value,
        newPassword: document.getElementById('profile-new-password').value
    };

    try {
        const response = await fetch('/api/admin/staff/profile/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const txt = await response.text();
            // Handle wrong password specifically
            if(response.status === 401) throw new Error("Incorrect Current Password");
            throw new Error(txt || 'Update failed');
        }

        if(window.showToast) showToast('Profile updated successfully', 'success');
        closeProfileModal();
        loadAdminProfile(); // Refresh header

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast(err.message, 'error');
    }
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    loadAdminProfile();

    // Expose globally for HTML access
    window.openProfileModal = openProfileModal;
    window.closeProfileModal = closeProfileModal;
    window.handleProfileUpdate = handleProfileUpdate;
});