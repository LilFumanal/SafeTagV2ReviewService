package com.lil.safetagv2reviewservice.dto;

import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;

public record DetailsAuditif(
        ThreeStateAnswer personnelLsf,
        ThreeStateAnswer boucleMagnetique
) {}
