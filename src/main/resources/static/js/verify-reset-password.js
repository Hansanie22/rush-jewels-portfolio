import Notification from './notification.js';

const notify = Notification({
    position: 'bottom-right',
    duration: 4000,
    hidePrevious: true,
});

const inputs = document.querySelectorAll('.digit-input');
const verifyBtn = document.getElementById('verify-btn');
const resendLink = document.getElementById('resend-link');

let canResend = true;

document.addEventListener('DOMContentLoaded', () => {
    // 1. පිටුව ආරම්භයේදී අන්තර්ගතය පෙන්වීම
    revealContent();

    // Auto focus logic
    inputs.forEach((input, idx) => {
        input.addEventListener('input', () => {
            if (input.value.length === 1 && idx < inputs.length - 1) {
                inputs[idx + 1].focus();
            }
        });

        input.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && input.value === '' && idx > 0) {
                inputs[idx - 1].focus();
            }
        });
    });
});

// ✅ Verify button click logic
verifyBtn.addEventListener('click', async () => {
    const code = Array.from(inputs).map(i => i.value).join('');

    if (code.length !== 6) {
        notify.error('Please enter all 6 digits.');
        return;
    }

    const email = sessionStorage.getItem('email');
    if (!email) {
        notify.error('Session expired. Please try again.');
        setTimeout(() => window.location.href = 'forgot-password.html', 1500);
        return;
    }

    // ✅ ලෝඩරය පෙන්වීම
    if (window.loader) window.loader.show();

    try {
        const response = await fetch('/api/auth/verify-reset-password', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email, code}),
        });

        const data = await response.json();

        if (response.ok && data.status) {
            notify.success(data.message || 'Verification successful!');

            // සාර්ථක නම් ලෝඩරය සමඟම Password Reset tab එකට Redirect වේ
            setTimeout(() => {
                window.location.href = data.redirect || '/account.html?tab=password';
            }, 1500);
        } else {
            // Error එකක් නම් ලෝඩරය අයින් කරන්න
            if (window.loader) window.loader.hide();
            notify.error(data.message || 'Verification failed. Please try again.');

            if (data.message && data.message.includes('expired')) {
                enableResendImmediately();
            }
        }
    } catch (err) {
        console.error("Verify Reset Fetch Error:", err);
        if (window.loader) window.loader.hide();
        notify.error('Network error. Please try again later.');
    }
});

// ✅ Resend code logic
async function resendCode() {
    if (!canResend) return;

    const email = sessionStorage.getItem('email');
    if (!email) {
        notify.error('Session missing. Please try again.');
        setTimeout(() => window.location.href = 'forgot-password.html', 1500);
        return;
    }

    // ✅ ලෝඩරය පෙන්වීම
    if (window.loader) window.loader.show();

    try {
        const response = await fetch('/api/auth/resend-reset-code', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email}),
        });

        const data = await response.json();

        // ලෝඩරය අයින් කිරීම
        if (window.loader) window.loader.hide();

        if (response.ok && data.status) {
            notify.success(data.message || 'Verification code has been resent!');
            clearInputs();
            startCooldown(180);
        } else {
            notify.error(data.message || 'Failed to resend code.');
        }
    } catch (err) {
        console.error("Resend Reset Fetch Error:", err);
        if (window.loader) window.loader.hide();
        notify.error('Network error. Please try again later.');
    }
}

resendLink.addEventListener('click', (e) => {
    e.preventDefault();
    resendCode();
});

// --- UI Helpers ---

function revealContent() {
    const main = document.getElementById('main-content');
    if (main) main.style.display = 'block';
    if (window.loader) {
        setTimeout(() => window.loader.hide(), 500);
    }
}

function clearInputs() {
    inputs.forEach(input => input.value = '');
    inputs[0].focus();
}

function startCooldown(seconds) {
    canResend = false;
    let countdown = seconds;
    const timer = setInterval(() => {
        countdown--;
        const min = Math.floor(countdown / 60);
        const sec = countdown % 60;
        resendLink.textContent = `Wait ${min}:${sec < 10 ? '0' : ''}${sec}`;
        if (countdown <= 0) {
            clearInterval(timer);
            resendLink.textContent = 'Resend';
            canResend = true;
        }
    }, 1000);
}

function enableResendImmediately() {
    resendLink.textContent = 'Resend';
    canResend = true;
    resendLink.classList.add('text-red-500', 'font-bold');
}