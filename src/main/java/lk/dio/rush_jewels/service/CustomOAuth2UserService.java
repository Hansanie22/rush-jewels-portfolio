package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.Status;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.StatusRepository;
import lk.dio.rush_jewels.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final StatusRepository statusRepository;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   StatusRepository statusRepository) {
        this.userRepository = userRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // 1. Load user info from Google/Facebook
            OAuth2User oAuth2User = super.loadUser(userRequest);
            Map<String, Object> attributes = oAuth2User.getAttributes();

            if (attributes == null || attributes.isEmpty()) {
                throw new OAuth2AuthenticationException(new OAuth2Error("no_attributes", "Attributes empty", null));
            }

            // 2. Determine Provider
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            String provider = registrationId != null ? registrationId.toUpperCase() : "UNKNOWN";

            // 3. Get Email (Safe & Lowercase)
            String email = (String) attributes.get("email");
            if (email == null || email.trim().isEmpty()) {
                throw new OAuth2AuthenticationException(new OAuth2Error("email_missing", "Email not found from provider", null));
            }
            // IMPORTANT: Normalize email to lowercase to prevent duplicates
            email = email.trim().toLowerCase();

            // 4. Extract Name & ID details
            String fname = "User";
            String lname = "";
            String providerId = null;

            if ("GOOGLE".equals(provider)) {
                fname = (String) attributes.get("given_name");
                lname = (String) attributes.get("family_name");
                providerId = (String) attributes.get("sub");
            } else if ("FACEBOOK".equals(provider)) {
                String fullName = (String) attributes.get("name");
                providerId = (String) attributes.get("id");
                if (fullName != null) {
                    String[] parts = fullName.split(" ", 2);
                    fname = parts[0];
                    lname = (parts.length > 1) ? parts[1] : "";
                }
            }

            // Default fallback for names
            if (fname == null) fname = "User";
            if (lname == null) lname = "";

            // 5. === CORE LOGIC: Find or Create User ===
            User user;
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                // --- EXISTING USER ---
                user = userOptional.get();

                // Check if they are trying to login via Google but registered as LOCAL
                if ("LOCAL".equalsIgnoreCase(user.getLoginProvider())) {
                    throw new OAuth2AuthenticationException(new OAuth2Error("email_exists",
                            "This email is already registered as a Local account. Please use your password.", null));
                }

                // Update provider details if needed
                if (!provider.equals(user.getLoginProvider()) || !Objects.equals(providerId, user.getProviderId())) {
                    user.setLoginProvider(provider);
                    user.setProviderId(providerId);
                    user = userRepository.save(user);
                }

            } else {
                // --- NEW USER ---
                user = new User();
                user.setEmail(email);
                user.setFname(fname);
                user.setLname(lname);
                user.setCreatedAt(new Date());
                user.setLoginProvider(provider);
                user.setProviderId(providerId);
                user.setVerification("Verified");
                user.setPassword(null); // No password for OAuth

                // Set Status
                Status status = statusRepository.findById(1).orElse(null);
                if (status == null) {
                    // Fallback if DB is empty, or handle error
                    System.err.println("Warning: Status ID 1 not found!");
                }
                user.setStatus(status);

                // Set User Type (Admin or User)
                List<String> adminEmails = Arrays.asList("rushjewelsofficial@gmail.com", "hansanieprabodha@gmail.com");
                user.setType(adminEmails.contains(email.toLowerCase()) ? "ADMIN" : "USER");

                // Save
                user = userRepository.save(user);
            }

            // 6. === AUTO LOGIN FIX ===
            // Return a user with the CORRECT authorities from your Database
            // This ensures Spring Security lets them access pages requiring ROLE_USER or ROLE_ADMIN
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getType())); // e.g. ROLE_USER

            return new DefaultOAuth2User(
                    authorities,
                    attributes,
                    "email" // Attribute key name
            );

        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OAuth2AuthenticationException(new OAuth2Error("server_error", e.getMessage(), null));
        }
    }
}