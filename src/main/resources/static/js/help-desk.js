// Wrap in IIFE to prevent global scope pollution
(function() {
    'use strict';

    if (window.helpDeskInitialized) return;
    window.helpDeskInitialized = true;

    const API_SUPPORT = '/api/support';
    let pollingInterval = null;

    // --- HELPER: Handle 401 Redirect cleanly ---
    function handleAuthRedirect() {
        stopPolling();

        const modal = document.getElementById('help-desk-modal');
        if (modal) modal.classList.add('hidden');
        document.body.style.overflow = '';
        document.documentElement.style.overflow = '';

        const currentPath = window.location.pathname;
        if (currentPath.includes('auth.html')) return;

        // Save URL and Flag with footer anchor
        const returnUrl = window.location.origin + window.location.pathname + '#footer-container';
        sessionStorage.setItem('returnUrl', returnUrl);
        sessionStorage.setItem('openHelpDeskAfterLogin', 'true');

        window.location.replace('auth.html');
    }

    function stopPolling() {
        if (pollingInterval) {
            clearInterval(pollingInterval);
            pollingInterval = null;
        }
    }

    // --- HELPER: Disable/Enable Reply Form based on Status ---
    function updateReplyUI(status) {
        const input = document.getElementById('reply-input');
        const fileInput = document.getElementById('chat-attachment');
        const fileLabel = document.getElementById('chat-file-label');

        // Try to find the button relative to the input form
        const form = input ? input.closest('form') : document.getElementById('chat-form'); // Fallback ID if exists
        const btn = form ? form.querySelector('button[type="submit"]') : null;

        const isClosed = (status === 'SOLVED' || status === 'CLOSED');

        if (input) {
            input.disabled = isClosed;
            if (isClosed) {
                input.placeholder = "This ticket is closed.";
                input.classList.add('bg-gray-100', 'cursor-not-allowed', 'text-gray-500');
                input.classList.remove('bg-white');
            } else {
                input.placeholder = "Type a message...";
                input.classList.remove('bg-gray-100', 'cursor-not-allowed', 'text-gray-500');
                input.classList.add('bg-white');
            }
        }

        if (fileInput) {
            fileInput.disabled = isClosed;
        }

        if (fileLabel) {
            if (isClosed) {
                fileLabel.classList.add('opacity-50', 'cursor-not-allowed', 'pointer-events-none');
            } else {
                fileLabel.classList.remove('opacity-50', 'cursor-not-allowed', 'pointer-events-none');
            }
        }

        if (btn) {
            btn.disabled = isClosed;
            if (isClosed) {
                btn.classList.add('opacity-50', 'cursor-not-allowed');
            } else {
                btn.classList.remove('opacity-50', 'cursor-not-allowed');
            }
        }
    }

    // --- Main Functions ---

    window.openHelpDesk = async function openHelpDesk() {
        const modal = document.getElementById('help-desk-modal');
        if (!modal) {
            return;
        }

        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
        document.documentElement.style.overflow = 'hidden';

        await loadTickets();
    };

    window.closeHelpDesk = function closeHelpDesk() {
        const modal = document.getElementById('help-desk-modal');
        if (!modal) return;
        modal.classList.add('hidden');
        document.body.style.overflow = '';
        document.documentElement.style.overflow = '';
        stopPolling();
    };

    window.showCreateTicket = function showCreateTicket() {
        stopPolling();
        document.getElementById('view-ticket-list').classList.add('hidden');
        document.getElementById('view-chat').classList.add('hidden');
        document.getElementById('view-create-ticket').classList.remove('hidden');
    };

    window.showTicketList = function showTicketList() {
        stopPolling();
        document.getElementById('view-create-ticket').classList.add('hidden');
        document.getElementById('view-chat').classList.add('hidden');
        document.getElementById('view-ticket-list').classList.remove('hidden');
        loadTickets();
    };

    // 1. Load Tickets List
    async function loadTickets() {
        const container = document.getElementById('ticket-container');
        if(container) container.innerHTML = '<div class="text-center py-4"><i class="fas fa-circle-notch fa-spin text-gold"></i></div>';

        try {
            const res = await fetch(`${API_SUPPORT}/tickets`, { credentials: 'include' });

            if(res.status === 401) {
                handleAuthRedirect();
                return;
            }

            const data = await res.json();

            if (!data.tickets || data.tickets.length === 0) {
                if(container) container.innerHTML = '<div class="text-center mt-10"><i class="far fa-comments text-4xl text-gray-200 mb-3"></i><p class="text-sm text-gray-400">No support tickets yet.</p></div>';
                return;
            }

            if(container) {
                container.innerHTML = data.tickets.map(t => {
                    const statusColor = t.status === 'OPEN' ? 'bg-green-100 text-green-700' : 'bg-gray-200 text-gray-600';
                    return `
                <div onclick="openChat(${t.id})" class="bg-white p-4 rounded border-l-4 border-transparent hover:border-gold shadow-sm hover:shadow-md cursor-pointer transition-all group mb-2">
                    <div class="flex justify-between items-start mb-1">
                        <span class="font-bold text-sm text-gray-900 truncate w-40 group-hover:text-gold transition">${t.subject}</span>
                        <span class="text-[10px] font-bold uppercase px-2 py-0.5 rounded ${statusColor}">${t.status}</span>
                    </div>
                    <div class="flex justify-between text-[10px] text-gray-400 mt-2">
                        <span>Ticket #${t.id}</span>
                        <span>${t.createdAt}</span>
                    </div>
                </div>`;
                }).join('');
            }

        } catch (e) {
            if (container) container.innerHTML = '<p class="text-red-500 text-center text-sm">Failed to connect to server.</p>';
        }
    }

    // 2. Submit New Ticket
    window.submitNewTicket = async function submitNewTicket(e) {
        e.preventDefault();
        const form = e.target;
        const formData = new FormData(form);
        const btn = form.querySelector('button[type="submit"]');
        const oldText = btn.innerText;

        const fileInput = form.querySelector('input[type="file"]');
        if (fileInput && fileInput.files.length > 0) {
            if (fileInput.files[0].size > 800 * 1024) {
                alert('File size exceeds 800KB.');
                return;
            }
        }

        btn.innerText = "Processing...";
        btn.disabled = true;

        try {
            const res = await fetch(`${API_SUPPORT}/tickets`, {
                method: 'POST',
                body: formData,
                credentials: 'include'
            });

            if(res.status === 401) {
                handleAuthRedirect();
                return;
            }

            const data = await res.json();

            if (data.status) {
                form.reset();
                const fileDisplay = document.getElementById('file-name-display');
                if(fileDisplay) fileDisplay.textContent = 'Upload a photo...';
                window.openChat(data.ticket.id);
            } else {
                alert(data.message || 'Error creating ticket');
            }
        } catch (err) {
            alert("Network error occurred");
        } finally {
            btn.innerText = oldText;
            btn.disabled = false;
        }
    };

    // 3. Open Chat
    window.openChat = async function openChat(ticketId) {
        document.getElementById('view-ticket-list').classList.add('hidden');
        document.getElementById('view-create-ticket').classList.add('hidden');
        document.getElementById('view-chat').classList.remove('hidden');
        document.getElementById('current-ticket-id').value = ticketId;

        await fetchAndRenderChat(ticketId, true);

        stopPolling();
        pollingInterval = setInterval(() => {
            fetchAndRenderChat(ticketId, false);
        }, 3000);
    };

    async function fetchAndRenderChat(ticketId, isFirstLoad) {
        const chatContainer = document.getElementById('chat-messages');

        if(isFirstLoad && chatContainer) {
            chatContainer.innerHTML = '<div class="text-center py-10 flex justify-center"><i class="fas fa-circle-notch fa-spin text-gold"></i></div>';
        }

        try {
            const res = await fetch(`${API_SUPPORT}/tickets/${ticketId}`, { credentials: 'include' });

            if(res.status === 401) {
                handleAuthRedirect();
                return;
            }

            const contentType = res.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) return;

            const data = await res.json();

            if (data.status) {
                const t = data.ticket;
                const subjectEl = document.getElementById('chat-subject');
                const statusEl = document.getElementById('chat-status');

                if(subjectEl) subjectEl.innerText = t.subject;
                if(statusEl) statusEl.innerText = t.status;

                // --- NEW: Update UI based on Status ---
                updateReplyUI(t.status);

                if(chatContainer) {
                    const isNearBottom = chatContainer.scrollHeight - chatContainer.scrollTop <= chatContainer.clientHeight + 150;
                    renderMessages(t.messages);

                    // Auto scroll on first load or if user is near bottom
                    if (isFirstLoad || isNearBottom) {
                        setTimeout(() => scrollToBottom(), 100);
                    }
                }
            }
        } catch (e) {
        }
    }

    function renderMessages(messages) {
        const chatContainer = document.getElementById('chat-messages');
        if(!chatContainer) return;

        if(!messages || messages.length === 0) {
            chatContainer.innerHTML = '<p class="text-center text-xs text-gray-400 mt-4">Start of conversation.</p>';
            return;
        }

        const html = messages.map(m => {
            const isUser = m.senderType === 'USER';
            const align = isUser ? 'justify-end' : 'justify-start';
            const bubbleColor = isUser ? 'bg-black text-white rounded-br-none' : 'bg-gray-100 text-gray-900 rounded-bl-none';
            const label = isUser ? 'You' : 'Support Team';

            let attachmentHtml = m.attachmentUrl ? `
            <div class="mt-2 pt-2 border-t ${isUser ? 'border-gray-700' : 'border-gray-300'}">
                <a href="${m.attachmentUrl}" target="_blank" class="flex items-center gap-2 text-xs hover:underline">
                    <i class="fas fa-file-download"></i> View Attachment
                </a>
            </div>` : '';

            const messageText = (m.message || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>');

            return `
        <div class="flex flex-col ${align} mb-1">
            <div class="max-w-[85%] ${bubbleColor} p-3 rounded-2xl shadow-sm text-sm relative group">
                <p class="leading-relaxed break-words">${messageText}</p>
                ${attachmentHtml}
                <div class="text-[9px] opacity-60 mt-1 text-right flex justify-between gap-4">
                    <span>${label}</span>    
                    <span>${m.timestamp || ''}</span>
                </div>
            </div>
        </div>`;
        }).join('');

        chatContainer.innerHTML = html;
    }

    // 4. Submit Reply (UPDATED)
    window.submitReply = async function submitReply(e) {
        e.preventDefault();

        // Guard: Do not allow submission if disabled
        const input = document.getElementById('reply-input');
        if (input && input.disabled) {
            return;
        }

        const ticketId = document.getElementById('current-ticket-id').value;
        const form = e.target;

        const fileInput = document.getElementById('chat-attachment');
        const btn = form.querySelector('button[type="submit"]');

        if(!input.value.trim() && (!fileInput || !fileInput.files.length)) {
            return;
        }

        const originalIcon = btn.innerHTML;
        btn.innerHTML = '<i class="fas fa-circle-notch fa-spin"></i>';
        btn.disabled = true;

        const formData = new FormData();
        formData.append('message', input.value);

        if (fileInput && fileInput.files.length > 0) {
            formData.append('attachment', fileInput.files[0]);
        }

        try {
            const res = await fetch(`${API_SUPPORT}/tickets/${ticketId}/reply`, {
                method: 'POST',
                body: formData,
                credentials: 'include'
            });

            if(res.status === 401) {
                handleAuthRedirect();
                return;
            }

            const data = await res.json();
            if(data.status) {
                // Clear Input
                input.value = '';

                // --- UPDATE: Clear File Input & Label ---
                if(fileInput) {
                    fileInput.value = ''; // Reset file input
                    // Reset Label Text and Color
                    const label = document.getElementById('chat-file-label');
                    if(label) {
                        label.textContent = 'Attach File';
                        label.classList.remove('text-gold');
                    }
                }

                await fetchAndRenderChat(ticketId, false);
                scrollToBottom();

                if (window.notify) window.notify.success('Message sent successfully!');
            } else {
                if (window.notify) window.notify.error(data.message || 'Failed to send message');
                else alert(data.message);
            }
        } catch(e) {
            if (window.notify) window.notify.error('Failed to send message.');
        } finally {
            btn.innerHTML = originalIcon;
            btn.disabled = false;
            if(!input.disabled) input.focus();
        }
    };

    function scrollToBottom() {
        const chat = document.getElementById('chat-messages');
        if(chat) chat.scrollTop = chat.scrollHeight;
    }

})();