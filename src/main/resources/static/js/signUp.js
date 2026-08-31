import Notification from './notification.js';

const notify = Notification({
    position: 'bottom-right',
    duration: 3000,
    hidePrevious: true,
    maxVisible: 5,
});

document.addEventListener("DOMContentLoaded", () => {
    const registerBtn = document.getElementById("register-btn");
    const passwordInput = document.getElementById("register-password");
    const confirmPasswordInput = document.getElementById("confirm-password");

    const specialCharsRegex = /[@$!%*?&#]/;
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    const validatePasswordStrength = (password) => {
        return (
            password.length >= 8 &&
            /[A-Z]/.test(password) &&
            /[a-z]/.test(password) &&
            /\d/.test(password) &&
            specialCharsRegex.test(password)
        );
    };

    const setPasswordErrorStyle = (isError) => {
        const action = isError ? 'add' : 'remove';
        if(passwordInput) passwordInput.classList[action]("border-red-500", "bg-red-50");
        if(confirmPasswordInput) confirmPasswordInput.classList[action]("border-red-500", "bg-red-50");
    };

    if (registerBtn) {
        registerBtn.addEventListener("click", async (e) => {
            e.preventDefault();

            const fname = document.getElementById("first-name").value.trim();
            const lname = document.getElementById("last-name").value.trim();
            const email = document.getElementById("register-email").value.trim();
            const password = passwordInput.value.trim();
            const confirmPassword = confirmPasswordInput.value.trim();
            const agreeTerms = document.getElementById("agree-terms").checked;

            setPasswordErrorStyle(false);

            // --- Client-side Validations ---
            if (!fname || !lname || !email) return notify.error("Please fill all required fields.");
            if (!emailRegex.test(email)) return notify.error("Invalid email format.");
            if (!password || !confirmPassword) return notify.error("Please enter and confirm your password.");

            if (password !== confirmPassword) {
                setPasswordErrorStyle(true);
                return notify.error("Passwords do not match.");
            }

            if (!validatePasswordStrength(password)) {
                setPasswordErrorStyle(true);
                return notify.error("Password is too weak. Please check the requirements.");
            }

            if (!agreeTerms) return notify.error("Please accept the Terms & Conditions.");

            const user = { fname, lname, email, password, loginProvider: "LOCAL" };

            try {
                // ✅ 1. ලෝඩරය පෙන්වීම (Validations අවසන් වූ පසු)
                if (window.loader) window.loader.show();

                const response = await fetch("/api/auth/register", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify(user)
                });

                const json = await response.json();

                if (response.ok && json.status !== false) {
                    notify.success(json.message || "Registration successful!");
                    sessionStorage.setItem("email", email);

                    // ✅ සාර්ථක නම් ලෝඩරය දිගටම තබාගෙන Verification පිටුවට යයි
                    setTimeout(() => {
                        window.location.href = "verify-account.html";
                    }, 1000);
                } else {
                    // ලියාපදිංචිය අසාර්ථක නම් ලෝඩරය අයින් කර පණිවිඩය පෙන්වයි
                    if (window.loader) window.loader.hide();
                    notify.error(json.message || "Registration failed.");
                }

            } catch (err) {
                // Network error එකක් ආවොත් ලෝඩරය අයින් කරයි
                if (window.loader) window.loader.hide();
                console.error("Fetch error:", err);
                notify.error("Network error. Check your connection.");
            }
            // සටහන: Redirect වන විට hide කිරීම අවශ්‍ය නොවේ, එවිට transition එක smooth වේ.
        });
    }

    // --- Toggle Password Visibility (Design Intact) ---
    document.querySelectorAll(".toggle-password").forEach((btn) => {
        btn.addEventListener("click", () => {
            const target = document.getElementById(btn.dataset.target);
            const icon = btn.querySelector("i");
            if (!target || !icon) return;
            const isPassword = target.type === "password";
            target.type = isPassword ? "text" : "password";
            icon.classList.replace(isPassword ? "fa-eye" : "fa-eye-slash", isPassword ? "fa-eye-slash" : "fa-eye");
        });
    });

    // Live validation
    if (passwordInput && confirmPasswordInput) {
        [passwordInput, confirmPasswordInput].forEach(input => {
            input.addEventListener('input', () => {
                if (passwordInput.value.trim() === confirmPasswordInput.value.trim()) {
                    setPasswordErrorStyle(false);
                }
            });
        });
    }
});