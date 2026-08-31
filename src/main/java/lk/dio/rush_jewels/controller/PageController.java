package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PageController {

    // ---------------- Public pages ----------------
    @GetMapping({"/", "/index"})
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/shop")
    public String shop() {
        return "forward:/shop.html";
    }

    @GetMapping("/product-details")
    public String productDetails() {
        return "forward:/product-details.html";
    }

    @GetMapping("/account")
    public String account() {
        return "forward:/account.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forward:/forgot-password.html";
    }

    @GetMapping("/auth")
    public String auth() {
        return "forward:/auth.html";
    }

    @GetMapping("/404")
    public String notFound() {
        return "forward:/404.html";
    }

    // ---------------- Workflow-protected pages ----------------

    // Step 1: User clicks "Proceed to Checkout"
    @GetMapping("/go-to-checkout")
    public String goToCheckout(HttpSession session) {
        session.setAttribute("CAN_ACCESS_CHECKOUT", true);
        return "forward:/checkout.html";
    }

    // Step 2: Checkout page
    @GetMapping("/checkout")
    public String checkout(HttpSession session) {
        Boolean canAccess = (Boolean) session.getAttribute("CAN_ACCESS_CHECKOUT");
        if (canAccess != null && canAccess) {
            session.removeAttribute("CAN_ACCESS_CHECKOUT"); // consume flag
            return "forward:/checkout.html";
        } else {
            return "forward:/404.html"; // block manual URL typing
        }
    }

    // Step 3: Order confirmation page
    @GetMapping("/order-confirmation")
    public String orderConfirmation(HttpSession session) {
        Boolean cameFromCheckout = (Boolean) session.getAttribute("CAME_FROM_CHECKOUT");
        if (cameFromCheckout != null && cameFromCheckout) {
            session.removeAttribute("CAME_FROM_CHECKOUT"); // consume flag
            return "forward:/order-confirmation.html";
        } else {
            return "forward:/404.html"; // block manual URL typing
        }
    }

    // Step 4: After order completion
    @PostMapping("/complete-order")
    public String completeOrder(HttpSession session) {
        session.setAttribute("CAME_FROM_CHECKOUT", true);
        return "redirect:/order-confirmation";
    }
}
