package com.lil.safetagv2reviewservice.dto;

import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;

// --- Catégorie Auditif ---
public record CategorieAuditif(
        ThreeStateAnswer status,
        DetailsAuditif details
) {}
