package com.lil.safetagv2reviewservice.entity;
import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import com.lil.safetagv2reviewservice.domain.ReviewStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // --- Identifiants externes (Microservices) ---
    @Column(nullable = false)
    @NotBlank(message = "L'identifiant RPPS du praticien est obligatoire")
    private String rppsId;

    @Column(nullable = false)
    @NotNull(message = "L'identifiant de l'auteur est obligatoire")
    private UUID userId;

    @ElementCollection
    @CollectionTable(name = "review_address_ids", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "address_id")
    private List<UUID> addressIds = new ArrayList<>();
    private boolean isTeleconsultation = false;

    // --- Données de l'avis ---
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Le contenu de l'avis ne peut pas être vide")
    @Size(min = 10, message = "Le commentaire doit faire au moins 10 caractères")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt; // Remplace review_date

    // --- Relations ---
    // Relation One-to-Many avec les tags évalués
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewTag> tags = new ArrayList<>();

    @ElementCollection(targetClass = PathologyFamily.class)
    @CollectionTable(name = "review_pathologies", joinColumns = @JoinColumn(name = "review_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "pathology")
    private List<PathologyFamily> pathologies = new ArrayList<>();

    public void addPathology(PathologyFamily pathology) {
        pathologies.add(pathology);
    }

    public Review() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Méthode utilitaire pour lier un tag facilement
    public void addTag(ReviewTag tag) {
        tags.add(tag);
        tag.setReview(this);
    }

    @AssertTrue(message = "Veuillez renseigner au moins un mode de consultation (visio ou adresse physique)")
    public boolean isConsultationModeValid() {
        boolean hasAddress = this.addressIds != null && !this.addressIds.isEmpty();

        // Valide si c'est une téléconsultation OU s'il y a au moins une adresse
        return this.isTeleconsultation || hasAddress;
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.APPROVED;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @ElementCollection
    @CollectionTable(name = "review_accessible_address_ids", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "address_id")
    private List<UUID> accessibleAddressIds = new ArrayList<>();

    @Column(nullable = false)
    private boolean signLanguage = false; // Compétence du praticien (LSF)
}