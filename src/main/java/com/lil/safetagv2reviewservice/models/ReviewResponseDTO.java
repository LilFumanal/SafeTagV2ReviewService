package com.lil.safetagv2reviewservice.models;

import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import com.lil.safetagv2reviewservice.domain.ReviewStatus;
import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReviewResponseDTO(
        UUID id,
        String rppsId,
        UUID userId,
        List<UUID> addressIds,
        String comment,
        boolean isTeleconsultation,

        // --- Accessibilité PMR par adresse ---
        List<AddressAccessibilityDTO> addressAccessibility,

        // --- Langues & LSF ---
        ThreeStateAnswer signLanguage,
        List<String> languages,
        String customLanguage,

        List<PathologyFamily> pathologies,
        LocalDateTime createdAt,
        ReviewStatus status
) {}