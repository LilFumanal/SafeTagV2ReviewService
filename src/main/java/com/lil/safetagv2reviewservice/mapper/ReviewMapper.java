package com.lil.safetagv2reviewservice.mapper;

import com.lil.safetagv2reviewservice.entity.Review;
import com.lil.safetagv2reviewservice.models.ReviewCreateDTO;
import com.lil.safetagv2reviewservice.models.ReviewResponseDTO;
import org.springframework.stereotype.Component;

// Dans com.lil.safetagv2reviewservice.mapper.ReviewMapper
@Component
public class ReviewMapper {

    public Review toEntity(ReviewCreateDTO dto) {
        Review review = new Review();
        review.setRppsId(dto.rppsId());
        review.setUserId(dto.userId());
        review.setAddressIds(dto.addressIds());
        review.setComment(dto.comment());
        review.setTeleconsultation(dto.isTeleconsultation());
        review.setAccessibleAddressIds(dto.accessibleAddressIds()); // MAJ ici
        review.setSignLanguage(dto.signLanguage());
        review.setPathologies(dto.pathologies());
        return review;
    }

    public ReviewResponseDTO toResponseDTO(Review entity) {
        return new ReviewResponseDTO(
                entity.getId(),
                entity.getRppsId(),
                entity.getUserId(),
                entity.getAddressIds(),
                entity.getComment(),
                entity.isTeleconsultation(),
                entity.getAccessibleAddressIds(), // MAJ ici
                entity.isSignLanguage(),
                entity.getPathologies(),
                entity.getCreatedAt(),
                entity.getStatus()
        );
    }
}
