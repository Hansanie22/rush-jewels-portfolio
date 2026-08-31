package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpServletRequest; // Import Request
import jakarta.servlet.http.HttpSession;      // Import Session
import lk.dio.rush_jewels.model.Admin;
import lk.dio.rush_jewels.repository.AdminRepository;
import lk.dio.rush_jewels.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminLoginController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    @PostMapping("/login")
    // Note: Use HttpServletRequest here, NOT HttpSession
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest loginRequest, HttpServletRequest request) {

        AdminLoginResponse response = adminService.authenticateAdmin(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/validate-session")
    public ResponseEntity<AdminLoginResponse> validateSession(HttpServletRequest request) {
        AdminLoginResponse response = new AdminLoginResponse();
        HttpSession session = request.getSession(false); // Get existing session

        if (session != null && session.getAttribute("adminId") != null) {
            Integer adminId = (Integer) session.getAttribute("adminId");
            Optional<Admin> adminOpt = adminRepository.findById(adminId);
            
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                response.setSuccess(true);
                response.setMessage("Session valid");
                response.setName(admin.getName());
                response.setRole(admin.getRole());
                response.setImagePath(admin.getImagePath());
                return ResponseEntity.ok(response);
            }
        }
        
        response.setSuccess(false);
        response.setMessage("Invalid");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<AdminLoginResponse> logout(HttpServletRequest request) {
        AdminLoginResponse response = new AdminLoginResponse();
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.setSuccess(true);
        response.setMessage("Logged out");
        response.setRedirect("/admin-login.html");
        return ResponseEntity.ok(response);
    }
}