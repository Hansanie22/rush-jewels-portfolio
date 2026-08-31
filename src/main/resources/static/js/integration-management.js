// ==========================================
// INTEGRATION MANAGEMENT LOGIC
// ==========================================

async function loadIntegrations() {
    try {
        const response = await fetch('/api/admin/integrations');
        if (!response.ok) throw new Error('Failed to fetch integrations');

        const integrations = await response.json();
        renderIntegrations(integrations);

    } catch (err) {
        console.error(err);
        if(window.showToast) showToast('Error loading integrations', 'error');
    }
}

function renderIntegrations(data) {
    const container = document.getElementById('integrations-grid');
    if (!container) return;

    if (data.length === 0) {
        container.innerHTML = '<p class="col-span-3 text-center text-gray-400 py-10">No integrations available.</p>';
        return;
    }

    container.innerHTML = data.map(item => {
        const btnClass = item.connected
            ? "bg-green-50 text-green-600 border-green-200 hover:bg-red-50 hover:text-red-600 hover:border-red-200"
            : "bg-white text-gray-600 border-gray-300 hover:bg-gray-50";

        const btnText = item.connected ? "Connected" : "Connect";
        const statusIcon = item.connected ? '<i class="fas fa-check-circle ml-1"></i>' : '';

        // If connected, click -> disconnect. If disconnected, click -> modal.
        const action = item.connected ? `disconnectIntegration(${item.id})` : `openConnectModal(${item.id}, '${item.name}')`;

        return `
            <div class="bg-white border border-gray-200 p-6 text-center shadow-sm hover:shadow-md transition-shadow">
                <i class="${item.iconClass} text-4xl mb-4"></i>
                <h4 class="font-bold text-gray-800 text-sm uppercase mb-4">${item.name}</h4>
                
                <div class="text-xs text-gray-400 mb-4 font-mono truncate px-4">
                    ${item.connected ? (item.apiKey ? 'Key: ••••••••' + item.apiKey.slice(-4) : 'Active') : 'Not Connected'}
                </div>

                <button onclick="${action}" 
                        class="mt-2 border w-full py-2 px-4 text-xs font-bold uppercase rounded transition-all ${btnClass}">
                    ${btnText} ${statusIcon}
                </button>
            </div>
        `;
    }).join('');
}

// --- MODAL LOGIC ---

function openConnectModal(id, name) {
    document.getElementById('connect-id').value = id;
    document.getElementById('connect-key').value = '';

    const label = document.getElementById('connect-label');
    if(name.toLowerCase().includes('meta')) label.innerText = 'Meta Pixel ID';
    else if(name.toLowerCase().includes('google')) label.innerText = 'Measurement ID (G-XXXX)';
    else label.innerText = 'API Access Token';

    const modal = document.getElementById('connect-integration-modal');
    modal.classList.remove('hidden');
    modal.classList.add('flex');
}

function closeConnectModal() {
    const modal = document.getElementById('connect-integration-modal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
}

async function submitConnection() {
    const id = document.getElementById('connect-id').value;
    const key = document.getElementById('connect-key').value;

    // Key is optional for some services, but recommended
    await updateStatus(id, key);
    closeConnectModal();
}

async function disconnectIntegration(id) {
    // Use custom confirm if available, or native
    if(window.showConfirm) {
        window.showConfirm("Disconnect this service?", () => updateStatus(id, null));
    } else if(confirm('Disconnect this service?')) {
        await updateStatus(id, null);
    }
}

// Core API Call
async function updateStatus(id, key) {
    try {
        const response = await fetch(`/api/admin/integrations/${id}/toggle`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ apiKey: key })
        });

        // ✅ FIX: Read the actual error message from the server
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Update failed');
        }

        const updated = await response.json();

        if(window.showToast) {
            const msg = updated.connected ? 'Integration Connected' : 'Integration Disconnected';
            showToast(msg, updated.connected ? 'success' : 'info');
        }

        loadIntegrations();

    } catch (err) {
        console.error(err);
        // Show the specific error (e.g., "Column 'api_key' cannot be null")
        if(window.showToast) showToast(err.message, 'error');
    }
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    loadIntegrations();
});
