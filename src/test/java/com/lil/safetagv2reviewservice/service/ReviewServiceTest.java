package com.lil.safetagv2reviewservice.service;

import com.lil.safetagv2reviewservice.client.RppsClient;
import com.lil.safetagv2reviewservice.domain.ReviewStatus;
import com.lil.safetagv2reviewservice.domain.TagCategory;
import com.lil.safetagv2reviewservice.domain.TagVote;
import com.lil.safetagv2reviewservice.entity.Review;
import com.lil.safetagv2reviewservice.entity.ReviewTag;
import com.lil.safetagv2reviewservice.exception.ResourceNotFoundException;
import com.lil.safetagv2reviewservice.mapper.ReviewMapper;
import com.lil.safetagv2reviewservice.models.ReviewCreateDTO;
import com.lil.safetagv2reviewservice.models.ReviewResponseDTO;
import com.lil.safetagv2reviewservice.repository.ReviewRepository;
import com.lil.safetagv2reviewservice.repository.ReviewTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_ShouldSetReviewInTagsAndSave() {
        // Préparation (Arrange)
        UUID userId = UUID.randomUUID();
        String rppsId = "12345678910";

        ReviewCreateDTO dto = new ReviewCreateDTO(
                rppsId, userId, Collections.emptyList(), "Un commentaire tout à fait correct",
                false, Collections.emptyList(), false, Collections.emptyList()
        );

        Review reviewEntity = new Review();
        reviewEntity.setRppsId(rppsId);
        reviewEntity.setUserId(userId);
        reviewEntity.setComment("Un commentaire tout à fait correct");

        ReviewTag tag1 = new ReviewTag();
        ReviewTag tag2 = new ReviewTag();
        reviewEntity.setTags(List.of(tag1, tag2));

        ReviewResponseDTO responseDTO = new ReviewResponseDTO(
                UUID.randomUUID(), rppsId, userId, Collections.emptyList(),
                "Un commentaire tout à fait correct", false, Collections.emptyList(),
                false, Collections.emptyList(), LocalDateTime.now(), ReviewStatus.APPROVED
        );

        // Mocks de la nouvelle logique
        when(reviewMapper.toEntity(dto)).thenReturn(reviewEntity);
        when(reviewRepository.existsByUserIdAndRppsId(userId, rppsId)).thenReturn(false);
        // rppsClient.getPractitionerByRpps ne renvoie rien ou un objet ignoré, on ne mocke pas d'exception
        when(userClient.userExists(userId)).thenReturn(true);
        when(moderationClient.moderateComment(anyString())).thenReturn(ReviewStatus.APPROVED);
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewEntity);
        when(reviewMapper.toResponseDTO(reviewEntity)).thenReturn(responseDTO);

        // Exécution (Act)
        ReviewResponseDTO savedReviewDTO = reviewService.createReview(dto, userId);

        // Vérification (Assert)
        assertNotNull(savedReviewDTO);
        assertEquals(reviewEntity, tag1.getReview()); // Vérifie la relation bidirectionnelle
        assertEquals(reviewEntity, tag2.getReview());
        verify(reviewRepository, times(1)).save(reviewEntity);
        verify(moderationClient, times(1)).moderateComment(anyString());
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

        TagCategory category1 = TagCategory.values()[0];
        TagCategory category2 = TagCategory.values()[1];

        ReviewTag tag1 = new ReviewTag();
        tag1.setCategory(category1);
        tag1.setVote(TagVote.POSITIVE);
        ReviewTag tag2 = new ReviewTag();
        tag2.setCategory(category1);
        tag2.setVote(TagVote.POSITIVE);
        ReviewTag tag3 = new ReviewTag();
        tag3.setCategory(category1);
        tag3.setVote(TagVote.NEGATIVE);

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

        org.springframework.data.domain.Page<Review> expectedPage =
                new org.springframework.data.domain.PageImpl<>(List.of(new Review()));

        // Correction : on mock bien findByRppsIdAndStatus
        when(reviewRepository.findByRppsIdAndStatus(
                eq(rppsId),
                eq(com.lil.safetagv2reviewservice.domain.ReviewStatus.APPROVED),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        // Act
        org.springframework.data.domain.Page<Review> result = reviewService.getReviewsByRppsId(rppsId, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(reviewRepository, times(1)).findByRppsIdAndStatus(
                eq(rppsId),
                eq(com.lil.safetagv2reviewservice.domain.ReviewStatus.APPROVED),
                any(Pageable.class)
        );
    }
}
