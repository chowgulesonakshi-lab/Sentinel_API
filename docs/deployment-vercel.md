# Deploy Sentinel API frontend with Vercel

The React/Vite frontend is deployed separately from the Spring Boot backend. Vercel serves the static frontend; Railway continues to host the API and Neon remains the database.

## 1. Create the Vercel project

1. Import the `chowgulesonakshi-lab/Sentinel_API` GitHub repository into Vercel.
2. Select the branch that contains the deployment changes.
3. Set **Root Directory** to `frontend`.
4. Select the Vite framework preset.
5. Use `npm run build` as the build command and `dist` as the output directory.
6. Keep the install command as `npm install` (or Vercel's detected npm install command).

The `frontend/vercel.json` rewrite sends client-side routes back to `index.html`, so refreshing routes such as `/dashboard` or `/dependencies` does not return a Vercel 404.

## 2. Configure the frontend API URL

Add this Vercel environment variable for Production (and Preview if you want preview deployments to use the same backend):

| Variable | Value |
| --- | --- |
| `VITE_API_BASE_URL` | The Railway backend's public HTTPS origin, for example `https://your-service.up.railway.app` |

Do not add a trailing slash or an `/api` suffix. The backend routes are served from the origin root. The frontend uses this one value for REST requests and the authenticated SSE stream.

The file `frontend/.env.example` contains only a placeholder. Vite variables are public in the built browser bundle; never put database passwords, JWT signing secrets, or private webhook credentials in a `VITE_*` variable.

## 3. Update Railway CORS

After Vercel assigns a production domain:

1. Open the Railway backend service's Variables page.
2. Set `CORS_ALLOWED_ORIGINS` to the exact Vercel origin, including `https://` and without a trailing slash.
3. If local development is still needed, include the local origin as a comma-separated value, for example:

```text
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app,http://localhost:5173
```

4. Redeploy the Railway service after changing the variable.

The backend uses an explicit origin allow-list with credentials enabled. Do not use `*` for credentialed CORS. Add a custom Vercel domain or a specific preview origin only when it is needed.

## 4. Verify the live application

1. Open the Vercel URL and confirm the login page loads over HTTPS.
2. Sign in with an existing account; the frontend stores the JWT and the shared Axios client attaches it to protected API calls.
3. Open Dashboard, Projects, Services, Endpoints, Metrics, Alerts, and Dependencies.
4. Refresh a nested route directly (for example, `/dashboard`) to verify the SPA rewrite.
5. Trigger an endpoint check and confirm the Dashboard/Metrics views update through the SSE stream.
6. Confirm browser developer tools show successful API responses and no CORS errors.

If API calls fail, verify the Vercel `VITE_API_BASE_URL`, Railway's `CORS_ALLOWED_ORIGINS`, and that the Railway backend health/readiness endpoints are healthy.
