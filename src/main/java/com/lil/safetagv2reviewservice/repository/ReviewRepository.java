package com.lil.safetagv2reviewservice.repository;

import com.lil.safetagv2reviewservice.domain.ReviewStatus;
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
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Spring génère automatiquement la requête SQL pour trouver les avis d'un praticien
    List<ReviewResponseDTO> findByRppsId(String rppsId);
    Page<ReviewResponseDTO> findByRppsId(String rppsId, Pageable pageable);
    boolean existsByUserIdAndRppsId(UUID userId, String rppsId);
    List<ReviewResponseDTO> findByUserId(UUID userId);
    Page<ReviewResponseDTO> findByRppsIdAndStatus(String rppsId, ReviewStatus status, Pageable pageable);

    Optional<Review> findById(UUID reviewId);

    // Filtre : "Quels praticiens ont au moins une adresse accessible ?"
    @Query("SELECT DISTINCT r.rppsId FROM Review r JOIN r.accessibleAddressIds addr WHERE r.status = 'APPROVED'")
    List<String> findRppsIdsWithWheelchairAccess();

    // Filtre : "Quels praticiens pratiquent la LSF ?"
    @Query("SELECT DISTINCT r.rppsId FROM Review r WHERE r.signLanguage = true AND r.status = 'APPROVED'")
    List<String> findRppsIdsWithSignLanguage();

}
