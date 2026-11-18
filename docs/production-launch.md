# Production launch, CI/CD, and verification

## 1. Continuous integration

The repository runs two GitHub Actions workflows on branch pushes and pull requests to `main`:

- **Backend CI**: Java 21 Gradle tests, then a Docker image build.
- **Frontend CI**: install frontend dependencies and run the Vite production build.

Protect `main` with required status checks for Backend CI and Frontend CI. Merge only after both checks pass and the pull request has been reviewed. Configure the GitHub `production` environment with required reviewers if you want an approval gate before either deployment job can run.

## 2. Optional production deployment workflow

`.github/workflows/deploy-production.yml` deploys only from `main` (or a manual dispatch on `main`). It deploys the backend to Railway and the frontend to Vercel. Each deployment step is skipped until its required credentials and identifiers are configured.

Add these GitHub repository secrets:

| Secret | Purpose |
| --- | --- |
| `RAILWAY_TOKEN` | Railway project token scoped to the production environment |
| `VERCEL_TOKEN` | Vercel deployment token |

Add these GitHub repository variables:

| Variable | Purpose |
| --- | --- |
| `RAILWAY_PROJECT_ID` | Railway project ID |
| `RAILWAY_SERVICE_ID` | Railway backend service ID |
| `RAILWAY_ENVIRONMENT` | Railway environment name; defaults to `production` |
| `VERCEL_ORG_ID` | Vercel team/user organization ID |
| `VERCEL_PROJECT_ID` | Vercel frontend project ID |

The Railway deployment command targets the configured project, service, and environment explicitly, and uploads only the backend directory as the deployment root. The Vercel job pulls the Production project environment, builds the frontend, and deploys the prebuilt output.

Set `VITE_API_BASE_URL` in Vercel's project environment settings to the Railway public HTTPS origin. Set `CORS_ALLOWED_ORIGINS` in Railway to the exact Vercel production origin. These values must be configured in their respective platforms; do not add them as hard-coded source values.

If you prefer Vercel's built-in Git integration, you can leave the Vercel GitHub Actions secrets unset. The frontend CI workflow still validates every change.

## 3. Production smoke checks

The script `scripts/smoke-test.ps1` checks readiness, authenticated dashboard/project/service/endpoint/metrics/alert/incident routes, Prometheus access, dependency graph access when a project exists, and the authenticated SSE response content type.

From PowerShell:

```powershell
$env:SENTINEL_API_BASE_URL = "https://<RAILWAY-SERVICE-DOMAIN>"
$env:SENTINEL_SMOKE_TEST_TOKEN = "<JWT-FROM-LOGIN>"
.\scripts\smoke-test.ps1
```

To also request AI analysis for the first existing incident:

```powershell
.\scripts\smoke-test.ps1 -IncludeAiCheck
```

The JWT is supplied through the environment and is not stored in the script or repository. Use a test account and non-sensitive production data for smoke tests.

## 4. End-to-end launch checklist

- [ ] Signup and login return a valid JWT.
- [ ] Protected routes reject missing or invalid tokens.
- [ ] Project, service, and endpoint create/read/update/delete operations work for the owning user.
- [ ] A manual endpoint check completes and records a metric.
- [ ] Scheduled checks continue after the laptop is offline.
- [ ] Dashboard and Metrics pages refresh from the SSE stream.
- [ ] Alert creation, resolution, and optional webhook delivery work.
- [ ] Incident lifecycle transitions and audit timeline work.
- [ ] AI incident analysis works when a reachable Ollama-compatible provider is configured; deterministic fallback behavior is also verified.
- [ ] Service dependency graph and impact analysis return only the signed-in user's project data.
- [ ] Readiness health, Prometheus metrics, correlation IDs, and trace/span IDs are observable.
- [ ] A Neon backup has been restored successfully into a separate recovery target.
- [ ] Production CORS contains only the intended frontend origins.

A green CI build proves compilation, unit tests, and image/frontend build steps. It does not prove the live Railway, Neon, Vercel, browser-authentication, Ollama, webhook, or backup integrations. Complete the live checks above after entering the real platform configuration.
