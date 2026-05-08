package com.lil.safetagv2reviewservice.service;

import com.lil.safetagv2reviewservice.client.ModerationClient;
import com.lil.safetagv2reviewservice.client.RppsClient;
import com.lil.safetagv2reviewservice.client.UserClient;
import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import com.lil.safetagv2reviewservice.domain.ReviewStatus;
import com.lil.safetagv2reviewservice.domain.TagCategory;
import com.lil.safetagv2reviewservice.domain.TagVote;
import com.lil.safetagv2reviewservice.entity.Review;
import com.lil.safetagv2reviewservice.entity.ReviewTag;
import com.lil.safetagv2reviewservice.exception.ResourceNotFoundException;
import com.lil.safetagv2reviewservice.mapper.ReviewMapper;
import com.lil.safetagv2reviewservice.models.ReviewCreateDTO;
import com.lil.safetagv2reviewservice.models.ReviewResponseDTO;
import com.lil.safetagv2reviewservice.models.UpdateReviewRequest;
import com.lil.safetagv2reviewservice.repository.ReviewRepository;
import com.lil.safetagv2reviewservice.repository.ReviewTagRepository;
import feign.FeignException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ReviewRepository reviewRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ModerationClient moderationClient;
    private final UserClient userClient;
    private final RppsClient rppsClient;

    // L'injection de dépendance se fait via le constructeur
    public ReviewService(ReviewMapper reviewMapper, ReviewRepository reviewRepository, ReviewTagRepository reviewTagRepository, ModerationClient moderationClient, UserClient userClient, RppsClient rppsClient) {
        this.reviewMapper = reviewMapper;
        this.reviewRepository = reviewRepository;
        this.reviewTagRepository = reviewTagRepository;
        this.moderationClient = moderationClient;
        this.userClient = userClient;
        this.rppsClient = rppsClient;
    }

    @Transactional
    public ReviewResponseDTO createReview(ReviewCreateDTO dto, UUID userId) {
        // 1. Mapping initial & injection du userId
        Review review = reviewMapper.toEntity(dto);
        review.setUserId(userId);

        if (reviewRepository.existsByUserIdAndRppsId(userId, review.getRppsId())) {
            throw new IllegalStateException("L'utilisateur a déjà publié un avis pour ce praticien.");
        }

        try {
            rppsClient.getPractitionerByRpps(review.getRppsId());
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("Le praticien RPPS " + review.getRppsId() + " est introuvable.");
        }

        if (!userClient.userExists(userId)) {
            throw new ResourceNotFoundException("Utilisateur introuvable pour l'ID : " + userId);
        }

        // 4. Maintenance du lien bidirectionnel (Tags)
        if (review.getTags() != null) {
            review.getTags().forEach(tag -> tag.setReview(review));
        }

        // 5. Modération automatique
        if (review.getComment() != null && !review.getComment().isBlank()) {
            ReviewStatus status = moderationClient.moderateComment(review.getComment());
            review.setStatus(status);
        } else {
            review.setStatus(ReviewStatus.APPROVED);
        }

        // 6. Sauvegarde et conversion pour la réponse
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponseDTO(savedReview);
    }

    public List<Review> getReviewsByPractitioner(String rppsId) {
        return reviewRepository.findByRppsId(rppsId);
    }

    public Map<TagCategory, Double> getPractitionerStats(String rppsId) {
        List<ReviewTag> tags = reviewTagRepository.findByReview_RppsId(rppsId);

        if (tags.isEmpty()) return Collections.emptyMap();

        return tags.stream()
                .collect(Collectors.groupingBy(
                        ReviewTag::getCategory,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long positive = list.stream().filter(t -> t.getVote() == TagVote.POSITIVE).count();
                                    double percentage = (double) positive / list.size() * 100;
                                    return Math.round(percentage * 10.0) / 10.0;
                                }
                        )
                ));
    }

    public Page<Review> getReviewsByRppsId(String rppsId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return reviewRepository.findByRppsIdAndStatus(rppsId, ReviewStatus.APPROVED, pageable);
    }

    public Review getReviewById(UUID id) {

        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable pour l'ID : " + id));
    }

    public Page<ReviewResponseDTO> getReviewsByUser(UUID userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public ReviewResponseDTO updateReview(UUID id, UUID userId, UpdateReviewRequest request) {

        // 1. Récupération de l'avis existant
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));

        // 2. Vérification de sécurité
        if (!review.getUserId().equals(userId)) {
            throw new IllegalStateException("Action non autorisée");
        }

        // 3. Mise à jour des données simples
        review.setComment(request.getComment());
        review.setTeleconsultation(request.isTeleconsultation());
        review.setSignLanguage(request.isSignLanguage());

        // 4. Gestion des collections (Tags)
        review.getTags().clear();
        if (request.getTags() != null) {
            for (ReviewTag tag : request.getTags()) {
                review.addTag(tag);
            }
        }

        // 5. Gestion des pathologies
        review.getPathologies().clear();
        if (request.getPathologies() != null) {
            review.getPathologies().addAll(request.getPathologies());
        }

        // 6. Gestion des adresses
        review.setAddressIds(
                request.getAddressIds() != null ? new ArrayList<>(request.getAddressIds()) : new ArrayList<>()
        );

        // 7. Modération et changement de statut
        ReviewStatus oldStatus = review.getStatus();

        if (review.getComment() != null && !review.getComment().isBlank()) {
            ReviewStatus autoModStatus = moderationClient.moderateComment(review.getComment());

            // Logique de bascule en PENDING si le texte est validé mais l'avis était banni
            if (autoModStatus == ReviewStatus.APPROVED &&
                    (oldStatus == ReviewStatus.REJECTED || oldStatus == ReviewStatus.REPORTED)) {
                review.setStatus(ReviewStatus.PENDING);
            } else {
                review.setStatus(autoModStatus);
            }
        } else {
            review.setStatus(ReviewStatus.APPROVED);
        }

        // 8. Sauvegarde et conversion en DTO de réponse
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponseDTO(savedReview);
    }


    public void reportReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));
        review.setStatus(ReviewStatus.REPORTED);
        reviewRepository.save(review);

        moderationClient.reportReview(reviewId);
    }

    public void updateReviewStatusToRejected(UUID reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));
        review.setStatus(ReviewStatus.REJECTED);
        review.setRejectionReason(reason);
        reviewRepository.save(review);
    }

    public void markAsPending(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));
        review.setStatus(ReviewStatus.PENDING);
        reviewRepository.save(review);
    }

    public void approveReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));
        review.setStatus(ReviewStatus.APPROVED);
        reviewRepository.save(review);
    }

    // 2. Correction du type de retour et de l'argument pour l'accessibilité
    public Page<String> getRppsWithWheelchairAccess(Pageable pageable) {
        return reviewRepository.findRppsIdsWithWheelchairAccess(pageable);
    }

    // 3. Ajout de la pagination pour la langue des signes (cohérence)
    public Page<String> getRppsWithSignLanguage(Pageable pageable) {
        return reviewRepository.findRppsIdsWithSignLanguage(pageable);
    }

    // 4. Correction de l'appel au repository avec l'argument pageable manquant
    public Page<String> getRppsMissingTags(List<TagCategory> categories, Pageable pageable) {
        if (categories == null || categories.isEmpty()) {
            return reviewRepository.findRppsIdsWithNoTagsAtAll(pageable);
        }
        // Ajout de pageable ici
        return reviewRepository.findRppsIdsMissingSpecificCategories(categories, pageable);
    }
    public Page<String> getRppsByPathology(PathologyFamily pathology, Pageable pageable) {
        return reviewRepository.findRppsIdsByPathology(pathology, pageable);
    }

}
