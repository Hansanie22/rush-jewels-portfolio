document.addEventListener("DOMContentLoaded", () => {
    const colorList = document.getElementById("colorList");

    // Fetch colors from your Spring Boot REST API
    fetch("/api/colors")
        .then(res => res.json())
        .then(data => {
            colorList.innerHTML = "";
            if (Array.isArray(data)) {
                data.forEach(color => {
                    const name = String(color.color || "").replace(/[<>]/g, "");
                    const li = document.createElement("li");
                    li.className = "flex items-center";

                    const input = document.createElement("input");
                    input.type = "checkbox";
                    // IMPORTANT: Ensure this value matches p.metal in product.js
                    input.value = name.toLowerCase();
                    input.id = `color-${color.id}`;
                    input.className = "mr-2";

                    const label = document.createElement("label");
                    label.setAttribute("for", `color-${color.id}`);
                    label.className = "text-gray-600";
                    label.textContent = name;

                    li.appendChild(input);
                    li.appendChild(label);
                    colorList.appendChild(li);
                });
            }
        })
        .catch(err => console.error("Error loading colors:", err));
});
