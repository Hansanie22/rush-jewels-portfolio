// ==========================================
// NOTIFICATION SYSTEM LOGIC
// ==========================================

async function loadNotifications() {
    try {
        const response = await fetch('/api/admin/notifications');
        if (!response.ok) return; // Silent fail to not annoy user

        let notifications = await response.json();
        
        // Filter out cleared notifications
        const clearedNotifs = JSON.parse(localStorage.getItem('cleared_notifications') || '[]');
        notifications = notifications.filter(n => !clearedNotifs.includes(n.message));

        renderNotifications(notifications);

    } catch (err) {
        console.error("Notification Error:", err);
    }
}

function renderNotifications(data) {
    const list = document.getElementById('notif-list');
    const badge = document.getElementById('notif-badge');

    if (!list || !badge) return;

    // Update Badge Count
    if (data.length > 0) {
        badge.innerText = data.length > 9 ? '9+' : data.length;
        badge.classList.remove('hidden');
    } else {
        badge.classList.add('hidden');
    }

    // Render List
    if (data.length === 0) {
        list.innerHTML = '<div class="p-4 text-center text-gray-400 text-xs">No new notifications</div>';
        return;
    }

    list.innerHTML = data.map(n => `
        <div class="p-3 hover:bg-gray-50 border-b border-gray-100 cursor-pointer transition" onclick="navigateToSection('${n.link}')">
            <div class="flex items-start gap-3">
                <div class="mt-1"><i class="${n.iconClass}"></i></div>
                <div>
                    <p class="text-xs font-bold text-gray-800">${n.message}</p>
                    <p class="text-[10px] text-gray-400 mt-0.5">${n.timeAgo}</p>
                </div>
            </div>
        </div>
    `).join('');
}

function navigateToSection(sectionId) {
    // Use the global navigation function from app.js
    if (typeof showSection === 'function') {
        showSection(sectionId);
    }
    // Close dropdown
    document.getElementById('notif-dropdown').classList.add('hidden');
}

// Poll every 60 seconds
setInterval(loadNotifications, 60000);

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadNotifications();
});