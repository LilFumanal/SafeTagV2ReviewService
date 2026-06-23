package com.lil.safetagv2reviewservice.dto;

import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;

public record AccessibiliteResponse(
        ThreeStateAnswer accessibiliteGlobale,
        CategorieMoteur moteur,
        CategorieAuditif auditif,
        CategorieVisuel visuel
) {}


