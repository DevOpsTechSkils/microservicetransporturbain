# service-reservation

Service Spring Boot gérant le cycle de vie des réservations de trajets.

## Prérequis
- Java 17
- Maven 3.8+
- PostgreSQL 15 (ou Docker)
- Eureka Server démarré sur le port 8761
- service-paiement démarré (pour confirmer les paiements)

## Démarrage sans Docker

1. Créer la base de données :
```sql
CREATE DATABASE reservation_db;
```

2. Lancer le service :
```bash
mvn spring-boot:run
```

Le service démarre sur le **port 8082**.

## Démarrage avec Docker

```bash
docker-compose up -d
```

## Endpoints disponibles

| Méthode | URL                                        | Description                        |
|---------|--------------------------------------------|------------------------------------|
| POST    | /api/reservations                          | Créer une réservation              |
| GET     | /api/reservations/{id}                     | Obtenir une réservation par ID     |
| GET     | /api/reservations/client/{clientId}        | Réservations d'un client           |
| GET     | /api/reservations                          | Toutes les réservations            |
| PUT     | /api/reservations/{id}/annuler             | Annuler une réservation            |

## Exemple de requête POST

```json
POST http://localhost:8082/api/reservations
Content-Type: application/json

{
  "clientId": 1,
  "ligneTrajet": "Douala-Yaoundé",
  "villeDepart": "Douala",
  "villeArrivee": "Yaoundé",
  "dateTrajet": "2026-06-15",
  "heureDepart": "08:00",
  "nombrePlaces": 2,
  "prixTotal": 15000.00
}
```

## Statuts possibles d'une réservation

- `EN_ATTENTE` : créée, paiement pas encore traité
- `CONFIRMEE`  : paiement accepté par service-paiement
- `ECHOUEE`    : paiement refusé
- `ANNULEE`    : annulée par le client

## Communication avec service-paiement

Le service appelle automatiquement `service-paiement` via OpenFeign
juste après la création d'une réservation. Si service-paiement est
indisponible, la réservation reste en statut `EN_ATTENTE`.
