package com.lil.safetagv2reviewservice.mapper;

import com.lil.safetagv2reviewservice.entity.Review;
import com.lil.safetagv2reviewservice.entity.ReviewTag;
import com.lil.safetagv2reviewservice.models.ReviewCreateDTO;
import com.lil.safetagv2reviewservice.models.ReviewResponseDTO;
import com.lil.safetagv2reviewservice.models.TagDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
        review.setPathologies(dto.pathologies());
        if (dto.tags() != null) {
            List<ReviewTag> tags = dto.tags().stream()
                    .map(t -> new ReviewTag(t.category(), t.vote(), review))
                    .collect(Collectors.toList());
            review.setTags(tags);
        }
        return review;
    }

    public ReviewResponseDTO toResponseDTO(Review entity) {
        List<TagDTO> tagDTOs = entity.getTags().stream()
                .map(tag -> new TagDTO(tag.getCategory(), tag.getVote()))
                .toList();
        return new ReviewResponseDTO(
                entity.getId(),
                entity.getRppsId(),
                entity.getUserId(),
                entity.getAddressIds(),
                entity.getComment(),
                entity.isTeleconsultation(),
                entity.getPathologies(),
                tagDTOs,
                entity.getCreatedAt(),
                entity.getStatus()
        );
    }
}
