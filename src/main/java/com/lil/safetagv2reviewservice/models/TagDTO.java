package com.lil.safetagv2reviewservice.models;

import com.lil.safetagv2reviewservice.domain.TagCategory;
import com.lil.safetagv2reviewservice.domain.TagVote;

public record TagDTO(
        TagCategory category,
        TagVote vote
) {}