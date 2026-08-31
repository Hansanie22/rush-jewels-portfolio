// ==========================================
// DASHBOARD MANAGEMENT LOGIC
// ==========================================

// 1. Load Dashboard Data (Stats + Chart + Alerts)
async function loadDashboardData() {
    const loader = document.getElementById('dashboard-loader');
    if(loader) {
        loader.classList.remove('hidden');
        loader.style.opacity = '1';
    }

    try {
        await Promise.all([
            loadDashboardStats(),
            updateDashboardChart('daily'),
            checkStockAlerts()
        ]);
    } catch (error) {
        console.error("Dashboard Load Error:", error);
    } finally {
        if(loader) {
            loader.style.opacity = '0';
            setTimeout(() => loader.classList.add('hidden'), 500); // Wait for transition
        }
    }
}

// 2. Load Key Statistics
async function loadDashboardStats() {
    try {
        const response = await fetch('/api/admin/dashboard/stats');
        if (!response.ok) throw new Error("Failed to load stats");
        const stats = await response.json();

        document.getElementById('dash-revenue').innerText = formatCompactCurrency(stats.totalRevenue);
        document.getElementById('dash-orders').innerText = stats.totalOrders.toLocaleString();
        document.getElementById('dash-pending').innerText = `${stats.pendingOrders} New Orders`;
        document.getElementById('dash-customers').innerText = stats.totalCustomers.toLocaleString();
        document.getElementById('dash-new-customers').innerText = `+${stats.newCustomersThisMonth} New this month`;
        document.getElementById('dash-avg').innerText = formatCompactCurrency(stats.averageOrderValue);

    } catch (err) {
        console.error(err);
    }
}

// 3. Load & Render Chart
async function updateDashboardChart(filterType) {
    document.querySelectorAll('.chart-filter-btn').forEach(btn => {
        if (btn.innerText.toLowerCase() === filterType) {
            btn.className = "chart-filter-btn px-3 py-1 text-xs font-bold uppercase bg-black text-gold-400 transition";
        } else {
            btn.className = "chart-filter-btn px-3 py-1 text-xs font-bold uppercase bg-gray-100 text-gray-600 hover:bg-gray-200 transition";
        }
    });

    try {
        const response = await fetch(`/api/admin/dashboard/chart?filter=${filterType}`);
        if (!response.ok) throw new Error("Chart data failed");
        const data = await response.json();
        renderCustomChart(data.labels, data.data);
    } catch (err) {
        console.error(err);
        const chartContainer = document.getElementById('chart-bars');
        if(chartContainer) chartContainer.innerHTML = '<p class="w-full text-center text-xs text-red-500 self-center">Error loading chart</p>';
    }
}

let dashboardChartInstance = null;

function renderCustomChart(labels, data) {
    const canvas = document.getElementById('premiumChart');
    if (!canvas) return;

    if (dashboardChartInstance) {
        dashboardChartInstance.destroy();
    }

    if (!data || data.length === 0 || data.every(v => v === 0)) {
        const ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.font = '12px Montserrat';
        ctx.fillStyle = '#9ca3af';
        ctx.textAlign = 'center';
        ctx.fillText('No sales data available yet.', canvas.width / 2, canvas.height / 2);
        return;
    }

    const ctx = canvas.getContext('2d');
    dashboardChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Revenue (LKR)',
                data: data,
                backgroundColor: '#111111',
                hoverBackgroundColor: '#d4af37',
                borderRadius: 4,
                borderSkipped: false,
                barPercentage: 0.6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: 'rgba(0,0,0,0.9)',
                    titleFont: { family: 'Montserrat', size: 13 },
                    bodyFont: { family: 'Montserrat', size: 14, weight: 'bold' },
                    callbacks: {
                        label: function(context) {
                            return 'LKR ' + context.parsed.y.toLocaleString();
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: '#f3f4f6', drawBorder: false },
                    ticks: {
                        font: { family: 'Montserrat', size: 10 },
                        color: '#6b7280',
                        callback: function(value) {
                            if (value >= 1000000) return (value / 1000000).toFixed(1) + 'M';
                            if (value >= 1000) return (value / 1000).toFixed(1) + 'K';
                            return value;
                        }
                    }
                },
                x: {
                    grid: { display: false, drawBorder: false },
                    ticks: { font: { family: 'Montserrat', size: 10 }, color: '#6b7280' }
                }
            }
        }
    });
}

// 5. Stock Alert Logic (Pop Up)
async function checkStockAlerts() {
    try {
        const response = await fetch('/api/admin/dashboard/alerts');
        if (!response.ok) return;
        const alerts = await response.json();

        if (alerts && alerts.length > 0) {
            const tbody = document.getElementById('stock-alert-body');
            tbody.innerHTML = '';

            alerts.forEach(item => {
                const isOut = item.status === "Out of Stock";
                const statusBadge = isOut
                    ? `<span class="bg-red-100 text-red-800 text-xs font-bold px-2 py-1 rounded">Out of Stock</span>`
                    : `<span class="bg-yellow-100 text-yellow-800 text-xs font-bold px-2 py-1 rounded">Low Stock</span>`;

                // ✅ Added Warehouse Badge Logic
                const warehouseBadge = `<span class="bg-blue-50 text-blue-700 border border-blue-200 text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider">${item.warehouse}</span>`;

                const row = document.createElement('tr');
                row.className = "border-b hover:bg-gray-50 transition";
                row.innerHTML = `
                    <td class="p-3 font-medium text-gray-800">${item.productName}</td>
                    <td class="p-3 text-center text-gray-500 text-xs">${item.type}</td>
                    <td class="p-3 text-center">${warehouseBadge}</td>
                    <td class="p-3 text-center">${statusBadge}</td>
                    <td class="p-3 text-right font-bold ${isOut ? 'text-red-600' : 'text-gray-700'}">${item.qty}</td>
                `;
                tbody.appendChild(row);
            });

            const modal = document.getElementById('stock-alert-modal');
            const content = document.getElementById('stock-modal-content');

            modal.classList.remove('hidden');
            setTimeout(() => {
                modal.classList.remove('opacity-0');
                content.classList.remove('scale-95');
                content.classList.add('scale-100');
            }, 10);
        }
    } catch (err) {
        console.error("Alert Check Error:", err);
    }
}

function closeStockModal() {
    const modal = document.getElementById('stock-alert-modal');
    const content = document.getElementById('stock-modal-content');

    modal.classList.add('opacity-0');
    content.classList.remove('scale-100');
    content.classList.add('scale-95');

    setTimeout(() => {
        modal.classList.add('hidden');
    }, 300);
}

// Helper: Format Currency
function formatCompactCurrency(num) {
    return 'LKR ' + new Intl.NumberFormat('en-US', {
        notation: "compact",
        maximumFractionDigits: 1
    }).format(num);
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();
});