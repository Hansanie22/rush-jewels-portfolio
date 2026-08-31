document.addEventListener("DOMContentLoaded", () => {
    const categoryList = document.getElementById("categoryList");

    // ✅ Updated to match your Spring Boot REST API
    fetch("/api/categories")
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to load categories: " + response.status);
            }
            return response.json();
        })
        .then(data => {
            categoryList.innerHTML = "";

            if (Array.isArray(data) && data.length > 0) {
                data.forEach(category => {
                    const name = String(category.category || "").replace(/[<>]/g, "");
                    const li = document.createElement("li");
                    li.className = "flex items-center";

                    const input = document.createElement("input");
                    input.type = "checkbox";
                    input.value = name.toLowerCase();
                    input.id = `cat-${category.id}`;
                    input.className = "mr-2";

                    const label = document.createElement("label");
                    label.setAttribute("for", `cat-${category.id}`);
                    label.className = "text-gray-600";
                    label.textContent = name;

                    li.appendChild(input);
                    li.appendChild(label);
                    categoryList.appendChild(li);
                });
            } else {
                categoryList.innerHTML = `<li class="text-gray-400">No categories found</li>`;
            }
        })
        .catch(err => {
            console.error("Error loading categories:", err);
            categoryList.innerHTML = `<li class="text-red-500">Failed to load categories</li>`;
        });
});
