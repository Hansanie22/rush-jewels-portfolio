package lk.dio.rush_jewels.validation;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.regex.Pattern;

@Component
public class Validation {

    // --- Precompiled Regex Patterns (faster & thread-safe) ---
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$");

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^0(7)([0|1|2|4|5|6|7|8])[0-9]{7}$");

    private static final Pattern CODE_PATTERN =
            Pattern.compile("^\\d{4,6}$");

    private static final Pattern INTEGER_PATTERN =
            Pattern.compile("^\\d+$");

    private static final Pattern DOUBLE_PATTERN =
            Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    private static final Pattern ADDRESS_PATTERN =
            Pattern.compile("^[a-zA-Z0-9\\s,.'-/#]{5,100}$");


    private static final SecureRandom RANDOM = new SecureRandom();

    // ====================================================================
    // 🔢 Generate a 6-digit verification code (secure & zero-padded)
    // ====================================================================
    public String generateCode() {
        int code = RANDOM.nextInt(1_000_000); // 0–999999
        return String.format("%06d", code);
    }

    // ====================================================================
    // ✅ Validation Methods
    // ====================================================================
    public boolean isEmailValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isPasswordValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public boolean isMobileValid(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile).matches();
    }

    public boolean isCodeValid(String code) {
        return code != null && CODE_PATTERN.matcher(code).matches();
    }

    public boolean isInteger(String value) {
        return value != null && INTEGER_PATTERN.matcher(value).matches();
    }

    public boolean isDouble(String text) {
        return text != null && DOUBLE_PATTERN.matcher(text).matches();
    }

    // --- NEW: Address Line Validation Method ---
    public boolean isAddressLineValid(String address) {
        return address != null && ADDRESS_PATTERN.matcher(address).matches();
    }
}