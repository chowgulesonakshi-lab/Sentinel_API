# Deploy Sentinel API backend with Neon and Railway

This deployment runs the Spring Boot backend as a Docker service on Railway and uses Neon PostgreSQL for managed storage. No credentials are stored in this repository.

## 1. Create the Neon database

1. Create a Neon project and database.
2. Open the connection details and select the pooled connection endpoint.
3. Copy the host, database name, username, and password into Railway variables.
4. Use SSL in the JDBC URL:

```text
jdbc:postgresql://<NEON_POOLER_HOST>/<DATABASE>?sslmode=require
```

Keep the pooler hostname and the username exactly as provided by Neon. Do not paste the password into source files or commit history.

## 2. Create the Railway service

1. Create a Railway project and add a service from this GitHub repository.
2. Select the branch containing the deployment changes.
3. Keep the service **Root Directory** at its default repository root. The production GitHub Actions workflow uploads only `./backend` with `--path-as-root`, so the Dockerfile becomes the root of the uploaded deployment archive.
4. Let Railway detect and build the uploaded `Dockerfile`.
5. Set the health check path to `/actuator/health/readiness` and allow up to 300 seconds for the first deployment.

The container builds the Spring Boot executable JAR with Java 21, runs on a Java 21 JRE image, and uses a non-root Linux user. Railway's injected `PORT` is used by Spring Boot. If you choose Railway's native GitHub autodeploy instead of the repository's CLI workflow, set the service Root Directory to `backend` and do not use the CLI `--path-as-root` upload command.

## 3. Configure Railway variables

Set these values in the Railway service's Variables page. Use the Neon values from step 1.

| Variable | Value |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://<NEON_POOLER_HOST>/<DATABASE>?sslmode=require` |
| `DB_USERNAME` | Neon pooled-connection username |
| `DB_PASSWORD` | Neon pooled-connection password |
| `FLYWAY_DB_URL` | Direct (unpooled) Neon JDBC URL for schema migrations |
| `FLYWAY_DB_USERNAME` | Direct-connection username |
| `FLYWAY_DB_PASSWORD` | Direct-connection password |
| `JWT_SECRET` | A long, randomly generated secret; do not reuse a sample value |
| `DB_POOL_MAX_SIZE` | `5` |
| `DB_POOL_MIN_IDLE` | `1` |
| `CORS_ALLOWED_ORIGINS` | Local frontend origins initially; replace/add the Vercel origin in Milestone 11 |
| `SENTINEL_AI_ENABLED` | `false` unless a reachable remote Ollama-compatible service is configured |
| `TRACING_SAMPLING_PROBABILITY` | `0.1` |

Optional integrations:
- Set `ZIPKIN_ENDPOINT` only when a Zipkin-compatible collector is reachable from Railway.
- Set `ALERT_WEBHOOK_ENABLED=true` and `ALERT_WEBHOOK_URL` only when you have a public webhook receiver. Keep secret webhook URLs in Railway Variables.
- Railway supplies `PORT`; do not hard-code it.

The file `backend/.env.example` is a variable-name reference only. It intentionally contains blank credential values and placeholders.

## 4. Database startup and migrations

The backend already uses Flyway migrations and Hibernate schema validation. On startup, Flyway applies pending migrations before the application begins serving traffic. Hibernate remains in `validate` mode; production startup does not use automatic schema creation.

The application/Hikari datasource uses Neon's pooled endpoint. Flyway uses the separate direct endpoint through `FLYWAY_DB_URL`, `FLYWAY_DB_USERNAME`, and `FLYWAY_DB_PASSWORD`; this keeps schema migrations and their connection-level locking away from transaction pooling. The Flyway variables fall back to the normal `DB_*` values for local development. Use the small Hikari pool above and avoid configuring a large pool on top of Neon's own pooler.

## 5. Verify the deployment

After Railway reports a successful deployment:

1. Open the service's public domain.
2. Check `/actuator/health` and `/actuator/health/readiness`.
3. Inspect Railway deploy logs for Flyway migration completion and successful Spring Boot startup.
4. Test signup/login and an authenticated dashboard request after the Vercel frontend is configured in Milestone 11.
5. Confirm a scheduled endpoint check creates a metrics row in Neon.

The Prometheus endpoint remains protected by the application's JWT security rules. Do not expose it publicly without a dedicated scrape-authentication or network-access policy.

## 6. Local Docker build

From the repository's `backend` directory:

```powershell
docker build -t sentinel-api-backend:local .
```

Run the container only after supplying the database and JWT variables through your local environment or a non-committed env file.
