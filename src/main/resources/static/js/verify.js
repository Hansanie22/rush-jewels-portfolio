import Notification from './notification.js';

// Notification setup
const notifier = Notification({position: 'bottom-right', duration: 4000});

const inputs = document.querySelectorAll('.digit-input');
const verifyBtn = document.getElementById('verify-btn');
const verifyLink = document.getElementById('resend-link');

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

// ✅ Verify button logic
verifyBtn.addEventListener('click', async () => {
    const code = Array.from(inputs).map(i => i.value).join('');

    if (code.length !== 6) {
        notifier.validation("Please enter all 6 digits.");
        return;
    }

    const userEmail = sessionStorage.getItem("email");
    if (!userEmail) {
        notifier.error("Session expired. Redirecting to login...");
        setTimeout(() => window.location = "auth.html", 1500);
        return;
    }

    // ✅ ලෝඩරය පෙන්වීම
    if (window.loader) window.loader.show();

    try {
        const response = await fetch("/api/auth/verify", {
            method: "POST",
            body: JSON.stringify({
                verificationCode: code,
                email: userEmail
            }),
            headers: {"Content-Type": "application/json"}
        });

        const json = await response.json();

        if (json.status) {
            notifier.success("Verification successful!");
            sessionStorage.removeItem("email");
            // සාර්ථක නම් ලෝඩරය සමඟම Redirect වේ
            setTimeout(() => window.location = "index.html", 1000);
            return;
        }

        // Error එකක් නම් ලෝඩරය අයින් කරන්න
        if (window.loader) window.loader.hide();

        if (json.message === "1") {
            notifier.warning("Email not found. Redirecting...");
            setTimeout(() => window.location = "auth.html", 1500);
        } else if (json.message === "Verification code has expired!") {
            notifier.warning("Code expired! Please resend.");
            enableResendImmediately();
        } else {
            notifier.error(json.message);
        }

    } catch (err) {
        if (window.loader) window.loader.hide();
        notifier.error("Server error.");
        console.error(err);
    }
});

// ✅ Resend code logic
async function resendCode() {
    if (!canResend) return;

    const userEmail = sessionStorage.getItem("email");
    if (!userEmail) {
        notifier.error("Session missing. Please login.");
        return;
    }

    // ✅ ලෝඩරය පෙන්වීම
    if (window.loader) window.loader.show();

    try {
        const response = await fetch("/api/auth/resend", {
            method: "POST",
            body: JSON.stringify({ email: userEmail }),
            headers: {"Content-Type": "application/json"}
        });

        const json = await response.json();

        // ලෝඩරය අයින් කිරීම
        if (window.loader) window.loader.hide();

        if (json.status) {
            notifier.success(json.message);
            clearInputs();
            startCooldown(180);
        } else {
            notifier.error(json.message);
        }

    } catch (err) {
        if (window.loader) window.loader.hide();
        notifier.error("Server error.");
    }
}

verifyLink.addEventListener('click', (e) => {
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

function startCooldown(seconds) {
    canResend = false;
    let countdown = seconds;
    const timer = setInterval(() => {
        countdown--;
        const min = Math.floor(countdown / 60);
        const sec = countdown % 60;
        verifyLink.textContent = `Wait ${min}:${sec < 10 ? '0' : ''}${sec}`;
        if (countdown <= 0) {
            clearInterval(timer);
            verifyLink.textContent = "Resend";
            canResend = true;
        }
    }, 1000);
}

function enableResendImmediately() {
    verifyLink.textContent = "Resend";
    canResend = true;
    verifyLink.classList.add('text-red-500', 'font-bold');
}

function clearInputs() {
    inputs.forEach(i => i.value = '');
    inputs[0].focus();
}