export default function Notification(options = {}) {
    let opts = {};
    const defDuration = 3000;
    const allowedPosition = ['top-right', 'top-left', 'bottom-right', 'bottom-left', 'center'];
    const defaultOpts = {
        position: 'bottom-right',
        duration: defDuration,
        hidePrevious: true,
        maxVisible: 5,
    };

    // --- FIX: Inject High Z-Index Styles ---
    const styleId = 'velora-notification-fix';
    if (!document.getElementById(styleId)) {
        const style = document.createElement('style');
        style.id = styleId;
        style.textContent = `
            .tw-notification-container {
                z-index: 2147483647 !important; /* Maximum possible Z-Index */
                position: fixed;
                pointer-events: none;
                display: flex;
                flex-direction: column;
                gap: 0.75rem;
            }
            .tw-notification {
                pointer-events: auto; /* Allow clicking on the notification */
            }
        `;
        document.head.appendChild(style);
    }

    // Merge and validate user options
    const setProperty = (obj = {}) => {
        opts = Object.assign({}, defaultOpts, obj);
        if (!allowedPosition.includes(opts.position)) opts.position = defaultOpts.position;
        opts.duration = parseInt(opts.duration);
        if (isNaN(opts.duration) || opts.duration < 1000) opts.duration = defDuration;
        opts.maxVisible = parseInt(opts.maxVisible);
        if (isNaN(opts.maxVisible) || opts.maxVisible < 1) opts.maxVisible = defaultOpts.maxVisible;
    };
    setProperty(options);

    // Internal state
    const classContainer = 'tw-notification-container';
    const classPopup = 'tw-notification';

    // Professional Elegant Theme: Gold (#D4AF37) and White
    // Error uses Red (#991B1B) and White
    const types = {
        info:       'bg-[#C49C5B] text-white shadow-[#C49C5B]/40',
        success:    'bg-[#C49C5B] text-white shadow-[#C49C5B]/40',
        warning:    'bg-[#C49C5B] text-white shadow-[#C49C5B]/40',
        validation: 'bg-[#C49C5B] text-white shadow-[#C49C5B]/40',
        // Error keeps the Red color for alerts
        error:      'bg-[#991B1B] text-white shadow-red-900/40',
    };

    const getPositionClass = (position) => {
        switch (position) {
            case 'top-right':
                return 'top-6 right-6 items-end';
            case 'top-left':
                return 'top-6 left-6 items-start';
            case 'bottom-right':
                return 'bottom-6 right-6 items-end';
            case 'bottom-left':
                return 'bottom-6 left-6 items-start';
            case 'center':
                return 'inset-0 flex items-center justify-center';
            default:
                return 'bottom-6 right-6';
        }
    };

    const createContainer = () => {
        let container = document.querySelector(`.${classContainer}.${opts.position.replace(/\s/g, '.')}`);
        if (!container) {
            container = document.createElement('div');
            // Added the injected classContainer and kept your Tailwind classes
            // Removed z-[9999] here because the injected CSS takes care of it more forcefully
            container.className = `${classContainer} ${opts.position} ${getPositionClass(opts.position)}`;
            document.body.appendChild(container);
        }
        return container;
    };

    const createPopup = (type, message) => {
        const container = createContainer();

        if (container.childElementCount >= opts.maxVisible) {
            container.firstChild?.remove();
        }

        const popup = document.createElement('div');

        // Updated classes for a more elegant, professional card look
        // Added 'pointer-events-auto' so clicking the notification works if needed, while container is none
        // Added font-serif or tracking-wide for elegance if desired, strictly keeping standard font here but cleaner spacing
        popup.className = `${classPopup} pointer-events-auto min-w-[300px] max-w-md px-6 py-4 text-sm font-medium shadow-xl transform transition-all duration-500 ease-out opacity-0 translate-y-4 select-none ${types[type] || types.info}`;

        // Sharp corners as requested in original code logic, but cleaner execution
        popup.style.borderRadius = '0';

        // Removed the close button as requested
        popup.innerHTML = `
            <div class="flex items-center justify-start gap-3">
                <span class="leading-relaxed tracking-wide">${message}</span>
            </div>
        `;

        // Add to container
        if (opts.position.includes('bottom')) container.prepend(popup);
        else container.appendChild(popup);

        // Animation in
        requestAnimationFrame(() => {
            popup.classList.remove('opacity-0', 'translate-y-4');
            popup.classList.add('opacity-100', 'translate-y-0');
        });

        // Auto-hide is now the only way to close
        if (opts.duration > 0) {
            setTimeout(() => removePopup(popup), opts.duration);
        }
        return popup;
    };

    const removePopup = (popup) => {
        if (!popup) return;
        popup.classList.remove('opacity-100', 'translate-y-0');
        popup.classList.add('opacity-0', '-translate-y-2');
        setTimeout(() => popup.remove(), 500); // Wait for transition
    };

    const show = (type, message) => {
        if (opts.hidePrevious) {
            document.querySelectorAll(`.${classPopup}`).forEach(n => removePopup(n));
        }
        createPopup(type, message);
    };

    // Confirmation Dialog - Updated to Gold/White Theme
    const confirm = (message, onConfirm, onCancel) => {
        const overlay = document.createElement('div');
        // Updated Z-Index to MAX here as well
        overlay.className = 'fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center transition-opacity duration-300 opacity-0';
        overlay.style.zIndex = '2147483647'; // Force on top

        const dialog = document.createElement('div');
        // Elegant White Box with Gold Border (Updated Color)
        dialog.className = 'bg-white text-[#4a4a4a] border-t-4 border-[#C49C5B] p-8 min-w-[320px] shadow-2xl transform scale-95 transition-transform duration-300';
        dialog.style.borderRadius = '0';

        dialog.innerHTML = `
        <div class="mb-6 text-center text-lg font-medium tracking-wide text-black">${message}</div>
        <div class="flex justify-center gap-4">
            <button class="ok-btn bg-[#C49C5B] text-white px-6 py-2 text-sm font-bold uppercase tracking-wider hover:bg-[#B38D50] transition-colors duration-200 shadow-md">OK</button>
            <button class="cancel-btn bg-white text-[#C49C5B] border border-[#C49C5B] px-6 py-2 text-sm font-bold uppercase tracking-wider hover:bg-[#FFFDF5] transition-colors duration-200">Cancel</button>
        </div>
    `;

        overlay.appendChild(dialog);
        document.body.appendChild(overlay);

        // Animate In
        requestAnimationFrame(() => {
            overlay.classList.remove('opacity-0');
            dialog.classList.remove('scale-95');
            dialog.classList.add('scale-100');
        });

        const cleanup = () => {
            overlay.classList.add('opacity-0');
            dialog.classList.remove('scale-100');
            dialog.classList.add('scale-95');
            setTimeout(() => overlay.remove(), 300);
        };

        dialog.querySelector('.ok-btn').addEventListener('click', () => {
            cleanup();
            if (typeof onConfirm === 'function') onConfirm();
        });
        dialog.querySelector('.cancel-btn').addEventListener('click', () => {
            cleanup();
            if (typeof onCancel === 'function') onCancel();
        });
    };
    // API
    const info = (msg) => show('info', msg);
    const success = (msg) => show('success', msg);
    const warning = (msg) => show('warning', msg);
    const error = (msg) => show('error', msg);
    const validation = (msg) => show('validation', msg);
    const hide = () => document.querySelectorAll(`.${classPopup}`).forEach(n => removePopup(n));

    return {info, success, warning, error, validation, hide, confirm, setProperty};
}