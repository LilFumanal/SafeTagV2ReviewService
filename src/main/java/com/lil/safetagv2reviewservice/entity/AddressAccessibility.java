package com.lil.safetagv2reviewservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Embeddable
@Getter
@Setter
public class AddressAccessibility {

    @Id
    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    // Stockage flexible des données brutes venant de l'API AccesLibre
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "acceslibre_data", columnDefinition = "jsonb")
    private Map<String, Object> accesLibreData;

}
