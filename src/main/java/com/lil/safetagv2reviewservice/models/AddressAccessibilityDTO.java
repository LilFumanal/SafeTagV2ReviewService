package com.lil.safetagv2reviewservice.models;

import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddressAccessibilityDTO(
        @NotNull UUID addressId,
        @NotNull ThreeStateAnswer accessible
) {}
