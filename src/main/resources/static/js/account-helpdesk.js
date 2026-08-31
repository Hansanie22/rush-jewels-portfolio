(function() {
    'use strict';

    const API_SUPPORT = '/api/support';

    let pollingInterval = null;
    let currentTicketId = null;

    // Public API exposed to window
    window.accountHelpDesk = {
        loadTickets: loadTicketsList,
        init: init
    };

    document.addEventListener('DOMContentLoaded', () => {
        const isMasterScriptPresent = window.loader && document.getElementById('main-content');

        // Master Script එක නැතිනම් පමණක් මෙය fallback එකක් ලෙස වැඩ කරයි
        if (!isMasterScriptPresent) {
            init();
            loadTicketsList();
        } else {
            // Master Script එක තිබේ නම්, දැනුම්දීම් (Notifications) පමණක් පසුබිමෙන් පරීක්ෂා කරන්න
            checkNotifications();
            setInterval(checkNotifications, 10000);
        }
    });
    function init() {
        checkNotifications();
        setInterval(checkNotifications, 10000);

        const createBtn = document.getElementById('btn-create-new-ticket');
        const cancelBtn = document.getElementById('acc-cancel-create');
        const backFromCreateBtn = document.getElementById('acc-back-from-create');
        const createForm = document.getElementById('acc-create-ticket-form');
        const replyForm = document.getElementById('acc-reply-form');
        const searchInput = document.getElementById('ticket-search');
        const backToListBtn = document.getElementById('acc-back-to-list');

        if(createBtn) createBtn.addEventListener('click', showCreateView);
        if(cancelBtn) cancelBtn.addEventListener('click', hideCreateView);
        if(backFromCreateBtn) backFromCreateBtn.addEventListener('click', hideCreateView);
        if(backToListBtn) backToListBtn.addEventListener('click', handleMobileBackToList);

        if(createForm) createForm.addEventListener('submit', submitNewTicket);
        if(replyForm) replyForm.addEventListener('submit', submitReply);

        if(searchInput) {
            searchInput.addEventListener('input', (e) => {
                const term = e.target.value.toLowerCase();
                const tickets = document.querySelectorAll('.ticket-item');
                tickets.forEach(t => {
                    const text = t.innerText.toLowerCase();
                    t.style.display = text.includes(term) ? 'block' : 'none';
                });
            });
        }
    }

    // --- Helper to reveal page & hide loader ---
    function revealContent() {
        const main = document.getElementById('main-content');
        if (main && main.style.display === 'none') {
            main.style.display = 'block';
            main.classList.add('animate__animated', 'animate__fadeIn');
        }
        if (window.loader) {
            setTimeout(() => {
                window.loader.hide();
            }, 400);
        }
    }

    // --- Mobile View Toggles ---
    function handleMobileView(view) {
        const listCol = document.getElementById('helpdesk-list-col');
        const chatCol = document.getElementById('helpdesk-chat-col');
        if (!listCol || !chatCol) return;

        if (view === 'chat') {
            listCol.classList.add('hidden');
            listCol.classList.add('md:flex');
            chatCol.classList.remove('hidden');
            chatCol.classList.add('flex');
        } else {
            listCol.classList.remove('hidden');
            chatCol.classList.add('hidden');
            chatCol.classList.add('md:flex');
        }
    }

    function handleMobileBackToList() {
        handleMobileView('list');
        currentTicketId = null;
        loadTicketsList();
    }

    // --- 1. Notification & Data Fetching ---

    async function checkNotifications() {
        try {
            const res = await fetch(`${API_SUPPORT}/tickets`, { credentials: 'include' });
            if(res.status === 401) return;
            const data = await res.json();
            if(data.tickets) updateBadgeAndList(data.tickets);
        } catch (e) {
            console.error("Notification check failed", e);
        }
    }

    function updateBadgeAndList(tickets) {
        const unreadTickets = tickets.filter(t => {
            if (currentTicketId && t.id === currentTicketId) return false;
            if (t.messages && Array.isArray(t.messages)) {
                return t.messages.some(m => m.senderType === 'ADMIN' && m.isRead === false);
            }
            return false;
        });
        const unreadCount = unreadTickets.length;
        const badge = document.getElementById('nav-badge-count');
        if(badge) {
            if(unreadCount > 0) {
                badge.innerText = unreadCount;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        }
    }

    // --- 2. Ticket List Logic ---

    async function loadTicketsList() {
        // ✅ එකතු කළා: ලෝඩරය පෙන්වීම ආරම්භ කිරීම
        if (window.loader) window.loader.show();

        const container = document.getElementById('acc-ticket-list');
        if(!container) return;

        handleMobileView('list');

        try {
            const res = await fetch(`${API_SUPPORT}/tickets`, { credentials: 'include' });
            if(res.status === 401) {
                window.location.href = 'auth.html';
                return;
            }
            const data = await res.json();
            updateBadgeAndList(data.tickets || []);
            renderTicketList(data.tickets || []);
        } catch (e) {
            container.innerHTML = '<p class="text-red-500 text-center text-xs">Failed to load tickets.</p>';
        } finally {
            // ✅ දත්ත ලැබුණු පසු Loader එක අයින් කිරීම
            revealContent();
        }
    }
    function renderTicketList(tickets) {
        const container = document.getElementById('acc-ticket-list');
        if(!container) return;

        if (tickets.length === 0) {
            container.innerHTML = '<div class="text-center mt-10 p-4"><i class="far fa-comments text-3xl text-gray-200 mb-2"></i><p class="text-xs text-gray-400">No tickets found.</p></div>';
            return;
        }

        container.innerHTML = tickets.map(t => {
            const hasUnreadAdminMsg = t.messages && t.messages.some(m => m.senderType === 'ADMIN' && m.isRead === false);
            const showUnread = hasUnreadAdminMsg && (t.id !== currentTicketId);
            let statusClass = 'text-gray-500 bg-gray-50 border-gray-200';
            if (t.status === 'OPEN') statusClass = 'text-green-600 bg-green-50 border-green-200';
            if (t.status === 'SOLVED') statusClass = 'text-blue-600 bg-blue-50 border-blue-200';
            if (t.status === 'CLOSED') statusClass = 'text-gray-600 bg-gray-200 border-gray-300';
            const titleClass = showUnread ? 'font-bold text-black' : 'font-medium text-gray-700';
            const bgClass = showUnread ? 'bg-white' : 'bg-gray-50/50';
            const unreadDot = showUnread ? '<div class="unread-dot h-2 w-2 rounded-full bg-gold absolute top-4 right-3 shadow-sm"></div>' : '';
            const activeClass = (t.id === currentTicketId) ? 'border-l-4 border-l-gold bg-yellow-50' : 'border-l-0';

            return `
                <div onclick="window.accountHelpDeskOpenChat(${t.id})" class="ticket-item cursor-pointer p-3 border-b border-gray-100 transition-all hover:bg-gray-50 relative ${bgClass} ${activeClass}" id="ticket-item-${t.id}">
                    ${unreadDot}
                    <div class="flex justify-between items-start mb-1 pr-4">
                        <h4 class="${titleClass} text-sm truncate w-3/4">${t.subject}</h4>
                        <span class="text-[10px] px-1.5 py-0.5 rounded-none border ${statusClass} font-semibold">${t.status}</span>
                    </div>
                    <div class="flex justify-between items-center text-[11px] text-gray-400">
                        <span>#${t.id}</span>
                        <span>${t.createdAt}</span>
                    </div>
                </div>
            `;
        }).join('');
    }

    // --- 3. Chat Logic ---

    window.accountHelpDeskOpenChat = async function(ticketId) {
        currentTicketId = ticketId;
        handleMobileView('chat');
        document.getElementById('acc-chat-placeholder').classList.add('hidden');
        document.getElementById('acc-create-ticket-view').classList.add('hidden');
        document.getElementById('acc-active-chat-view').classList.remove('hidden');

        document.querySelectorAll('.ticket-item').forEach(el => {
            el.classList.remove('border-l-4', 'border-l-gold', 'bg-yellow-50');
            el.classList.add('border-l-0');
        });
        const activeItem = document.getElementById(`ticket-item-${ticketId}`);
        if(activeItem) {
            activeItem.classList.remove('border-l-0');
            activeItem.classList.add('border-l-4', 'border-l-gold', 'bg-yellow-50');
            const goldDot = activeItem.querySelector('.unread-dot');
            if(goldDot) {
                goldDot.remove();
                activeItem.classList.remove('bg-white');
                activeItem.classList.add('bg-gray-50/50');
                const title = activeItem.querySelector('h4');
                if(title) title.classList.remove('font-bold', 'text-black');
            }
        }

        const badge = document.getElementById('nav-badge-count');
        if(badge && !badge.classList.contains('hidden')) {
            const currentVal = parseInt(badge.innerText);
            if (!isNaN(currentVal) && currentVal > 0) {
                const newVal = currentVal - 1;
                if (newVal <= 0) badge.classList.add('hidden');
                else badge.innerText = newVal;
            }
        }

        document.getElementById('acc-chat-id').innerText = `#${ticketId}`;
        document.getElementById('acc-current-ticket-id').value = ticketId;
        await fetchChatDetails(ticketId, true);
        checkNotifications();
        if(pollingInterval) clearInterval(pollingInterval);
        pollingInterval = setInterval(() => fetchChatDetails(ticketId, false), 3000);
    };

    async function fetchChatDetails(ticketId, isFirstLoad) {
        const chatContainer = document.getElementById('acc-chat-messages');
        if(isFirstLoad && chatContainer) {
            chatContainer.innerHTML = '<div class="flex justify-center py-10"><i class="fas fa-circle-notch fa-spin text-gold"></i></div>';
        }
        try {
            const res = await fetch(`${API_SUPPORT}/tickets/${ticketId}`, { credentials: 'include' });
            const data = await res.json();
            if (data.status) {
                const t = data.ticket;
                document.getElementById('acc-chat-subject').innerText = t.subject;
                const statusEl = document.getElementById('acc-chat-status');
                statusEl.innerText = t.status;
                const replyForm = document.getElementById('acc-reply-form');
                if (t.status === 'OPEN') {
                    statusEl.className = "uppercase font-bold text-green-600 text-[10px] md:text-xs";
                    if (replyForm) {
                        replyForm.classList.remove('hidden');
                        const closedMsg = document.getElementById('ticket-closed-msg');
                        if (closedMsg) closedMsg.remove();
                    }
                } else {
                    statusEl.className = "uppercase font-bold text-gray-500 text-[10px] md:text-xs";
                    if (replyForm) {
                        replyForm.classList.add('hidden');
                        if (!document.getElementById('ticket-closed-msg')) {
                            const msgDiv = document.createElement('div');
                            msgDiv.id = 'ticket-closed-msg';
                            msgDiv.className = 'p-4 text-center text-xs text-gray-500 bg-gray-50 border-t border-gray-100';
                            msgDiv.innerHTML = '<i class="fas fa-lock mr-2"></i>This ticket is closed. You cannot reply.';
                            replyForm.parentNode.insertBefore(msgDiv, replyForm.nextSibling);
                        }
                    }
                }
                renderMessages(t.messages, isFirstLoad);
            }
        } catch (e) {
            console.error("Chat load error", e);
        }
    }

    function renderMessages(messages, isFirstLoad) {
        const container = document.getElementById('acc-chat-messages');
        if (!container) return;
        const currentMsgCount = container.querySelectorAll('.msg-bubble').length;
        if (!isFirstLoad && messages.length === currentMsgCount) return;

        if (!messages || messages.length === 0) {
            container.innerHTML = '<p class="text-center text-xs text-gray-300 mt-4">Start of conversation</p>';
            return;
        }

        const html = messages.map(m => {
            const isUser = m.senderType === 'USER';
            const align = isUser ? 'items-end' : 'items-start';
            const bubbleStyle = isUser ? 'bg-dark text-white rounded-br-none' : 'bg-gray-100 text-dark rounded-bl-none border border-gray-200';
            const senderName = isUser ? 'You' : 'Support Team';
            const time = m.timestamp ? m.timestamp.split(' ')[1] : '';
            let attachmentHtml = m.attachmentUrl ? `
                <div class="mt-2 pt-2 border-t ${isUser ? 'border-gray-700' : 'border-gray-300'}">
                    <a href="${m.attachmentUrl}" target="_blank" class="flex items-center gap-2 text-xs hover:underline opacity-80">
                        <i class="fas fa-file-download"></i> Attachment
                    </a>
                </div>` : '';
            const safeMsg = (m.message || '').replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\n/g, "<br>");
            return `
                <div class="flex flex-col ${align} w-full msg-bubble animate-fade-in mb-3">
                    <div class="max-w-[85%] ${bubbleStyle} p-2.5 md:p-3 rounded-lg shadow-sm text-sm">
                        <p>${safeMsg}</p>
                        ${attachmentHtml}
                    </div>
                    <span class="text-[9px] md:text-[10px] text-gray-400 mt-1 px-1">${senderName} • ${time}</span>
                </div>
            `;
        }).join('');
        container.innerHTML = html;
        if (isFirstLoad || (container.scrollHeight - container.scrollTop <= container.clientHeight + 300)) {
            setTimeout(() => { container.scrollTop = container.scrollHeight; }, 100);
        }
    }

    async function submitNewTicket(e) {
        e.preventDefault();
        const form = e.target;
        const btn = form.querySelector('button[type="submit"]');
        const oldText = btn.innerText;
        btn.innerText = "Creating...";
        btn.disabled = true;
        const formData = new FormData(form);
        try {
            const res = await fetch(`${API_SUPPORT}/tickets`, {
                method: 'POST',
                body: formData,
                credentials: 'include'
            });
            const data = await res.json();
            if (data.status) {
                form.reset();
                await loadTicketsList();
                if(data.ticket && data.ticket.id) {
                    setTimeout(() => window.accountHelpDeskOpenChat(data.ticket.id), 200);
                }
            } else {
                alert(data.message || "Failed to create ticket");
            }
        } catch (error) {
            alert("Network error.");
        } finally {
            btn.innerText = oldText;
            btn.disabled = false;
        }
    }

    async function submitReply(e) {
        e.preventDefault();
        const ticketId = document.getElementById('acc-current-ticket-id').value;
        const input = document.getElementById('acc-reply-input');
        const fileInput = document.getElementById('acc-reply-attachment');
        if (!input.value.trim() && (!fileInput.files.length)) return;
        const btn = e.target.querySelector('button');
        const oldIcon = btn.innerHTML;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
        const formData = new FormData();
        formData.append('message', input.value);
        if (fileInput.files.length > 0) formData.append('attachment', fileInput.files[0]);
        try {
            const res = await fetch(`${API_SUPPORT}/tickets/${ticketId}/reply`, {
                method: 'POST',
                body: formData,
                credentials: 'include'
            });
            const data = await res.json();
            if (data.status) {
                input.value = '';
                fileInput.value = '';
                const label = document.getElementById('acc-file-label');
                if(label) label.innerHTML = '<i class="fas fa-paperclip"></i>';
                fetchChatDetails(ticketId, false);
            }
        } catch (error) {
            console.error(error);
        } finally {
            btn.innerHTML = oldIcon;
        }
    }

    function showCreateView() {
        handleMobileView('chat');
        document.getElementById('acc-active-chat-view').classList.add('hidden');
        document.getElementById('acc-chat-placeholder').classList.add('hidden');
        document.getElementById('acc-create-ticket-view').classList.remove('hidden');
        if(pollingInterval) clearInterval(pollingInterval);
    }

    function hideCreateView() {
        document.getElementById('acc-create-ticket-view').classList.add('hidden');
        if(currentTicketId) {
            document.getElementById('acc-active-chat-view').classList.remove('hidden');
        } else {
            handleMobileBackToList();
            document.getElementById('acc-chat-placeholder').classList.remove('hidden');
        }
    }

})();