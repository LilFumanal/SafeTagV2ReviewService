package com.lil.safetagv2reviewservice.mapper;

import com.lil.safetagv2reviewservice.domain.ThreeStateAnswer;
import com.lil.safetagv2reviewservice.dto.*;
import com.lil.safetagv2reviewservice.entity.AddressAccessibility;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import java.util.Map;

@Getter
@Setter
@Component
public class AccessibilityMapper {

    public AccessibiliteResponse toResponse(AddressAccessibility entity) {
        if (entity == null || entity.getAccesLibreData() == null) {
            return buildUnknownResponse(); // Retourne un DTO full UNKNOWN
        }

        Map<String, Object> data = entity.getAccesLibreData();

        DetailsMoteur detailsMoteur = new DetailsMoteur(
                toThreeState(data.get("stationnement_pmr")),
                toThreeState(data.get("entree_plain_pied")),
                toThreeState(data.get("accueil_ascenseur")),
                toThreeState(data.get("sanitaires_adaptes"))
        );

        CategorieMoteur moteur = new CategorieMoteur(
                calculateStatus(detailsMoteur), // Logique métier (YES si 1 équipement OK)
                detailsMoteur
        );

        DetailsVisuel detailsVisuel = new DetailsVisuel(
                toThreeState(data.get("bandeGuidage")),
                toThreeState(data.get("audiodescription"))
        );

        CategorieVisuel categorieVisuel = new CategorieVisuel(
                calculateStatus(detailsMoteur), // Logique métier (YES si 1 équipement OK)
                detailsVisuel
        );

        DetailsAuditif detailsAuditif = new DetailsAuditif(
                toThreeState(data.get("personnelLsf")),
                toThreeState(data.get("boucleMagnetique"))
        );

        CategorieAuditif categorieAuditif = new CategorieAuditif(
                calculateStatus(detailsMoteur), // Logique métier (YES si 1 équipement OK)
                detailsAuditif
        );

        // A faire: Auditif et Visuel sur le même modèle...

        return new AccessibiliteResponse(
                ThreeStateAnswer.UNKNOWN, // A calculer globalement
                moteur,
                null, // auditif
                null  // visuel
        );
    }

    // Convertit le booléen d'AccesLibre en ThreeStateAnswer
    private ThreeStateAnswer toThreeState(Object value) {
        if (value == null) return ThreeStateAnswer.UNKNOWN;
        if (value instanceof Boolean b) {
            return b ? ThreeStateAnswer.YES : ThreeStateAnswer.NO;
        }
        return ThreeStateAnswer.UNKNOWN;
    }

    private ThreeStateAnswer calculateStatus(DetailsMoteur details) {
        if (details.accesPlainPied() == ThreeStateAnswer.YES || details.ascenseur() == ThreeStateAnswer.YES) {
            return ThreeStateAnswer.YES;
        }
        return ThreeStateAnswer.UNKNOWN;
    }
    private AccessibiliteResponse buildUnknownResponse() {
        DetailsMoteur unknownMoteurDetails = new DetailsMoteur(
                ThreeStateAnswer.UNKNOWN, ThreeStateAnswer.UNKNOWN,
                ThreeStateAnswer.UNKNOWN, ThreeStateAnswer.UNKNOWN
        );
        CategorieMoteur unknownMoteur = new CategorieMoteur(ThreeStateAnswer.UNKNOWN, unknownMoteurDetails);

        DetailsAuditif unknownAuditifDetails = new DetailsAuditif(
                ThreeStateAnswer.UNKNOWN, ThreeStateAnswer.UNKNOWN
        );
        CategorieAuditif unknownAuditif = new CategorieAuditif(ThreeStateAnswer.UNKNOWN, unknownAuditifDetails);

        DetailsVisuel unknownVisuelDetails = new DetailsVisuel(
                ThreeStateAnswer.UNKNOWN, ThreeStateAnswer.UNKNOWN
        );
        CategorieVisuel unknownVisuel = new CategorieVisuel(ThreeStateAnswer.UNKNOWN, unknownVisuelDetails);

        return new AccessibiliteResponse(
                ThreeStateAnswer.UNKNOWN,
                unknownMoteur,
                unknownAuditif,
                unknownVisuel
        );
    }
}
