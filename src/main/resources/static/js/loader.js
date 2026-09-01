(function () {

    // 1. Loader Design (UNCHANGED)
    const loaderTemplate = `
        <style>
            body.loading-active { overflow: hidden !important; }

            #velora-loader-container {
                position: fixed;
                top: 0; left: 0;
                width: 100%;
                height: 100dvh;
                display: flex;
                justify-content: center;
                align-items: center;
                background-color: #121212;
                z-index: 2147483647;
                transition: opacity 0.5s ease-out;
                opacity: 1;
                pointer-events: all;
            }

            #velora-loader-container.fade-out {
                opacity: 0;
                pointer-events: none;
            }

            .velora-loader {
                font-family: 'Playfair Display', serif;
                font-size: clamp(40px, 12vw, 80px);
                font-weight: 900;
                letter-spacing: clamp(4px, 1.5vw, 8px);
                background: linear-gradient(#D4AF37 0 0) 0/0% no-repeat #333;
                -webkit-background-clip: text;
                background-clip: text;
                color: transparent;
                animation: l1 2s infinite linear;
                padding: 0 20px;
                text-align: center;
                white-space: nowrap;
                user-select: none;
            }

            @keyframes l1 {
                100% { background-size: 100% }
            }
        </style>

        <div id="velora-loader-container">
            <div class="velora-loader">VELORA</div>
        </div>
    `;

    // 2. Inject Loader (ONCE only)
    function injectLoader() {
        if (document.getElementById('velora-loader-container')) return;

        const wrapper = document.createElement('div');
        wrapper.innerHTML = loaderTemplate;

        if (document.body) {
            document.body.prepend(wrapper);
            document.body.classList.add('loading-active');
        } else {
            document.addEventListener('DOMContentLoaded', () => {
                document.body.prepend(wrapper);
                document.body.classList.add('loading-active');
            });
        }
    }

    // 3. Global Loader API
    window.loader = {

        show() {
            injectLoader();

            const loader = document.getElementById('velora-loader-container');
            if (!loader) return;

            loader.style.display = 'flex';
            void loader.offsetWidth; // force reflow
            loader.classList.remove('fade-out');
            loader.style.opacity = '1';
            document.body.classList.add('loading-active');
        },

        hide() {
            const loader = document.getElementById('velora-loader-container');
            if (!loader) return;

            loader.classList.add('fade-out');
            setTimeout(() => {
                loader.style.display = 'none';
                document.body.classList.remove('loading-active');
            }, 500);
        }
    };

    // 4. Backward compatibility
    window.showLoader = window.loader.show;
    window.hideLoader = window.loader.hide;

    // 5. Auto loader on page load (RESPECT redirect flag)
    if (!sessionStorage.getItem('skipNextLoader')) {
        injectLoader();
    } else {
        sessionStorage.removeItem('skipNextLoader');
    }

    // 6. Show loader on normal link navigation
    document.addEventListener('DOMContentLoaded', () => {
        document.body.addEventListener('click', (e) => {
            if (e.defaultPrevented) return;

            const link = e.target.closest('a[href]');
            if (!link) return;

            if (
                link.hasAttribute('data-no-loader') ||
                link.target ||
                link.href.startsWith('#') ||
                link.href.startsWith('javascript')
            ) return;

            window.loader.show();
        });
    });

})();
