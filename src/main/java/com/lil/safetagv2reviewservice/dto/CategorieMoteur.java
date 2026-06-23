package com.lil.safetagv2reviewservice.dto;

import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;

// --- Catégorie Moteur ---
public record CategorieMoteur(
        ThreeStateAnswer status,
        DetailsMoteur details
) {}

