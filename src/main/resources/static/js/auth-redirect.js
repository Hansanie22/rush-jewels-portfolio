/**
 * Global Auth Redirect Utility Module
 * Provides consistent redirect-back-after-login functionality across the entire site
 */

/**
 * Saves the current page URL and optional state flags, then redirects to auth.html
 * @param {Object} options - Configuration options
 * @param {string} options.message - Optional notification message
 * @param {Object} options.stateFlags - Optional state flags (e.g., {openHelpDeskAfterLogin: true})
 * @param {number} options.delay - Delay in milliseconds before redirect (default: 0)
 */
export function redirectToLogin(options = {}) {
    const {
        message = null,
        stateFlags = {},
        delay = 0
    } = options;

    // 1. Prevent redirect loop if already on auth page
    const currentPath = window.location.pathname;
    if (currentPath.includes('auth.html') || currentPath.includes('admin-login.html')) {
        return;
    }

    // 2. Save the current page URL for return after login
    const returnUrl = window.location.href;
    sessionStorage.setItem('returnUrl', returnUrl);

    // 3. Save any state flags for UI restoration
    Object.keys(stateFlags).forEach(key => {
        sessionStorage.setItem(key, String(stateFlags[key]));
    });

    // 4. Show notification if provided
    if (message && window.notify) {
        window.notify.warning(message);
    }

    // 5. Redirect to login page
    const redirectAction = () => {
        window.location.href = 'auth.html';
    };

    if (delay > 0) {
        setTimeout(redirectAction, delay);
    } else {
        redirectAction();
    }
}

/**
 * Handles post-login redirect back to the saved URL or default page
 * Call this function after successful login
 * @param {string} defaultRedirect - Default page to redirect to if no returnUrl exists
 */
export function handlePostLoginRedirect(defaultRedirect = 'account.html') {
    // 1. Check for saved return URL
    const returnUrl = sessionStorage.getItem('returnUrl');

    // 2. Clear the returnUrl from storage
    sessionStorage.removeItem('returnUrl');

    // 3. Redirect to saved URL or default
    if (returnUrl) {
        setTimeout(() => {
            window.location.href = returnUrl;
        }, 1000);
    } else {
        setTimeout(() => {
            window.location.href = defaultRedirect;
        }, 1000);
    }
}

/**
 * Checks for state restoration flags on page load
 * Call this in DOMContentLoaded or main.js
 * @returns {Object} Object containing all state flags found
 */
export function getStateFlags() {
    const flags = {};

    // Known state flags to check
    const knownFlags = [
        'openHelpDeskAfterLogin',
        'openCartAfterLogin',
        'scrollToCheckoutAfterLogin'
    ];

    knownFlags.forEach(flag => {
        const value = sessionStorage.getItem(flag);
        if (value !== null) {
            flags[flag] = value === 'true';
            // Clear the flag after reading
            sessionStorage.removeItem(flag);
        }
    });

    return flags;
}

/**
 * Universal 401 error handler for fetch requests
 * Use this in catch blocks or when checking response status
 * @param {Response} response - Fetch API response object
 * @param {Object} options - Same options as redirectToLogin
 * @returns {boolean} True if 401 was handled, false otherwise
 */
export function handle401Error(response, options = {}) {
    if (response && response.status === 401) {
        redirectToLogin({
            message: options.message || 'Please login to continue.',
            stateFlags: options.stateFlags || {},
            delay: options.delay || 0
        });
        return true;
    }
    return false;
}

/**
 * Wrapper for fetch that automatically handles 401 errors
 * @param {string} url - URL to fetch
 * @param {Object} fetchOptions - Standard fetch options
 * @param {Object} redirectOptions - Options for redirect behavior
 * @returns {Promise<Response>}
 */
export async function fetchWithAuthCheck(url, fetchOptions = {}, redirectOptions = {}) {
    try {
        const response = await fetch(url, fetchOptions);

        if (response.status === 401) {
            handle401Error(response, redirectOptions);
            throw new Error('Authentication required');
        }

        return response;
    } catch (error) {
        // Re-throw for caller to handle
        throw error;
    }
}

// Export as default object as well for convenience
export default {
    redirectToLogin,
    handlePostLoginRedirect,
    getStateFlags,
    handle401Error,
    fetchWithAuthCheck
};

