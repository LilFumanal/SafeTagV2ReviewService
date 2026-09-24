package com.lil.safetagv2reviewservice.models;

import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ReviewCreateDTO(
        @NotBlank String rppsId,
        @NotNull UUID userId,
        @NotNull List<UUID> addressIds,
        @NotBlank @Size(min = 10) String comment,
        Boolean isTeleconsultation,
        List<PathologyFamily> pathologies,
        List<TagDTO> tags
) {
    @AssertTrue(message = "Veuillez renseigner au moins un mode de consultation (visio ou adresse physique)")
    public boolean consultationModeValid() {
        boolean teleconsult = Boolean.TRUE.equals(isTeleconsultation);
        return isTeleconsultation || (addressIds != null && !addressIds.isEmpty());
    }
}
