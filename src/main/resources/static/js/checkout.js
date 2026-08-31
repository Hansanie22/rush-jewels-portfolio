import Notification from './notification.js';

const notify = Notification({
    position: 'bottom-right',
    duration: 3000,
    hidePrevious: true,
    maxVisible: 5,
});

document.addEventListener('DOMContentLoaded', () => {

    // --- Configuration ---
    const API_BASE_URL = '/api';

    // --- DOM Elements (Shipping Address) ---
    const emailInput = document.getElementById('email');
    const firstNameInput = document.getElementById('firstName');
    const lastNameInput = document.getElementById('lastName');
    const addressInput = document.getElementById('address');
    const apartmentInput = document.getElementById('apartment');
    const countrySelect = document.getElementById('country');
    const stateWrapper = document.getElementById('state-wrapper');
    const cityWrapper = document.getElementById('city-wrapper');
    const zipInput = document.getElementById('zip');
    const phoneInput = document.getElementById('phone');
    const orderNotesInput = document.getElementById('orderNotes');
    const agreeTermsCheckbox = document.getElementById('agreeTerms');

    // --- DOM Elements (Checkboxes & Sections) ---
    const billingCheckbox = document.getElementById('differentBilling');
    const billingSection = document.getElementById('billing-address-section');
    const saveAddressCheckbox = document.getElementById('saveAddress');
    const saveAddressWrapper = document.getElementById('saveAddress')?.parentElement;
    const shippingContainer = document.getElementById('shipping-methods-container');
    const paymentContainer = document.getElementById('payment-methods-container');
    const subscribeCheckbox = document.getElementById('subscribe');
    const subscribeWrapper = document.getElementById('subscribe-wrapper');
    const checkoutForm = document.getElementById('checkout-form');
    const submitOrderButton = document.getElementById('submit-order');
    const isGiftCheckbox = document.getElementById('isGift');

    // --- NEW: DOM Elements (Billing Address) ---
    const billingCountrySelect = document.getElementById('billingCountry');
    const billingStateWrapper = document.getElementById('billing-state-wrapper');
    const billingCityWrapper = document.getElementById('billing-city-wrapper');

    // --- DOM Elements (Order Summary) ---
    const orderItemsContainer = document.getElementById('order-items');
    const subtotalSpan = document.getElementById('subtotal');
    const shippingSpan = document.getElementById('shipping-cost');
    const taxSpan = document.getElementById('tax');
    const totalSpan = document.getElementById('total');
    const installmentContainer = document.getElementById('installment-details');

    // --- DOM Elements (Discount) ---
    const discountInput = document.getElementById('discount-code');
    const discountButton = document.getElementById('apply-discount');
    const discountMessage = document.getElementById('discount-message');
    const discountRow = document.getElementById('discount-row');
    const discountAmountSpan = document.getElementById('discount-amount');

    // =================================================================
    // 🔴 FIX: DISABLE BROWSER DEFAULT VALIDATION
    // =================================================================
    if (checkoutForm) {
        checkoutForm.noValidate = true;
    }

    // --- State Variables ---
    let prefillData = null;
    let cartItems = [];
    let cartSubtotal = 0;
    let cartTax = 0;
    let shippingCost = 0;
    let discountAmount = 0;
    let billingCountriesLoaded = false;
    let hasLoadedAddress = false;

    // --- PayHere loader/injector ---
    const PAYHERE_CDN = 'https://www.payhere.lk/lib/payhere.js';

    function loadScript(src, timeoutMs = 8000) {
        return new Promise((resolve, reject) => {
            if (window.payhere) return resolve();
            if (document.querySelector(`script[src="${src}"]`)) {
                const tStart = Date.now();
                const poll = setInterval(() => {
                    if (window.payhere) {
                        clearInterval(poll);
                        resolve();
                    } else if (Date.now() - tStart > timeoutMs) {
                        clearInterval(poll);
                        reject(new Error('Script present but payhere global not available'));
                    }
                }, 100);
                return;
            }
            const s = document.createElement('script');
            let timedOut = false;
            const t = setTimeout(() => {
                timedOut = true;
                s.onerror = null;
                s.onload = null;
                reject(new Error('Script load timeout'));
            }, timeoutMs);

            s.src = src;
            s.async = true;
            s.onload = () => {
                if (timedOut) return;
                clearTimeout(t);
                resolve();
            };
            s.onerror = () => {
                clearTimeout(t);
                reject(new Error('Failed to load script ' + src));
            };
            document.head.appendChild(s);
        });
    }

    async function waitForPayHere(timeoutMs = 10000) {
        if (window.payhere) return window.payhere;
        try {
            await loadScript(PAYHERE_CDN, Math.min(8000, timeoutMs));
        } catch (e) {
            console.warn('Injecting payhere script failed or timed out:', e.message);
        }
        const pollInterval = 100;
        const start = Date.now();
        while (Date.now() - start < timeoutMs) {
            if (window.payhere) return window.payhere;
            await new Promise(r => setTimeout(r, pollInterval));
        }
        throw new Error('payhere.js not loaded within ' + timeoutMs + 'ms');
    }

    // =================================================================
    // 💡 SUBMISSION & PAYLOAD LOGIC
    // =================================================================

    function saveBillingAddressToLocalStorage() {
        const billingSection = document.getElementById('billing-address-section');
        if (!billingSection) return;
        const billingData = {};
        const billingInputs = billingSection.querySelectorAll('input, select');
        billingInputs.forEach(input => {
            billingData[input.id] = input.value?.trim() || '';
        });
        localStorage.setItem('billingAddress', JSON.stringify(billingData));
    }

    function getItemsDisplayString() {
        if (cartItems.length === 0) return 'Online Order';
        const names = cartItems.map(item => `${item.name} (x${item.quantity})`);
        if (names.length > 3) {
            return `${names.slice(0, 3).join(', ')} and ${names.length - 3} more item(s).`;
        }
        return names.join(', ');
    }

    function buildCheckoutPayload(shippingValue) {
        const getValue = id => document.getElementById(id)?.value?.trim() || '';
        const getSelectValue = id => parseInt(document.getElementById(id)?.value) || null;
        const isSelect = id => document.getElementById(id)?.tagName === 'SELECT';
        const getSelectText = id => {
            const el = document.getElementById(id);
            return (el && el.tagName === 'SELECT' && el.selectedIndex >= 0) ? el.options[el.selectedIndex].text : null;
        };

        // --- Logic to extract City properly ---
        const cityIsSelect = isSelect('city');
        const cityId = cityIsSelect ? getSelectValue('city') : null;
        const cityText = cityIsSelect ? getSelectText('city') : getValue('city');
        const provinceText = isSelect('state') ? getSelectText('state') : getValue('state');

        // Billing City Logic
        let billingCityId = null;
        let billingCityText = null;
        let billingProvinceText = null;

        if (billingCheckbox?.checked) {
            const billingCityIsSelect = isSelect('billingCity');
            billingCityId = billingCityIsSelect ? getSelectValue('billingCity') : null;
            billingCityText = billingCityIsSelect ? getSelectText('billingCity') : getValue('billingCity');
            billingProvinceText = isSelect('billingState') ? getSelectText('billingState') : getValue('billingState');
        }

        return {
            firstName: getValue('firstName'),
            lastName: getValue('lastName'),
            contactNo: getValue('phone'),
            email: getValue('email'),
            addressLine1: getValue('address'),
            addressLine2: getValue('apartment'),
            postalCode: getValue('zip'),
            countryId: getSelectValue('country'),
            provinceId: isSelect('state') ? getSelectValue('state') : null,
            provinceText: provinceText,

            cityId: cityId,
            cityOther: cityText,
            provinceOther: provinceText,

            saveAddress: !!document.getElementById('saveAddress')?.checked,
            differentBilling: !!billingCheckbox?.checked,
            billingFirstName: billingCheckbox.checked ? getValue('billingFirstName') : null,
            billingLastName: billingCheckbox.checked ? getValue('billingLastName') : null,
            billingAddressLine1: billingCheckbox.checked ? getValue('billingAddress') : null,
            billingAddressLine2: billingCheckbox.checked ? getValue('billingApartment') : null,
            billingPostalCode: billingCheckbox.checked ? getValue('billingZip') : null,
            billingCountryId: billingCheckbox.checked ? getSelectValue('billingCountry') : null,
            billingProvinceId: billingCheckbox.checked ? (isSelect('billingState') ? getSelectValue('billingState') : null) : null,
            billingProvinceText: billingCheckbox.checked ? billingProvinceText : null,

            billingCityId: billingCityId,
            billingCityOther: billingCityText,
            billingProvinceOther: billingProvinceText,

            selectedPaymentMethod: document.querySelector('input[name="payment"]:checked')?.value || null,
            selectedShippingMethodValue: shippingValue || null,
            couponCode: getValue('discount-code'),
            orderNotes: getValue('orderNotes'),
            itemsDisplay: getItemsDisplayString(),
            isGift: !!isGiftCheckbox?.checked,
            subscribed: !!subscribeCheckbox?.checked,
            agreeTerms: !!agreeTermsCheckbox?.checked,
            cartSubtotal,
            shippingCost: shippingCost,
            shippingMethodName: document.querySelector('input[name="shipping"]:checked')?.dataset.method || 'Standard',
            taxAmount: 0,
            discountAmount,
            finalTotal: cartSubtotal + shippingCost - discountAmount
        };
    }

    async function submitOrder(payload) {
        // Highlight Step 3 (Confirm) right before submission
        const step3Ind = document.getElementById('step-3-indicator');
        const step3Text = document.getElementById('step-3-text');
        if (step3Ind && step3Text) {
            step3Ind.classList.replace('bg-gray-600', 'bg-gold');
            step3Ind.classList.replace('text-white', 'text-dark');
            step3Text.classList.replace('text-white', 'text-gold');
            
            // Revert Step 2 to gray
            const step2Ind = document.getElementById('step-2-indicator');
            const step2Text = document.getElementById('step-2-text');
            if (step2Ind) {
                step2Ind.classList.replace('bg-gold', 'bg-gray-600');
                step2Ind.classList.replace('text-dark', 'text-white');
                step2Text.classList.replace('text-gold', 'text-white');
            }
        }

        try {
            const res = await fetch(`${API_BASE_URL}/order`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(payload)
            });
            const text = await res.text();
            let data;
            try { data = JSON.parse(text); } catch (e) {
                console.error("Failed to parse JSON response", text);
                notify.error("Server returned an invalid response.");
                return null;
            }
            if (!res.ok || !data.status) {
                notify.error(data?.message || `Order failed (${res.status})`);
                return null;
            }
            return data;
        } catch (err) {
            console.error(err);
            notify.error("Network or server error occurred.");
            return null;
        }
    }

    // --- Validation Helper ---
    function validateInput(id, errorMessage) {
        const el = document.getElementById(id);

        // 👇 අලුතින් එකතු කළ කොටස (Element එක නැත්නම් Error නොදී ඉස්සරහට යන්න)
        if (!el) {
            console.warn(`Validation Skipped: Element #${id} missing`);
            return true;
        }
        // 👆

        const value = el.value;
        const isValid = value && value.toString().trim().length > 0;

        if (!isValid) {
            notify.error(errorMessage);
            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            el.focus();
            el.classList.add('border-red-500');
            const eventType = el.tagName === 'SELECT' ? 'change' : 'input';
            el.addEventListener(eventType, () => el.classList.remove('border-red-500'), { once: true });
            return false;
        }
        return true;
    }

    checkoutForm.addEventListener('submit', async e => {
        e.preventDefault();

        // 1. VALIDATIONS
        if (!agreeTermsCheckbox.checked) {
            notify.error("You must agree to the Terms & Conditions.");
            agreeTermsCheckbox.parentElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
            return;
        }

        const selectedShippingEl = document.querySelector('input[name="shipping"]:checked');
        const isStorePickup = selectedShippingEl && selectedShippingEl.dataset.method === "Store Pickup (BOPIS)";

        if (!validateInput('firstName', 'Please enter your First Name.')) return;
        if (!validateInput('lastName', 'Please enter your Last Name.')) return;
        if (!validateInput('phone', 'Please enter your Phone Number.')) return;

        if (!isStorePickup) {
            if (!validateInput('address', 'Please enter your Address.')) return;
            if (!validateInput('country', 'Please select your Country.')) return;
            if (!validateInput('state', 'Please select or enter your Province/State.')) return;
            if (!validateInput('city', 'Please select or enter your Shipping City.')) return;
            if (!validateInput('zip', 'Please enter your Zip/Postal Code.')) return;
        }

        if (billingCheckbox.checked) {
            if (!validateInput('billingFirstName', 'Please enter Billing First Name.')) return;
            if (!validateInput('billingLastName', 'Please enter Billing Last Name.')) return;
            if (!validateInput('billingAddress', 'Please enter Billing Address.')) return;
            if (!validateInput('billingCountry', 'Please select Billing Country.')) return;
            if (!validateInput('billingState', 'Please select or enter Billing Province/State.')) return;
            if (!validateInput('billingCity', 'Please select or enter your Billing City.')) return;
            if (!validateInput('billingZip', 'Please enter Billing Zip/Postal Code.')) return;
            const billingPhoneEl = document.getElementById('billingPhone');
            if (billingPhoneEl) {
                if (!validateInput('billingPhone', 'Please enter Billing Phone Number.')) return;
            }
        }

        const selectedPaymentMethod = document.querySelector('input[name="payment"]:checked')?.value;
        const selectedShippingValue = document.querySelector('input[name="shipping"]:checked')?.value;

        if (!selectedPaymentMethod || !selectedShippingValue) {
            notify.error("Please select both a shipping and payment method.");
            return;
        }

        if (billingCheckbox.checked && ['card'].includes(selectedPaymentMethod)) {
            saveBillingAddressToLocalStorage();
        } else {
            localStorage.removeItem('billingAddress');
        }

        const payload = buildCheckoutPayload(selectedShippingValue);
        
        // --- Bank Transfer Slip Upload ---
        if (selectedPaymentMethod === 'bank') {
            const bankSlipInput = document.getElementById('bankSlip');
            const bankSlipError = document.getElementById('bankSlip-error');
            
            if (bankSlipError) bankSlipError.textContent = '';
            
            if (!bankSlipInput || !bankSlipInput.files || bankSlipInput.files.length === 0) {
                notify.error("Please upload the payment slip for the Bank Transfer.");
                if (bankSlipError) bankSlipError.textContent = 'Please upload a file.';
                document.getElementById('bank-transfer-details')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
                return;
            }
            
            submitOrderButton.disabled = true;
            submitOrderButton.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Uploading Slip...';
            
            try {
                const formData = new FormData();
                formData.append('file', bankSlipInput.files[0]);
                const uploadRes = await fetch(`${API_BASE_URL}/upload/slip`, {
                    method: 'POST',
                    body: formData
                });
                const uploadData = await uploadRes.json();
                if (uploadData.status && uploadData.url) {
                    payload.bankSlipUrl = uploadData.url;
                } else {
                    notify.error(uploadData.message || "Failed to upload payment slip.");
                    submitOrderButton.disabled = false;
                    submitOrderButton.innerHTML = 'Complete Order';
                    return;
                }
            } catch (err) {
                console.error("Slip upload error", err);
                notify.error("An error occurred while uploading the payment slip.");
                submitOrderButton.disabled = false;
                submitOrderButton.innerHTML = 'Complete Order';
                return;
            }
        }
        // ---------------------------------
        const originalBtnHtml = submitOrderButton.innerHTML;

        submitOrderButton.disabled = true;
        submitOrderButton.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i> Processing...';

        try {
            const result = await submitOrder(payload);

            if (!result) {
                submitOrderButton.disabled = false;
                submitOrderButton.innerHTML = originalBtnHtml;
                return;
            }

            const payType = result.paymentType?.toUpperCase();
            if (payType === 'COD') {
                notify.success("Order placed successfully! Redirecting...");
                setTimeout(() => {
                    if (result.redirectUrl) window.location.href = result.redirectUrl;
                    else window.location.href = '/orders.html';
                }, 1000);
            } else if (['CARD'].includes(payType)) {
                if (!result.payhereData) {
                    notify.error("Payment data missing.");
                    submitOrderButton.disabled = false;
                    submitOrderButton.innerHTML = originalBtnHtml;
                    return;
                }

                const phPayload = { ...result.payhereData };
                phPayload.amount = Number(phPayload.amount || 0).toFixed(2);

                try {
                    const ph = await waitForPayHere(10000);
                    if (phPayload.sandbox === true) {
                        console.log("🔧 Force Sandbox Mode: ON");
                        if (typeof ph.setSandbox === 'function') {
                            ph.setSandbox(true);
                        }
                    }

                    ph.onCompleted = function(orderId) {
                        notify.success('Payment completed.');
                        localStorage.removeItem('billingAddress');
                        window.location.href = `/order-confirmation.html?order=${encodeURIComponent(orderId)}`;
                    };
                    ph.onDismissed = function() {
                        notify.warning('Payment window dismissed.');
                        submitOrderButton.disabled = false;
                        submitOrderButton.innerHTML = originalBtnHtml;
                    };
                    ph.onError = function() {
                        notify.error('Payment error occurred.');
                        submitOrderButton.disabled = false;
                        submitOrderButton.innerHTML = originalBtnHtml;
                    };
                    ph.startPayment(phPayload);

                } catch (err) {
                    const endpoint = phPayload.payhere_post_url;
                    const form = document.createElement('form');
                    form.method = 'POST';
                    form.action = endpoint;
                    form.style.display = 'none';

                    const fieldsToSubmit = [
                        'merchant_id', 'return_url', 'cancel_url', 'notify_url',
                        'order_id', 'items', 'currency', 'amount',
                        'first_name', 'last_name', 'email', 'phone',
                        'address', 'city', 'country', 'hash', 'custom_1'
                    ];

                    fieldsToSubmit.forEach(key => {
                        if (phPayload[key] !== undefined && phPayload[key] !== null) {
                            const input = document.createElement('input');
                            input.type = 'hidden';
                            input.name = key;
                            input.value = String(phPayload[key]);
                            form.appendChild(input);
                        }
                    });
                    document.body.appendChild(form);
                    form.submit();
                }
            } else {
                notify.info("Order placed. Awaiting confirmation...");
                submitOrderButton.disabled = false;
                submitOrderButton.innerHTML = originalBtnHtml;
            }
        } catch (error) {
            console.error('Submission error:', error);
            submitOrderButton.disabled = false;
            submitOrderButton.innerHTML = originalBtnHtml;
        }
    });

    // =================================================================
    // ⚙️ INITIALIZATION & HELPER FUNCTIONS
    // =================================================================

    async function loadCheckoutDetails() {
        try {
            const response = await fetch(`${API_BASE_URL}/addresses/checkout-details`, { credentials: 'include' });
            if (response.status === 401) {
                const emailHelpText = document.getElementById('emailHelp');
                if (emailHelpText) emailHelpText.textContent = "Proceeding as guest. Log in to save your address.";
                window.location.href = 'auth.html';
                return;
            }
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const result = await response.json();
            if (result.status && result.data) {
                const data = result.data;
                prefillData = data;
                if (emailInput && data.email) emailInput.value = data.email;
                if (data.userFirstName) firstNameInput.value = data.userFirstName;
                if (data.userLastName) lastNameInput.value = data.userLastName;

                if (subscribeWrapper && data.subscribed === true) {
                    subscribeWrapper.style.display = 'none';
                    if (subscribeCheckbox) subscribeCheckbox.checked = true;
                } else if (subscribeCheckbox && data.subscribed !== undefined) {
                    subscribeCheckbox.checked = data.subscribed;
                }

                if (data.address && data.address.defaultAddress === true) {
                    hasLoadedAddress = true;
                    if (saveAddressWrapper) saveAddressWrapper.classList.add('hidden');
                    if (saveAddressCheckbox) saveAddressCheckbox.checked = false;

                    const addr = data.address;
                    firstNameInput.value = addr.firstName || data.userFirstName || '';
                    lastNameInput.value = addr.lastName || data.userLastName || '';
                    addressInput.value = addr.addressLine1 || '';
                    apartmentInput.value = addr.addressLine2 || '';
                    zipInput.value = addr.postalCode || '';
                    phoneInput.value = addr.contactNo || '';
                } else {
                    hasLoadedAddress = false;
                    if (saveAddressWrapper) saveAddressWrapper.classList.remove('hidden');
                    if (saveAddressCheckbox) saveAddressCheckbox.checked = false;
                    if (prefillData.address) prefillData.address = null;
                }
            }
        } catch (error) {
            console.error('Error loading details:', error);
            if (error.message.includes('401')) window.location.href = 'auth.html';
        }
    }

    // --- Loading Geo Data ---
    async function loadCountries() {
        if (!countrySelect) return;
        try {
            const response = await fetch(`${API_BASE_URL}/countries`);
            const countries = await response.json();
            countrySelect.innerHTML = '<option value="">Select a country</option>';
            countries.forEach(country => {
                const option = document.createElement('option');
                option.value = country.id;
                option.textContent = country.country;
                option.dataset.code = country.code;
                countrySelect.appendChild(option);
            });
            countrySelect.disabled = false;

            if (prefillData && prefillData.address && prefillData.address.countryId) {
                countrySelect.value = prefillData.address.countryId;
                await loadProvinces(prefillData.address.countryId);
            } else {
                const defaultOption = Array.from(countrySelect.options).find(o => o.dataset.code === 'LK');
                if (defaultOption) {
                    countrySelect.value = defaultOption.value;
                    await loadProvinces(defaultOption.value);
                }
            }
        } catch (error) { console.error(error); }
    }

    // --- UPDATED: Load Provinces (Handles Text/Input Prefill Correctly) ---
    async function loadProvinces(countryId) {
        if (!countryId) return;
        try {
            const response = await fetch(`${API_BASE_URL}/provinces?countryId=${countryId}`);
            const data = await response.json();
            const provinces = data.provinces;
            let createdInput = null;

            // Clean previous
            stateWrapper.innerHTML = '';

            if (provinces && provinces.length > 0) {
                // 1. Create Dropdown
                const stateSelect = document.createElement('select');
                stateSelect.id = 'state';
                stateSelect.name = 'state';
                stateSelect.className = 'form-select';
                stateSelect.required = true;
                stateSelect.innerHTML = '<option value="">Select a province</option>';

                provinces.forEach(p => {
                    const option = document.createElement('option');
                    option.value = p.id;
                    option.textContent = p.province;
                    stateSelect.appendChild(option);
                });

                // Add "Other"
                const otherOpt = document.createElement('option');
                otherOpt.value = 'other';
                otherOpt.textContent = 'Other (Type Province)';
                stateSelect.appendChild(otherOpt);

                // Add Label & Select to DOM
                stateWrapper.innerHTML = `<label for="state" class="form-label">Province *</label>`;
                stateWrapper.appendChild(stateSelect);

                createdInput = stateSelect;

                // Event Listener
                stateSelect.addEventListener('change', () => {
                    if (stateSelect.value === 'other') {
                        createStateInput('Province / State *', false).focus();
                        loadCities(null);
                    } else {
                        loadCities(stateSelect.value);
                    }
                });

            } else {
                // 2. No Provinces -> Create Input directly
                createdInput = createStateInput('Province / State *', false);
                loadCities(null);
            }

            // --- PREFILL LOGIC ---
            if (prefillData && prefillData.address) {
                const addr = prefillData.address;

                if (createdInput.tagName === 'SELECT') {
                    if (addr.provinceId) {
                        // ID exists in DB, Select it
                        createdInput.value = addr.provinceId;
                        createdInput.disabled = false; // Enable!
                        await loadCities(addr.provinceId);
                    } else if (addr.stateText && !['select a province', 'select a country first', 'select a city'].includes(addr.stateText.toLowerCase().trim())) {
                        // Text exists in DB, Switch to Input
                        createdInput.value = 'other';
                        createdInput = createStateInput('Province / State *', false);
                        createdInput.value = addr.stateText;
                        await loadCities(null);
                    } else {
                        await loadCities(null);
                    }
                } else {
                    // Already Input
                    if (addr.stateText && !['select a province', 'select a country first', 'select a city'].includes(addr.stateText.toLowerCase().trim())) {
                        createdInput.value = addr.stateText;
                    }
                    await loadCities(null);
                }
            }

        } catch (error) { console.error(error); }
    }

    // --- UPDATED: Load Cities (Handles Text/Input Prefill Correctly) ---
    async function loadCities(provinceId) {
        if (!provinceId) {
            // If no province ID, we usually default to manual city input unless province was also manual
            let input = createCityInput('City *', false);
            // Check if we need to prefill text
            if (prefillData && prefillData.address && prefillData.address.cityText) {
                input.value = prefillData.address.cityText;
            }
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/cities?provinceId=${provinceId}`);
            const data = await response.json();
            const cities = data.cities;
            let createdInput = null;

            // Clean previous
            cityWrapper.innerHTML = '';

            if (cities && cities.length > 0) {
                // Sort cities
                cities.sort((a, b) => a.city.localeCompare(b.city));

                const citySelect = document.createElement('select');
                citySelect.id = 'city';
                citySelect.name = 'city';
                citySelect.className = 'form-select';
                citySelect.required = true;
                citySelect.innerHTML = '<option value="">Select a city</option>';

                cities.forEach(c => {
                    const option = document.createElement('option');
                    option.value = c.id;
                    option.textContent = c.city;
                    citySelect.appendChild(option);
                });

                const otherOpt = document.createElement('option');
                otherOpt.value = 'other';
                otherOpt.textContent = 'Other (Type City)';
                citySelect.appendChild(otherOpt);

                cityWrapper.innerHTML = `<label for="city" class="form-label">City *</label>`;
                cityWrapper.appendChild(citySelect);
                createdInput = citySelect;

                citySelect.addEventListener('change', () => {
                    if (citySelect.value === 'other') {
                        createCityInput('City Name *', false).focus();
                    }
                });

            } else {
                createdInput = createCityInput('City *', false);
            }

            // --- PREFILL LOGIC ---
            if (prefillData && prefillData.address) {
                const addr = prefillData.address;
                if (createdInput.tagName === 'SELECT') {
                    if (addr.cityId) {
                        createdInput.value = addr.cityId;
                        createdInput.disabled = false; // Enable!
                    } else if (addr.cityText && !['select a province', 'select a country first', 'select a city'].includes(addr.cityText.toLowerCase().trim())) {
                        // Switch to Input
                        createdInput.value = 'other';
                        createdInput = createCityInput('City Name *', false);
                        createdInput.value = addr.cityText;
                    }
                } else if (createdInput.tagName === 'INPUT' && addr.cityText && !['select a province', 'select a country first', 'select a city'].includes(addr.cityText.toLowerCase().trim())) {
                    createdInput.value = addr.cityText;
                }
            }
        } catch (error) { console.error(error); }
    }

    // --- Dynamic Input Helpers (Ensure disabled=false is passed) ---
    function createStateInput(text, disabled) {
        stateWrapper.innerHTML = `<label for="state" class="form-label">${text}</label><input type="text" id="state" name="state" class="form-input" placeholder="State" ${disabled ? 'disabled' : ''} required>`;
        return stateWrapper.querySelector('#state');
    }
    function createCityInput(text, disabled) {
        cityWrapper.innerHTML = `<label for="city" class="form-label">${text}</label><input type="text" id="city" name="city" class="form-input" placeholder="City" ${disabled ? 'disabled' : ''} required>`;
        return cityWrapper.querySelector('#city');
    }

    // --- Billing Loaders ---
    async function loadBillingCountries() {
        if (!billingCountrySelect) return;
        const response = await fetch(`${API_BASE_URL}/countries`);
        const countries = await response.json();
        billingCountrySelect.innerHTML = '<option value="">Select a country</option>';
        countries.forEach(c => {
            const o = document.createElement('option');
            o.value = c.id;
            o.textContent = c.country;
            o.dataset.code = c.code;
            billingCountrySelect.appendChild(o);
        });
        billingCountrySelect.disabled = false;
        const def = Array.from(billingCountrySelect.options).find(o => o.dataset.code === 'LK');
        if (def) { billingCountrySelect.value = def.value; await loadBillingProvinces(def.value); }
    }
    async function loadBillingProvinces(cid) {
        createBillingCitySelect('Select a province first', true);
        if(!cid) return;
        const res = await fetch(`${API_BASE_URL}/provinces?countryId=${cid}`);
        const data = await res.json();
        if(data.provinces?.length > 0) {
            const s = createBillingStateSelect('Select a province', false);
            data.provinces.forEach(p => { const o = document.createElement('option'); o.value=p.id; o.textContent=p.province; s.appendChild(o); });
            
            const otherOpt = document.createElement('option');
            otherOpt.value = 'other';
            otherOpt.textContent = 'Other (Type Province)';
            s.appendChild(otherOpt);
            
            s.addEventListener('change', () => {
                if (s.value === 'other') {
                    createBillingStateInput('Province / State *', false).focus();
                    loadBillingCities(null);
                } else {
                    loadBillingCities(s.value);
                }
            });
        } else {
            createBillingStateInput('Province / State *', false); createBillingCityInput('City *', false);
        }
    }
    async function loadBillingCities(pid) {
        if(!pid) { createBillingCityInput('City *', false); return; }
        const res = await fetch(`${API_BASE_URL}/cities?provinceId=${pid}`);
        const data = await res.json();

        if(data.cities?.length > 0) {
            // Sort cities alphabetically (Ascending Order)
            data.cities.sort((a, b) => a.city.localeCompare(b.city));

            const s = createBillingCitySelect('Select a city', false);
            data.cities.forEach(c => {
                const o = document.createElement('option');
                o.value = c.id;
                o.textContent = c.city;
                s.appendChild(o);
            });

            const other = document.createElement('option');
            other.value = 'other';
            other.textContent = 'Other (Type City)';
            s.appendChild(other);

            s.addEventListener('change', (e) => {
                if(e.target.value === 'other') {
                    createBillingCityInput('City Name *', false).focus();
                }
            });

        } else {
            createBillingCityInput('City *', false);
        }
    }

    function createBillingStateSelect(t, d) { billingStateWrapper.innerHTML = `<label class="form-label">Province *</label><select id="billingState" class="form-select" ${d?'disabled':''} required><option value="">${t}</option></select>`; return billingStateWrapper.querySelector('#billingState'); }
    function createBillingStateInput(t, d) { billingStateWrapper.innerHTML = `<label class="form-label">${t}</label><input type="text" id="billingState" class="form-input" placeholder="State" ${d?'disabled':''} required>`; return billingStateWrapper.querySelector('#billingState'); }
    function createBillingCitySelect(t, d) { billingCityWrapper.innerHTML = `<label class="form-label">City *</label><select id="billingCity" class="form-select" ${d?'disabled':''} required><option value="">${t}</option></select>`; return billingCityWrapper.querySelector('#billingCity'); }
    function createBillingCityInput(t, d) { billingCityWrapper.innerHTML = `<label class="form-label">${t}</label><input type="text" id="billingCity" class="form-input" placeholder="City" ${d?'disabled':''} required>`; return billingCityWrapper.querySelector('#billingCity'); }

    // --- Loading Methods ---
    async function loadShippingMethods() {
        if (!shippingContainer) return;
        const res = await fetch(`${API_BASE_URL}/shipping-methods`);
        const result = await res.json();
        
        // Filter out POS store pickup from DB to avoid duplication
        let methods = result.methods.filter(m => !m.shippingMethod.toLowerCase().includes('pickup'));
        
        // Inject BOPIS option
        methods.unshift({
            shippingMethod: "Store Pickup (BOPIS)",
            description: "Pick up at our flagship store. Usually ready in 2 hours.",
            value: 0.0
        });

        shippingContainer.innerHTML = '';
        methods.forEach((m, i) => {
            const active = i === 0 ? 'active' : '';
            const checked = i === 0 ? 'checked' : '';
            if (i === 0) shippingCost = m.value;
            const html = `<div class="payment-method ${active}" data-shipping="${m.value}">
                <div class="flex items-center justify-between"><div class="flex items-center flex-1">
                <input type="radio" id="ship-${i}" name="shipping" value="${m.value}" data-method="${m.shippingMethod}" ${checked} class="mr-3 w-4 h-4">
                <label for="ship-${i}" class="cursor-pointer flex-1"><div class="font-semibold">${m.shippingMethod}</div>
                <div class="text-sm text-gray-600">${m.description || ''}</div></label></div>
                <div class="font-bold text-gold">${m.value === 0 ? 'FREE' : 'LKR ' + m.value.toFixed(0)}</div></div></div>`;
            shippingContainer.insertAdjacentHTML('beforeend', html);
        });

        // Hide shipping initially if Store pickup is first
        const initMethod = document.querySelector('input[name="shipping"]:checked');
        const shipSec = document.getElementById('shipping-address-section');
        if(initMethod && initMethod.dataset.method === "Store Pickup (BOPIS)") {
            if(shipSec) shipSec.style.display = 'none';
        }

        shippingContainer.querySelectorAll('.payment-method').forEach(el => el.addEventListener('click', () => {
            shippingContainer.querySelectorAll('.payment-method').forEach(m => m.classList.remove('active'));
            el.classList.add('active');
            el.querySelector('input').checked = true;
            shippingCost = parseFloat(el.querySelector('input').value);
            
            // Toggle Address block
            const methodName = el.querySelector('input').dataset.method;
            
            // Update COD label dynamically
            const isPickup = methodName === "Store Pickup (BOPIS)";
            document.querySelectorAll('.cod-label-text').forEach(span => {
                span.textContent = isPickup ? 'Pay at Store' : 'Cash on Delivery';
            });

            if(shipSec) {
                shipSec.style.display = isPickup ? 'none' : 'block';
                // Toggle required fields
                shipSec.querySelectorAll('input, select').forEach(inp => {
                    if(isPickup) {
                        inp.removeAttribute('required');
                    } else {
                        inp.setAttribute('required', 'required');
                    }
                });
            }
            
            calculateAndDisplayTotals();
        }));
    }

    async function loadPaymentMethods() {
        if (!paymentContainer) return;
        const res = await fetch(`${API_BASE_URL}/payment-methods`);
        const result = await res.json();
        paymentContainer.innerHTML = '';
        
        // Filter out "Cash" since it's for POS only (Keep Cash on Delivery)
        let methods = result.methods.filter(m => m.method.trim().toLowerCase() !== 'cash');

        methods.forEach((m, i) => {
            const active = i === 0 ? 'active' : '';
            const checked = i === 0 ? 'checked' : '';
            let icon = 'fas fa-question-circle text-gold';
            let val = 'cod';
            let displayMethod = m.method;
            let labelClass = '';
            
            const initMethod = document.querySelector('input[name="shipping"]:checked');
            const isStorePickup = initMethod && initMethod.dataset.method === "Store Pickup (BOPIS)";

            if(m.method.includes('Card')) { icon = 'fas fa-credit-card text-gold'; val = 'card'; }
            else if(m.method.includes('Cash on Delivery') || m.method.includes('COD')) { 
                icon = 'fas fa-money-bill-wave text-gold'; 
                val = 'cod'; 
                labelClass = 'cod-label-text';
                if (isStorePickup) displayMethod = 'Pay at Store';
            }
            else if(m.method.includes('Bank')) { icon = 'fas fa-university text-gold'; val = 'bank'; }

            const html = `<div class="payment-method ${active}" data-payment="${val}">
                <div class="flex items-center"><input type="radio" id="pay-${val}" name="payment" value="${val}" ${checked} class="mr-3 w-4 h-4">
                <label for="pay-${val}" class="flex flex-col cursor-pointer flex-1">
                <div class="flex items-center space-x-2"><i class="${icon}"></i><span class="font-semibold ${labelClass}">${displayMethod}</span></div></label></div></div>`;
            paymentContainer.insertAdjacentHTML('beforeend', html);
        });
        
        const initialPayment = document.querySelector('input[name="payment"]:checked');
        const bankDetails = document.getElementById('bank-transfer-details');
        if (bankDetails && initialPayment && initialPayment.value === 'bank') {
            bankDetails.classList.remove('hidden');
        } else if (bankDetails) {
            bankDetails.classList.add('hidden');
        }
        
        paymentContainer.querySelectorAll('.payment-method').forEach(el => el.addEventListener('click', () => {
            paymentContainer.querySelectorAll('.payment-method').forEach(m => m.classList.remove('active'));
            el.classList.add('active');
            el.querySelector('input').checked = true;
            
            if (bankDetails) {
                if (el.dataset.payment === 'bank') {
                    bankDetails.classList.remove('hidden');
                } else {
                    bankDetails.classList.add('hidden');
                }
            }
            
            calculateAndDisplayTotals();
        }));
    }

    // --- Order Summary & Discount ---
    async function handleApplyDiscount() {
        const code = discountInput.value.trim().toUpperCase();
        if (!code) { notify.warning('Enter code'); return; }
        discountButton.disabled = true;
        try {
            const response = await fetch(`${API_BASE_URL}/discounts/validate`, {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code: code, subtotal: cartSubtotal })
            });
            const result = await response.json();
            if (result.valid) {
                discountAmount = result.discountAmount;
                discountAmountSpan.textContent = `- LKR ${discountAmount.toLocaleString('en-US', {minimumFractionDigits:2})}`;
                if (discountRow) discountRow.style.display = 'flex';
                notify.success(result.message);
                calculateAndDisplayTotals();
            } else {
                notify.error(result.message);
                discountAmount = 0;
                if(discountRow) discountRow.style.display = 'none';
                calculateAndDisplayTotals();
            }
        } catch (e) { notify.error('Error validating coupon'); }
        finally { discountButton.disabled = false; }
    }

    async function loadOrderSummary() {
        try {
            const res = await fetch(`${API_BASE_URL}/cart`, { credentials: 'include' });
            if(res.status === 401) {
                sessionStorage.setItem('returnUrl', window.location.href);
                window.location.href='auth.html';
                return;
            }
            const result = await res.json();
            if(result.success) {
                cartItems = result.cartItems || [];
                cartSubtotal = result.subtotal || 0;
                cartTax = result.tax || 0;
                displayOrderItems();
            }
        } catch(e) { console.error(e); }
    }

    function displayOrderItems() {
        if (!orderItemsContainer) return;
        let html = '<div class="space-y-4">';
        cartItems.forEach(item => {
            const price = item.finalPrice || item.price || 0;
            const total = price * item.quantity;
            html += `<div class="flex items-center gap-3 pb-4 border-b border-gray-100">
                <img src="${item.image}" class="w-16 h-16 object-cover" loading="lazy">
                <div class="flex-1"><h4 class="font-semibold text-sm">${item.name}</h4><p class="text-xs text-gray-500">Qty: ${item.quantity}</p></div>
                <div class="font-bold text-sm">LKR ${total.toLocaleString('en-US', {minimumFractionDigits:2})}</div></div>`;
        });
        html += '</div>';
        orderItemsContainer.innerHTML = html;
    }

    function calculateAndDisplayTotals() {
        const finalTotal = cartSubtotal + shippingCost - discountAmount;
        const fmt = v => `LKR ${v.toLocaleString('en-US', {minimumFractionDigits:2})}`;
        if(subtotalSpan) subtotalSpan.textContent = fmt(cartSubtotal);
        if(shippingSpan) shippingSpan.textContent = shippingCost === 0 ? 'Free' : fmt(shippingCost);
        if(totalSpan) totalSpan.textContent = fmt(Math.max(0, finalTotal));

        // Installments
        const payMethod = document.querySelector('input[name="payment"]:checked')?.value;
        if(installmentContainer) {
            installmentContainer.innerHTML = '';
        }
    }

    async function checkIsBuyNow() {
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('mode') === 'buynow') return true;
        try {
            const res = await fetch(`${API_BASE_URL}/order/buy-now`, { credentials: 'include' });
            const r = await res.json();
            return r.status && r.data;
        } catch(e) { return false; }
    }

    async function loadBuyNowItem() {
        try {
            const res = await fetch(`${API_BASE_URL}/order/buy-now`, { credentials: 'include' });
            const result = await res.json();
            if(result.status) {
                const item = result.data;
                cartItems = [{
                    name: item.name, quantity: item.quantity, price: item.price,
                    image: item.image, finalPrice: item.finalPrice
                }];
                cartSubtotal = item.subtotal;
                cartTax = item.tax;
                displayOrderItems();
                calculateAndDisplayTotals();
                return true;
            }
        } catch(e) { console.error(e); }
        return false;
    }

// ✅ Checkout පිටුව පෙන්වන සහ ලෝඩරය අයින් කරන Helper Function එක
    function revealCheckoutPage() {
        const header = document.getElementById('checkout-header');
        const main = document.getElementById('main-content');

        // Header එක සහ Main Content එක පෙන්වීම
        if (header) header.style.display = 'block';
        if (main) {
            main.style.display = 'block';
            main.classList.add('animate__animated', 'animate__fadeIn');
        }

        if (window.loader) {
            // Dropdowns සහ Categories Render වීමට තත්පර 0.5ක සහනයක් ලබා දෙයි
            setTimeout(() => {
                window.loader.hide();
            }, 500);
        }
    }
// ✅ Checkout පිටුවේ ප්‍රධාන init ශ්‍රිතය
    async function init() {
        // 1. වහාම ලෝඩරය පෙන්වීම
        if (window.loader) window.loader.show();

        try {
            // 2. සියලුම පද්ධති (දත්ත + Components) එකවර ලෝඩ් වන තෙක් බලා සිටීම
            // Footer Categories පිරෙන තෙක් loadAllComponents() මඟින් බලා සිටියි
            await Promise.all([
                loadCheckoutDetails(),
                loadCountries(),
                loadShippingMethods(),
                loadPaymentMethods(),
                window.componentLoader ? window.componentLoader.loadAllComponents() : Promise.resolve()
            ]);

            // 3. Buy Now හෝ Cart දත්ත ලබා ගැනීම
            const isBuyNow = await checkIsBuyNow();
            if (isBuyNow) {
                await loadBuyNowItem();
            } else {
                await loadOrderSummary();
            }

            // 4. ගණනය කිරීම් සිදු කිරීම
            calculateAndDisplayTotals();

            // 5. Listeners සම්බන්ධ කිරීම
            bindLocalListeners();

        } catch (error) {
            console.error("Checkout initialization failed:", error);
            // දෝෂයක් ආවත් ලෝඩරය හිර නොවී පිටුව පෙන්වයි
        } finally {
            // 6. සියල්ල සාර්ථක වූ පසු (Footer ඇතුළුව) පිටුව පෙන්වීම
            revealCheckoutPage();
        }
    }
// ✅ Event Listeners සම්බන්ධ කිරීමේ ශ්‍රිතය
    function bindLocalListeners() {
        // Country Change
        if(countrySelect) {
            countrySelect.addEventListener('change', e => loadProvinces(e.target.value));
        }

        // Billing Country Change
        if(billingCountrySelect) {
            billingCountrySelect.addEventListener('change', e => loadBillingProvinces(e.target.value));
        }

        // Billing Address Toggle
        if(billingCheckbox) {
            billingCheckbox.addEventListener('change', () => {
                if(billingCheckbox.checked) {
                    billingSection.classList.remove('hidden');
                    if(!billingCountriesLoaded) {
                        loadBillingCountries();
                        billingCountriesLoaded = true;
                    }
                } else {
                    billingSection.classList.add('hidden');
                }
            });
        }

        // Discount Apply
        if(discountButton) {
            discountButton.addEventListener('click', handleApplyDiscount);
        }
    }
// අවසානයට init call කිරීම
    init();

    // --- Step Indicator Logic ---
    function setupStepIndicators() {
        const step1Ind = document.getElementById('step-1-indicator');
        const step1Text = document.getElementById('step-1-text');
        const step2Ind = document.getElementById('step-2-indicator');
        const step2Text = document.getElementById('step-2-text');
        
        const paymentSection = document.getElementById('payment-methods-container');
        
        if (!paymentSection || !step1Ind || !step2Ind) return;

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    // Payment section in view -> Highlight Step 2
                    step2Ind.classList.replace('bg-gray-600', 'bg-gold');
                    step2Ind.classList.replace('text-white', 'text-dark');
                    step2Text.classList.replace('text-white', 'text-gold');
                    
                    step1Ind.classList.replace('bg-gold', 'bg-gray-600');
                    step1Ind.classList.replace('text-dark', 'text-white');
                    step1Text.classList.replace('text-gold', 'text-white');
                } else {
                    // Payment section out of view (scrolled up) -> Highlight Step 1
                    step1Ind.classList.replace('bg-gray-600', 'bg-gold');
                    step1Ind.classList.replace('text-white', 'text-dark');
                    step1Text.classList.replace('text-white', 'text-gold');
                    
                    step2Ind.classList.replace('bg-gold', 'bg-gray-600');
                    step2Ind.classList.replace('text-dark', 'text-white');
                    step2Text.classList.replace('text-gold', 'text-white');
                }
            });
        }, { threshold: 0.5 });
        
        observer.observe(paymentSection);
    }
    
    setupStepIndicators();

});