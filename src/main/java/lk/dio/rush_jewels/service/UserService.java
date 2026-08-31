package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.model.Status;
import lk.dio.rush_jewels.repository.UserRepository;
import lk.dio.rush_jewels.repository.StatusRepository;
import lk.dio.rush_jewels.validation.Validation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StatusRepository statusRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Validation validation;

    public UserService(
            UserRepository userRepository,
            StatusRepository statusRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            Validation validation
    ) {
        this.userRepository = userRepository;
        this.statusRepository = statusRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.validation = validation;
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }
    // ====================================================================
    // 🔍 Find User by Email
    // ====================================================================
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ====================================================================
    // 👤 Local Registration Logic
    // ====================================================================
    @Transactional
    public User registerLocalUser(String fname, String lname, String email, String rawPassword) throws Exception {

        // ✅ Validate email
        if (!validation.isEmailValid(email)) {
            throw new Exception("Invalid email format.");
        }

        // ✅ Validate password strength
        if (!validation.isPasswordValid(rawPassword)) {
            throw new Exception("Password must meet complexity requirements.");
        }

        // ✅ Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("A user with this email already exists.");
        }

        // ✅ Always use status_id = 1
        Status defaultStatus = statusRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Default Status (ID 1) not found."));

        // ✅ Create user object
        User newUser = new User();
        newUser.setFname(fname);
        newUser.setLname(lname);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setCreatedAt(new Date());
        newUser.setLoginProvider("LOCAL");
        newUser.setStatus(defaultStatus);
        newUser.setType("USER");

        // ✅ Generate verification code (valid for 15 minutes)
        String verificationCode = validation.generateCode();
        newUser.setVerification(verificationCode);

        // 15 minutes = 15 * 60 * 1000 milliseconds
        newUser.setVerificationExpiry(new Date(System.currentTimeMillis() + 15 * 60 * 1000L));

        // ✅ Save and send verification email
        User savedUser = userRepository.save(newUser);
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerification());

        return savedUser;
    }

    // ====================================================================
    // 🌐 Social Registration Logic
    // ====================================================================
    @Transactional
    public User registerSocialUser(String fname, String lname, String email, String loginProvider, String providerId) {

        // ✅ Always use status_id = 1 (active by default)
        Status defaultStatus = statusRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Default Status (ID 1) not found."));

        // ✅ Create social user
        User newUser = new User();
        newUser.setFname(fname);
        newUser.setLname(lname);
        newUser.setEmail(email);
        newUser.setCreatedAt(new Date());
        newUser.setLoginProvider(loginProvider);
        newUser.setProviderId(providerId);
        newUser.setStatus(defaultStatus);
        newUser.setVerification("Verified");
        newUser.setVerificationExpiry(null);
        newUser.setType("USER");

        return userRepository.save(newUser);
    }

    /**
     * Process OAuth2 user - find existing or create new
     * Used by OAuth2 authentication flow
     */
    @Transactional
    public User processOAuth2User(String email, String name, String provider) {
        // Check if user exists
        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            // Update last login info
            existingUser.setLoginProvider(provider);
            return userRepository.save(existingUser);
        }

        // Create new user
        Status defaultStatus = statusRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Default Status (ID 1) not found."));

        // Split name into first and last name
        String[] nameParts = (name != null) ? name.split(" ", 2) : new String[]{"User", ""};
        String fname = nameParts.length > 0 ? nameParts[0] : "User";
        String lname = nameParts.length > 1 ? nameParts[1] : "";

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFname(fname);
        newUser.setLname(lname);
        newUser.setCreatedAt(new Date());
        newUser.setLoginProvider(provider);
        newUser.setStatus(defaultStatus);
        newUser.setVerification("Verified");
        newUser.setVerificationExpiry(null);
        newUser.setPassword(null); // OAuth2 users don't have passwords
        newUser.setType("USER");

        return userRepository.save(newUser);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public boolean resendVerificationCode(String email) throws Exception {
        Optional<User> optional = userRepository.findByEmail(email);

        if (optional.isEmpty()) return false;

        User user = optional.get();

        // generate new 6-digit code
        String newCode = validation.generateCode();
        user.setVerification(newCode);

        // ✅ Updated resend expiry to 15 minutes as well for consistency
        user.setVerificationExpiry(new Date(System.currentTimeMillis() + 15 * 60 * 1000L));

        userRepository.save(user);
        emailService.sendVerificationEmail(email, newCode);

        return true;
    }

    // ====================================================================
    // --- NEW: Update Subscription Status ---
    // ====================================================================
    @Transactional
    public void updateSubscriptionStatus(String email, boolean isSubscribed) {
        // Find the user by their email (which is their username)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Update the subscribed status
        user.setSubscribed(isSubscribed);

        // Save the updated user entity
        userRepository.save(user);
    }
}