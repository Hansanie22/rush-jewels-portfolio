// ==========================================
// STAFF (ADMIN) MANAGEMENT LOGIC
// ==========================================

async function loadStaff() {
    try {
        const response = await fetch('/api/admin/staff');
        const staff = await response.json();

        const tbody = document.getElementById('staff-body');
        if(staff.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="px-6 py-8 text-center text-gray-500">No admin users found.</td></tr>';
            return;
        }

        tbody.innerHTML = staff.map(s => `
            <tr class="hover:bg-gray-50 border-b border-gray-100">
                <td class="px-6 py-4">
                <div class="font-bold text-gray-900">${s.name}</div>
                <div class="text-xs text-gray-500">${s.role || 'ADMIN'}</div>
            </td>
            <td class="px-6 py-4">${s.email}</td>
                <td class="px-6 py-4 text-xs text-gray-500">
                    ${s.lastLogin ? new Date(s.lastLogin).toLocaleString() : 'Never'}
                </td>
                <td class="px-6 py-4 text-xs text-gray-500">
                    ${new Date(s.createdAt).toLocaleDateString()}
                </td>
                <td class="px-6 py-4">
                    <label class="inline-flex relative items-center cursor-pointer">
                        <input type="checkbox" class="sr-only peer" 
                               ${s.statusId === 1 ? 'checked' : ''} 
                               onchange="toggleAdminStatus(${s.id}, this.checked)">
                        <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-gold-300 rounded-full peer peer-checked:bg-gold-400 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border after:rounded-full after:h-4 after:w-4 after:transition-all"></div>
                    </label>
                </td>
            </tr>
        `).join('');

    } catch(e) { console.error(e); }
}

function openAddAdminModal() {
    document.getElementById('admin-name').value = '';
    document.getElementById('admin-email').value = '';
    document.getElementById('admin-password').value = '';

    document.getElementById('add-admin-modal').classList.remove('hidden');
    document.getElementById('add-admin-modal').classList.add('flex');
}

function closeAddAdminModal() {
    document.getElementById('add-admin-modal').classList.add('hidden');
    document.getElementById('add-admin-modal').classList.remove('flex');
}

async function handleCreateAdmin(e) {
    e.preventDefault();

    const name = document.getElementById('admin-name').value.trim();
    const email = document.getElementById('admin-email').value.trim();
    const password = document.getElementById('admin-password').value;

    // Validation
    if (!name) {
        if (window.showToast) showToast("Name is required", "error");
        return;
    }

    if (!email) {
        if (window.showToast) showToast("Email is required", "error");
        return;
    }

    // Simple email regex check
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        if (window.showToast) showToast("Invalid email format", "error");
        return;
    }

    if (!password) {
        if (window.showToast) showToast("Password is required", "error");
        return;
    }

    if (password.length < 6) {
        if (window.showToast) showToast("Password must be at least 6 characters", "error");
        return;
    }

    const role = document.getElementById('admin-role').value;
        
    try {
        const response = await fetch('/api/admin/staff', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role })
        });

        if (!response.ok) {
            const txt = await response.text();
            throw new Error(txt || 'Creation failed');
        }

        if (window.showToast) showToast('Admin created successfully', 'success');
        closeAddAdminModal();
        loadStaff();
    } catch (err) {
        console.error(err);
        if (window.showToast) showToast(err.message, 'error');
    }
}

async function toggleAdminStatus(id, isActive) {
    try {
        await fetch(`/api/admin/staff/${id}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ active: isActive })
        });
        if(window.showToast) showToast('Status updated', 'success');
    } catch(e) {
        console.error(e);
        loadStaff(); // Revert
    }
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadStaff();
});