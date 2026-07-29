package com.lil.safetagv2reviewservice.models;

import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ReviewCreateDTO(
        @NotBlank String rppsId,
        @NotNull UUID userId,
        List<UUID> addressIds,
        @NotBlank @Size(min = 10) String comment,
        boolean isTeleconsultation,
        boolean wheelchairAccessible,
        boolean signLanguage,
        List<PathologyFamily> pathologies
) {}
