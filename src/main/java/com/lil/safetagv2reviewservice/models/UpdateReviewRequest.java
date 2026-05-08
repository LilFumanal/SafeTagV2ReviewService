package com.lil.safetagv2reviewservice.models;

import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import com.lil.safetagv2reviewservice.entity.ReviewTag;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UpdateReviewRequest {

    private String comment;
    private List<ReviewTag> tags;
    private List<PathologyFamily> pathologies;
    private List<UUID> addressIds;
    private boolean isTeleconsultation;
    private boolean wheelchairAccessible;
    private boolean signLanguage;
}
