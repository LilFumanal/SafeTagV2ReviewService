# SafeTag - Review Service

Ce microservice gère la consultation et l'ajout des avis laissés sur les professionnels de santé.

## ⚙️ Fonctionnement et Règles de gestion

* **Pas de notation chiffrée :** Le système ne propose aucune note ou système d'étoiles (Rating). L'évaluation se fait par le commentaire (obligatoire si tags présents) et les tags.
* **Lieu de consultation (Obligatoire) :** Chaque avis doit **obligatoirement** renseigner le lieu de la rencontre. Cela passe par une liste d'identifiants d'adresses (`addressIds`) ou la mention explicite d'une téléconsultation (`isTeleconsultation`).
* **Pathologies (DSM-5) :** Les familles de pathologies associées à l'avis sont contrôlées et doivent obligatoirement faire partie d'une énumération basée sur le DSM-5.
* **Tags (Dimensions sensibles) :** Liste fermée de tags évaluant l'expérience patient sur des critères de discrimination (Positive / Négative). Les catégories sont strictement limitées à : `GENRE`, `POIDS`, `COULEUR_DE_PEAU`, `AGE`, `ORIENTATION_SEXUELLE`, `ETHNICITE`, `APPARENCE`, `TRANSIDENTITE`, `HANDICAP`, `PRECARITE`, `RELIGION`.
* **Identifiant Praticien (RPPS) :** Le numéro RPPS cible doit obligatoirement contenir **exactement 11 chiffres**.

---

## 🚀 Endpoints de l'API

### 1. Consulter les avis d'un praticien
Récupère la liste paginée des avis pour un professionnel de santé donné.

* **URL :** `/api/v1/reviews/practitioner/{rppsId}`
* **Méthode :** `GET`

**Paramètres :**
* `rppsId` (Path) : Numéro RPPS du praticien (11 chiffres).
* `page` (Query, optionnel) : Numéro de la page (défaut: 0).
* `size` (Query, optionnel) : Nombre d'éléments par page (défaut: 10).

**Réponses :**
* **🟢 200 OK** : Liste paginée des avis.
* **🟠 400 Bad Request** : RPPS mal formaté.

---

### 2. Ajouter un avis
Permet de soumettre un nouvel avis sur un praticien.

* **URL :** `/api/v1/reviews`
* **Méthode :** `POST`

**Payload (Body JSON) attendu :**
```json
{
  "rppsId": "10101912421",
  "userId": "d3b07384-d113-49cd-a5d6-83a061488c03",
  "comment": "Médecin très à l'écoute et particulièrement attentif aux besoins spécifiques.",
  "isTeleconsultation": true,
  "addressIds": [
    "7fe7581b-800a-4b95-a22a-f9eb6e2f1832"
  ],
  "addressAccessibilities": [
    {
      "addressId": "7fe7581b-800a-4b95-a22a-f9eb6e2f1832",
      "accessible": true
    }
  ],
  "signLanguage": false,
  "languages": [
    "FRENCH",
    "ENGLISH"
  ],
  "customLanguage": "Arabe dialectal (Darija)",
  "pathologies": [
    "TROUBLE_ANXIEUX",
    "DEPRESSION"
  ]
}
```

**Réponses :**

* **🟢 201 Created** : Avis enregistré avec succès.
* **🟠 400 Bad Request** : Données invalides (RPPS incorrect, addressIds vide ET isTeleconsultation à false, pathologie ou tag hors énumération).

---
### 🛠️ Stack Technique

Java 21 / Spring Boot
API REST
Validation (Jakarta Bean Validation)
