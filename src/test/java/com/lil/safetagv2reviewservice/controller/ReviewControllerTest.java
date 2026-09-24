package com.lil.safetagv2reviewservice.controller;

import com.lil.safetagv2reviewservice.models.ReviewResponseDTO;
import com.lil.safetagv2reviewservice.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
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

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void createReview_ShouldReturn201Created() throws Exception {
        ReviewResponseDTO mockReview = new ReviewResponseDTO(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                "12345678910",
                USER_ID,
                List.of(UUID.randomUUID()),
                "Très bon praticien. Un commentaire assez long pour la validation.",
                false,
                List.of(),
                List.of(),
                null,
                null
        );

        when(reviewService.createReview(any(), any(UUID.class))).thenReturn(mockReview);

        String jsonPayload = """
                {
                    "rppsId": "12345678910",
                    "userId": "%s",
                    "addressIds": ["%s"],
                    "isTeleconsultation": false,
                    "comment": "Très bon praticien. Un commentaire assez long pour la validation.",
                    "tags": [],
                    "pathologies": []
                }
                """.formatted(USER_ID, UUID.randomUUID());

        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());
    }

    @Test
    void getReviewsByRppsId_ShouldReturn200Ok() throws Exception {
        String rppsId = "12345678910";
        ReviewResponseDTO review = new ReviewResponseDTO(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                rppsId,
                USER_ID,
                List.of(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")),
                "Très bon praticien, je recommande.",
                false,
                List.of(),
                List.of(),
                null,
                null
        );

        org.springframework.data.domain.Page<ReviewResponseDTO> reviewPage =
                new org.springframework.data.domain.PageImpl<>(List.of(review));

        when(reviewService.getReviewsByRppsId(eq(rppsId), anyInt(), anyInt()))
                .thenReturn(reviewPage);

        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", rppsId)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath("$.content[0].comment").value("Très bon praticien, je recommande."));
    }

    @Test
    void getReviewsByRppsId_ShouldReturn500WhenServiceFails() throws Exception {
        String rppsId = "12345678910";
        when(reviewService.getReviewsByRppsId(eq(rppsId), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Erreur base de données"));

        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", rppsId)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createReview_ShouldReturn400_WhenNoConsultationMode() throws Exception {
        String invalidJson = """
        {
          "rppsId": "12345678910",
          "userId": "%s",
          "comment": "Un commentaire assez long pour passer la validation.",
          "addressIds": [],
          "isTeleconsultation": false,
          "tags": [],
          "pathologies": []
        }
        """.formatted(USER_ID);

        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.consultationModeValid")
                        .value("Veuillez renseigner au moins un mode de consultation (visio ou adresse physique)"));
    }

    @Test
    void createReview_ShouldReturn400_WhenInvalidPathology() throws Exception {
        String invalidEnumJson = """
        {
          "rppsId": "12345678910",
          "userId": "%s",
          "comment": "Un commentaire valide.",
          "isTeleconsultation": true,
          "pathologies": ["PATHOLOGIE_IMPOSSIBLE"]
        }
        """.formatted(USER_ID);

        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEnumJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReviewsByPractitioner_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        String rppsId = "12345678910";

        when(reviewService.getReviewsByRppsId(eq(rppsId), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database connection failure"));

        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", rppsId)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getReviewsByPractitioner_ShouldReturn400_WhenRppsIdIsInvalid() throws Exception {
        String invalidRppsId = "12345ABCDEF";

        mockMvc.perform(get("/api/v1/reviews/practitioner/{rppsId}", invalidRppsId)
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}