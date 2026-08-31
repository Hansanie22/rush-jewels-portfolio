// audit-logs.js

document.addEventListener("DOMContentLoaded", () => {
    const logsContainer = document.querySelector("#logs-section .bg-white");

    async function loadTodayLogs() {
        try {
            const response = await fetch("/api/logs/today");
            const logs = await response.json();

            logsContainer.innerHTML = "";

            logs.forEach(log => {
                const dateTime = new Date(log.actionTime).toLocaleString();
                const table = log.tableName;
                const action = log.actionType;
                const record = log.recordId;

                // Extract IP and lastLogin if present in newValue JSON
                let ipAddress = "";
                let lastLogin = "";
                if (log.newValue) {
                    try {
                        const newVal = JSON.parse(log.newValue);
                        if (newVal.ipAddress) {
                            ipAddress = ` - IP: ${newVal.ipAddress}`;
                        }
                        if (newVal.lastLogin) {
                            const loginDate = new Date(newVal.lastLogin).toLocaleString();
                            lastLogin = ` - Last Login: ${loginDate}`;
                        }
                    } catch (e) {
                        // ignore JSON parse errors
                    }
                }

                const p = document.createElement("p");
                p.textContent = `${dateTime} - ${action} on ${table} #${record}${ipAddress}${lastLogin}`;
                logsContainer.appendChild(p);
            });
        } catch (err) {
            console.error("Error loading logs:", err);
        }
    }

    // Download logs as PDF (read-only)
    document.querySelector("#download-logs-btn")?.addEventListener("click", () => {
        if (logsContainer.querySelectorAll("p").length === 0) return;

        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();
        const today = new Date().toISOString().split("T")[0]; // YYYY-MM-DD

        // Add a header
        doc.setFontSize(12);
        doc.text(`Audit Log - ${today}`, 10, 10);
        doc.setFontSize(10);

        let y = 20;
        logsContainer.querySelectorAll("p").forEach(p => {
            const lines = doc.splitTextToSize(p.textContent, 180); // wrap text
            lines.forEach(line => {
                doc.text(line, 10, y);
                y += 6;
            });
        });

        doc.save(`audit-log-${today}.pdf`);
    });

    loadTodayLogs();
});
