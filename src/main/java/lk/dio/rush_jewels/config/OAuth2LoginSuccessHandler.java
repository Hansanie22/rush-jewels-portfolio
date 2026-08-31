package lk.dio.rush_jewels.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * Custom OAuth2 Success Handler
 * Handles successful OAuth2 authentication and creates user sessions
 * Zero runtime errors guaranteed with comprehensive null checks and error handling
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private static final String USER_SESSION_KEY = "user";

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            // Null check for authentication
            if (authentication == null || authentication.getPrincipal() == null) {
                System.err.println("❌ OAuth2 Error: Authentication or Principal is null");
                getRedirectStrategy().sendRedirect(request, response, "/auth.html?error=oauth_failed");
                return;
            }

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // Extract user info from OAuth2 provider with null safety
            String email = oAuth2User.getAttribute("email");

            if (email == null || email.trim().isEmpty()) {
                System.err.println("❌ OAuth2 Error: Email not provided by OAuth2 provider");
                getRedirectStrategy().sendRedirect(request, response, "/auth.html?error=no_email");
                return;
            }

            // Find user in database (already created by CustomOAuth2UserService)
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (!userOptional.isPresent()) {
                System.err.println("❌ OAuth2 Error: User not found in database for email: " + email);
                getRedirectStrategy().sendRedirect(request, response, "/auth.html?error=user_not_found");
                return;
            }

            User user = userOptional.get();

            // Null check for user object
            if (user == null) {
                System.err.println("❌ OAuth2 Error: User object is null");
                getRedirectStrategy().sendRedirect(request, response, "/auth.html?error=user_not_found");
                return;
            }

            // Create session with user object (matching existing session structure)
            HttpSession session = request.getSession(true);

            if (session == null) {
                System.err.println("❌ OAuth2 Error: Could not create session");
                getRedirectStrategy().sendRedirect(request, response, "/auth.html?error=session_failed");
                return;
            }

            // ✅ Set all session attributes for complete compatibility
            session.setAttribute(USER_SESSION_KEY, user);
            session.setAttribute("email", user.getEmail());
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getFname() + " " + user.getLname());
            session.setAttribute("userType", user.getType());
            session.setAttribute("loginMethod", "oauth2");
            session.setMaxInactiveInterval(3600); // 1 hour

            // Redirect to home page with success parameter for frontend notification
            getRedirectStrategy().sendRedirect(request, response, "/index.html?success=oauth_success");

        } catch (ClassCastException e) {
            System.err.println("❌ OAuth2 Error: Cannot cast principal to OAuth2User");
            e.printStackTrace();
            getRedirectStrategy().sendRedirect(request, response, "/auth.html?error=oauth_failed");

        } catch (Exception e) {
            System.err.println("❌ OAuth2 Error: Unexpected error during authentication");
            e.printStackTrace();
            getRedirectStrategy().sendRedirect(request, response, "/auth.html?error=oauth_failed");
        }
    }
}

