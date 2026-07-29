package com.lil.safetagv2reviewservice.models;

import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import com.lil.safetagv2reviewservice.domain.ReviewStatus;

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
        boolean wheelchairAccessible,
        boolean signLanguage,
        List<PathologyFamily> pathologies,
        LocalDateTime createdAt,
        ReviewStatus status
) {}
