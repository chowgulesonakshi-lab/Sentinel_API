# Deploy Sentinel API backend with Neon and Render

This deployment runs the Spring Boot backend as a Docker Web Service on Render and uses Neon PostgreSQL for managed storage. No credentials are stored in this repository.

## 1. Create the Neon database

1. Create a Neon project and database.
2. Open the connection details and select the pooled connection endpoint for application traffic.
3. Record the host, database name, role username, and password securely.
4. Use SSL in the JDBC URL:

```text
jdbc:postgresql://<NEON_POOLER_HOST>/<DATABASE>?sslmode=require
```

Keep the pooler hostname and username exactly as provided by Neon. Do not paste passwords into source files or commit history.

## 2. Create the Render Web Service

1. Connect this GitHub repository to a new Render Web Service.
2. Select the deployment branch.
3. Set **Language** to Docker and **Root Directory** to `backend`.
4. Set **Docker Build Context Directory** to `backend/` and **Dockerfile Path** to `backend/Dockerfile` (both paths are relative to the repository root).
5. Choose a region close to the Neon project. The deployed service currently uses Ohio (US East).
6. Set the health check path to `/actuator/health`.

The multi-stage container builds the Spring Boot executable JAR with Java 21, runs on a Java 21 JRE image, and uses a non-root Linux user. The application binds to Render's injected `PORT`.

## 3. Configure Render environment variables

Set these values in the Render service's **Environment** page. Use the Neon values from step 1.

| Variable | Value |
| --- | --- |
| `DB_URL` | JDBC URL using the Neon pooled hostname |
| `DB_USERNAME` | Neon database role username |
| `DB_PASSWORD` | Neon database role password |
| `FLYWAY_DB_URL` | JDBC URL using the direct (non-pooled) Neon hostname |
| `FLYWAY_DB_USERNAME` | Neon database role username |
| `FLYWAY_DB_PASSWORD` | Neon database role password |
| `JWT_SECRET` | Long, randomly generated secret |
| `DB_POOL_MAX_SIZE` | `5` |
| `DB_POOL_MIN_IDLE` | `1` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins; add the deployed frontend origin in Milestone 11 |
| `SENTINEL_AI_ENABLED` | `false` unless a reachable AI provider is configured |
| `TRACING_SAMPLING_PROBABILITY` | `0` when no Zipkin collector is provisioned |

Keep all credential values in Render's environment settings. Never commit actual passwords, JWT secrets, or webhook URLs.

## 4. Database startup and migrations

The application/Hikari datasource uses Neon's pooled endpoint. Flyway uses the separate direct endpoint through `FLYWAY_DB_URL`, `FLYWAY_DB_USERNAME`, and `FLYWAY_DB_PASSWORD`. The Flyway variables fall back to the normal `DB_*` values for local development.

Flyway applies pending migrations at startup; Hibernate uses schema validation. The deployed logs confirmed Neon connectivity, Hikari pool startup, validation of 10 migrations, and schema version 10 being up to date.

## 5. Verify the deployment

After Render reports the service as **Live**:

1. Open the service's public URL.
2. Check `/actuator/health` and confirm the response reports `UP`.
3. Inspect Render logs for successful database connectivity, Flyway completion, and Spring Boot startup.
4. Test signup/login and authenticated API requests after the frontend is deployed.
5. Confirm scheduled endpoint checks persist metrics in Neon.

The Prometheus endpoint remains protected by the application's JWT security rules. Do not expose it publicly without an appropriate authentication or network-access policy.

The current Render Free instance can spin down after inactivity. The first request after an idle period may take longer while the service starts; free compute is intended for demos and development rather than production workloads.

## 6. Local Docker build

From the repository's `backend` directory:

```powershell
docker build -t sentinel-api-backend:local .
```

Run the container only after supplying database and JWT variables through your local environment or a non-committed env file.
