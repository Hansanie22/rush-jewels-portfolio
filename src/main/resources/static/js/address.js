import Notification from './notification.js';

document.addEventListener("DOMContentLoaded", async () => {
    const notif = Notification();

    // --- DOM Elements ---
    const line1Input = document.getElementById("line1");
    const line2Input = document.getElementById("line2");
    const postalInput = document.getElementById("postal");
    const countrySelect = document.getElementById("country");
    const saveBtn = document.getElementById("saveAddressBtn");
    const provinceWrapper = document.getElementById("province-wrapper");
    const cityWrapper = document.getElementById("city-wrapper");


    // --- Validators ---
    const isValidPostalCode = (postalCode) => {
        const code = postalCode.trim();
        return code.length >= 3 && code.length <= 10 && /^[a-zA-Z0-9\s-]+$/.test(code);
    };

    // --- Generic fetch ---
    async function fetchJson(url) {
        const res = await fetch(url, { credentials: "include" });
        const text = await res.text();
        if (!res.ok) {
            throw new Error(`Failed to fetch ${url}: ${res.status} ${res.statusText} - ${text}`);
        }
        if (!text) {
            console.warn(`Received empty response from ${url}`);
            return null;
        }
        try {
            return JSON.parse(text);
        } catch (jsonErr) {
            console.error(`Invalid JSON from ${url}:`, text);
            throw new Error(`Invalid JSON response from ${url}`);
        }
    }

    // --- Helper functions to create dynamic fields ---
    function createProvinceSelect(defaultOptionText, isDisabled) {
        provinceWrapper.innerHTML = `
            <label for="province" class="block elegant-label mb-2">Province / State</label>
            <select id="province" name="province" class="sharp-input border w-full px-4 h-12" ${isDisabled ? 'disabled' : ''} required>
                <option value="">${defaultOptionText}</option>
            </select>`;
        return provinceWrapper.querySelector('#province');
    }
    function createProvinceInput(labelText, isDisabled) {
        provinceWrapper.innerHTML = `
            <label for="province" class="block elegant-label mb-2">${labelText}</label>
            <input type="text" id="province" name="province" class="sharp-input border w-full px-4 h-12" placeholder="Province / State" ${isDisabled ? 'disabled' : ''} required>`;
        return provinceWrapper.querySelector('#province');
    }
    function createCitySelect(defaultOptionText, isDisabled) {
        cityWrapper.innerHTML = `
            <label for="city" class="block elegant-label mb-2">City</label>
            <select id="city" name="city" class="sharp-input border w-full px-4 h-12" ${isDisabled ? 'disabled' : ''} required>
                <option value="">${defaultOptionText}</option>
            </select>`;
        return cityWrapper.querySelector('#city');
    }
    function createCityInput(labelText, isDisabled) {
        cityWrapper.innerHTML = `
            <label for="city" class="block elegant-label mb-2">${labelText}</label>
            <input type="text" id="city" name="city" class="sharp-input border w-full px-4 h-12" placeholder="Enter City Name" ${isDisabled ? 'disabled' : ''} required>`;
        return cityWrapper.querySelector('#city');
    }

    // --- Load Countries ---
    async function loadCountries() {
        try {
            const data = await fetchJson("/api/countries");
            if (!data) return;
            const countries = Array.isArray(data) ? data : data.countries;
            if (!Array.isArray(countries)) throw new Error("Invalid countries response");

            countrySelect.innerHTML = '<option value="">Select Country</option>';
            countries.forEach(c => {
                const option = document.createElement("option");
                option.value = c.id;
                option.textContent = c.country;
                countrySelect.appendChild(option);
            });
        } catch (err) {
            console.error(err);
            notif.error("Failed to load country options.");
        }
    }

    // --- Load Provinces ---
    async function loadProvincesByCountry(countryId) {
        createCitySelect('Select a province first', true);
        if (!countryId) {
            createProvinceSelect('Select a country first', true);
            return;
        }
        createProvinceSelect('Loading provinces...', true);
        try {
            const data = await fetchJson(`/api/provinces?countryId=${countryId}`);
            const provinces = (data && data.provinces) ? data.provinces : [];

            if (provinces.length > 0) {
                const provinceSelect = createProvinceSelect('Select Province/State', false);
                provinces.forEach(p => {
                    const option = document.createElement("option");
                    option.value = p.id;
                    option.textContent = p.province;
                    provinceSelect.appendChild(option);
                });
            } else {
                createProvinceInput('Province / State', false);
                createCityInput('City', false);
            }
        } catch (err) {
            console.error(err);
            notif.error("Failed to load province options.");
            createProvinceInput('Error. Please enter province.', false);
        }
    }

    // --- Load Cities ---
// --- Load Cities ---
    async function loadCitiesByProvince(provinceId) {
        if (!provinceId) {
            createCitySelect('Select a province first', true);
            return;
        }
        createCitySelect('Loading cities...', true);
        try {
            const data = await fetchJson(`/api/cities?provinceId=${provinceId}`);
            const cities = (data && data.cities) ? data.cities : [];

            if (cities.length > 0) {
                // *** SORTING APPLIED HERE (A-Z) ***
                cities.sort((a, b) => a.city.localeCompare(b.city));

                const citySelect = createCitySelect('Select City', false);
                cities.forEach(c => {
                    const option = document.createElement("option");
                    option.value = c.id;
                    option.textContent = c.city;
                    citySelect.appendChild(option);
                });

                // Add "Other" Option at the very bottom
                const otherOpt = document.createElement("option");
                otherOpt.value = "other";
                otherOpt.textContent = "Other";
                citySelect.appendChild(otherOpt);

            } else {
                createCityInput('City', false);
            }
        } catch (err) {
            console.error(err);
            notif.error("Failed to load city options.");
            createCityInput('Error. Please enter city.', false);
        }
    }
    // --- Load Existing Address ---
    window.loadAddress = async function loadAddress() {
        // 1. පටන් ගන්නා විට ලෝඩරය පෙන්වීම
        try {
            const data = await fetchJson("/api/addresses");
            if (!data || !data.status || !data.addresses?.length) {
                revealContent();
                return;
            }
            const addr = data.addresses.find(a => a.defaultAddress === true);

            if (!addr) {
                return;
            }

            line1Input.value = (addr.no ? addr.no + ' ' : '') + (addr.line1 || '');
            line2Input.value = addr.line2 || "";
            postalInput.value = addr.postalCode || "";

            if (addr.country?.id) {
                countrySelect.value = addr.country.id;

                await loadProvincesByCountry(addr.country.id);
                const provinceField = document.getElementById("province");

                if (addr.province?.id && provinceField.tagName === 'SELECT') {
                    provinceField.value = addr.province.id;
                    await loadCitiesByProvince(addr.province.id);
                } else if (addr.stateText && provinceField.tagName === 'INPUT') {
                    provinceField.value = addr.stateText;
                    createCityInput('City', false);
                }

                // *** MODIFICATION: Handle City Selection or Text ***
                // At this point, loadCitiesByProvince has likely created a SELECT box.
                // We need to check if the saved address has an ID or Text.
                let cityField = document.getElementById("city");

                if (addr.city?.id && cityField.tagName === 'SELECT') {
                    // It matches a city in the list
                    cityField.value = addr.city.id;
                } else if (addr.cityText) {
                    // It was a manually typed city ("Other").
                    // Force switch to INPUT mode and set the value.
                    createCityInput('City', false);
                    cityField = document.getElementById("city"); // Re-select the new input element
                    cityField.value = addr.cityText;
                }
            }
        } catch (err) {
            console.error("Error loading address:", err);
            notif.error("Could not load address.");
        }finally {
            // ✅ 2. සියල්ල අවසන් වූ පසු පිටුව පෙන්වා ලෝඩරය අයින් කිරීම
            revealContent();
        }
    }

    function revealContent() {
        const main = document.getElementById('main-content');
        if (main) {
            main.style.display = 'block';
            main.classList.add('animate__animated', 'animate__fadeIn');
        }
        if (window.loader) {
            // Dropdowns Render වීමට තත්පර 0.4ක සහනයක් ලබා දෙන්න
            setTimeout(() => {
                window.loader.hide();
            }, 400);
        }
    }
    // --- Event Listeners ---
    countrySelect.addEventListener('change', (e) => {
        loadProvincesByCountry(e.target.value);
    });

    provinceWrapper.addEventListener('change', (e) => {
        if (e.target && e.target.id === 'province' && e.target.tagName === 'SELECT') {
            loadCitiesByProvince(e.target.value);
        }
    });

    // *** MODIFICATION: Detect "Other" selection in City ***
    cityWrapper.addEventListener('change', (e) => {
        if (e.target && e.target.id === 'city' && e.target.tagName === 'SELECT') {
            if (e.target.value === 'other') {
                createCityInput('City (Type Manually)', false);
                document.getElementById('city').focus();
            }
        }
    });

    // --- Save / Update Address ---
    if (saveBtn) {
        saveBtn.addEventListener('click', async (e) => {
            e.preventDefault();

            const provinceField = document.getElementById("province");
            const cityField = document.getElementById("city");

            // Validation
            if (!line1Input.value.trim()) {
                return notif.warning("Address is required.");
            }
            if (!countrySelect.value) {
                return notif.warning("Please select a Country.");
            }
            if (!provinceField || !provinceField.value.trim()) {
                return notif.warning("Please select or enter a Province/State.");
            }
            if (!cityField || !cityField.value.trim()) {
                return notif.warning("Please select or enter a City.");
            }
            if (!isValidPostalCode(postalInput.value)) {
                return notif.warning("Please enter a valid Postal Code.");
            }

            let provinceId = null;
            let provinceOther = null;
            let cityId = null;
            let cityOther = null;

            if (provinceField.tagName === 'SELECT') {
                provinceId = provinceField.value ? parseInt(provinceField.value) : null;
            } else {
                provinceOther = provinceField.value.trim();
            }

            // *** Check if City is Select or Input ***
            if (cityField.tagName === 'SELECT') {
                cityId = cityField.value ? parseInt(cityField.value) : null;
            } else {
                // If input, send as 'cityOther'
                cityOther = cityField.value.trim();
            }

            const payload = {
                line1: line1Input.value.trim(),
                line2: line2Input.value.trim(),
                postalCode: postalInput.value.trim(),
                countryId: parseInt(countrySelect.value),
                provinceId: provinceId,
                provinceOther: provinceOther,
                cityId: cityId,
                cityOther: cityOther
            };

            saveBtn.disabled = true;
            saveBtn.textContent = "Saving...";
            try {
                const res = await fetch("/api/addresses", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    credentials: "include",
                    body: JSON.stringify(payload)
                });

                const result = await res.json();
                if (result.status) {
                    notif.success(result.message || "Address saved successfully!");
                    await loadAddress();
                } else {
                    notif.error(result.message || "Address save failed.");
                }
            } catch (err) {
                console.error("Update address error:", err);
                notif.error("Network error. Could not save address.");
            } finally {
                saveBtn.disabled = false;
                saveBtn.textContent = "Save Address";
            }
        });
    }

    const isMasterScriptPresent = window.loader && document.getElementById('main-content');
    if (!isMasterScriptPresent) {
        await loadCountries();
        await window.loadAddress();
    }
});