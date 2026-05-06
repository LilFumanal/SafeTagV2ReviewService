package com.lil.safetagv2reviewservice.domain;


public enum ReviewStatus {
    APPROVED, // Visible publiquement
    REPORTED, // Signalé, en attente de modération
    REJECTED,
    PENDING// Refusé (auto-modération échouée ou admin), visible que par l'auteur
}
