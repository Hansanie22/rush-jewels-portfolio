import Notification from './notification.js';

const notify = Notification({
    position: 'bottom-right',
    duration: 3000,
    hidePrevious: true,
    maxVisible: 5,
});

async function logoutUser() {
    try {
        // Clear client storage
        localStorage.clear();
        sessionStorage.clear();

        // Use POST /api/auth/logout
        const response = await fetch("/api/auth/logout", {
            method: "POST",
            headers: { "Accept": "application/json" },
            credentials: "include"
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

        const data = await response.json();

        if (data.status) {
            notify.success("Logged out successfully!");
            setTimeout(() => window.location.href = "auth.html", 500);
        } else {
            notify.error("Logout failed. Reloading page...");
            setTimeout(() => window.location.reload(), 1500);
        }
    } catch (err) {
        console.error("Error during logout:", err);
        notify.error("Network error. Try again.");
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const logoutBtn = document.getElementById("logout-tab-btn");
    if (logoutBtn) logoutBtn.addEventListener("click", logoutUser);
});
