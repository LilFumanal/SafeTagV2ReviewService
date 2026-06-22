package com.lil.safetagv2reviewservice.repository;

import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import com.lil.safetagv2reviewservice.domain.ReviewStatus;
import com.lil.safetagv2reviewservice.domain.TagCategory;
import com.lil.safetagv2reviewservice.entity.Review;
import com.lil.safetagv2reviewservice.models.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    // Spring génère automatiquement la requête SQL pour trouver les avis d'un praticien
    List<Review> findByRppsId(String rppsId);
    Page<Review> findByRppsId(String rppsId, Pageable pageable);
    boolean existsByUserIdAndRppsId(UUID userId, String rppsId);
    Page<ReviewResponseDTO> findByUserId(UUID userId, Pageable pageable);
    Page<Review> findByRppsIdAndStatus(String rppsId, ReviewStatus status, Pageable pageable);

    // Filtre : "Quels praticiens ont au moins une adresse accessible ?"
    @Query("SELECT DISTINCT r.rppsId FROM Review r JOIN r.accessibleAddressIds addr WHERE r.status = 'APPROVED'")
    Page<String> findRppsIdsWithWheelchairAccess(Pageable pageable);

    // Filtre : "Quels praticiens pratiquent la LSF ?"
    @Query("SELECT DISTINCT r.rppsId FROM Review r WHERE r.signLanguage = true AND r.status = 'APPROVED'")
    Page<String> findRppsIdsWithSignLanguage(Pageable pageable);

    // Recherche par pathologie
    @Query("SELECT DISTINCT r.rppsId FROM Review r JOIN r.pathologies p " +
            "WHERE p = :pathology AND r.status = 'APPROVED'")
    Page<String> findRppsIdsByPathology(PathologyFamily pathology, Pageable pageable);

    // Recherche par tags manquants
    @Query("SELECT DISTINCT r.rppsId FROM Review r WHERE r.status = 'APPROVED' " +
            "AND r.rppsId NOT IN (" +
            "  SELECT DISTINCT r2.rppsId FROM Review r2 JOIN r2.tags t " +
            "  WHERE t.category IN :categories AND r2.status = 'APPROVED'" +
            ")")
    Page<String> findRppsIdsMissingSpecificCategories(List<TagCategory> categories, Pageable pageable);

    // Recherche sans aucun tag
    @Query("SELECT DISTINCT r.rppsId FROM Review r WHERE r.status = 'APPROVED' " +
            "AND NOT EXISTS (SELECT t FROM r.tags t)")
    Page<String> findRppsIdsWithNoTagsAtAll(Pageable pageable);
}
