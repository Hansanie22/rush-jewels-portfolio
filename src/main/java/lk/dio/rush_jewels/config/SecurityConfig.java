package lk.dio.rush_jewels.config;

import jakarta.servlet.DispatcherType;
import lk.dio.rush_jewels.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
                          OAuth2LoginFailureHandler oAuth2LoginFailureHandler) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.oAuth2LoginFailureHandler = oAuth2LoginFailureHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .servletApi(api -> api.disable())
                .requestCache(cache -> cache.disable())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(buildCSPPolicy()))
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                )
                .addFilterBefore(new UserSessionFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // ✅ CRITICAL FIX: Forward සහ Error requests වලදී Security Loop එක කඩන්න
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                        .requestMatchers("/admin-login.html", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/404.html").permitAll()
                        .requestMatchers("/api/admin/login").permitAll()
                        .requestMatchers("/admin-pos.html", "/api/pos/**").hasAnyAuthority("ADMIN", "CASHIER")
                        .requestMatchers("/admin.html", "/admin/**", "/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(
                                "/", "/index.html", "/shop.html", "/product-details.html",
                                "/account.html", "/forgot-password.html", "/verify-reset-password.html", "/auth.html","/blog.html",
                                "/api/public/**", "/api/auth/**",
                                "/oauth2/**", "/login/oauth2/**"
                        ).permitAll()
                        .requestMatchers("/checkout.html", "/order-confirmation.html", "/payment-cancel.html", "/api/support/**")
                        .authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth.html")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/index.html")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(403);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"status\":false,\"message\":\"Access denied\"}");
                            } else {
                                response.sendRedirect("/404.html");
                            }
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(401);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"status\":false,\"message\":\"Unauthorized\"}");
                            } else if (request.getRequestURI().startsWith("/admin")) {
                                response.sendRedirect("/admin-login.html");
                            } else {
                                response.sendRedirect("/auth.html");
                            }
                        })
                );

        return http.build();
    }

    private String buildCSPPolicy() {
        return "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://maps.googleapis.com https://maps.gstatic.com https://*.payhere.lk https://cdn.tailwindcss.com https://cdnjs.cloudflare.com https://www.google-analytics.com https://maxcdn.bootstrapcdn.com https://ajax.googleapis.com https://cdn.jsdelivr.net; " +
                "style-src 'self' 'unsafe-inline' https://maps.googleapis.com https://maps.gstatic.com https://*.payhere.lk https://cdnjs.cloudflare.com https://fonts.googleapis.com https://maxcdn.bootstrapcdn.com https://cdn.jsdelivr.net; " +
                "img-src 'self' data: blob: https://maps.googleapis.com https://maps.gstatic.com https://res.cloudinary.com https://ui-avatars.com; " +
                "media-src 'self' https://res.cloudinary.com; " +
                "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com https://maxcdn.bootstrapcdn.com; " +
                "connect-src 'self' https://maps.googleapis.com https://maps.gstatic.com https://*.payhere.lk http://localhost:8080 ws://localhost:8080 https://rushjewels.com wss://rushjewels.com https://generativelanguage.googleapis.com; " +
                "frame-src 'self' https://*.payhere.lk https://player.vimeo.com; " +
                "frame-ancestors 'self'; " +
                "form-action 'self' https://*.payhere.lk; " +
                "base-uri 'self'; object-src 'none';";
    }
}