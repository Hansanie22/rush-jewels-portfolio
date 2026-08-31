// ===================== FLASH SALE LOADER ===================== //

/**
 * Wait for the Flash Sale Popup element to exist in the DOM
 */
async function waitForPopup() {
    let attempts = 0;
    // උපරිම තත්පර 4ක් (20 * 200ms) බලා සිටී
    while (!document.getElementById("flash-sale-popup") && attempts < 20) {
        await new Promise(r => setTimeout(r, 200));
        attempts++;
    }
    return document.getElementById("flash-sale-popup");
}

/**
 * Fetch the latest flash sale from backend
 * ✅ යාවත්කාලීන කළා: Master Loader එකට හඳුනාගත හැකි වන පරිදි window object එකට එක් කළා
 */
window.loadFlashSale = async function() {

    try {
        // 1. HTML එක එනකම් බලා සිටීම
        const popup = await waitForPopup();
        if (!popup) return false;

        // 2. දත්ත ගෙන්වීම
        const response = await fetch("/api/flash-sale/latest");

        if (!response.ok) return false;

        const text = await response.text();
        if (!text) {
            console.log("No active flash sale found.");
            return true; // සක්‍රීය sale එකක් නැති වුණත් loader එකට ඉදිරියට යන්න ඉඩ දෙනවා
        }

        const data = JSON.parse(text);

        if (!data || (!data.active && data.active !== undefined)) {
            return true;
        }

        // --- SESSION CHECK ---
        // Use data.name since data.id doesn't exist in the DTO
        const seenSaleId = sessionStorage.getItem('flash_sale_seen');
        if (data.name && seenSaleId === data.name) {
            return true;
        }

        // 3. UI එක යාවත්කාලීන කිරීම (ඔබේ මුල් ඩිසයින් එකම වේ)
        updateFlashSaleUI(data);

        // 4. Countdown එක පටන් ගැනීම
        if (data.endDate) {
            startCountdown(data.endDate);
        }

        // 5. Popup එක පෙන්වීම
        popup.classList.remove("hidden");
        if (data.name) {
            sessionStorage.setItem('flash_sale_seen', data.name);
        }

        // --- BUTTON REDIRECT LOGIC ---
        const shopBtn = document.getElementById("shop-flash-sale") || popup.querySelector("a[href*='shop.html']");
        if (shopBtn) {
            shopBtn.removeAttribute('href');
            shopBtn.style.cursor = 'pointer';
            shopBtn.onclick = function(e) {
                e.preventDefault();
                const saleType = (data.type || 'PRODUCT').toUpperCase();
                window.location.href = (saleType === 'COLLECTION') ? 'collections.html?sale=true' : 'shop.html?sale=true';
            };
        }

        // 6. Close Buttons Logic
        const closeBtn = document.getElementById("close-flash-sale");
        if (closeBtn) closeBtn.onclick = () => popup.classList.add("hidden");

        const declineBtn = document.getElementById("decline-flash-sale");
        if (declineBtn) declineBtn.onclick = () => popup.classList.add("hidden");

        return true; // සියල්ල සාර්ථකයි

    } catch (err) {
        console.error("Flash sale system error:", err);
        return false;
    }
};

/**
 * Update UI Elements (Design Intact)
 */
function updateFlashSaleUI(data) {
    const popup = document.getElementById("flash-sale-popup");
    if (!popup) return;

    const discountEl = popup.querySelector(".flash-sale-discount");
    if (discountEl) discountEl.textContent = Math.round(data.discountPercentage) + "%";

    const nameEl = popup.querySelector(".flash-sale-name");
    if (nameEl) nameEl.textContent = data.name;

    const descEl = popup.querySelector(".flash-sale-description");
    if (descEl && data.description) descEl.textContent = data.description;
}

/**
 * Countdown Timer Logic
 */
function startCountdown(endDate) {
    const endStr = endDate.includes('T') ? endDate : endDate + "T23:59:59";
    const end = new Date(endStr).getTime();

    const updateTimer = () => {
        const now = Date.now();
        const distance = end - now;

        if (distance <= 0) {
            document.getElementById("flash-sale-popup")?.classList.add("hidden");
            return false;
        }

        const days = Math.floor(distance / (1000 * 60 * 60 * 24));
        const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const mins = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        const secs = Math.floor((distance % (1000 * 60)) / 1000);

        const dEl = document.getElementById("days");
        const hEl = document.getElementById("hours");
        const mEl = document.getElementById("minutes");
        const sEl = document.getElementById("seconds");

        if (dEl) dEl.textContent = days.toString().padStart(2, "0");
        if (hEl) hEl.textContent = hours.toString().padStart(2, "0");
        if (mEl) mEl.textContent = mins.toString().padStart(2, "0");
        if (sEl) sEl.textContent = secs.toString().padStart(2, "0");

        return true;
    };

    if (updateTimer()) {
        const interval = setInterval(() => {
            if (!updateTimer()) clearInterval(interval);
        }, 1000);
    }
}

// Fallback: Master Loader එක නැති තැන්වලදීත් වැඩ කිරීමට
document.addEventListener("DOMContentLoaded", () => {
    if (!window.loader) {
        window.loadFlashSale();
    }
});