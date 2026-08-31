/**
 * Drag-to-Scroll Utility
 * Enables smooth horizontal scrolling via mouse drag on desktop for .products-scroll containers
 */
document.addEventListener('DOMContentLoaded', () => {
    // Select all scrollable containers we want to apply this to
    const sliders = document.querySelectorAll('.products-scroll, .overflow-x-auto');
    
    let isDown = false;
    let startX;
    let scrollLeft;

    sliders.forEach(slider => {
        // Change cursor to indicate grab ability
        slider.style.cursor = 'grab';

        slider.addEventListener('mousedown', (e) => {
            isDown = true;
            slider.style.cursor = 'grabbing';
            startX = e.pageX - slider.offsetLeft;
            scrollLeft = slider.scrollLeft;
        });

        slider.addEventListener('mouseleave', () => {
            isDown = false;
            slider.style.cursor = 'grab';
        });

        slider.addEventListener('mouseup', () => {
            isDown = false;
            slider.style.cursor = 'grab';
        });

        slider.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault(); // Stop text selection
            const x = e.pageX - slider.offsetLeft;
            const walk = (x - startX) * 2; // The multiplier determines scroll speed
            slider.scrollLeft = scrollLeft - walk;
        });
    });
});
