# MediCare backend deployment

## Render with Aiven MySQL

Deploy this repository as a Docker web service from the `main` branch. The Dockerfile is at the repository root and `/health` is the health-check path.

Configure the following in Render's environment settings. Keep secret values in Render; do not put them in Git or frontend configuration.

| Variable | Value |
| --- | --- |
| `DB_HOST` | Aiven service host |
| `DB_PORT` | Aiven service port |
| `DB_NAME` | `defaultdb` for the configured Aiven service |
| `DB_USERNAME` | Aiven service user |
| `DB_PASSWORD` | Aiven service password (secret) |
| `DB_SSL_MODE` | `REQUIRED` |
| `JWT_SECRET` | Base64-encoded random secret of at least 32 bytes (secret) |
| `BOOTSTRAP_ADMIN_EMAIL` | Private administrator email |
| `BOOTSTRAP_ADMIN_PASSWORD` | Private password, 12 or more characters (secret) |
| `DEMO_DATA_ENABLED` | `false` |
| `CORS_ALLOWED_ORIGINS` | Exact Netlify site origin, e.g. `https://24eg107f09.netlify.app` |

The app listens on Render's `PORT` and binds to `0.0.0.0`. Render provides the port automatically. Do not set legacy `SPRING_DATASOURCE_*` variables; this application uses the `DB_*` variables above.

Set `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` before deployment. The application creates or updates this administrator on startup. When demo data is disabled, known demo passwords are invalidated during the password migration. Existing non-demo plaintext passwords are migrated to BCrypt.

The login endpoint returns `{ "token": "...", "user": { ... } }`. Include the token as `Authorization: Bearer <token>` when calling protected endpoints. Public doctor directory reads and patient registration do not require a token. Admin operations require an administrator account; patient and doctor records are scoped to their owner.

## Local development

Set `JWT_SECRET` to a private Base64-encoded random value of at least 32 bytes. Set `DEMO_DATA_ENABLED=true` only for a local or test database to create the sample accounts. Start the backend with Maven:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "arjuna"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-local-password"
$env:DB_SSL_MODE = "DISABLED"
$env:JWT_SECRET = "your-private-base64-secret"
$env:DEMO_DATA_ENABLED = "true"
mvn spring-boot:run
```
