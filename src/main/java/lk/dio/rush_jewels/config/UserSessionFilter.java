package lk.dio.rush_jewels.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserSessionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Check if already logged in (Spring Security Context)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                // 2. Get session
                HttpSession session = request.getSession(false);

                if (session != null) {
                    Object userObj = session.getAttribute("user");
                    Object adminIdObj = session.getAttribute("adminId");

                    if (userObj instanceof User) {
                        User user = (User) userObj;

                        // 3. Create Role based on User Type
                        List<GrantedAuthority> authorities = new ArrayList<>();
                        String userType = (user.getType() != null) ? user.getType() : "USER";

                        if ("ADMIN".equalsIgnoreCase(userType)) {
                            authorities.add(new SimpleGrantedAuthority("ADMIN"));
                        } else if ("CASHIER".equalsIgnoreCase(userType)) {
                            authorities.add(new SimpleGrantedAuthority("CASHIER"));
                        } else {
                            authorities.add(new SimpleGrantedAuthority("USER"));
                        }

                        // 4. Inject into Spring Security
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(user, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else if (adminIdObj != null) {
                        // Admin Dashboard User (from admin table)
                        List<GrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority("ADMIN"));
                        
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken("admin_dashboard_user", null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception e) {
            // Error එකක් ආවොත් Log කරන්න, නමුත් Loop එක නවත්වන්න ඉදිරියට යවන්න
            logger.error("Error in UserSessionFilter: " + e.getMessage());
        }

        // 5. ඊළඟ ෆිල්ටරයට යවන්න
        filterChain.doFilter(request, response);
    }

    /**
     * ✅ CRITICAL FIX: Infinite Loop (StackOverflowError) නවත්වන කොටස.
     * Error pages සහ Static resources වලදී මේ Filter එක Run වෙන්නේ නෑ.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images") ||
                path.startsWith("/webjars") ||
                path.equals("/error"); // Error dispatch වලදී මෙය මග හරින්න
    }
}