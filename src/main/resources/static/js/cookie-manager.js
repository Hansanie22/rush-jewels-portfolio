/**
 * Professional Cookie Management Script
 * Version: 2.2 (Consent-Based, GDPR-Friendly)
 */

document.addEventListener('DOMContentLoaded', () => {
    const banner = document.getElementById('cookieBanner');
    const overlay = document.getElementById('cookieOverlay');
    const consent = localStorage.getItem('cookiesAccepted');

    // Show banner ONLY if user has not made a choice
    if (consent === null) {
        document.body.classList.add('no-scroll');

        if (overlay) overlay.classList.remove('hidden');
        if (banner) {
            banner.classList.remove('hidden');
            setTimeout(() => {
                banner.classList.remove('translate-y-full');
            }, 100);
        }
    }
});

/**
 * User ACCEPTS cookies
 */
function acceptCookies() {
    localStorage.setItem('cookiesAccepted', 'true');
    hideBannerAndUnlock();

    console.log("Cookies accepted.");
}

/**
 * User DECLINES cookies
 * - No cookies stored
 * - Banner closes
 */
function declineCookies() {
    localStorage.setItem('cookiesAccepted', 'false'); // record choice ONLY
    hideBannerAndUnlock();

    console.log("Cookies declined.");
}

/**
 * Close banner (called by Decline button)
 * Redirects to declineCookies logic
 */
function closeCookieBanner() {
    declineCookies();
}

/**
 * Hide banner and restore page access
 */
function hideBannerAndUnlock() {
    const banner = document.getElementById('cookieBanner');
    const overlay = document.getElementById('cookieOverlay');

    if (banner) {
        banner.classList.add('translate-y-full');
        setTimeout(() => {
            banner.classList.add('hidden');
            if (overlay) overlay.classList.add('hidden');
            document.body.classList.remove('no-scroll');
        }, 500);
    }
}
