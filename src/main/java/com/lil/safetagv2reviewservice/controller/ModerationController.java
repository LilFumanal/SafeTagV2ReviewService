package com.lil.safetagv2reviewservice.controller;

import com.lil.safetagv2reviewservice.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/reviews")
public class ModerationController {

    private final ReviewService reviewService;
    private final String ROLE_MODERATOR = "MODERATOR"; // À adapter selon vos noms de rôles

    public ModerationController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{reviewId}/reject")
    public ResponseEntity<Void> rejectReview(
            @PathVariable UUID reviewId,
            @RequestHeader("X-User-Roles") String roles,
            @RequestBody Map<String, String> payload){

        checkModerator(roles);
        String reason = payload.getOrDefault("reason", "Le contenu ne respecte pas nos conditions d'utilisation.");
        reviewService.updateReviewStatusToRejected(reviewId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/pending")
    public ResponseEntity<Void> markAsPending(
            @PathVariable UUID reviewId,
            @RequestHeader("X-User-Roles") String roles) {

        checkModerator(roles);
        reviewService.markAsPending(reviewId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/approve")
    public ResponseEntity<Void> approveReview(
            @PathVariable UUID reviewId,
            @RequestHeader("X-User-Roles") String roles) {

        checkModerator(roles);
        reviewService.approveReview(reviewId);
        return ResponseEntity.ok().build();
    }

    // Méthode utilitaire privée
    private void checkModerator(String roles) {
        if (roles == null || !roles.contains(ROLE_MODERATOR)) {
            // On peut lever une exception personnalisée qui renverra un 403
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès réservé aux modérateurs");
        }
    }

}