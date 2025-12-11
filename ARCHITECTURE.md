# 📋 Planification Architecturale - SmartLogi Delivery Management System v0.1.0

## 🎯 Vue d'Ensemble du Projet

**SmartLogi** est un système de gestion de livraison développé avec **Spring Boot 3.5.7** et **Java 17** (migration vers Java 21 en cours). Le système permet de gérer l'ensemble du cycle de vie des colis, des missions de livraison, des livreurs et des clients.

---

## 🏗️ Architecture Globale

### Architecture en Couches (Clean Architecture / Hexagonal)

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
│  (Controllers REST + DTOs + Validation + Authentication)        │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│                   APPLICATION LAYER                             │
│  (Use Cases + Services métier + Mappers + DTOs)                │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│                     DOMAIN LAYER                                │
│  (Entities + Value Objects + Repositories Interfaces)           │
│  (Business Logic + Domain Events + Exceptions)                  │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┘
│                  INFRASTRUCTURE LAYER                           │
│  (JPA Repositories + Configuration + Email + Security)          │
│  (Adapters + Persistence + External Services)                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 Détail des Couches

### 1️⃣ **Couche Présentation** (`presentation/`)

**Responsabilité** : Exposer les API REST et gérer les requêtes HTTP.

**Composants** :
- **Controllers** :
  - `ColisController` - Gestion des colis (CRUD)
  - `LivreurController` - Gestion des livreurs
  - `MissionController` - Gestion des missions de livraison
  - `AuthentificationController` - Authentification JWT

- **DTOs** (Request/Response) :
  - `RegisterRequest`, `AuthentificationRequest`, `AuthentificationResponse`
  - DTOs métier pour validation des entrées

**Technologies** :
- Spring Web MVC
- Spring Validation
- Swagger/OpenAPI (`springdoc-openapi-starter-webmvc-ui`)

---

### 2️⃣ **Couche Application** (`application/`)

**Responsabilité** : Orchestrer la logique métier et coordonner les use cases.

**Composants** :

#### **Services** (`application/service/`) :
- `ColisService` - Logique métier des colis
- `LivreurService` - Logique métier des livreurs
- `MissionService` - Logique métier des missions
- `DestinataireService` - Gestion des destinataires
- `BaseUserService` - Gestion des utilisateurs
- `AuthentificationService` - Logique d'authentification
- `JWTService` - Génération et validation des tokens JWT
- `EmailService` - Envoi d'emails (événements asynchrones)

#### **DTOs** (`application/dto/`) :
- `MissionRequestDTO`, `MissionResponseDTO`
- `UserRequestRegisterDTO`, `UserResponseRegisterDTO`
- `DestinataireRequestDTO`
- `ColisRequestDTO`, `ColisResponseDTO`

#### **Mappers** (`application/mapper/`) :
- Utilisation de **MapStruct** pour la conversion Entité ↔ DTO
- Configuration : `@Mapper(componentModel = "spring")`

#### **Validation** (`application/validation/`) :
- `EmailValidator` + `@EmailValid`
- `PasswordMatchesValidator` + `@PasswordValid`
- `ColisDestinationValidator` + `@ColisDestinataireValid`

---

### 3️⃣ **Couche Domaine** (`domain/`)

**Responsabilité** : Définir les entités métier, les règles business et les interfaces des repositories.

**Composants** :

#### **Entités** (`domain/model/entity/`) :

**Entités Principales** :
- `Colis` - Représente un colis à livrer
  - Attributs : id, description, poids, villeDestination, statut, priorité
  - Relations : ClientExpediteur, Destinataire, Livreur (collecte/livraison), Zone, HistoriqueLivraison
  
- `Mission` - Représente une mission de livraison/collecte
  - Attributs : id, type, statut, origineAdresse, destinationAdresse, datePrevue, dateEffective
  - Relations : Livreur, Colis
  
- `Zone` - Zone géographique de livraison
  
- `HistoriqueLivraison` - Traçabilité des livraisons

**Hiérarchie Utilisateurs** (`users/`) :
- `BaseUser` (classe parent abstraite)
  - Implémente `UserDetails` (Spring Security)
  - Attributs communs : id, firstName, lastName, email, password, adresse, telephone, role
  - Héritage : `InheritanceType.JOINED`
  
- `ClientExpediteur` - Client qui envoie des colis
- `Destinataire` - Personne qui reçoit des colis
- `Livreur` - Livreur avec véhicule
- `Gestionnaire` - Gestionnaire du système

#### **Value Objects** (`domain/model/vo/`) :
- `Adresse` - VO pour les adresses (rue, numero, ville, codePostal, pays)
- `Telephone` - VO pour les téléphones (codePays, nombre)
- `Poids` - VO pour le poids (valeur, unité)
- `Vehicule` - VO pour les véhicules

#### **Enums** (`domain/model/enums/`) :
- `StatusColis` - EN_STOCK, EN_COURS_COLLECTE, COLLECTE, EN_TRANSIT, EN_COURS_LIVRAISON, LIVRE
- `PriorityColis` - STANDARD, EXPRESS, URGENT
- `MissionType` - COLLECTE, LIVRAISON
- `StatutMission` - PLANIFIEE, EN_COURS, TERMINEE, ANNULEE
- `Role` - CLIENT_EXPEDITEUR, DESTINATAIRE, LIVREUR, GESTIONNAIRE

#### **Repositories** (`domain/repository/`) :
Interfaces JPA Repository :
- `ColisRepository`
- `LivreurRepository`
- `MissionRepository`
- `DestinataireRepository`
- `ClientExpediteurRepository`
- `BaseUserRepository`
- `ZoneRepository`

#### **Exceptions** (`domain/exception/`) :
- `ResourceNotFoundException`

#### **Events** (`domain/event/`) :
- Événements de domaine pour la gestion asynchrone

---

### 4️⃣ **Couche Infrastructure** (`infrastructure/`)

**Responsabilité** : Implémentation technique des interfaces du domaine et configuration.

**Composants** :

#### **Configuration** (`infrastructure/configuration/`) :
- `ApplicationConfig` - Configuration générale
  - Bean `userDetailsService`
  - Bean `authenticationProvider`
  - Bean `passwordEncoder` (BCrypt)
  
- `SecurityConfiguration` - Configuration Spring Security
  - JWT Filter Chain
  - CORS Configuration
  - Endpoints publics/privés
  
- `AsyncConfiguration` - Configuration asynchrone (@EnableAsync)

#### **Persistence** (`infrastructure/persistence/`) :
- Implémentations JPA des repositories
- Configuration Hibernate avec PostgreSQL

#### **Email** (`infrastructure/email/`) :
- `EmailService` - Envoi d'emails via SMTP (Gmail)
- `EmailExpediteurEvent` - Événements d'envoi d'email
- Templates Thymeleaf (`email-template.html`)

#### **Adapters** (`infrastructure/adapter/`) :
- Adaptateurs pour services externes

#### **Utils** (`infrastructure/utils/`) :
- Utilitaires techniques

#### **Handlers** (`infrastructure/handler/`) :
- Gestion globale des exceptions (@ControllerAdvice)

---

## 🔒 Sécurité

### Architecture de Sécurité

```
Client Request → JWT Filter → Authentication → Authorization → Controller
```

**Composants** :
- **JWT (JSON Web Token)** :
  - Bibliothèque : `jjwt` (0.13.0)
  - Service : `JWTService`
  - Génération/Validation/Extraction des tokens
  
- **Spring Security** :
  - Authentication Provider avec `UserDetailsService`
  - Password Encoder : BCrypt
  - Filter Chain personnalisé
  
- **Rôles** :
  - `CLIENT_EXPEDITEUR` - Peut créer des colis
  - `DESTINATAIRE` - Reçoit des colis
  - `LIVREUR` - Effectue les livraisons
  - `GESTIONNAIRE` - Administration

---

## 💾 Base de Données

### PostgreSQL 15

**Configuration** :
- Host : `localhost:5432` (dev) / `sdms-db-v2:5432` (Docker)
- Database : `smartlogi_db_v2`
- User : `postgres`
- Password : `1234`

### Gestion des Migrations : Liquibase

**Fichiers** :
- Master : `db/changelog/db.changelog-master.xml`
- Changelogs incrémentaux dans `db/changelog/2025/11/`

**Stratégie** :
- `spring.jpa.hibernate.ddl-auto: none`
- Toutes les modifications de schéma via Liquibase
- Traçabilité complète des changements

### Schéma de Base de Données

**Tables Principales** :
- `base_user` - Table parent des utilisateurs
- `client_expediteur` - Clients expéditeurs
- `destinataire` - Destinataires
- `livreur` - Livreurs
- `gestionnaire` - Gestionnaires
- `colis` - Colis
- `mission` - Missions
- `zone` - Zones géographiques
- `historique_livraison` - Historique des livraisons

**Relations** :
- `Colis` → `ClientExpediteur` (ManyToOne)
- `Colis` → `Destinataire` (ManyToOne)
- `Colis` → `Livreur` (collecte/livraison) (ManyToOne)
- `Colis` → `Zone` (ManyToOne)
- `Colis` → `HistoriqueLivraison` (OneToMany)
- `Mission` → `Livreur` (ManyToOne)
- `Mission` → `Colis` (ManyToOne)

---

## 🚀 Stack Technique

### Backend
| Technologie | Version | Usage |
|-------------|---------|-------|
| **Java** | 17 (→ 21) | Langage principal |
| **Spring Boot** | 3.5.7 | Framework principal |
| **Spring Data JPA** | 3.5.7 | Persistance |
| **Spring Security** | 6.x | Sécurité |
| **Spring Validation** | 3.5.7 | Validation |
| **Spring Mail** | 3.5.7 | Envoi d'emails |
| **Liquibase** | 4.31.1 | Migrations DB |
| **PostgreSQL Driver** | 42.7.8 | Driver JDBC |
| **Hibernate** | 6.x | ORM |
| **MapStruct** | 1.5.5 | Mapping DTO/Entités |
| **Lombok** | 1.18.32 | Réduction boilerplate |
| **JWT (jjwt)** | 0.13.0 | Tokens JWT |
| **Commons Lang3** | 3.18.0 | Utilitaires |
| **Dotenv** | 3.0.0 | Variables d'environnement |

### Documentation
| Technologie | Version | Usage |
|-------------|---------|-------|
| **SpringDoc OpenAPI** | 2.2.0 | Documentation API (Swagger UI) |

### Monitoring & Observabilité
| Technologie | Version | Usage |
|-------------|---------|-------|
| **Spring Actuator** | 3.5.7 | Endpoints de monitoring |
| **Micrometer Prometheus** | Latest | Métriques Prometheus |
| **Logstash Logback** | 7.4 | Logging structuré |

### Email
| Technologie | Version | Usage |
|-------------|---------|-------|
| **Thymeleaf** | Latest | Templates emails |
| **Spring Mail** | 3.5.7 | Envoi SMTP |

### Build & Dev Tools
| Technologie | Version | Usage |
|-------------|---------|-------|
| **Maven** | 3.x | Build tool |
| **Spring DevTools** | 3.5.7 | Hot reload |

---

## 🐳 Infrastructure Docker

### Services Docker Compose

```yaml
services:
  1. app (Spring Boot)      - Port 8080
  2. db (PostgreSQL 15)     - Port 5432
  3. prometheus             - Port 9090
  4. grafana                - Port 3000
  5. elasticsearch          - Port 9200
  6. logstash               - Port 5044
  7. kibana                 - Port 5601
```

**Stack ELK** :
- **Elasticsearch** - Stockage des logs
- **Logstash** - Ingestion des logs
- **Kibana** - Visualisation des logs

**Monitoring** :
- **Prometheus** - Collecte des métriques
- **Grafana** - Dashboards de monitoring

---

## 📊 Flux de Données

### Flux de Création d'un Colis

```
1. Client → POST /api/colis (ColisRequestDTO)
2. ColisController → Validation (@Valid)
3. ColisController → ColisService.createColis()
4. ColisService → Mapper (DTO → Entity)
5. ColisService → ColisRepository.save()
6. ColisService → EmailService.sendEmail() [Async]
7. ColisService → Mapper (Entity → DTO)
8. ColisController → Response (ColisResponseDTO)
```

### Flux d'Authentification JWT

```
1. Client → POST /api/auth/login (email, password)
2. AuthentificationController → AuthentificationService
3. AuthentificationService → AuthenticationProvider.authenticate()
4. UserDetailsService → BaseUserRepository.findByEmail()
5. Password verification (BCrypt)
6. JWTService → generateToken(UserDetails)
7. Response → AuthentificationResponse (token, user info)
```

### Flux de Mission de Livraison

```
1. Gestionnaire → POST /api/missions (MissionRequestDTO)
2. MissionController → MissionService.createMission()
3. MissionService → Validate (Livreur disponible, Colis en stock)
4. MissionService → Update Colis.statut → EN_COURS_COLLECTE
5. MissionService → Create Mission entity
6. MissionService → Notify Livreur [Email/Push]
7. Response → MissionResponseDTO
```

---

## 🔄 Patterns de Conception

### 1. **Repository Pattern**
- Abstraction de la couche de persistance
- Interfaces dans `domain/repository/`
- Implémentations JPA automatiques (Spring Data)

### 2. **DTO Pattern**
- Séparation Entités ↔ DTOs
- Mapping avec MapStruct
- Validation avec Bean Validation

### 3. **Service Layer Pattern**
- Services dans `application/service/`
- Logique métier centralisée
- Transactions gérées par Spring

### 4. **Value Object Pattern**
- `Adresse`, `Telephone`, `Poids`, `Vehicule`
- Immuabilité et encapsulation
- Embedded dans les entités

### 5. **Strategy Pattern**
- Gestion des différents types de missions (COLLECTE/LIVRAISON)
- Gestion des statuts de colis

### 6. **Observer Pattern**
- Événements de domaine (`@EventListener`)
- EmailService asynchrone (@Async)

### 7. **Builder Pattern**
- Lombok @Builder pour les entités
- Construction fluide des objets

---

## 🧪 Tests

### Structure des Tests

```
src/test/java/com/smartlogi/sdms/
├── unit/          # Tests unitaires (services, mappers)
└── integration/   # Tests d'intégration (repositories, API)
```

### Stratégie de Tests

**Tests Unitaires** :
- Services métier avec mocks
- Mappers MapStruct
- Validateurs personnalisés

**Tests d'Intégration** :
- Repositories avec base H2/TestContainers
- Controllers REST avec MockMvc
- Tests de sécurité JWT

**Technologies** :
- JUnit 5
- Mockito
- Spring Test
- TestContainers (recommandé)

---

## 📈 Monitoring et Observabilité

### Endpoints Actuator

```
/actuator/health     - État de santé
/actuator/info       - Informations de l'application
/actuator/prometheus - Métriques Prometheus
```

### Métriques Prometheus

**Métriques collectées** :
- JVM (heap, threads, GC)
- HTTP (requêtes, latence, erreurs)
- Base de données (connexions, requêtes)
- Métriques métier personnalisées

### Logging

**Configuration** :
- Logback avec encoder Logstash
- Logs structurés en JSON
- Niveaux : ERROR, WARN, INFO, DEBUG

**Destinations** :
- Console (dev)
- Fichiers (prod)
- Elasticsearch via Logstash

---

## 🔐 Configuration de Sécurité

### Endpoints Publics
```
/api/auth/**       - Authentification
/actuator/health   - Health check
/swagger-ui/**     - Documentation
/v3/api-docs/**    - OpenAPI
```

### Endpoints Protégés
```
/api/colis/**      - ROLE_CLIENT_EXPEDITEUR, ROLE_GESTIONNAIRE
/api/missions/**   - ROLE_GESTIONNAIRE, ROLE_LIVREUR
/api/livreurs/**   - ROLE_GESTIONNAIRE
```

### CORS Configuration
- Origines autorisées : configurable
- Méthodes : GET, POST, PUT, DELETE, PATCH
- Headers : Authorization, Content-Type

---

## 📧 Service Email

### Configuration SMTP
- Provider : Gmail
- Host : smtp.gmail.com
- Port : 587 (STARTTLS)
- Auth : safiakhoulaid11@gmail.com

### Templates Thymeleaf
- `email-template.html` - Template de base
- Variables dynamiques : nom, prénom, message

### Cas d'Usage
- Notification création de colis
- Notification affectation de mission
- Notification changement de statut

---

## 🚦 États et Transitions

### Cycle de Vie d'un Colis

```
EN_STOCK → EN_COURS_COLLECTE → COLLECTE → EN_TRANSIT → 
EN_COURS_LIVRAISON → LIVRE
```

### Cycle de Vie d'une Mission

```
PLANIFIEE → EN_COURS → TERMINEE
                    ↓
                ANNULEE
```

---

## 🛠️ Points d'Amélioration Identifiés

### 1. **Migration Java 21**
- ✅ Actuellement : Java 17
- 🎯 Objectif : Java 21 LTS
- Bénéfices : Virtual Threads, Pattern Matching, Records

### 2. **Problème de Configuration JPA**
- ❌ Erreur actuelle : `Cannot resolve reference to bean 'jpaSharedEM_entityManagerFactory'`
- 🔍 Cause probable : Configuration multi-datasource incomplète
- 🔧 Solution : Réviser la configuration JPA dans `ApplicationConfig`

### 3. **Connexion PostgreSQL**
- ❌ Erreur : `Connection to localhost:5432 refused`
- 🔧 Solution : Démarrer PostgreSQL ou utiliser Docker Compose

### 4. **Tests Manquants**
- ⚠️ Structure créée mais tests non implémentés
- 🎯 Objectif : Couverture > 80%

### 5. **Documentation API**
- ✅ Swagger configuré
- ⚠️ Descriptions manquantes sur endpoints
- 🎯 Ajouter `@Operation`, `@ApiResponse`

### 6. **Gestion des Erreurs**
- ⚠️ `ResourceNotFoundException` présent
- 🎯 Ajouter `@ControllerAdvice` global
- 🎯 Standardiser les réponses d'erreur

### 7. **Validation**
- ✅ Validateurs personnalisés créés
- ⚠️ Manque validation sur certains endpoints
- 🎯 Compléter `@Valid` sur tous les DTOs

### 8. **Sécurité**
- ⚠️ Credentials en clair dans `application.yml`
- 🎯 Utiliser variables d'environnement
- 🔧 Implémenter rotation des secrets

---

## 📝 Roadmap Technique

### Phase 1 : Stabilisation (En cours)
- [x] Architecture Clean Architecture
- [x] Configuration Spring Boot 3.5.7
- [x] Sécurité JWT
- [x] Liquibase
- [ ] Migration Java 21
- [ ] Fix configuration JPA
- [ ] Tests unitaires complets

### Phase 2 : Amélioration (À venir)
- [ ] Tests d'intégration avec TestContainers
- [ ] Cache Redis pour sessions
- [ ] Message Queue (RabbitMQ/Kafka) pour événements
- [ ] API REST complète (HATEOAS)
- [ ] Pagination et filtrage avancé

### Phase 3 : Production (Future)
- [ ] CI/CD (GitHub Actions/GitLab CI)
- [ ] Kubernetes Deployment
- [ ] Monitoring avancé (APM)
- [ ] Alerting (PagerDuty/Opsgenie)
- [ ] Backup automatisé DB
- [ ] Documentation technique complète

---

## 🎓 Bonnes Pratiques Appliquées

✅ **Clean Architecture** - Séparation claire des responsabilités  
✅ **SOLID Principles** - Code maintenable et extensible  
✅ **DRY (Don't Repeat Yourself)** - Réutilisation avec MapStruct  
✅ **Single Responsibility** - Classes avec responsabilités uniques  
✅ **Dependency Injection** - Inversion de contrôle avec Spring  
✅ **Immutability** - Value Objects immuables  
✅ **Fail Fast** - Validation en entrée de controller  
✅ **Logging** - Logs structurés JSON  
✅ **Configuration externalisée** - application.yml + dotenv  
✅ **Migrations versionnées** - Liquibase  

---

## 📚 Références et Documentation

### Documentation Technique
- **Spring Boot** : https://docs.spring.io/spring-boot/
- **Spring Security** : https://docs.spring.io/spring-security/
- **Liquibase** : https://docs.liquibase.com/
- **MapStruct** : https://mapstruct.org/
- **JWT** : https://jwt.io/

### API Documentation
- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **OpenAPI JSON** : http://localhost:8080/v3/api-docs

### Monitoring
- **Prometheus** : http://localhost:9090
- **Grafana** : http://localhost:3000
- **Kibana** : http://localhost:5601

---

## 👥 Équipe et Responsabilités

### Développement
- **Backend** : Spring Boot, Java, PostgreSQL
- **DevOps** : Docker, Monitoring
- **QA** : Tests, Validation

### Rôles dans l'Application
- **CLIENT_EXPEDITEUR** - Crée et suit les colis
- **DESTINATAIRE** - Reçoit les colis
- **LIVREUR** - Effectue les missions
- **GESTIONNAIRE** - Administre le système

---

## 🎯 Métriques de Succès

### Performance
- Temps de réponse API < 200ms (p95)
- Throughput > 100 req/s
- Disponibilité > 99.9%

### Qualité
- Couverture de tests > 80%
- Zéro vulnérabilités critiques
- Code review systématique

### Business
- Traçabilité complète des colis
- Notifications temps réel
- Rapports de livraison automatisés

---

## 📞 Support et Contact

### Logs
- Fichiers : `/var/log/smartlogi/`
- Format : JSON (Logstash)
- Retention : 30 jours

### Incidents
- Alertes : Prometheus + Alertmanager
- Escalade : Email + Slack
- SLA : 4h (critique), 24h (majeur)

---

**Version** : 0.1.0  
**Date** : Novembre 2025  
**Statut** : En Développement  
**Prochaine Release** : Migration Java 21 + Stabilisation

---

## 🔄 Changelog

### v0.1.0 (Novembre 2025)
- ✅ Architecture initiale Clean Architecture
- ✅ Spring Boot 3.5.7 + Java 17
- ✅ Authentification JWT
- ✅ CRUD Colis, Missions, Livreurs
- ✅ Liquibase migrations
- ✅ Docker Compose
- ✅ Monitoring Prometheus + Grafana
- ✅ Logging ELK Stack
- ⚠️ Migration Java 21 en cours
- ❌ Tests incomplets

---

*Cette documentation sera mise à jour régulièrement au fur et à mesure de l'évolution du projet.*
