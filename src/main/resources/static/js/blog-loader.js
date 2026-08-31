/**
 * Latest Blog Posts Loader for Rush Jewels
 * - Fetches snippets from public API
 * - Preserves exact article design and hover effects
 */

// ✅ යාවත්කාලීන කළා: Master Loader එකෙන් ඇමතීමට හැකි වන ලෙස window object එකට එක් කළා
window.loadLatestBlogs = async function() {
    const container = document.getElementById('latest-blog-container');
    if (!container) return false;

    try {
        const response = await fetch('/api/public/post/latest');
        if (!response.ok) throw new Error('Failed to fetch blogs');

        const blogs = await response.json();

        container.innerHTML = '';

        if (!blogs || blogs.length === 0) {
            container.innerHTML = '<p class="text-center col-span-full text-gray-500">No blog posts available yet.</p>';
            return true;
        }

        const blogHtml = blogs.map(blog => {
            const imageUrl = blog.imagePath || 'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=800&q=80';
            const category = blog.category || (blog.tags && blog.tags.length > 0 ? blog.tags[0] : "Tips & Care");
            const snippet = blog.content || '';

            return `
            <article class="bg-white shadow-lg overflow-hidden group hover:shadow-2xl transition-shadow duration-300 h-full flex flex-col">
                <div class="relative overflow-hidden h-64 shrink-0">
                    <img src="${imageUrl}"
                         alt="${blog.title}"
                         onerror="this.src='https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=800&q=80'"
                         class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" loading="lazy">
                    <div class="absolute top-4 left-4 bg-gold text-white px-3 py-1 text-xs font-medium uppercase tracking-wider">
                        ${category}
                    </div>
                </div>
                <div class="p-6 flex flex-col flex-grow">
                    <div class="flex items-center text-sm text-gray-500 mb-3">
                        <i class="far fa-calendar-alt mr-2"></i>
                        <span>${blog.date}</span>
                        <span class="mx-3">•</span>
                        <i class="far fa-clock mr-2"></i>
                        <span>${blog.readTime}</span>
                    </div>
                    <h3 class="text-xl font-playfair font-bold mb-3 text-dark group-hover:text-gold transition-colors">
                        ${blog.title}
                    </h3>
                    <p class="text-gray-600 mb-4 line-clamp-3 flex-grow">
                        ${snippet}
                    </p>
                    <a href="blog.html?id=${blog.id}" class="inline-flex items-center text-gold font-medium hover:text-dark transition-colors group mt-auto">
                        Read More
                        <i class="fas fa-arrow-right ml-2 transform group-hover:translate-x-2 transition-transform"></i>
                    </a>
                </div>
            </article>
            `;
        }).join('');

        container.innerHTML = blogHtml;
        return true;

    } catch (error) {
        console.error('Error loading blogs:', error);
        container.innerHTML = '<p class="text-center col-span-full text-red-500">Failed to load latest news.</p>';
        return false;
    }
};

// Fallback: Master Loader එක නැති තැන්වලදී (උදා: වෙනත් පිටුවක බ්ලොග් පෙන්වනවා නම්) වැඩ කිරීමට
document.addEventListener('DOMContentLoaded', () => {
    if (!window.loader) {
        window.loadLatestBlogs();
    }
});