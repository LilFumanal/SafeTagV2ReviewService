package com.lil.safetagv2reviewservice.dto;

import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;

public record DetailsMoteur(
        ThreeStateAnswer stationnementAdapte,
        ThreeStateAnswer accesPlainPied,
        ThreeStateAnswer ascenseur,
        ThreeStateAnswer sanitairesAdaptes
) {}
