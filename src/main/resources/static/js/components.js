// components.js

// Check if ComponentLoader is already defined to prevent redeclaration errors
if (typeof window.ComponentLoader === 'undefined') {

    window.ComponentLoader = class ComponentLoader {
        constructor() {
            this.loadedComponents = new Set();
            this.loadingPromises = new Map(); // ✅ ප්‍රහ්න මඟ හරින නව Promise පද්ධතිය
            this.initializationInProgress = new Set();
        }

        async loadComponent(componentPath, targetSelector) {
            // දැනටමත් සම්පූර්ණයෙන් ලෝඩ් වී ඇත්නම් වහාම ඉවත් වේ
            if (this.loadedComponents.has(componentPath)) return;

            // දැනටමත් ලෝඩ් වෙමින් පවතී නම්, එම ක්‍රියාවලිය (Promise) අවසන් වන තෙක් බලා සිටී
            if (this.loadingPromises.has(componentPath)) {
                return this.loadingPromises.get(componentPath);
            }

            const loadPromise = (async () => {
                try {
                    let targetElement = document.querySelector(targetSelector);
                    if (!targetElement) {
                        if (componentPath.includes('nav')) {
                            targetElement = document.createElement('div');
                            targetElement.id = 'navbar-container';
                            document.body.prepend(targetElement);
                        } else if (componentPath.includes('footer')) {
                            targetElement = document.createElement('div');
                            targetElement.id = 'footer-container';
                            document.body.appendChild(targetElement);
                        } else if (componentPath.includes('BackToTopComponent')) {
                            targetElement = document.createElement('div');
                            targetElement.id = 'backtotop-container';
                            document.body.appendChild(targetElement);
                        } else {
                            return;
                        }
                    }

                    const response = await fetch(componentPath);
                    if (!response.ok) throw new Error(`Failed to load component: ${componentPath}`);

                    const html = await response.text();
                    targetElement.innerHTML = html;
                    this.executeScripts(targetElement);

                    // ✅ දත්ත පිරවීම (Categories ආදිය) අවසන් වන තෙක් await කරයි
                    if (componentPath.includes('nav.html')) {
                        await this.initializeNavbarFunctionality();
                        document.dispatchEvent(new Event('navbar-loaded'));
                    } else if (componentPath.includes('footer.html')) {
                        await this.initializeFooterFunctionality();
                    }

                    this.loadedComponents.add(componentPath);
                } catch (error) {
                    console.error(`Error loading component ${componentPath}:`, error);
                } finally {
                    this.loadingPromises.delete(componentPath);
                }
            })();

            this.loadingPromises.set(componentPath, loadPromise);
            return loadPromise;
        }

        executeScripts(container) {
            const scripts = container.querySelectorAll('script');
            scripts.forEach(oldScript => {
                const newScript = document.createElement('script');
                Array.from(oldScript.attributes).forEach(attr => {
                    newScript.setAttribute(attr.name, attr.value);
                });
                newScript.textContent = oldScript.textContent;
                oldScript.parentNode.replaceChild(newScript, oldScript);
            });
        }

        async initializeNavbarFunctionality() {
            if (this.initializationInProgress.has('navbar')) return;
            this.initializationInProgress.add('navbar');

            await new Promise(resolve => setTimeout(resolve, 50));

            const searchBtn = document.getElementById('search-btn');
            if (searchBtn) {
                searchBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    if (typeof window.openSearchPanel === 'function') {
                        window.openSearchPanel();
                    }
                });
            }

            if (window.cartManager && typeof window.cartManager.loadData === 'function') {
                window.cartManager.loadData();
            }

            this.initializationInProgress.delete('navbar');
        }

        async initializeFooterFunctionality() {
            if (this.initializationInProgress.has('footer')) return;
            this.initializationInProgress.add('footer');

            await new Promise(resolve => setTimeout(resolve, 50));

            const copyrightElements = document.querySelectorAll('footer p');
            copyrightElements.forEach(element => {
                const currentYear = new Date().getFullYear();
                if (element.textContent.match(/202[0-9]/)) {
                    element.textContent = element.textContent.replace(/202[0-9]/, currentYear);
                }
            });

            const list = document.getElementById('footer-categories-list');
            if (list) {
                const fallbackHTML = `
                    <li><a href="/shop.html?category=rings" class="text-gray-300 hover:text-gold transition-colors duration-300 text-sm">Rings</a></li>
                    <li><a href="/shop.html?category=necklaces" class="text-gray-300 hover:text-gold transition-colors duration-300 text-sm">Necklaces</a></li>
                    <li><a href="/shop.html?category=earrings" class="text-gray-300 hover:text-gold transition-colors duration-300 text-sm">Earrings</a></li>
                    <li><a href="/shop.html?category=bracelets" class="text-gray-300 hover:text-gold transition-colors duration-300 text-sm">Bracelets</a></li>
                `;
                try {
                    // Try to fetch with a 5-second timeout
                    const controller = new AbortController();
                    const timeoutId = setTimeout(() => controller.abort(), 5000);
                    
                    const response = await fetch('/api/navigation/init', { signal: controller.signal });
                    clearTimeout(timeoutId);

                    if (response.ok) {
                        const data = await response.json();
                        if (data.categories && data.categories.length > 0) {
                            // ✅ ඔබේ මෝස්තරයට අනුව Categories නිවැරදිව පිරවීම
                            list.innerHTML = data.categories.map(catName => `
                                <li><a href="/shop.html?category=${encodeURIComponent(catName)}" 
                                    class="text-gray-300 hover:text-gold transition-colors duration-300 text-sm capitalize">${catName}</a></li>
                            `).join('');
                        } else {
                            list.innerHTML = fallbackHTML;
                        }
                    } else {
                        list.innerHTML = fallbackHTML;
                    }
                } catch (e) {
                    console.warn("Footer API failed or timed out, using fallback.");
                    list.innerHTML = fallbackHTML;
                }
            }
            this.initializationInProgress.delete('footer');
        }

        async loadNavbar(targetSelector = '#navbar-container') {
            return this.loadComponent('/components/nav.html', targetSelector);
        }
        async loadFooter(targetSelector = '#footer-container') {
            return this.loadComponent('/components/footer.html', targetSelector);
        }
        async loadBackToTop(targetSelector = '#backtotop-container') {
            return this.loadComponent('/components/BackToTopComponent.html', targetSelector);
        }

        async loadAllComponents() {
            // ✅ Navbar එක මඟ හැරීමට ඇති හැකියාව තහවුරු කරයි
            const tasks = [this.loadFooter(), this.loadBackToTop()];

            if (window.skipNavbarLoad !== true) {
                tasks.push(this.loadNavbar());
            }

            return Promise.all(tasks);
        }
    };

    const componentLoader = new window.ComponentLoader();
    window.componentLoader = componentLoader;

    // Automatic loading on page load
    document.addEventListener('DOMContentLoaded', () => {
        // ✅ පිටුවේ පවතින Nav එකක් තිබේ නම් Navbar එක ලෝඩ් නොකරයි
        const hasBuiltInNav = document.querySelector('nav');
        if (hasBuiltInNav) window.skipNavbarLoad = true;

        componentLoader.loadAllComponents();
    });

    if (typeof module !== 'undefined' && module.exports) {
        module.exports = window.ComponentLoader;
    }
}