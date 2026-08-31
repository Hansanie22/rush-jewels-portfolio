package lk.dio.rush_jewels.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // Get the status code
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // Handle 404 Not Found
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "forward:/404.html";
            }

            // Handle 403 Forbidden
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "forward:/404.html";
            }

            // Handle 500 Internal Server Error
            if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "forward:/404.html";
            }
        }

        // Default fallback to 404 page for any other error
        return "forward:/404.html";
    }
}