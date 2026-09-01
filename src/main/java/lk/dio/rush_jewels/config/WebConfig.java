package lk.dio.rush_jewels.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ========================================
    // CORS Configuration
    // ========================================
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("https://velorajewellery.com", "https://www.velorajewellery.com", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // ========================================
    // Static Resource Handlers
    // ========================================
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 1. Handle Uploaded Images & Media
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/", "file:/app/uploads/", "classpath:/static/uploads/");

        // 2. Handle Static Resources (CSS, JS, Images in classpath)
        // URL: http://localhost:8080/css/style.css etc.
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // 3. Handle missing favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");
    }
}