package com.lil.safetagv2reviewservice.controller;

import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import com.lil.safetagv2reviewservice.domain.TagCategory;
import com.lil.safetagv2reviewservice.entity.Review;
import com.lil.safetagv2reviewservice.models.ReviewCreateDTO;
import com.lil.safetagv2reviewservice.models.ReviewResponseDTO;
import com.lil.safetagv2reviewservice.models.UpdateReviewRequest;
import com.lil.safetagv2reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Créer un nouvel avis
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid ReviewCreateDTO dto) {
        ReviewResponseDTO createdReview = reviewService.createReview(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    // Récupérer les avis d'un praticien spécifique (PAGINÉ)
    @GetMapping("/practitioner/{rppsId}")
    public ResponseEntity<Page<Review>> getReviewsByPractitioner(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable @Pattern(regexp = "^\\d{11}$", message = "Le numéro RPPS doit contenir exactement 11 chiffres") String rppsId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Review> reviews = reviewService.getReviewsByRppsId(rppsId, page, size);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{reviewId}/report")
    public ResponseEntity<Void> reportReview(
            @PathVariable UUID reviewId,
            @RequestHeader("X-User-Id") UUID userId) {

        reviewService.reportReview(reviewId);
        return ResponseEntity.ok().build();
    }

    // Récupérer les scores de tags pour un praticien spécifique
    @GetMapping("/practitioner/{rppsId}/stats")
    public ResponseEntity<Map<TagCategory, Double>> getPractitionerStats(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable String rppsId) {
        Map<TagCategory, Double> stats = reviewService.getPractitionerStats(rppsId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/me")
    public ResponseEntity<Page<ReviewResponseDTO>> getMyReviews(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Assure-toi que reviewService.getReviewsByUser mappe bien la Page<Review> en Page<ReviewResponseDTO>
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByUser(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(reviews);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid UpdateReviewRequest request) {

        ReviewResponseDTO updated = reviewService.updateReview(id, userId, request);
        return ResponseEntity.ok(updated);
    }

    // 2. Ajout de la pagination pour la langue des signes
    @GetMapping("/search-filters/sign-language")
    public ResponseEntity<Page<String>> getFilterSignLanguage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(reviewService.getRppsWithSignLanguage(PageRequest.of(page, size)));
    }

    // 3. Ajout de la pagination pour l'accès PMR
    @GetMapping("/search-filters/wheelchair-accessible")
    public ResponseEntity<Page<String>> getFilterWheelchair(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(reviewService.getRppsWithWheelchairAccess(PageRequest.of(page, size)));
    }
    @GetMapping("/search-filters/pathology/{family}")
    public ResponseEntity<Page<String>> getFilterByPathology(
            @PathVariable PathologyFamily family,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(reviewService.getRppsByPathology(family, PageRequest.of(page, size)));
    }

    @GetMapping("/search-filters/missing-tags")
    public ResponseEntity<Page<String>> getMissingTagsFilter(
            @RequestParam(required = false) List<TagCategory> categories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(reviewService.getRppsMissingTags(categories, PageRequest.of(page, size)));
    }
}
