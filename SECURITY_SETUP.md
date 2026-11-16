# Security configuration

The application now requires a persistent JWT signing key. Do not commit the key to Git.

## Required environment variables

- `JWT_SECRET`: Base64-encoded random secret that decodes to at least 32 bytes (256 bits).
- `JWT_EXPIRATION_MS`: Optional token lifetime in milliseconds (defaults to 1 hour).
- `FRONTEND_ORIGIN`: Exact frontend origin allowed by CORS (defaults to `http://localhost:5173`).

Generate a key locally with:

```bash
openssl rand -base64 32
```

Set the resulting value as `JWT_SECRET` in your local environment and in the backend host's secret/environment-variable settings. Never put the generated value in application.yaml, source control, screenshots, or logs.

## Endpoint monitoring network policy

Sentinel rejects non-HTTP(S) URLs, URLs containing credentials, local hostnames, and hosts resolving to private, loopback, link-local, multicast, or reserved address ranges.

Application-level DNS validation is only one layer of SSRF defense. Production deployment must also enforce outbound network policy at the hosting/network layer: block private, loopback, link-local, and cloud-metadata destinations; restrict redirects; and prevent DNS rebinding/TOCTOU bypasses. Do not treat the application validator as a replacement for egress firewall rules.

## Authorization notes

Public signup always assigns the VIEWER role. User administration endpoints require ADMIN. All application endpoints require a valid bearer token. Project read/update/delete operations are scoped to the authenticated user's email.

Existing accounts and integrations should be regression-tested after this change. Existing JWTs will no longer validate because the signing key is now persistent and externally configured; users must sign in again.
