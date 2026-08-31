// ==========================================
// MARKETING DASHBOARD LOGIC
// ==========================================

async function loadMarketingDashboard() {
    loadMarketingStats();
    loadMarketingChart();
}

// 1. Load Stats
async function loadMarketingStats() {
    try {
        const response = await fetch('/api/admin/marketing/stats');
        if (!response.ok) throw new Error('Failed to fetch stats');

        const stats = await response.json();

        animateValue("mkt-sub-rate", 0, stats.subscriberRate, 1000, "%");
        animateValue("mkt-usage", 0, stats.couponUsageRate, 1000, "%");
        document.getElementById('mkt-revenue').innerText = formatCurrencyCompact(stats.attributedRevenue);

    } catch (err) {
        console.error(err);
    }
}

// 2. Load Chart
async function loadMarketingChart() {
    try {
        const response = await fetch('/api/admin/marketing/chart');
        if (!response.ok) throw new Error("Chart failed");
        const data = await response.json();

        renderMarketingChart(data.labels, data.data);
    } catch (err) {
        console.error(err);
        document.getElementById('mkt-chart-bars').innerHTML = '<p class="text-xs text-red-400 self-center w-full text-center">Failed to load chart</p>';
    }
}

// 3. Render CSS Chart
function renderMarketingChart(labels, data) {
    const container = document.getElementById('mkt-chart-bars');
    container.innerHTML = '';

    if (!data || data.length === 0 || data.every(v => v === 0)) {
        container.innerHTML = '<p class="text-xs text-gray-400 self-center w-full text-center">No revenue data available</p>';
        return;
    }

    const maxVal = Math.max(...data) || 1;

    data.forEach((val, index) => {
        const heightPct = (val / maxVal) * 100;
        const label = labels[index];
        const tooltipVal = formatCurrencyCompact(val);

        const bar = document.createElement('div');
        bar.className = "flex-1 flex flex-col items-center justify-end group relative h-full";

        // Dark bar with gold hover
        bar.innerHTML = `
            <div class="w-full max-w-[40px] bg-gray-800 hover:bg-gold-500 transition-all duration-500 rounded-t relative cursor-pointer" 
                 style="height: ${heightPct}%;">
                
                <div class="absolute -top-10 left-1/2 transform -translate-x-1/2 bg-black text-white text-[10px] font-bold px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition pointer-events-none whitespace-nowrap z-20 shadow-lg">
                    ${tooltipVal}
                    <div class="absolute top-full left-1/2 transform -translate-x-1/2 -mt-1 border-4 border-transparent border-t-black"></div>
                </div>
            </div>
            <span class="text-[10px] text-gray-500 mt-2 font-mono w-full text-center truncate">${label}</span>
        `;
        container.appendChild(bar);
    });
}

// Helper: Format Currency
function formatCurrencyCompact(num) {
    return 'LKR ' + new Intl.NumberFormat('en-US', {
        notation: "compact",
        maximumFractionDigits: 1
    }).format(num);
}

// Helper: Animation
function animateValue(id, start, end, duration, suffix = "") {
    const obj = document.getElementById(id);
    if(!obj) return;

    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        const value = Math.floor(progress * (end - start) + start);
        obj.innerHTML = value + suffix;
        if (progress < 1) {
            window.requestAnimationFrame(step);
        } else {
            obj.innerHTML = end.toFixed(1) + suffix;
        }
    };
    window.requestAnimationFrame(step);
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadMarketingDashboard();
});