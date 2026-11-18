# Production database backup and recovery

Sentinel uses Neon PostgreSQL in production. Use both Neon's point-in-time recovery and independent logical dumps. A recovery plan is only useful if a restore has been rehearsed.

## 1. Neon point-in-time recovery

1. In the Neon Console, select the production project and its root production branch.
2. Open **Backup & Restore** / **Instant Restore**.
3. Use Time Travel Assist to inspect the data at the candidate recovery time before changing production.
4. Confirm the target timestamp and branch carefully, then restore.
5. After the restore, verify the application and inspect the automatically preserved pre-restore backup branch.

Important: Neon point-in-time restore creates a restored branch at the selected timestamp/LSN. Depending on the Branch Restore flow you choose, Neon can move the production endpoint to the restored branch while preserving the pre-restore branch under a backup name. Writes after the selected recovery point are not present in the restored branch, and switching the endpoint can briefly interrupt connections. Verify the target branch, endpoint switch, and preserved original branch in the Console before confirming. Keep the configured history window long enough for the recovery objective; the available history duration depends on the plan.

## 2. Independent logical backup with pg_dump

Use a direct, unpooled Neon connection for `pg_dump` and `pg_restore`. The application's Railway connection should continue using the pooled endpoint; the backup utilities should not.

Install PostgreSQL client tools and set these variables in a secure PowerShell session:

- `NEON_DIRECT_HOST`
- `NEON_DATABASE`
- `NEON_DIRECT_USERNAME`
- `NEON_DIRECT_PASSWORD`

Create a custom-format backup:

```powershell
$env:PGPASSWORD = $env:NEON_DIRECT_PASSWORD
$backupFile = ".\sentinel-$(Get-Date -Format 'yyyyMMdd-HHmmss').dump"
pg_dump --format=custom --no-owner --host $env:NEON_DIRECT_HOST --username $env:NEON_DIRECT_USERNAME --dbname $env:NEON_DATABASE --file $backupFile
Remove-Item Env:PGPASSWORD
Write-Host "Created backup: $backupFile"
```

Store the dump in encrypted storage outside the Git repository. Do not upload database dumps to GitHub source control or public artifacts.

## 3. Test a restore before production recovery

Restore into a separate Neon recovery project or branch first. This verifies the dump and prevents overwriting production during a rehearsal.

```powershell
$env:PGPASSWORD = $env:NEON_DIRECT_PASSWORD
pg_restore --clean --if-exists --no-owner --host $env:NEON_RECOVERY_HOST --username $env:NEON_DIRECT_USERNAME --dbname $env:NEON_RECOVERY_DATABASE $backupFile
Remove-Item Env:PGPASSWORD
```

The `--clean --if-exists` flags drop conflicting objects in the target database. Use them only against the intended recovery target.

After restore:
1. Confirm the expected tables and row counts.
2. Start a backend instance pointed at the recovery database.
3. Confirm Flyway validation succeeds.
4. Run `scripts/smoke-test.ps1` against the recovery instance.
5. Record the restore duration and any missing data.

## 4. Recovery checklist

- [ ] Neon history window is configured for the required recovery window.
- [ ] A recent logical dump exists outside the repository.
- [ ] Backup retention and encryption are documented.
- [ ] Restore has been rehearsed against a non-production target.
- [ ] Railway database variables can be updated without committing credentials.
- [ ] After recovery, verify readiness, authentication, projects, services, endpoints, metrics, alerts, incidents, dependencies, and the SSE stream.

Never run a destructive restore against production until the recovery point, target branch, and expected data loss window have been reviewed.
