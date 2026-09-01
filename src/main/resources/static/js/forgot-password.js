import Notification from './notification.js';

const notify = Notification({
    position: 'bottom-right',
    duration: 4000,
    hidePrevious: true,
});

document.addEventListener('DOMContentLoaded', () => {
    // 1. පිටුව ආරම්භයේදී අන්තර්ගතය පෙන්වීම
    revealContent();
});

async function sendResetLink() {
    const emailInput = document.getElementById('email');
    if (!emailInput) return;

    const email = emailInput.value.trim();

    // මූලික Validation
    if (!email) {
        return notify.error('Please enter your email address.');
    }

    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email)) {
        return notify.warning('Please enter a valid email address.');
    }

    const button = document.querySelector('#forgot-password-form button');

    try {
        if (button) {
            button.disabled = true;
            button.classList.add('opacity-70', 'cursor-not-allowed');
        }

        // ✅ 2. VELORA LOADER පෙන්වීම (Request එක යන අතරතුර)
        if (window.loader) window.loader.show();

        const res = await fetch('/api/auth/forgot-password', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ email }),
        });

        const data = await res.json();

        if (res.ok && data.status) {
            notify.success(data.message || 'Reset link sent successfully!');

            sessionStorage.setItem('email', email);
            emailInput.value = '';

            // සාර්ථක නම් ලෝඩරය සමඟම Redirect වේ
            setTimeout(() => {
                window.location.href = 'verify-reset-password.html';
            }, 1500);

        } else {
            // Error එකක් නම් ලෝඩරය අයින් කරන්න
            if (window.loader) window.loader.hide();
            notify.error(data.message || 'Error sending reset link.');

            if (button) {
                button.disabled = false;
                button.classList.remove('opacity-70', 'cursor-not-allowed');
            }
        }
    } catch (err) {
        console.error("Forgot Password Fetch Error:", err);
        if (window.loader) window.loader.hide();
        notify.error('Network error. Please try again later.');

        if (button) {
            button.disabled = false;
            button.classList.remove('opacity-70', 'cursor-not-allowed');
        }
    }
}

/**
 * පිටුව පෙන්වන සහ ලෝඩරය අයින් කරන Helper Function එක
 */
function revealContent() {
    const main = document.getElementById('main-content');
    if (main) {
        main.style.display = 'block';
        main.classList.add('animate__animated', 'animate__fadeIn');
    }

    if (window.loader) {
        setTimeout(() => {
            window.loader.hide();
        }, 500);
    }
}

// Global function එකක් ලෙස පිටතින් ඇමතීමට හැකි වන ලෙස
window.sendResetLink = sendResetLink;