package com.lil.safetagv2reviewservice.mapper;

import com.lil.safetagv2reviewservice.entity.AddressAccessibility;
import com.lil.safetagv2reviewservice.entity.Review;
import com.lil.safetagv2reviewservice.models.AddressAccessibilityDTO;
import com.lil.safetagv2reviewservice.models.ReviewCreateDTO;
import com.lil.safetagv2reviewservice.models.ReviewResponseDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Dans com.lil.safetagv2reviewservice.mapper.ReviewMapper
@Component
public class ReviewMapper {

    public Review toEntity(ReviewCreateDTO dto) {
        Review review = new Review();
        review.setRppsId(dto.rppsId());
        review.setUserId(dto.userId());
        review.setAddressIds(dto.addressIds() != null ? dto.addressIds() : new ArrayList<>());
        review.setComment(dto.comment());
        review.setTeleconsultation(dto.isTeleconsultation());
        // --- Conversion de l'accessibilité par adresse ---
        if (dto.addressAccessibility() != null) {
            List<AddressAccessibility> accessibilities = dto.addressAccessibility().stream()
                    .map(accDto -> {
                        AddressAccessibility acc = new AddressAccessibility();
                        acc.setAddressId(accDto.addressId());
                        acc.setAccessible(accDto.accessible());
                        return acc;
                    })
                    .collect(Collectors.toList());
            review.setAddressAccessibility(accessibilities);
        }
        // --- Données linguistiques & LSF ---
        review.setSignLanguage(dto.signLanguage());
        review.setLanguages(dto.languages() != null ? dto.languages() : new ArrayList<>());
        review.setCustomLanguage(dto.customLanguage());
        review.setPathologies(dto.pathologies() != null ? dto.pathologies() : new ArrayList<>());
        return review;
    }

    public ReviewResponseDTO toResponseDTO(Review entity) {
        // --- Conversion de l'accessibilité par adresse ---
        List<AddressAccessibilityDTO> accessibilityDTOs = new ArrayList<>();
        if (entity.getAddressAccessibility() != null) {
            accessibilityDTOs = entity.getAddressAccessibility().stream()
                    .map(acc -> new AddressAccessibilityDTO(acc.getAddressId(), acc.getAccessible()))
                    .collect(Collectors.toList());
        }
        return new ReviewResponseDTO(
                entity.getId(),
                entity.getRppsId(),
                entity.getUserId(),
                entity.getAddressIds(),
                entity.getComment(),
                entity.isTeleconsultation(),
                accessibilityDTOs,
                entity.getSignLanguage(),
                entity.getLanguages(),
                entity.getCustomLanguage(),
                entity.getPathologies(),
                entity.getCreatedAt(),
                entity.getStatus()
        );
    }
}
