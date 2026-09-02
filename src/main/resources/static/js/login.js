import Notification from './notification.js';

const notify = Notification({
    position: 'bottom-right',
    duration: 3000,
    hidePrevious: true,
    maxVisible: 5,
});

window.addEventListener("DOMContentLoaded", () => {

    const loginBtn = document.getElementById("login-btn");
    const emailInput = document.getElementById("login-email");
    const passwordInput = document.getElementById("login-password");
    const rememberMe = document.getElementById("remember-me");

    // Auto-fill saved email
    const savedEmail = localStorage.getItem("savedEmail");
    if (savedEmail) emailInput.value = savedEmail;

    // Toggle password visibility (Design intact)
    document.querySelectorAll(".toggle-password-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            const input = btn.closest("div.relative").querySelector("input");
            const icon = btn.querySelector("i");

            if (input.type === "password") {
                input.type = "text";
                icon.classList.replace("fa-eye", "fa-eye-slash");
            } else {
                input.type = "password";
                icon.classList.replace("fa-eye-slash", "fa-eye");
            }
        });
    });

    // Login button click
    if (loginBtn) {
        loginBtn.addEventListener("click", async (e) => {
            e.preventDefault();

            const email = emailInput.value.trim();
            const password = passwordInput.value.trim();

            // 🔹 Input validation
            if (!email) return notify.error("Please enter your email address.");
            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                return notify.error("Please enter a valid email address.");
            }
            if (!password) return notify.error("Please enter your password.");

            // ✅ 1. ලෝඩරය පෙන්වීම (Request එක පටන් ගන්නා විට)
            if (window.loader) window.loader.show();

            // 🔹 Remember Me (email only)
            if (rememberMe?.checked) {
                localStorage.setItem("savedEmail", email);
            } else {
                localStorage.removeItem("savedEmail");
            }

            try {
                const response = await fetch("/api/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ email, password }),
                });

                const json = await response.json();

                if (json.status) {
                    notify.success(json.message || "Login successful!");
                    sessionStorage.setItem("user", JSON.stringify(json.user));

                    // Sync any guest cart from localStorage before redirecting
                    try {
                        const guestCart = localStorage.getItem('guest_cart');
                        if (guestCart) {
                            const items = JSON.parse(guestCart);
                            if (items && Array.isArray(items) && items.length > 0) {
                                const payload = items.map(item => ({
                                    varianceId: item.varianceId || null,
                                    collectionId: item.collectionId || null,
                                    quantity: item.quantity || 1
                                }));
                                await fetch('/api/cart/sync', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify(payload)
                                });
                                localStorage.removeItem('guest_cart');
                            }
                        }
                    } catch (syncErr) {
                        console.warn('Guest cart sync error:', syncErr);
                    }

                    const returnUrl = sessionStorage.getItem("returnUrl");

                    if (returnUrl) {
                        sessionStorage.removeItem("returnUrl");
                        window.location.href = returnUrl;
                    } else if (json.redirect) {
                        window.location.href = json.redirect;
                    } else {
                        window.location.href = "account.html";
                    }

                } else {
                    // ❌ Login අසාර්ථක නම් පමණක් ලෝඩරය අයින් කරන්න
                    if (window.loader) window.loader.hide();
                    notify.error(json.message || "Login failed.");
                }

            } catch (err) {
                // ❌ Network error ආවොත් ලෝඩරය අයින් කරන්න
                if (window.loader) window.loader.hide();
                console.error("Fetch error:", err);
                notify.error("Network error. Please check your connection.");
            }
        });
    }
});