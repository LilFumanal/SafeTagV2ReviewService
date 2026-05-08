package com.lil.safetagv2reviewservice.client;

import com.lil.safetagv2reviewservice.domain.ReviewStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.UUID;

@Service
public class ModerationClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String moderationUrl;

    public ModerationClient(@Value("${services.moderationService}")  String moderationService) {
        this.moderationUrl = moderationService + "/check";
    }

    public ReviewStatus moderateComment(String text) {
        Map<String, String> request = Map.of("text", text);

        try {
            // Appel POST au moderation-service
            Map response = restTemplate.postForObject(moderationUrl, request, Map.class);

            if (response != null && response.get("status") != null) {
                return ReviewStatus.valueOf(response.get("status").toString());
            }

            // Si la réponse est bizarre, on sécurise en le mettant en attente
            throw new IllegalStateException("Réponse invalide du service de modération.");

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Si le moderation-service a renvoyé une erreur 4xx (texte refusé)
            throw new IllegalArgumentException("Le contenu de l'avis ne respecte pas nos règles de modération. Veuillez le modifier avant de publier.");

        } catch (Exception e) {
            throw new IllegalStateException("Le service de modération est temporairement indisponible. Veuillez réessayer plus tard.");
        }
    }

    public void reportReview(UUID reviewId) {
        // On construit l'URL de signalement
        String reportUrl = moderationUrl + "/report/" + reviewId;

        try {
            // On envoie un POST vide (null) car l'ID est dans l'URL
            restTemplate.postForLocation(reportUrl, null);
        } catch (Exception e) {
            // On log l'erreur mais on ne bloque pas l'utilisateur
            // car le statut a déjà été changé en base locale
            System.err.println("Erreur lors de la notification de modération : " + e.getMessage());
        }
    }
}
