package com.lil.safetagv2reviewservice.models;

import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import jakarta.validation.constraints.AssertTrue;
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
        List<UUID> accessibleAddressIds,
        boolean signLanguage,
        List<PathologyFamily> pathologies
) {
    @AssertTrue(message = "Veuillez renseigner au moins un mode de consultation (visio ou adresse physique)")
    public boolean isConsultationModeValid() {
        return isTeleconsultation || (addressIds != null && !addressIds.isEmpty());
    }

}
