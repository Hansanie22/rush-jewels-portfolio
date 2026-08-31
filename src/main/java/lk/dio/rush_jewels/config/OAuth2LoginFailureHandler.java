package lk.dio.rush_jewels.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Custom OAuth2 Failure Handler
 * Handles OAuth2 authentication failures with user-friendly error messages
 */
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                       HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {

        String errorCode = "oauth_failed";
        String errorMessage = "Social login failed. Please try again.";

        // Check if it's an OAuth2 exception with custom error
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            String errorCodeFromException = oauth2Exception.getError().getErrorCode();

            if ("email_exists_local".equals(errorCodeFromException)) {
                errorCode = "email_exists_local";
                errorMessage = oauth2Exception.getError().getDescription();
                System.err.println("❌ OAuth2 Login Blocked: " + errorMessage);

            } else if ("no_email".equals(errorCodeFromException) || "email_not_provided".equals(errorCodeFromException)) {
                errorCode = "no_email";
                errorMessage = "Could not retrieve email from social provider. Please ensure email permission is granted.";
                System.err.println("❌ OAuth2 Login Failed: No email provided");

            } else if ("user_not_found".equals(errorCodeFromException)) {
                errorCode = "user_not_found";
                errorMessage = "User account could not be created. Please try again.";
                System.err.println("❌ OAuth2 Login Failed: User not found");

            } else {
                errorMessage = oauth2Exception.getError().getDescription();
                System.err.println("❌ OAuth2 Login Failed: " + errorMessage);
            }
        } else {
            System.err.println("❌ OAuth2 Login Failed: " + exception.getMessage());
        }

        // Redirect to auth page with error code and message
        String redirectUrl = "/auth.html?error=" + errorCode +
                           "&message=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

