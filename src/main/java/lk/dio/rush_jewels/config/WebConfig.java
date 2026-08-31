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
                .allowedOriginPatterns("https://rushjewels.com", "https://www.rushjewels.com", "http://localhost:8080")
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

        // ✅ NOTE: Uploads folders are NO LONGER needed here.
        // Images are served directly from Cloudinary URLs.

        // 1. Handle Static Resources (CSS, JS, Images in classpath)
        // URL: http://localhost:8080/css/style.css etc.
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // 2. Handle missing favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");
    }
}