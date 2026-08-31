package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.ReviewsDTO;
import lk.dio.rush_jewels.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin; // ✅ Import this
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/testimonials")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewsDTO>> getTopTestimonials() {
        List<ReviewsDTO> testimonials = reviewService.getTopTestimonials();
        return ResponseEntity.ok(testimonials);
    }
}