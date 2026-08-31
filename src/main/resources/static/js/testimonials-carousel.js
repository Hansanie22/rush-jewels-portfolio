/**
 * Testimonials Carousel Logic for Rush Jewels
 * - Fetches Approved Reviews from API
 * - Dynamic Rendering
 * - Features: Autoplay, Pause on Hover, Responsive navigation
 */

let currentSlideIndex = 0;
let slideElements = [];
let dotElements = [];
let autoplayInterval = null; // ඉබේ මාරු වීමට අවශ්‍ය ටයිමර් එක

// ✅ Master Loader එකට සහ Autoplay එකට ගැළපෙන පරිදි යාවත්කාලීන කළා
window.initTestimonialsCarousel = async function() {
    const sliderContainer = document.getElementById('testimonials-slider');
    const dotContainer = document.getElementById('carousel-dots-testimonials');
    const prevButton = document.getElementById('testimonials-prev');
    const nextButton = document.getElementById('testimonials-next');

    if (!sliderContainer || !dotContainer) return false;

    try {
        const response = await fetch('/api/v1/testimonials');
        if (!response.ok) throw new Error(`Failed to fetch data: ${response.status}`);

        const reviews = await response.json();

        // පරණ දත්ත සහ ටයිමර් Clear කිරීම
        stopAutoplay();
        sliderContainer.innerHTML = '';
        dotContainer.innerHTML = '';
        slideElements = [];
        dotElements = [];

        if (!reviews || reviews.length === 0) {
            sliderContainer.innerHTML = '<div class="w-full px-4 text-center py-10 text-gray-500">No approved reviews yet.</div>';
            return true;
        }

        // Render Slides and Dots
        reviews.forEach((review, index) => {
            const slide = createSlideElement(review);
            sliderContainer.appendChild(slide);

            const dot = createDotElement(index, index === 0);
            dotContainer.appendChild(dot);
            dotElements.push(dot);
        });

        slideElements = Array.from(sliderContainer.children);

        // --- NAVIGATION SETUP ---
        if (reviews.length > 1) {
            // Button Events (CloneNode භාවිතා කරන්නේ පරණ listeners අයින් කිරීමටයි)
            const newPrev = prevButton.cloneNode(true);
            const newNext = nextButton.cloneNode(true);
            prevButton.parentNode.replaceChild(newPrev, prevButton);
            nextButton.parentNode.replaceChild(newNext, nextButton);

            newPrev.addEventListener('click', () => { navigateSlide(-1); resetAutoplay(); });
            newNext.addEventListener('click', () => { navigateSlide(1); resetAutoplay(); });

            dotElements.forEach(dot => {
                dot.addEventListener('click', (e) => {
                    const index = parseInt(e.target.dataset.slide);
                    goToSlide(index);
                    resetAutoplay();
                });
            });

            // Hover එකේදී Autoplay එක නතර කිරීමට
            const testimonialsSection = sliderContainer.parentElement;
            testimonialsSection.addEventListener('mouseenter', stopAutoplay);
            testimonialsSection.addEventListener('mouseleave', startAutoplay);

            // Autoplay පටන් ගැනීම
            startAutoplay();
        }

        updateCarouselDisplay();
        return true;

    } catch (error) {
        console.error('Error loading testimonials:', error);
        return false;
    }
}

// --- AUTOPLAY FUNCTIONS (Merged from HTML script) ---
function startAutoplay() {
    if (autoplayInterval) clearInterval(autoplayInterval);
    autoplayInterval = setInterval(() => {
        navigateSlide(1);
    }, 5000); // තත්පර 5කට වරක් මාරු වේ
}

function stopAutoplay() {
    if (autoplayInterval) {
        clearInterval(autoplayInterval);
        autoplayInterval = null;
    }
}

function resetAutoplay() {
    stopAutoplay();
    startAutoplay();
}

// --- AVATAR & UI GENERATION (Design Intact) ---
const generateAvatar = (name, size = 64) => {
    if (!name || name.trim() === '') name = 'User';
    const initials = name.split(' ').map(n => n[0]?.toUpperCase()).filter(Boolean).slice(0, 2).join('');
    const hue = (name.charCodeAt(0) || 0) * 13 % 360;
    const bgColor = `hsl(${hue}, 70%, 50%)`;
    return `data:image/svg+xml;base64,${btoa(`<svg width="${size}" height="${size}" xmlns="http://www.w3.org/2000/svg"><rect width="${size}" height="${size}" fill="${bgColor}" /><text x="50%" y="50%" text-anchor="middle" dominant-baseline="central" fill="#fff" font-size="${size/2.5}" font-family="Arial" font-weight="bold">${initials}</text></svg>`)}`;
};

function createDotElement(index, isActive) {
    const dotBtn = document.createElement('button');
    dotBtn.className = 'testimonial-dot w-3 h-3 transition-all duration-300 mx-1';
    dotBtn.setAttribute('data-slide', index);
    if (isActive) dotBtn.classList.add('bg-gold');
    else dotBtn.classList.add('bg-gray-300', 'hover:bg-gold');
    return dotBtn;
}

function createSlideElement(review) {
    const slideDiv = document.createElement('div');
    slideDiv.className = 'w-full flex-shrink-0 px-4';
    let imgSrc = (review.profileImagePath && review.profileImagePath.trim() !== "") ? review.profileImagePath : generateAvatar(review.reviewerName);
    const starsHtml = Array(5).fill(0).map((_, i) => `<i class="fas fa-star ${i < review.rating ? 'text-gold' : 'text-gray-300'}"></i>`).join('');

    slideDiv.innerHTML = `
        <div class="bg-white p-8 shadow-lg hover:shadow-xl transition-shadow duration-300 mx-auto max-w-2xl border border-gray-100">
            <div class="flex items-center mb-6 justify-center">
                <img src="${imgSrc}" alt="${review.reviewerName}" class="w-16 h-16 mr-4 object-cover shadow-sm" onerror="this.src='${generateAvatar(review.reviewerName)}'" loading="lazy"> 
                <div class="text-center sm:text-left">
                    <h4 class="font-semibold text-dark text-lg">${review.reviewerName}</h4>
                    <div class="text-sm mt-1">${starsHtml}</div>
                </div>
            </div>
            <p class="text-gray-600 italic text-center text-lg leading-relaxed">"${review.comment}"</p>
        </div>
    `;
    return slideDiv;
}

// --- NAVIGATION LOGIC ---
function goToSlide(index) {
    if (index >= 0 && index < slideElements.length) {
        currentSlideIndex = index;
        updateCarouselDisplay();
    }
}

function navigateSlide(direction) {
    let newIndex = currentSlideIndex + direction;
    if (newIndex >= slideElements.length) newIndex = 0;
    else if (newIndex < 0) newIndex = slideElements.length - 1;
    goToSlide(newIndex);
}

function updateCarouselDisplay() {
    const sliderContainer = document.getElementById('testimonials-slider');
    if (!sliderContainer || slideElements.length === 0) return;
    const totalWidth = sliderContainer.offsetWidth;
    sliderContainer.style.transform = `translateX(${-currentSlideIndex * totalWidth}px)`;

    dotElements.forEach((dot, index) => {
        if (index === currentSlideIndex) {
            dot.classList.remove('bg-gray-300', 'hover:bg-gold');
            dot.classList.add('bg-gold');
        } else {
            dot.classList.remove('bg-gold');
            dot.classList.add('bg-gray-300', 'hover:bg-gold');
        }
    });
}

window.addEventListener('resize', updateCarouselDisplay);

// Master Loader එක නැති තැන්වලදීත් වැඩ කිරීමට
document.addEventListener('DOMContentLoaded', () => {
    if (!window.loader) {
        window.initTestimonialsCarousel();
    }
});