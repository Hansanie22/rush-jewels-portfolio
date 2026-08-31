// ==========================================
// SUPPORT TICKET LOGIC (ADMIN SIDE)
// ==========================================

let currentOpenTicketId = null;

/**
 * Checks if a ticket has any UNREAD messages from the USER.
 */
function isTicketUnreadForAdmin(ticket) {
    if (!ticket || !ticket.messages || !Array.isArray(ticket.messages) || ticket.messages.length === 0) {
        return false;
    }
    return ticket.messages.some(m => {
        const sender = m.senderType || m.sender;
        return sender === 'USER' && m.isRead === false;
    });
}

/**
 * Helper: Toggles the reply form state based on ticket status.
 * Disables inputs if status is SOLVED or CLOSED.
 */
function toggleReplyFormState(status) {
    const modal = document.getElementById('ticket-modal');
    if (!modal) return;

    const replyBtn = modal.querySelector('button[type="submit"]');
    const replyInput = document.getElementById('ticket-reply-msg');
    const replyFile = document.getElementById('ticket-reply-file');

    // Check if status allows replying (Only 'OPEN' allows replies)
    const isEditable = (status === 'OPEN');

    if (replyBtn) {
        if (!isEditable) {
            replyBtn.disabled = true;
            replyBtn.classList.add('opacity-50', 'cursor-not-allowed');
            replyBtn.innerText = "Ticket Closed";
        } else {
            replyBtn.disabled = false;
            replyBtn.classList.remove('opacity-50', 'cursor-not-allowed');
            replyBtn.innerText = "Send Reply";
        }
    }

    if (replyInput) {
        if (!isEditable) {
            replyInput.disabled = true;
            replyInput.classList.add('bg-gray-100', 'cursor-not-allowed');
            replyInput.placeholder = `This ticket is ${status}. Change status to OPEN to reply.`;
        } else {
            replyInput.disabled = false;
            replyInput.classList.remove('bg-gray-100', 'cursor-not-allowed');
            replyInput.placeholder = "Type your reply here...";
        }
    }

    if (replyFile) {
        replyFile.disabled = !isEditable;
    }
}

// 1. Check for unread support messages & Update UI Badges
window.checkUnreadSupportMessages = async function() {
    try {
        const response = await fetch('/api/admin/support');
        if (!response.ok) return;

        const tickets = await response.json();
        let unreadTicketCount = 0;

        tickets.forEach(t => {
            if (isTicketUnreadForAdmin(t)) {
                unreadTicketCount++;
            }
        });

        // Update Sidebar Gold Dot
        const sidebarDot = document.getElementById('sidebar-support-dot');
        if (sidebarDot) {
            if (unreadTicketCount > 0) {
                sidebarDot.classList.remove('hidden');
                sidebarDot.classList.add('bg-yellow-500');
            } else {
                sidebarDot.classList.add('hidden');
            }
        }

        // Update Section Red Badge
        const badge = document.getElementById('admin-support-badge');
        if (badge) {
            if (unreadTicketCount > 0) {
                badge.innerText = unreadTicketCount;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        }

    } catch(e) {
        console.error('[Support] Failed to check unread messages:', e);
    }
};

// 2. Load and Render Ticket List
async function loadSupportTickets() {
    try {
        const response = await fetch('/api/admin/support');
        const tickets = await response.json();

        const tbody = document.getElementById('support-body');
        if (!tbody) return;

        if(tickets.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="px-6 py-8 text-center text-gray-500">No support tickets found.</td></tr>';
            window.checkUnreadSupportMessages();
            return;
        }

        tbody.innerHTML = tickets.map(t => {
            let statusColor = 'bg-yellow-100 text-yellow-800';
            if(t.status === 'SOLVED') statusColor = 'bg-green-100 text-green-800';
            if(t.status === 'CLOSED') statusColor = 'bg-gray-200 text-gray-800';

            const isSolved = (t.status === 'SOLVED' || t.status === 'CLOSED');
            const isUnread = isTicketUnreadForAdmin(t);

            const rowClass = isUnread
                ? 'bg-white font-bold text-gray-900 border-l-4 border-l-red-500 shadow-sm'
                : 'hover:bg-gray-50 border-b border-gray-100 text-gray-600';

            const rowId = `ticket-row-${t.id}`;

            const idDisplay = isUnread
                ? `<div class="flex items-center gap-2">
                     <span class="w-2 h-2 rounded-full bg-red-600 animate-pulse"></span>
                     <span>#${t.id}</span>
                   </div>`
                : `#${t.id}`;

            return `
            <tr id="${rowId}" class="${rowClass} transition-all duration-200 cursor-pointer" onclick="openTicketModal(${t.id})">
                <td class="px-6 py-4 font-mono text-xs">${idDisplay}</td>
                <td class="px-6 py-4 text-sm">${t.subject}</td>
                <td class="px-6 py-4 text-xs">${t.customerName}</td>
                <td class="px-6 py-4 text-xs text-gray-400">${new Date(t.lastUpdated).toLocaleDateString()}</td>
                <td class="px-6 py-4"><span class="px-2 py-1 rounded text-[10px] font-bold uppercase ${statusColor}">${t.status}</span></td>
                <td class="px-6 py-4 text-right">
                    <button class="text-blue-600 hover:text-blue-800 text-xs font-bold uppercase">
                        ${isSolved ? 'View' : 'Reply'}
                    </button>
                </td>
            </tr>`;
        }).join('');

        window.checkUnreadSupportMessages();

    } catch(e) { console.error(e); }
}

// 3. Open Modal & Mark as Read
async function openTicketModal(id) {
    currentOpenTicketId = id;

    // Optimistic UI Update
    const row = document.getElementById(`ticket-row-${id}`);
    if (row) {
        row.className = 'hover:bg-gray-50 border-b border-gray-100 text-gray-600 transition-all duration-200';
        const idCell = row.querySelector('td:first-child');
        if (idCell) idCell.innerHTML = `#${id}`;
    }

    try {
        const response = await fetch(`/api/admin/support/${id}`);
        if (!response.ok) throw new Error("Failed to fetch ticket");

        const t = await response.json();

        document.getElementById('current-ticket-id').value = t.id;
        document.getElementById('ticket-subject').innerText = t.subject;
        document.getElementById('ticket-customer').innerText = t.customerName;
        document.getElementById('ticket-email').innerText = t.customerEmail;
        document.getElementById('ticket-contact').innerText = t.customerContact || 'N/A';

        // Set Status
        const statusSelect = document.getElementById('ticket-status-select');
        statusSelect.value = t.status;

        // --- DISABLE/ENABLE FORM BASED ON STATUS ---
        toggleReplyFormState(t.status);

        const msgContainer = document.getElementById('ticket-messages');
        msgContainer.innerHTML = t.messages.map(m => {
            const isAdmin = (m.senderType === 'ADMIN' || m.sender === 'ADMIN');
            const align = isAdmin ? 'items-end' : 'items-start';
            const bg = isAdmin ? 'bg-blue-50 border-blue-100' : 'bg-white border-gray-200';
            const senderName = isAdmin ? 'Support Agent' : t.customerName;

            let mediaHtml = '';
            const attachUrl = m.attachmentUrl || m.attachment;

            if(attachUrl) {
                if(attachUrl.match(/\.(jpeg|jpg|gif|png)$/i)) {
                    mediaHtml = `<img src="${attachUrl}" class="mt-2 max-w-xs rounded border border-gray-200 cursor-pointer hover:opacity-90" onclick="window.open('${attachUrl}')" loading="lazy">`;
                } else {
                    mediaHtml = `<a href="${attachUrl}" target="_blank" class="mt-2 text-xs text-blue-600 flex items-center gap-1 hover:underline"><i class="fas fa-paperclip"></i> View Attachment</a>`;
                }
            }

            return `
                <div class="flex flex-col ${align} mb-4">
                    <div class="max-w-[85%] ${bg} border p-3 rounded-lg shadow-sm">
                        <div class="flex justify-between items-center mb-1 gap-4">
                            <span class="text-xs font-bold uppercase text-gray-600">${senderName}</span>
                            <span class="text-[10px] text-gray-400">${m.timestamp || m.time}</span>
                        </div>
                        <p class="text-sm text-gray-800 whitespace-pre-wrap leading-relaxed">${m.message}</p>
                        ${mediaHtml}
                    </div>
                </div>
            `;
        }).join('');

        const modal = document.getElementById('ticket-modal');
        modal.classList.remove('hidden');
        modal.classList.add('flex');

        setTimeout(() => {
            msgContainer.scrollTop = msgContainer.scrollHeight;
        }, 100);

        await window.checkUnreadSupportMessages();

    } catch(e) {
        console.error(e);
        if(window.showToast) showToast("Error loading ticket", "error");
    }
}

// 4. Close Modal
function closeTicketModal() {
    currentOpenTicketId = null;
    const modal = document.getElementById('ticket-modal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
    loadSupportTickets();
}

// 5. Handle Reply
async function handleTicketReply(e) {
    e.preventDefault();
    const id = document.getElementById('current-ticket-id').value;
    const msg = document.getElementById('ticket-reply-msg').value.trim();
    const fileInput = document.getElementById('ticket-reply-file');
    const file = fileInput.files[0];

    // Double check status before sending (frontend guard)
    const statusSelect = document.getElementById('ticket-status-select');
    if (statusSelect.value !== 'OPEN') {
        if (window.showToast) showToast("Ticket is closed. Cannot reply.", "error");
        return;
    }

    if (!id || (!msg && !file)) return;

    const btn = document.querySelector('#ticket-modal button[type="submit"]');
    const originalText = btn.innerText;
    btn.innerText = "Sending...";
    btn.disabled = true;

    const formData = new FormData();
    if (msg) formData.append("message", msg);
    if (file) formData.append("file", file);

    try {
        const response = await fetch(`/api/admin/support/${id}/reply`, {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            document.getElementById('ticket-reply-msg').value = '';
            fileInput.value = '';
            const fileNameDisplay = document.getElementById('file-name-display');
            if(fileNameDisplay) fileNameDisplay.innerText = '';

            openTicketModal(id);
            if (window.showToast) showToast("Reply Sent", "success");
        } else {
            if (window.showToast) showToast("Failed to send reply", "error");
        }
    } catch (err) { console.error(err); }
    finally { btn.innerText = originalText; btn.disabled = false; }
}

// 6. Update Status
async function updateTicketStatus() {
    const id = document.getElementById('current-ticket-id').value;
    const status = document.getElementById('ticket-status-select').value;

    // --- IMMEDIATELY TOGGLE FORM STATE ---
    // This allows admin to unlock the form instantly by selecting 'OPEN'
    toggleReplyFormState(status);

    try {
        await fetch(`/api/admin/support/${id}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        if(window.showToast) showToast("Status Updated", "success");
        loadSupportTickets();
    } catch(e) { console.error(e); }
}

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    loadSupportTickets();
    checkUnreadSupportMessages();

    setInterval(() => {
        if(!currentOpenTicketId) {
            checkUnreadSupportMessages();
        }
    }, 10000);

    const fileInput = document.getElementById('ticket-reply-file');
    if(fileInput) {
        fileInput.addEventListener('change', function() {
            const display = document.getElementById('file-name-display');
            if(display) display.innerText = this.files[0] ? this.files[0].name : '';
        });
    }
});