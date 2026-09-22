# Kine-plan

Une application de gestion de cabinet de kinésithérapie

## Structure

- `api/` : API Java/Spring Boot
- `web/` : application Angular SSR
- `docker-compose.yml` : PostgreSQL et services applicatifs

## API

Le projet utilise Java 21, Spring Boot, PostgreSQL et Flyway.

### Démarrage local

1. Démarrer PostgreSQL avec `docker compose up -d postgres`.
2. Construire l’API avec `cd api && mvn clean verify`.
3. Lancer l’application avec `cd api && mvn spring-boot:run`.

Pour lancer PostgreSQL et l’API avec Docker :

```bash
docker compose up --build
```

La configuration de développement de l’API utilise PostgreSQL sur `localhost:5432`
avec les valeurs par défaut du fichier `application.yml`. Les secrets doivent être fournis
par variables d’environnement hors développement.

Swagger est disponible lorsque l’API est démarrée :

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

## Front-end

Le front-end utilise Angular 22, Angular Material, le routing standalone et SSR.

```bash
cd web
npm install
npm start
```

Application de développement : `http://localhost:4200`.
Le front-end consomme par défaut l’API sur `http://localhost:8080/api/v1`.

En profil `dev`, un compte de démonstration est créé automatiquement :

- Email : `admin@kineplan.local`
- Mot de passe : `ChangeMe!2026`
- Cabinet : `Cabinet Démo Kine-plan`

Ce compte est réservé au développement local et ne doit jamais être utilisé en production.

### Isolation

Le tenant courant est issu exclusivement du claim `cabinet_id` du JWT. Le contexte
ThreadLocal est nettoyé en fin de requête. Les tables métier sont protégées par
Hibernate et par PostgreSQL Row Level Security ; les connexions positionnent
`app.current_cabinet_id` avant toute requête.

Maven et Docker doivent être installés pour exécuter le build et les tests
d’intégration Testcontainers.
