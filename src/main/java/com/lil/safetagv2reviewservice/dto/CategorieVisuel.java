package com.lil.safetagv2reviewservice.dto;

import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;

// --- Catégorie Visuel ---
public record CategorieVisuel(
        ThreeStateAnswer status,
        DetailsVisuel details
) {}
