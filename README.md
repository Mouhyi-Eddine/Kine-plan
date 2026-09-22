# Kine-plan

Une application de gestion de cabinet de kinésithérapie

## API

Le projet utilise Java 21, Spring Boot, PostgreSQL et Flyway.

### Démarrage local

1. Démarrer PostgreSQL avec `docker compose up -d postgres`.
2. Construire l’API avec `mvn clean verify`.
3. Lancer l’application avec `mvn spring-boot:run`.

La configuration de développement utilise PostgreSQL sur `localhost:5432` avec les
valeurs par défaut du fichier `application.yml`. Les secrets doivent être fournis
par variables d’environnement hors développement.

### Isolation

Le tenant courant est issu exclusivement du claim `cabinet_id` du JWT. Le contexte
ThreadLocal est nettoyé en fin de requête. Les tables métier sont protégées par
Hibernate et par PostgreSQL Row Level Security ; les connexions positionnent
`app.current_cabinet_id` avant toute requête.

Maven et Docker doivent être installés pour exécuter le build et les tests
d’intégration Testcontainers.
