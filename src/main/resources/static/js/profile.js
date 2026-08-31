import Notification from './notification.js';

document.addEventListener("DOMContentLoaded", async () => {
    const notif = Notification();

    // --- DOM Elements ---
    const profileImg = document.getElementById("profile-image");
    const fnameInput = document.getElementById("fname");
    const lnameInput = document.getElementById("lname");
    const mobileInput = document.getElementById("mobile");
    const saveBtn = document.querySelector("#profile form button");
    const emailEl = document.getElementById("profile-email");
    const profileNameEl = document.getElementById("profile-name");
    const changePhotoBtn = document.getElementById("change-photo-btn");
    const uploadInput = document.getElementById("upload-photo");

    const tabs = document.querySelectorAll(".tab-button");
    const contents = document.querySelectorAll(".tab-content");

    let userEmail = null;
    let userId = null;
    let hasCustomImage = false; // Track if user uploaded a custom image

    // --- Utilities ---
    const isValidMobile = (mobile) => {
        if (!mobile) return true;
        return /^\+?\d{10,15}$/.test(mobile);
    };

    // --- Generate Square Avatar ---
    const generateAvatar = (name, size = 120) => {
        if (!name || name.trim() === '') {
            name = 'User'; // Fallback for empty names
        }
        const initials = name.split(' ')
            .map(n => n[0]?.toUpperCase())
            .filter(Boolean)
            .slice(0, 2)
            .join('');
        const hue = (name.charCodeAt(0) || 0) * 13 % 360;
        const bgColor = `hsl(${hue}, 70%, 50%)`;
        const textColor = '#fff';

        return `data:image/svg+xml;base64,${btoa(`
            <svg width="${size}" height="${size}" xmlns="http://www.w3.org/2000/svg">
                <rect width="${size}" height="${size}" fill="${bgColor}" />
                <text x="50%" y="50%" text-anchor="middle" dominant-baseline="central" 
                    fill="${textColor}" font-size="${size/2.5}" font-family="Arial" font-weight="bold">
                    ${initials}
                </text>
            </svg>
        `)}`;
    };

    // Function to load profile image with proper fallback
    const loadProfileImage = (imageUrl, fullName) => {
        // Set fallback avatar initially
        const avatar = generateAvatar(fullName);

        if (!imageUrl) {
            profileImg.src = avatar;
            hasCustomImage = false;
            return;
        }

        // Try loading the Cloudinary URL directly
        const img = new Image();
        img.onload = () => {
            profileImg.src = imageUrl;
            hasCustomImage = true;
        };
        img.onerror = () => {
            console.warn('Failed to load profile image, using avatar.');
            profileImg.src = avatar;
            hasCustomImage = false;
        };
        img.src = imageUrl;
    };

    // --- Tab Switching ---
    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(t => t.classList.remove("active"));
            contents.forEach(c => c.classList.add("hidden"));
            tab.classList.add("active");
            document.getElementById(tab.dataset.tab).classList.remove("hidden");
        });
    });

    // --- Load Profile ---
    window.loadProfile = async function loadProfile() {
        // 1. දත්ත අවශ්‍ය නිසා ලෝඩරය පෙන්වීම ආරම්භ කිරීම

        try {
            const res = await fetch("/api/profile", {
                credentials: "include",
                cache: 'no-cache'
            });
            const data = await res.json();

            if (!data.status) {
                notif.error(data.message || "Failed to load profile");
                revealPage();
                return false;
            }

            userEmail = data.email;
            userId = data.id;
            fnameInput.value = data.fname || "";
            lnameInput.value = data.lname || "";
            mobileInput.value = data.mobile || "";
            emailEl.textContent = userEmail;

            const fullName = `${data.fname || ''} ${data.lname || ''}`.trim() || 'User';
            profileNameEl.textContent = fullName;

            // Load Image (data.profileImage is now a Cloudinary URL)
            loadProfileImage(data.profileImage, fullName);

            return true;
        } catch (err) {
            console.error('Profile load error:', err);
            notif.error("Error fetching profile. Refresh the page.");
            return false;
        }finally {
            // ✅ 2. දත්ත සියල්ල ලැබී අවසන් වූ පසු පමණක් පිටුව පෙන්වීම
            revealPage();
        }
    }

    function revealPage() {
        const mainContent = document.getElementById('main-content');
        if (mainContent) {
            mainContent.style.display = 'block';
            mainContent.classList.add('animate__animated', 'animate__fadeIn');
        }

        if (window.loader) {
            // පින්තූර Render වීමට තත්පර 0.5ක සහනයක් ලබා දෙන්න
            setTimeout(() => {
                window.loader.hide();
            }, 500);
        }
    }
    // --- Save Profile ---
    if (saveBtn) {
        saveBtn.addEventListener("click", async (e) => {
            e.preventDefault();
            if (!userEmail) return notif.error("Profile not loaded yet");

            const payload = {
                fname: fnameInput.value.trim(),
                lname: lnameInput.value.trim(),
                mobile: mobileInput.value.trim()
            };

            if (!payload.fname || !payload.lname)
                return notif.warning("First name and Last name are required!");
            if (!isValidMobile(payload.mobile))
                return notif.warning("Please enter a valid mobile number (10-15 digits).");

            saveBtn.disabled = true;
            saveBtn.textContent = "Saving...";

            try {
                const res = await fetch("/api/profile/update", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    credentials: "include",
                    body: JSON.stringify(payload)
                });
                const result = await res.json();

                if (result.status) {
                    notif.success(result.message);
                    const fullName = `${payload.fname} ${payload.lname}`;
                    profileNameEl.textContent = fullName;

                    // If user hasn't uploaded a photo, regenerate avatar with new name
                    if (!hasCustomImage) {
                        profileImg.src = generateAvatar(fullName);
                    }
                } else {
                    notif.error(result.message);
                }
            } catch (err) {
                console.error(err);
                notif.error("Failed to update profile");
            } finally {
                saveBtn.disabled = false;
                saveBtn.textContent = "Save Changes";
            }
        });
    }

    // --- Upload Profile Image ---
    if (changePhotoBtn && uploadInput) {
        changePhotoBtn.addEventListener("click", () => uploadInput.click());

        uploadInput.addEventListener("change", async () => {
            const file = uploadInput.files[0];
            if (!file) return;

            if (!file.type.startsWith('image/')) {
                notif.error("Please select a valid image file");
                uploadInput.value = "";
                return;
            }

            if (file.size > 5 * 1024 * 1024) {
                notif.error("Image size should be less than 5MB");
                uploadInput.value = "";
                return;
            }

            const formData = new FormData();
            formData.append("image", file);

            const originalSrc = profileImg.src;
            profileImg.style.opacity = '0.5';

            try {
                const res = await fetch("/api/profile/upload-image", {
                    method: "POST",
                    body: formData,
                    credentials: "include"
                });

                const data = await res.json();
                if (data.status) {
                    notif.success(data.message);

                    // data.imagePath is the Cloudinary URL
                    const newImageUrl = data.imagePath;

                    const img = new Image();
                    img.onload = () => {
                        profileImg.src = newImageUrl;
                        profileImg.style.opacity = '1';
                        hasCustomImage = true;
                    };
                    img.onerror = () => {
                        notif.warning("Image uploaded but failed to display. Refresh the page.");
                        profileImg.src = originalSrc;
                        profileImg.style.opacity = '1';
                    };
                    img.src = newImageUrl;
                } else {
                    notif.error(data.message);
                    profileImg.style.opacity = '1';
                }
            } catch (err) {
                console.error('Upload error:', err);
                notif.error("Image upload failed");
                profileImg.src = originalSrc;
                profileImg.style.opacity = '1';
            } finally {
                uploadInput.value = "";
            }
        });
    }

    // --- Initialize ---
    const isMasterScriptPresent = window.loader && document.getElementById('main-content');
    if (!isMasterScriptPresent) {
        await window.loadProfile();
    }
});