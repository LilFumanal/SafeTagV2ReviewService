package com.lil.safetagv2reviewservice.controller;

import com.lil.safetagv2reviewservice.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/reviews")
public class ModerationController {

    private final ReviewService reviewService;

    public ModerationController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{reviewId}/reject")
    public ResponseEntity<Void> rejectReview(
            @PathVariable UUID id){
            reviewService.updateReviewStatusToRejected(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/report")
    public ResponseEntity<Void> reportReview(@PathVariable UUID id,
                                             @RequestHeader(value = "Authorization", required = true) String authHeader) {
        reviewService.reportReview(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/pending")
    public ResponseEntity<Void> markAsPending(@PathVariable UUID reviewId) {
        reviewService.markAsPending(reviewId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/approve")
    public ResponseEntity<Void> approveReview(@PathVariable UUID reviewId) {
        reviewService.approveReview(reviewId);
        return ResponseEntity.ok().build();
    }
}