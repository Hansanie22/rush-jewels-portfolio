package lk.dio.rush_jewels.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.controller.AdminLoginResponse;
import lk.dio.rush_jewels.model.Admin;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.AdminSession;
import lk.dio.rush_jewels.repository.AdminRepository;
import lk.dio.rush_jewels.repository.AdminSessionRepository;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminSessionRepository adminSessionRepository;

    @Autowired
    private AdminAuditLogRepository adminAuditLogRepository;

    // -----------------------------------------
    //   GET REAL CLIENT IP (WORKS IN HOSTING)
    // -----------------------------------------
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "X-Real-IP",
                "X-Cluster-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    // -----------------------------------------
    //              LOGIN METHOD
    // -----------------------------------------
    public AdminLoginResponse authenticateAdmin(String email, String password) {
        AdminLoginResponse response = new AdminLoginResponse();

        // 1. Check admin in database
        Optional<Admin> adminOpt = adminRepository.findByEmailAndPassword(email, password);

        if (adminOpt.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Invalid email or password!");
            return response;
        }

        Admin admin = adminOpt.get();

        // 2. Update last login
        admin.setLastLogin(LocalDateTime.now());
        adminRepository.save(admin);

        // 3. Create DB session (login record)
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String ipAddress = getClientIp(request);

        AdminSession adminSession = new AdminSession();
        adminSession.setAdmin(admin);
        adminSession.setLoginTime(LocalDateTime.now());
        adminSession.setIpAddress(ipAddress);

        adminSessionRepository.save(adminSession);

        // 4. Start HttpSession
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("adminId", admin.getId());
        httpSession.setAttribute("sessionId", adminSession.getId());
        httpSession.setMaxInactiveInterval(3600); // 1 hour

        // 5. Add audit log for login
        AdminAuditLog loginLog = new AdminAuditLog();
        loginLog.setActionType("LOGIN");
        loginLog.setTableName("admin");
        loginLog.setRecordId(String.valueOf(admin.getId()));
        loginLog.setOldValue(null);
        loginLog.setNewValue("{\"lastLogin\":\"" + admin.getLastLogin() + "\", \"ipAddress\":\"" + ipAddress + "\"}");
        loginLog.setActionTime(LocalDateTime.now());

        adminAuditLogRepository.save(loginLog);

        // 6. Response
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setRedirect("/admin.html");

        return response;
    }

    // -----------------------------------------
    //        VALIDATE ADMIN SESSION
    // -----------------------------------------
    public boolean validateAdminSession() {
        try {
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            HttpSession session = request.getSession(false);

            if (session == null) return false;

            return session.getAttribute("adminId") != null;

        } catch (Exception e) {
            return false;
        }
    }

    // -----------------------------------------
    //               LOGOUT METHOD
    // -----------------------------------------
    public void logoutAdmin() {
        try {
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            HttpSession session = request.getSession(false);

            if (session != null) {
                Integer sessionId = (Integer) session.getAttribute("sessionId");

                if (sessionId != null) {
                    Optional<AdminSession> adminSessionOpt =
                            adminSessionRepository.findById(sessionId);

                    if (adminSessionOpt.isPresent()) {
                        AdminSession adminSession = adminSessionOpt.get();
                        adminSession.setLogoutTime(LocalDateTime.now());
                        adminSessionRepository.save(adminSession);

                        // Add audit log for logout
                        AdminAuditLog logoutLog = new AdminAuditLog();
                        logoutLog.setActionType("LOGOUT");
                        logoutLog.setTableName("admin_session");
                        logoutLog.setRecordId(String.valueOf(adminSession.getId()));
                        logoutLog.setOldValue(null);
                        logoutLog.setNewValue("{\"logoutTime\":\"" + adminSession.getLogoutTime() + "\"}");
                        logoutLog.setActionTime(LocalDateTime.now());

                        adminAuditLogRepository.save(logoutLog);
                    }
                }

                session.invalidate();
            }
        } catch (Exception ignored) {
        }
    }
}
