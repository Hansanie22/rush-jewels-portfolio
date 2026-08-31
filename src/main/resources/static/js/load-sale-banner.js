/**
 * Seasonal Sale Banner Loader for Rush Jewels
 * - Fetches active sale data from API
 * - Updates image and description dynamically
 */

// ✅ යාවත්කාලීන කළා: Master Loader එකට සම්බන්ධ වීමට window object එකට එක් කළා
window.loadSeasonalSale = async function() {
    const saleSection = document.getElementById('seasonal-sale-section');
    const saleImage = document.getElementById('sale-image');
    const saleDesc = document.getElementById('sale-description');

    if (!saleSection) return;

    try {
        const response = await fetch('/api/seasonal-sale-banner');

        if (!response.ok) {
            throw new Error(`Failed to load banner data: ${response.status}`);
        }

        const data = await response.json();

        if (data && data.hasActiveSale) {
            // දත්ත පිරවීම
            if (saleDesc) saleDesc.textContent = data.description;

            if (saleImage) {
                // පින්තූරය පූරණය වන විට opacity වෙනස් කිරීමේ ඔබේ logic එක
                saleImage.src = data.imageUrl;

                // පින්තූරය සම්පූර්ණයෙන් download වන තෙක් බලා සිටීම සඳහා Promise එකක් භාවිතා කරමු
                return new Promise((resolve) => {
                    saleImage.onload = () => {
                        saleImage.classList.remove('opacity-0');
                        saleImage.classList.add('opacity-100');
                        resolve(true);
                    };
                    saleImage.onerror = () => resolve(false);
                });
            }
        } else {
            // සක්‍රීය sale එකක් නැතිනම් section එක සැඟවිය හැක (Optional)
            // saleSection.style.display = 'none';
        }

        return true;

    } catch (error) {
        console.error("Error loading seasonal sale banner:", error);
        return false;
    }
};

// පිටුව තනිව ලෝඩ් වන අවස්ථාවලදී ක්‍රියාත්මක වීමට (Fallback)
document.addEventListener('DOMContentLoaded', () => {
    // මෙය ක්‍රියාත්මක වන්නේ Master Loader එක නැති පිටුවල පමණි
    if (!window.loader) {
        window.loadSeasonalSale();
    }
});