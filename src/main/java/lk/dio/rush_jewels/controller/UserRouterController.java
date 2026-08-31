package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller // Use @Controller for view/redirect responses
public class UserRouterController {

    private static final String USER_SESSION_KEY = "user";

    /**
     * Handles the click on the user account icon to redirect to the appropriate page.
     * Maps to the new URL: /account-route
     * * @param request The incoming HTTP request.
     * @return A String redirecting the user.
     */
    @GetMapping("/account-route")
    public String routeUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        // Check if the session exists and if the 'user' attribute is set
        if (session != null && session.getAttribute(USER_SESSION_KEY) != null) {
            // User is logged in: Redirect to the account page
            return "redirect:/account.html";
        } else {
            // User is NOT logged in: Redirect to the authentication page
            return "redirect:/auth.html";
        }
    }
}