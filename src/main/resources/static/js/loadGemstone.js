document.addEventListener("DOMContentLoaded", () => {
    const gemstoneList = document.getElementById("gemstoneList");

    // ✅ Updated API path to match your Spring Boot REST endpoint
    fetch("/api/gemstones")
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to load gemstones");
            }
            return response.json();
        })
        .then(data => {
            gemstoneList.innerHTML = "";

            if (Array.isArray(data) && data.length > 0) {
                data.forEach(gem => {
                    const name = String(gem.gemStone || "").replace(/[<>]/g, "");

                    const li = document.createElement("li");
                    li.className = "flex items-center";

                    const input = document.createElement("input");
                    input.type = "checkbox";
                    // ✅ This must match p.gemstone in product.js
                    input.value = name.toLowerCase();
                    input.id = `gem-${gem.id}`;
                    input.className = "mr-2";

                    const label = document.createElement("label");
                    label.setAttribute("for", `gem-${gem.id}`);
                    label.className = "text-gray-600";
                    label.textContent = name;

                    li.appendChild(input);
                    li.appendChild(label);
                    gemstoneList.appendChild(li);
                });
            } else {
                gemstoneList.innerHTML = "<li class='text-gray-400'>No gemstones available</li>";
            }
        })
        .catch(err => {
            console.error("Error loading gemstones:", err);
            gemstoneList.innerHTML = "<li class='text-red-500'>Failed to load gemstones</li>";
        });
});
