import Notification from './notification.js';

// Initialize notification with fallback
let notify;
try {
    notify = Notification({
        position: 'bottom-right',
        duration: 4000,
        hidePrevious: true,
        maxVisible: 3
    });
    window.notify = notify;
} catch (err) {
    notify = {
        success: () => {},
        error: () => {},
        warning: () => {},
        info: () => {},
        confirm: (msg, onYes, onNo) => {
            if (window.confirm(msg)) onYes();
            else if (onNo) onNo();
        }
    };
    window.notify = notify;
}

/* ---------------------------------------------------
   Helper: Save return URL
--------------------------------------------------- */
function saveReturnUrlIfNeeded() {
    if (sessionStorage.getItem('returnUrl')) return;
    const path = window.location.pathname;
    if (!path.includes('auth.html') && !path.includes('admin-login.html')) {
        sessionStorage.setItem('returnUrl', window.location.href);
    } else if (document.referrer && !document.referrer.includes('auth.html') && !document.referrer.includes('admin-login.html')) {
        sessionStorage.setItem('returnUrl', document.referrer);
    }
}

/* ---------------------------------------------------
   Session check - updates navigation
--------------------------------------------------- */
document.addEventListener("DOMContentLoaded", async () => {
    const link = document.getElementById("account-link");
    if (!link) return;

    try {
        const cachedUser = sessionStorage.getItem('user');
        const res = await fetch("/api/auth/session-check", {
            credentials: "include",
            headers: { 'Accept': 'application/json' }
        });

        if (res.status === 404) {
            link.href = "auth.html";
            link.textContent = "Sign In";
            return;
        }

        const data = await res.json();

        if (data.loggedIn) {
            if (!cachedUser) {
                try {
                    const userRes = await fetch("/api/auth/me", {
                        credentials: "include",
                        headers: { 'Accept': 'application/json' }
                    });
                    if (userRes.ok) {
                        const userData = await userRes.json();
                        if (userData?.user) {
                            sessionStorage.setItem('user', JSON.stringify(userData.user));
                        }
                    }
                } catch {}
            }
            link.href = "account.html";
            link.textContent = "Account";
        } else {
            sessionStorage.removeItem('user');
            link.href = "auth.html";
            link.textContent = "Sign In";
        }
    } catch {
        sessionStorage.removeItem('user');
        link.href = "auth.html";
        link.textContent = "Sign In";
    }
});

/* ---------------------------------------------------
   Social login buttons
--------------------------------------------------- */
document.addEventListener("DOMContentLoaded", () => {

    // Google Login
    document.querySelectorAll(".google-login").forEach(button => {
        button.addEventListener("click", e => {
            e.preventDefault();
            saveReturnUrlIfNeeded();

            // ✅ ලෝඩරය පෙන්වීම
            if (window.loader) window.loader.show();

            notify.info("Redirecting to Google...");
            button.disabled = true;
            button.innerHTML = `<span class="flex items-center gap-2"><i class="fas fa-spinner fa-spin"></i> Redirecting...</span>`;

            setTimeout(() => {
                window.location.href = "/oauth2/authorization/google";
            }, 400);
        });
    });

    // Facebook Login
    document.querySelectorAll(".facebook-login").forEach(button => {
        button.addEventListener("click", e => {
            e.preventDefault();
            saveReturnUrlIfNeeded();

            // ✅ ලෝඩරය පෙන්වීම
            if (window.loader) window.loader.show();

            notify.info("Redirecting to Facebook...");
            button.disabled = true;
            button.innerHTML = `<span class="flex items-center gap-2"><i class="fas fa-spinner fa-spin"></i> Redirecting...</span>`;

            setTimeout(() => {
                window.location.href = "/oauth2/authorization/facebook";
            }, 400);
        });
    });

    // Apple Login
    document.querySelectorAll(".apple-login").forEach(button => {
        button.addEventListener("click", e => {
            e.preventDefault();
            saveReturnUrlIfNeeded();

            // Show loader
            if (window.loader) window.loader.show();

            notify.info("Redirecting to Apple...");
            button.disabled = true;
            button.innerHTML = `<span class="flex items-center gap-2"><i class="fas fa-spinner fa-spin"></i> Redirecting...</span>`;

            setTimeout(() => {
                window.location.href = "/oauth2/authorization/apple";
            }, 400);
        });
    });
});

/* ---------------------------------------------------
   OAuth success / error handling
--------------------------------------------------- */
document.addEventListener("DOMContentLoaded", () => {
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');
    const success = params.get('success');
    const message = params.get('message');

    if (error) {
        // ✅ ලොගින් වීමට උත්සාහ කර Error එකක් ආවොත් ලෝඩරය අයින් කරන්න
        if (window.loader) window.loader.hide();

        const errors = {
            oauth_failed: "Social login failed. Please try again.",
            no_email: "Email permission is required to continue.",
            user_not_found: "Account could not be created.",
            email_exists_local: message || "Email already registered. Use your password."
        };

        if (window.notify) notify.error(errors[error] || "An unexpected login error occurred.");
        window.history.replaceState({}, document.title, window.location.pathname);
        return;
    }

    if (success === 'oauth_success') {
        // ✅ සාර්ථක ලොගින් එකකින් පසු ලෝඩරය පෙන්වමින් වෙනත් පිටුවකට යොමු කිරීම
        if (window.loader) window.loader.show();

        if (window.notify) notify.success("Successfully logged in!");
        window.history.replaceState({}, document.title, window.location.pathname);

        const returnUrl = sessionStorage.getItem('returnUrl');
        setTimeout(() => {
            window.location.href = returnUrl || "account.html";
            if (returnUrl) sessionStorage.removeItem('returnUrl');
        }, 1200);
    }
});

/* ---------------------------------------------------
   Global Redirect Utilities
--------------------------------------------------- */

export function redirectToLogin(options = {}) {
    const { message = null, stateFlags = {}, delay = 0 } = options;
    const path = window.location.pathname;
    if (path.includes('auth.html') || path.includes('admin-login.html')) return;

    // ✅ වෙනත් පිටුවකට යොමු වීමට පෙර ලෝඩරය පෙන්වීම
    if (window.loader) window.loader.show();

    sessionStorage.setItem('returnUrl', window.location.href);
    Object.entries(stateFlags).forEach(([k, v]) => sessionStorage.setItem(k, String(v)));

    if (message && window.notify) window.notify.warning(message);

    const go = () => window.location.href = 'auth.html';
    delay ? setTimeout(go, delay) : go();
}

export function handlePostLoginRedirect(defaultRedirect = 'account.html') {
    if (window.loader) window.loader.show();
    const returnUrl = sessionStorage.getItem('returnUrl');
    sessionStorage.removeItem('returnUrl');
    setTimeout(() => { window.location.href = returnUrl || defaultRedirect; }, 1000);
}

export function getStateFlags() {
    const flags = {};
    ['openHelpDeskAfterLogin', 'openCartAfterLogin', 'scrollToCheckoutAfterLogin']
        .forEach(flag => {
            const v = sessionStorage.getItem(flag);
            if (v !== null) {
                flags[flag] = v === 'true';
                sessionStorage.removeItem(flag);
            }
        });
    return flags;
}

export function handle401Error(response, options = {}) {
    if (response?.status === 401) {
        redirectToLogin(options);
        return true;
    }
    return false;
}

export async function fetchWithAuthCheck(url, fetchOptions = {}, redirectOptions = {}) {
    const response = await fetch(url, fetchOptions);
    if (response.status === 401) {
        handle401Error(response, redirectOptions);
        throw new Error('Authentication required');
    }
    return response;
}

export default {
    redirectToLogin,
    handlePostLoginRedirect,
    getStateFlags,
    handle401Error,
    fetchWithAuthCheck
};