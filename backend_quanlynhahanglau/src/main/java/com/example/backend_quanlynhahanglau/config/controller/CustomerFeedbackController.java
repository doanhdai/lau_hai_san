package com.example.backend_quanlynhahanglau.config.controller;

import com.example.backend_quanlynhahanglau.dto.ApiResponse;
import com.example.backend_quanlynhahanglau.dto.feedback.FeedbackRequest;
import com.example.backend_quanlynhahanglau.dto.feedback.FeedbackResponse;
import com.example.backend_quanlynhahanglau.dto.feedback.PublicFeedbackRequest;
import com.example.backend_quanlynhahanglau.service.CustomerFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerFeedbackController {
    private final CustomerFeedbackService feedbackService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getAllFeedbacks() {
        List<FeedbackResponse> feedbacks = feedbackService.getAllFeedbacks();
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }

    @GetMapping("/unresolved")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getUnresolvedFeedbacks() {
        List<FeedbackResponse> feedbacks = feedbackService.getUnresolvedFeedbacks();
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedbackById(@PathVariable Long id) {
        FeedbackResponse feedback = feedbackService.getFeedbackById(id);
        return ResponseEntity.ok(ApiResponse.success(feedback));
    }

    @GetMapping("/average-rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRating() {
        Double rating = feedbackService.getAverageRating();
        return ResponseEntity.ok(ApiResponse.success(rating));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getPublicFeedbacks(
            @RequestParam(defaultValue = "6") int limit) {
        List<FeedbackResponse> feedbacks = feedbackService.getPublicFeedbacks(limit);
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedbackByReservationId(
            @PathVariable Long reservationId) {
        FeedbackResponse feedback = feedbackService.getFeedbackByReservationId(reservationId);
        if (feedback == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        return ResponseEntity.ok(ApiResponse.success(feedback));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createFeedback(@Valid @RequestBody FeedbackRequest request) {
        FeedbackResponse feedback = feedbackService.createFeedback(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phản hồi thành công", feedback));
    }

    @PostMapping("/public")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createPublicFeedback(@Valid @RequestBody PublicFeedbackRequest request) {
        FeedbackResponse feedback = feedbackService.createFeedbackFromReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cảm ơn bạn đã đánh giá!", feedback));
    }

    @PutMapping("/{id}/respond")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> respondToFeedback(
            @PathVariable Long id,
            @RequestBody String response) {
        FeedbackResponse feedback = feedbackService.respondToFeedback(id, response);
        return ResponseEntity.ok(ApiResponse.success("Phản hồi thành công", feedback));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa phản hồi thành công", null));
    }
}
