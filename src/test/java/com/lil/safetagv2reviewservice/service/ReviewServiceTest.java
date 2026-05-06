package com.lil.safetagv2reviewservice.service;

import com.lil.safetagv2reviewservice.client.RppsClient;
import com.lil.safetagv2reviewservice.domain.TagCategory;
import com.lil.safetagv2reviewservice.domain.TagVote;
import com.lil.safetagv2reviewservice.entity.Review;
import com.lil.safetagv2reviewservice.entity.ReviewTag;
import com.lil.safetagv2reviewservice.exception.ResourceNotFoundException;
import com.lil.safetagv2reviewservice.repository.ReviewRepository;
import com.lil.safetagv2reviewservice.repository.ReviewTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewTagRepository reviewTagRepository;

    @Mock
    private com.lil.safetagv2reviewservice.client.UserClient userClient;

    @Mock
    private com.lil.safetagv2reviewservice.client.ModerationClient moderationClient;

    @Mock
    private RppsClient rppsClient;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_ShouldSetReviewInTagsAndSave() {
        // Préparation (Arrange)
        Review review = new Review();
        ReviewTag tag1 = new ReviewTag();
        ReviewTag tag2 = new ReviewTag();
        review.setRppsId("12345678910");
        review.setTags(List.of(tag1, tag2));
        review.setUserId(java.util.UUID.randomUUID());
        review.setComment("Un commentaire tout à fait correct");

        when(userClient.userExists(any(UUID.class))).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Exécution (Act)
        Review savedReview = reviewService.createReview(review);

        // Vérification (Assert)
        assertNotNull(savedReview);
        assertEquals(review, tag1.getReview()); // Vérifie la relation bidirectionnelle
        assertEquals(review, tag2.getReview());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void getReviewById_ShouldReturnReview_WhenFound() {
        // Arrange
        UUID reviewId = java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Review review = new Review();
        review.setId(reviewId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act
        Review foundReview = reviewService.getReviewById(reviewId);

        // Assert
        assertNotNull(foundReview);
        assertEquals(reviewId, foundReview.getId());
        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    void getReviewById_ShouldThrowException_WhenNotFound() {
        // Arrange
        UUID reviewId = java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(reviewId));
        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    void getPractitionerStats_ShouldCalculateCorrectPercentages() {
        // Arrange
        String rppsId = "12345678910";

        // Utilisation dynamique des vraies valeurs de l'enum
        TagCategory category1 = TagCategory.values()[0];
        TagCategory category2 = TagCategory.values()[1];

        // Catégorie 1 : 2 positifs sur 3 = 66.666... arrondi à 66.7
        ReviewTag tag1 = new ReviewTag();
        tag1.setCategory(category1);
        tag1.setVote(TagVote.POSITIVE);
        ReviewTag tag2 = new ReviewTag();
        tag2.setCategory(category1);
        tag2.setVote(TagVote.POSITIVE);
        ReviewTag tag3 = new ReviewTag();
        tag3.setCategory(category1);
        tag3.setVote(TagVote.NEGATIVE);

        // Catégorie 2 : 0 positif sur 1 = 0.0
        ReviewTag tag4 = new ReviewTag();
        tag4.setCategory(category2);
        tag4.setVote(TagVote.NEGATIVE);

        when(reviewTagRepository.findByReview_RppsId(rppsId)).thenReturn(List.of(tag1, tag2, tag3, tag4));

        // Act
        Map<TagCategory, Double> stats = reviewService.getPractitionerStats(rppsId);

        // Assert
        assertNotNull(stats);
        assertEquals(2, stats.size());
        assertEquals(66.7, stats.get(category1));
        assertEquals(0.0, stats.get(category2));

        verify(reviewTagRepository, times(1)).findByReview_RppsId(rppsId);
    }


    @Test
    void getReviewsByRppsId_ShouldReturnPagedReviews() {
        // Arrange
        String rppsId = "12345678910";
        int page = 0;
        int size = 10;

        // On simule une page contenant 1 avis
        org.springframework.data.domain.Page<Review> expectedPage =
                new org.springframework.data.domain.PageImpl<>(List.of(new Review()));

        // On vérifie que le repository est appelé avec n'importe quel objet Pageable

    }

}