package com.lil.safetagv2reviewservice.controller;

import com.lil.safetagv2reviewservice.entity.Review;
import com.lil.safetagv2reviewservice.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void createReview_ShouldReturn201Created() throws Exception {
        // Création du DTO de réponse mocké
        UUID mockId = java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID mockUserId = UUID.randomUUID();

        com.lil.safetagv2reviewservice.models.ReviewResponseDTO mockResponse =
                new com.lil.safetagv2reviewservice.models.ReviewResponseDTO(
                        mockId,
                        "12345678910",
                        mockUserId,
                        java.util.Collections.emptyList(),
                        "Très bon praticien. Un commentaire assez long pour la validation.",
                        true,
                        java.util.Collections.emptyList(),
                        false,
                        java.util.Collections.emptyList(),
                        java.time.LocalDateTime.now(),
                        com.lil.safetagv2reviewservice.domain.ReviewStatus.APPROVED
                );

        // Correction : On passe bien les 2 arguments attendus par le service
        when(reviewService.createReview(
                any(com.lil.safetagv2reviewservice.models.ReviewCreateDTO.class),
                any(UUID.class)
        )).thenReturn(mockResponse);

        // Act & Assert
        String jsonPayload = """
                {
                    "rppsId": "12345678910",
                    "userId": "%s",
                    "addressIds": [],
                    "isTeleconsultation": true,
                    "comment": "Très bon praticien. Un commentaire assez long pour la validation.",
                    "accessibleAddressIds": [],
                    "signLanguage": false,
                    "tags": [],
                    "pathologies": []
                }
                """.formatted(mockUserId);

        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", mockUserId.toString()) // Ajout du header si ton controller l'attend
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mockId.toString()));
    }

    @Test
    void getReviewsByRppsId_ShouldReturn200Ok() throws Exception {
        // Arrange
        String rppsId = "12345678910";
        UUID userId = UUID.randomUUID();
        Review review = new Review();
        review.setId(java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        review.setRppsId(rppsId);
        review.setAddressIds(java.util.List.of(java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000")));
        review.setComment("Très bon praticien, je recommande.");

        org.springframework.data.domain.Page<Review> reviewPage =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(review));

        when(reviewService.getReviewsByRppsId(eq(rppsId), anyInt(), anyInt()))
                .thenReturn(reviewPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", rppsId)
                        .header("X-User-Id", userId.toString()) // <-- Ajout du header
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath("$.content[0].comment").value("Très bon praticien, je recommande."));
    }

    @Test
    void getReviewsByRppsId_ShouldReturn500WhenServiceFails() throws Exception {
        // Arrange
        String rppsId = "12345678910";
        when(reviewService.getReviewsByRppsId(eq(rppsId), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Erreur base de données"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", rppsId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createReview_ShouldReturn400_WhenNoConsultationMode() throws Exception {
        // Arrange
        String jsonPayload = """
                {
                  "rppsId": "12345678910",
                  "userId": "fd18c5de-641e-47f7-a7e2-acceb32b96f9",
                  "comment": "Un commentaire assez long pour passer la validation.",
                  "addressIds": [],
                  "isTeleconsultation": false,
                  "accessibleAddressIds": [],
                  "signLanguage": false,
                  "tags": [],
                  "pathologies": []
                }
                """;

        UUID userId = UUID.randomUUID(); // Génération d'un UUID pour le header

        // Act & Assert
        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", userId.toString()) // <-- Ajout du header ici
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }


    @Test
    void createReview_ShouldReturn400_WhenInvalidPathology() throws Exception {
        String jsonPayload = """
        {
          "rppsId": "12345678910",
          "userId": "fd18c5de-641e-47f7-a7e2-acceb32b96f9",
          "comment": "Un commentaire valide et suffisamment long.",
          "isTeleconsultation": true,
          "addressIds": [],
          "accessibleAddressIds": [],
          "signLanguage": false,
          "pathologies": ["PATHOLOGIE_IMPOSSIBLE"]
        }
        """;
        UUID userId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", userId.toString()) // <-- Ajout du header ici
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReviewsByPractitioner_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        String rppsId = "12345678910";

        when(reviewService.getReviewsByRppsId(eq(rppsId), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database connection failure"));

        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", rppsId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getReviewsByPractitioner_ShouldReturn400_WhenRppsIdIsInvalid() throws Exception {
        String invalidRppsId = "12345ABCDEF";
        UUID userId = UUID.randomUUID(); // Génération du header

        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", invalidRppsId)
                        .header("X-User-Id", userId.toString()) // <-- Ajout ici
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}