import Notification from './notification.js';

const notify = Notification({
    position: 'bottom-right',
    duration: 3000,
    hidePrevious: true,
    maxVisible: 5,
});

document.addEventListener("DOMContentLoaded", () => {

    // --- NEW: Toggle Password Visibility Function ---
    const setupPasswordToggle = (toggleBtnId, inputId) => {
        const toggleBtn = document.getElementById(toggleBtnId);
        const input = document.getElementById(inputId);

        if (toggleBtn && input) {
            toggleBtn.addEventListener('click', () => {
                const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                input.setAttribute('type', type);

                // Toggle Icon
                const icon = toggleBtn.querySelector('i');
                if (type === 'text') {
                    icon.classList.remove('fa-eye');
                    icon.classList.add('fa-eye-slash');
                } else {
                    icon.classList.remove('fa-eye-slash');
                    icon.classList.add('fa-eye');
                }
            });
        }
    };

    // Initialize toggles for New and Confirm password fields
    setupPasswordToggle('toggle-new-password', 'new-password');
    setupPasswordToggle('toggle-confirm-password', 'confirm-password');
    // ------------------------------------------------


    const button = document.getElementById("update-password-btn");
    if (!button) return;

    button.addEventListener("click", async () => {
        // CHANGED: Renamed variable for clarity
        const credential = document.getElementById("current-password").value.trim();
        const newPassword = document.getElementById("new-password").value.trim();
        const confirmPassword = document.getElementById("confirm-password").value.trim();

        // CHANGED: Updated check to use 'credential'
        if (!credential || !newPassword || !confirmPassword) {
            return notify.validation("All fields are required.");
        }

        if (newPassword !== confirmPassword) {
            return notify.error("New passwords do not match.");
        }

        // Password strength check (this is good, no change)
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{8,}$/;
        if (!passwordRegex.test(newPassword)) {
            return notify.warning(
                "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."
            );
        }

        button.disabled = true;
        button.textContent = "Updating...";

        try {
            const res = await fetch("/api/change-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                // CHANGED: Sending a 'credential' key instead of 'currentPassword'
                body: JSON.stringify({
                    credential: credential,
                    newPassword: newPassword,
                    confirmPassword: confirmPassword
                })
            });

            const data = await res.json();

            if (data.status) {
                notify.success(data.message || "Password updated successfully!");
                document.getElementById("password-form").reset();
            } else {
                notify.error(data.message || "Failed to update password.");
            }
        } catch (err) {
            console.error(err);
            notify.error("Network error. Please try again.");
        } finally {
            button.disabled = false;
            button.textContent = "Update Password";
        }
    });
});