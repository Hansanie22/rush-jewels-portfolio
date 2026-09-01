// Animation effects for Velora Fine Jewellery

document.addEventListener('DOMContentLoaded', function () {
    // Scroll reveal animation
    const scrollElements = document.querySelectorAll('.scroll-reveal');

    const elementInView = (el, dividend = 1) => {
        const elementTop = el.getBoundingClientRect().top;

        return (
            elementTop <= (window.innerHeight || document.documentElement.clientHeight) / dividend
        );
    };

    const displayScrollElement = (element) => {
        element.classList.add('active');
    };

    const handleScrollAnimation = () => {
        scrollElements.forEach((el) => {
            if (elementInView(el, 1.25)) {
                displayScrollElement(el);
            }
        });
    };

    // Initial check on page load
    handleScrollAnimation();

    // Throttle scroll event
    let throttleTimer;

    const throttle = (callback, time) => {
        if (throttleTimer) return;

        throttleTimer = true;
        setTimeout(() => {
            callback();
            throttleTimer = false;
        }, time);
    };

    // Add scroll event listener
    window.addEventListener('scroll', () => {
        throttle(handleScrollAnimation, 250);
    });

    // Testimonial cards animation
    const testimonialCards = document.querySelectorAll('.testimonial-card');

    const animateTestimonials = () => {
        testimonialCards.forEach((card, index) => {
            setTimeout(() => {
                if (elementInView(card, 1.2)) {
                    card.classList.add('active');
                }
            }, index * 200);
        });
    };

    // Initial check on page load
    animateTestimonials();

    // Add scroll event listener for testimonials
    window.addEventListener('scroll', () => {
        throttle(animateTestimonials, 250);
    });

    // Newsletter input animation
    const newsletterInput = document.querySelector('input[type="email"]');

    if (newsletterInput) {
        newsletterInput.addEventListener('focus', function () {
            this.classList.add('newsletter-input');
        });

        newsletterInput.addEventListener('blur', function () {
            this.classList.remove('newsletter-input');
        });
    }

    // Hero text animation
    const heroText = document.querySelector('.animate__animated');

    if (heroText) {
        heroText.addEventListener('animationend', function () {
            // Add additional animations if needed
        });
    }

    // Button hover glow effect
    const buttons = document.querySelectorAll('button');

    buttons.forEach(button => {
        button.addEventListener('mouseenter', function () {
            this.classList.add('btn-glow');
        });

        button.addEventListener('mouseleave', function () {
            this.classList.remove('btn-glow');
        });
    });

    // Product card hover effect
    const productCards = document.querySelectorAll('.product-card');

    productCards.forEach(card => {
        card.addEventListener('mouseenter', function () {
            const img = this.querySelector('img');
            if (img) {
                img.style.transform = 'scale(1.1)';
            }
        });

        card.addEventListener('mouseleave', function () {
            const img = this.querySelector('img');
            if (img) {
                img.style.transform = 'scale(1)';
            }
        });
    });

    // ... (lines 122-125)
    // Smooth scroll for anchor links
    const anchorLinks = document.querySelectorAll('a[href^="#"]');

    anchorLinks.forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();

            const targetId = this.getAttribute('href');

            if (targetId === '#' || targetId.length < 2) {
                return;
            }
            const targetElement = document.querySelector(targetId); // Line ~131

            if (targetElement) {
                window.scrollTo({
                    top: targetElement.offsetTop - 80,
                    behavior: 'smooth'
                });
            }
        });
    });

    // Parallax effect for hero section
    const heroSection = document.querySelector('.relative.h-screen');

    if (heroSection) {
        window.addEventListener('scroll', () => {
            const scrolled = window.pageYOffset;
            const parallax = document.querySelector('.relative.h-screen > div:first-child');

            if (parallax) {
                parallax.style.transform = `translateY(${scrolled * 0.5}px)`;
            }
        });
    }
});