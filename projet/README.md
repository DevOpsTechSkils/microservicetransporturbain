# Transport Urbain Cameroun — Microservices

Application de gestion du transport urbain à Douala, basée sur Spring Boot & Spring Cloud.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (Angular)                         │
│                       localhost:4200                            │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP
┌──────────────────────────▼──────────────────────────────────────┐
│                      API GATEWAY :8080                          │
│               (Filtre JWT + Routage vers services)              │
└──────┬───────────────────┬───────────────────┬──────────────────┘
       │                   │                   │
┌──────▼──────┐  ┌─────────▼─────┐  ┌──────────▼──────┐
│  service-   │  │  service-     │  │  service-       │
│  client     │  │  reservation  │  │  paiement       │
│  :8081      │  │  :8082        │  │  :8083          │
└──────┬──────┘  └──────┬────────┘  └──────┬──────────┘
       │                │                   │
┌──────▼──────┐  ┌──────▼────────┐  ┌──────▼──────────┐
│  db-client  │  │ db-reservation│  │  db-paiement    │
│  :5432      │  │  :5433        │  │  :5434          │
└─────────────┘  └───────────────┘  └─────────────────┘

               ┌──────────────────┐
               │  EUREKA SERVER   │  (registre des services)
               │  :8761           │
               └──────────────────┘
```

## Lancement rapide (Docker Compose)

```bash
# 1. Cloner le projet
git clone https://github.com/DevOpsTechSkills/microservicetransporturbain.git
cd microservicetransporturbain

# 2. Lancer l'infrastructure (Eureka + Gateway + bases de données)
docker compose up eureka-server api-gateway db-client db-reservation db-paiement -d

# 3. Vérifier qu'Eureka est démarré
open http://localhost:8761

# 4. Lancer tous les services
docker compose up -d
```

## Lancement en développement (sans Docker)

Chaque service se lance indépendamment dans son propre terminal :

```bash
# Terminal 1 — Eureka Server
cd eureka-server && mvn spring-boot:run

# Terminal 2 — API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 3 — Service Client (Dev 2)
cd service-client && mvn spring-boot:run

# Terminal 4 — Service Réservation (Dev 3)
cd service-reservation && mvn spring-boot:run

# Terminal 5 — Service Paiement (Dev 4)
cd service-paiement && mvn spring-boot:run
```

> **Prérequis** : Java 21, Maven 3.9+, PostgreSQL 15 en local.

## Compilation globale

```bash
# Compiler tous les modules depuis la racine
mvn clean install -DskipTests

# Compiler un seul module
cd eureka-server && mvn clean package -DskipTests
```

## CI/CD (GitHub Actions)

Le pipeline se déclenche automatiquement à chaque `push` sur `main` :

| Job | Action |
|-----|--------|
| `build-eureka` | Compile + teste Eureka Server |
| `build-gateway` | Compile + teste API Gateway |
| `build-docker` | Construit et pousse les images Docker Hub |

**Secrets à configurer dans GitHub** (Settings → Secrets and variables → Actions) :
- `DOCKER_USERNAME` : votre identifiant Docker Hub
- `DOCKER_PASSWORD` : votre token Docker Hub (dckr_pat_...)

## Branches de l'équipe

| Branche | Développeur | Responsabilité |
|---------|-------------|----------------|
| `dev1`  | Dev 1 | Eureka Server + API Gateway + Infrastructure |
| `dev2`  | Dev 2 | Service Client (Auth, profil) |
| `dev3`  | Dev 3 | Service Réservation |
| `dev4`  | Dev 4 | Service Paiement + BDD |
| `dev5`  | Dev 5 | Frontend Angular |
| `dev6`  | Dev 6 | Tests + Documentation |

## Ports utilisés

| Service | Port |
|---------|------|
| Eureka Server | 8761 |
| API Gateway | 8080 |
| Service Client | 8081 |
| Service Réservation | 8082 |
| Service Paiement | 8083 |
| Frontend Angular | 4200 |
| PostgreSQL Client | 5432 |
| PostgreSQL Réservation | 5433 |
| PostgreSQL Paiement | 5434 |
