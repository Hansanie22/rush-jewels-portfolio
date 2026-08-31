package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.UserRepository;
import lk.dio.rush_jewels.validation.Validation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture; // ✅ අලුතින් එකතු කළ Import එක

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final Validation validation;
    private final EmailService emailService;

    public PasswordResetService(UserRepository userRepository, Validation validation, EmailService emailService) {
        this.userRepository = userRepository;
        this.validation = validation;
        this.emailService = emailService;
    }

    @Transactional
    public boolean sendResetCode(String email) throws Exception {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        if (!validation.isEmailValid(email)) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("No account found with that email.");
        }

        User user = optionalUser.get();
        Date now = new Date();

        // Check if previous code is still valid
        if (user.getPassword() != null && user.getVerificationExpiry() != null &&
                user.getVerificationExpiry().after(now)) {
            throw new IllegalArgumentException("A verification code has already been sent. Please wait.");
        }

        // Generate 6-digit numeric code
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        // Temporarily store code in password column and set expiry
        user.setPassword(code);
        user.setVerificationExpiry(new Date(System.currentTimeMillis() + 15 * 60 * 1000L)); // 15 mins
        userRepository.save(user); // ✅ Database Transaction එක මෙතනින් ඉවරයි. Connection එක නිදහස් වෙනවා.

        // Send email with NEW LUXURY DESIGN (Design එක 100% එලෙසම ඇත)
        String subject = "Reset Your Rush Jewels Password";

        String body = "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Rush Jewels - Reset Password</title>" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Bodoni+Moda:ital,wght@0,400;0,700;1,400&family=Lato:wght@300;400;700&display=swap\" rel=\"stylesheet\">" +
                "    <style>" +
                "        body { font-family: 'Lato', sans-serif; line-height: 1.6; color: #1a1a1a; background-color: #e5e5e5; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; }" +
                "        .wrapper { width: 100%; background-color: #e5e5e5; padding: 40px 20px; display: table; justify-content: center; box-sizing: border-box; }" +
                "        .container { width: 100%; max-width: 600px; background: #ffffff; border-radius: 0; margin: 0 auto; border: 1px solid #dcdcdc; }" +
                "        .header { background: #121212; color: #C5A059; padding: 50px 20px; text-align: center; border-bottom: 4px solid #C5A059; }" +
                "        .header-logo { font-family: 'Playfair Display', serif; font-size: 42px; font-weight: 700; margin: 0; text-transform: uppercase; color: #C5A059; }" +
                "        .header-subtitle { font-size: 11px; letter-spacing: 4px; text-transform: uppercase; color: #C5A059; margin-top: 10px; font-weight: 400; }" +
                "        .content { padding: 50px 40px; text-align: center; }" +
                "        .hero-text { font-family: 'Bodoni Moda', serif; font-size: 32px; color: #121212; margin: 0 0 15px 0; font-style: italic; font-weight: 700; }" +
                "        .otp-box { background-color: #F9F8F4; border: 1px solid #C5A059; padding: 30px 0; margin: 30px 0; }" +
                "        .otp-code { font-family: 'Playfair Display', serif; font-size: 48px; font-weight: 700; letter-spacing: 5px; color: #121212; display: block; }" +
                "        .contact-strip { margin-top: 40px; border-top: 1px solid #e0e0e0; padding-top: 20px; font-size: 12px; color: #666; text-align: center; }" +
                "        .footer { background: #111; color: #666; padding: 30px 20px; text-align: center; font-size: 11px; text-transform: uppercase; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"wrapper\">" +
                "        <center>" +
                "        <div class=\"container\">" +
                "            <div class=\"header\">" +
                "                <h1 class=\"header-logo\">Rush Jewels</h1>" +
                "                <div class=\"header-subtitle\">Est. 2025 • Kandy</div>" +
                "            </div>" +
                "            <div class=\"content\">" +
                "                <h2 class=\"hero-text\">Reset Password</h2>" +
                "                <p style=\"color:#666; font-size:16px; margin:0;\">Hello " + user.getFname() + " " + user.getLname() + ",</p>" +
                "                <p style=\"color:#666; font-size:16px;\">We received a request to reset your password.</p>" +
                "                <div class=\"otp-box\">" +
                "                    <span class=\"otp-code\">" + code + "</span>" +
                "                </div>" +
                "                <p style=\"color:#666; font-size:13px;\">This code expires in 15 minutes.<br>Use it to create a new password.</p>" +
                "               <div class=\"contact-strip\">" +
                "                    <div style=\"margin-bottom: 5px;\">rushjewelsofficial@gmail.com</div>" +
                "                    <div>+94 75 483 2960</div>" +
                "                </div>" +
                "            </div>" +
                "            <div class=\"footer\">" +
                "                &copy; 2025 Rush Jewels. All rights reserved." +
                "            </div>" +
                "        </div>" +
                "        </center>" +
                "    </div>" +
                "</body>" +
                "</html>";

        // ✅ ASYNC EMAIL SENDING
        // මෙය Background එකේ ධාවනය වන නිසා App එක හිර වෙන්නේ නෑ.
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendHtmlEmail(user.getEmail(), subject, body);
            } catch (Exception e) {
                System.err.println("Failed to send background email: " + e.getMessage());
            }
        });

        return true;
    }

    @Transactional
    public boolean verifyResetCode(String email, String code) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Verification code cannot be empty.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("No account found with that email.");
        }

        User user = optionalUser.get();
        Date now = new Date();

        // Check if the code matches
        if (user.getPassword() == null || !user.getPassword().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code.");
        }

        // Check if the code has expired
        if (user.getVerificationExpiry() == null || user.getVerificationExpiry().before(now)) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }

        // Code is valid and not expired - keep it in password field for account.html to use
        return true;
    }

    @Transactional
    public boolean verifyCodeAndResetPassword(String email, String code, String newPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) throw new IllegalArgumentException("User not found");

        User user = optionalUser.get();
        Date now = new Date();

        if (user.getPassword() == null || !user.getPassword().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        if (user.getVerificationExpiry() == null || user.getVerificationExpiry().before(now)) {
            throw new IllegalArgumentException("Verification code expired");
        }

        // Save new password
        user.setPassword(newPassword);
        user.setVerificationExpiry(null); // Clear expiry
        userRepository.save(user);

        return true;
    }
}