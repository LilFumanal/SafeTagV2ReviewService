package com.lil.safetagv2reviewservice.dto;

import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;

public record DetailsVisuel(
        ThreeStateAnswer bandeGuidage,
        ThreeStateAnswer audiodescription
) {}
