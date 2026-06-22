package com.lil.safetagv2reviewservice.entity;

import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Embeddable
@Getter
@Setter
public class AddressAccessibility {

    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "accessible", nullable = false)
    private ThreeStateAnswer accessible = ThreeStateAnswer.UNKNOWN;
}
