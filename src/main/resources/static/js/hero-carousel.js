/**
 * Hero Section Carousel Logic for Velora Fine Jewellery
 */

// ✅ යාවත්කාලීන කළා: පිටතින් ඇමතීමට window object එකට සම්බන්ධ කළා
window.initHeroCarousel = async function() {
    const slidesContainer = document.getElementById('carousel-container');
    const dotsContainer = document.getElementById('carousel-dots');

    if (!slidesContainer || !dotsContainer) return;

    try {
        const response = await fetch('/api/v1/banners');
        if (!response.ok) return;

        const banners = await response.json();
        slidesContainer.innerHTML = '';
        dotsContainer.innerHTML = '';

        if (!banners || banners.length === 0) return;

        banners.forEach((banner, index) => {
            const slideHtml = createHeroSlideElement(banner, index);
            slidesContainer.insertAdjacentHTML('beforeend', slideHtml);

            const dotBtn = document.createElement('button');
            dotBtn.className = index === 0
                ? 'carousel-dot w-3 h-3 transition-all duration-300 transform bg-gold scale-125'
                : 'carousel-dot w-3 h-3 transition-all duration-300 transform bg-white bg-opacity-60 hover:bg-opacity-100';

            dotBtn.setAttribute('data-slide', index);
            dotsContainer.appendChild(dotBtn);
        });

        if (banners.length > 1) {
            startHeroCarouselAnimation(slidesContainer, dotsContainer, banners.length);
        }

        return true; // සාර්ථක බව දැනුම් දීම

    } catch (error) {
        console.error('Error initializing hero carousel:', error);
        return false;
    }
}

function createHeroSlideElement(banner, index) {
    const opacityClass = index === 0 ? 'opacity-100' : 'opacity-0';
    const timestamp = new Date().getTime();
    const fileUrl = `${banner.url}?t=${timestamp}`;
    let mediaContent = banner.type === 'VIDEO'
        ? `<video autoplay muted loop playsinline class="w-full h-full object-cover"><source src="${fileUrl}" type="video/mp4"></video>`
        : `<img src="${fileUrl}" alt="Velora Fine Jewellery Banner ${index + 1}" class="w-full h-full object-cover object-center" loading="lazy">`;

    return `<div class="carousel-slide absolute inset-0 ${opacityClass} transition-opacity duration-1000" data-index="${index}">${mediaContent}<div class="absolute inset-0 bg-black bg-opacity-30"></div></div>`;
}

function startHeroCarouselAnimation(container, dotsContainer, totalSlides) {
    let currentSlide = 0;
    const slides = container.querySelectorAll('.carousel-slide');
    const dots = dotsContainer.querySelectorAll('.carousel-dot');

    setInterval(() => {
        if (slides[currentSlide]) {
            slides[currentSlide].classList.replace('opacity-100', 'opacity-0');
            dots[currentSlide].classList.remove('bg-gold', 'scale-125');
            dots[currentSlide].classList.add('bg-white', 'bg-opacity-60');
        }

        currentSlide = (currentSlide + 1) % totalSlides;

        if (slides[currentSlide]) {
            slides[currentSlide].classList.replace('opacity-0', 'opacity-100');
            dots[currentSlide].classList.remove('bg-white', 'bg-opacity-60');
            dots[currentSlide].classList.add('bg-gold', 'scale-125');
        }
    }, 10000);
}