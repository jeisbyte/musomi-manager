# DEVOPS.md — DevOps Specialist Document

---

## 1. Who This Is For

You are the **DevOps Engineer** on Musomi Manager.

You set up the school server, deploy the system, keep it running, and back it up. You work alongside the Database role (same person in a small team) — this document covers the **ops** side. Database has its own document.

You work alongside:

- **Backend Lead** — packages the JAR you deploy
- **JavaFX Developer** — packages the .msi you distribute
- **Web Developer** — templates are served by the backend you run
- **Product + QA** — coordinates the pilot school setup with you

You own the servers, deployment, backups, and monitoring. You don't edit backend code, JavaFX code, or web templates.

---

## 2. What You Build in v1

- School server setup (Ubuntu 22.04 LTS)
- PostgreSQL installation and configuration
- Java 21 runtime installation
- Spring Boot app as a systemd service
- Cloudflare Tunnel for student web access
- GitHub Actions CI/CD pipeline
- Daily automated backup (pg_dump + cron)
- Backup to external drive
- Monitoring (uptime, disk, logs)
- Deployment scripts
- Runbooks (procedures for common tasks)
- Staging environment (optional in v1)
- School onboarding checklist

**Not in v1:** Docker, Kubernetes, load balancers, multi-server, Redis, message queues, CDN. Those are v2+ or never.

---

## 3. Server Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ SCHOOL SERVER (Ubuntu 22.04)                                │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Spring Boot App (JAR)                               │   │
│  │ - systemd service: musomi.service                   │   │
│  │ - Port 8080                                         │   │
│  │ - Heap: 2 GB                                        │   │
│  │ - User: musomi                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ PostgreSQL 15/16                                    │   │
│  │ - Port 5432                                         │   │
│  │ - Data dir: /var/lib/postgresql/16/main             │   │
│  │ - User: musomi                                      │   │
│  │ - Database: musomi_manager                          │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Cloudflare Tunnel (cloudflared)                     │   │
│  │ - Outbound to Cloudflare edge                       │   │
│  │ - Exposes localhost:8080 as https://school.app.com  │   │
│  │ - No port forwarding, no public IP needed           │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Backup Script (cron)                                │   │
│  │ - Daily pg_dump at 2 AM                             │   │
│  │ - Copy to /mnt/backup (external drive)              │   │
│  │ - Retention: 30 days                                │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Monitoring (cron + logs)                            │   │
│  │ - Uptime check every 5 minutes                      │   │
│  │ - Disk space check every hour                       │   │
│  │ - Log rotation via logrotate                        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**One server. No containers. No orchestration. Simple, reliable, easy to support.**

---

## 4. Folder Structure (on the server)

```
/opt/musomi/
├── app/
│   ├── musomi-manager.jar        ← the Spring Boot app
│   └── application-prod.yml      ← production config
│
├── logs/
│   ├── app.log
│   └── app-error.log
│
├── backups/                       ← local backup staging
│   ├── musomi_20260924.sql.gz
│   └── ...
│
├── scripts/
│   ├── backup.sh
│   ├── restore.sh
│   ├── deploy.sh
│   └── health-check.sh
│
└── config/
    └── cloudflared.yml

/etc/systemd/system/
├── musomi.service                 ← Spring Boot service
└── cloudflared.service            ← Tunnel service

/mnt/backup/                       ← external drive mount
└── musomi/
    ├── musomi_20260924.sql.gz
    └── ...
```

---

## 5. Server Setup (Fresh Ubuntu 22.04)

### Step 1 — Base system

```bash
# Update
sudo apt update && sudo apt upgrade -y

# Install essentials
sudo apt install -y curl wget git ufw fail2ban htop

# Set timezone
sudo timedatectl set-timezone Africa/Kampala

# Create app user
sudo useradd -m -s /bin/bash musomi
sudo usermod -aG sudo musomi
```

### Step 2 — Firewall

```bash
sudo ufw allow OpenSSH
sudo ufw allow 8080/tcp   # only from LAN, block from internet
sudo ufw enable
sudo ufw status
```

**Important:** Port 8080 is only for LAN access (teachers' desktop apps). The Cloudflare Tunnel handles student web access, so no inbound ports for that.

### Step 3 — Java 21

```bash
sudo apt install -y openjdk-21-jre-headless
java -version
# Expect: openjdk version "21.x.x"
```

### Step 4 — PostgreSQL 16

```bash
sudo apt install -y postgresql-16 postgresql-contrib-16

# Create database and user
sudo -u postgres psql <<EOF
CREATE USER musomi WITH PASSWORD 'STRONG_PASSWORD_HERE';
CREATE DATABASE musomi_manager OWNER musomi;
GRANT ALL PRIVILEGES ON DATABASE musomi_manager TO musomi;
EOF

# Verify
sudo -u postgres psql -c "\l"
```

### Step 5 — App user

```bash
sudo mkdir -p /opt/musomi/{app,logs,backups,scripts,config}
sudo chown -R musomi:musomi /opt/musomi
```

### Step 6 — Mount external backup drive

```bash
# Find the drive
lsblk

# Format (only once)
sudo mkfs.ext4 /dev/sdb1

# Mount
sudo mkdir -p /mnt/backup
sudo mount /dev/sdb1 /mnt/backup

# Persist in /etc/fstab
echo "/dev/sdb1 /mnt/backup ext4 defaults 0 2" | sudo tee -a /etc/fstab
```

---

## 6. Spring Boot Service (systemd)

Create `/etc/systemd/system/musomi.service`:

```ini
[Unit]
Description=Musomi Manager Backend
After=network.target postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=musomi
Group=musomi
WorkingDirectory=/opt/musomi/app
Environment="SPRING_PROFILES_ACTIVE=prod"
EnvironmentFile=/opt/musomi/config/app.env
ExecStart=/usr/bin/java \
  -Xms512m -Xmx2048m \
  -XX:+UseG1GC \
  -jar /opt/musomi/app/musomi-manager.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/musomi/logs/app.log
StandardError=append:/opt/musomi/logs/app-error.log

[Install]
WantedBy=multi-user.target
```

Create `/opt/musomi/config/app.env`:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/musomi_manager
DATABASE_USER=musomi
DATABASE_PASSWORD=STRONG_PASSWORD_HERE
JWT_SECRET=<generate-a-256-bit-secret>
SERVER_PORT=8080
```

Secure it:

```bash
sudo chmod 600 /opt/musomi/config/app.env
sudo chown musomi:musomi /opt/musomi/config/app.env
```

Enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable musomi.service
sudo systemctl start musomi.service
sudo systemctl status musomi.service
```

---

## 7. Cloudflare Tunnel

### Install

```bash
curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 \
  -o /usr/local/bin/cloudflared
sudo chmod +x /usr/local/bin/cloudflared
```

### Authenticate

```bash
cloudflared tunnel login
```

This opens a browser. Log in with the Cloudflare account. Choose the domain.

### Create the tunnel

```bash
cloudflared tunnel create musomi-school1
```

Saves credentials to `~/.cloudflared/<tunnel-id>.json`.

### Configure

Create `/etc/cloudflared/config.yml`:

```yaml
tunnel: <tunnel-id>
credentials-file: /etc/cloudflared/<tunnel-id>.json

ingress:
  - hostname: school1.musomi.app
    service: http://localhost:8080
  - service: http_status:404
```

### Route DNS

```bash
cloudflared tunnel route dns musomi-school1 school1.musomi.app
```

### Install as service

```bash
sudo cloudflared service install
sudo systemctl enable cloudflared
sudo systemctl start cloudflared
sudo systemctl status cloudflared
```

Now `https://school1.musomi.app` routes to the school server over an outbound-only tunnel. No firewall changes. No public IP needed.

---

## 8. CI/CD with GitHub Actions

`.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: musomi_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Build backend
        run: cd backend && mvn clean verify -B
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/musomi_test
          SPRING_DATASOURCE_USERNAME: test
          SPRING_DATASOURCE_PASSWORD: test
          JWT_SECRET: test-secret-for-ci-only-do-not-use-in-prod

      - name: Build desktop
        run: cd desktop && mvn clean verify -B

      - name: Upload JAR
        uses: actions/upload-artifact@v4
        with:
          name: backend-jar
          path: backend/target/*.jar
```

Rules:

- Every push to `main` runs CI
- Every PR runs CI
- Red build blocks merge
- Green build is a merge requirement

---

## 9. Backup Script

`/opt/musomi/scripts/backup.sh`:

```bash
#!/bin/bash
set -e

# Config
DB_NAME="musomi_manager"
DB_USER="musomi"
BACKUP_DIR="/opt/musomi/backups"
EXTERNAL_DIR="/mnt/backup/musomi"
RETENTION_DAYS=30
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup
echo "[$(date)] Starting backup..."
pg_dump -U "$DB_USER" -h localhost "$DB_NAME" | gzip > "$BACKUP_DIR/musomi_${DATE}.sql.gz"

# Copy to external drive
cp "$BACKUP_DIR/musomi_${DATE}.sql.gz" "$EXTERNAL_DIR/"

# Clean up old local backups
find "$BACKUP_DIR" -name "musomi_*.sql.gz" -mtime +$RETENTION_DAYS -delete

# Clean up old external backups
find "$EXTERNAL_DIR" -name "musomi_*.sql.gz" -mtime +$RETENTION_DAYS -delete

# Log
echo "[$(date)] Backup complete: musomi_${DATE}.sql.gz"
ls -lh "$EXTERNAL_DIR/musomi_${DATE}.sql.gz"
```

Make it executable:

```bash
sudo chmod +x /opt/musomi/scripts/backup.sh
sudo chown musomi:musomi /opt/musomi/scripts/backup.sh
```

Cron (runs as `musomi` user):

```bash
sudo crontab -u musomi -e
```

Add:

```
0 2 * * * /opt/musomi/scripts/backup.sh >> /opt/musomi/logs/backup.log 2>&1
```

Runs daily at 2 AM.

---

## 10. Restore Script

`/opt/musomi/scripts/restore.sh`:

```bash
#!/bin/bash
set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <backup-file.sql.gz>"
  exit 1
fi

BACKUP_FILE="$1"
DB_NAME="musomi_manager"
DB_USER="musomi"

echo "Restoring from: $BACKUP_FILE"

# Stop the app
sudo systemctl stop musomi.service

# Drop and recreate database
sudo -u postgres psql <<EOF
DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME OWNER $DB_USER;
EOF

# Restore
gunzip -c "$BACKUP_FILE" | psql -U "$DB_USER" -h localhost "$DB_NAME"

# Restart the app
sudo systemctl start musomi.service

echo "Restore complete."
```

**Test this monthly** — restore to a test database and verify row counts.

---

## 11. Deployment Script

`/opt/musomi/scripts/deploy.sh`:

```bash
#!/bin/bash
set -e

JAR_FILE="$1"

if [ ! -f "$JAR_FILE" ]; then
  echo "JAR not found: $JAR_FILE"
  exit 1
fi

echo "[$(date)] Deploying $JAR_FILE"

# Backup current JAR
cp /opt/musomi/app/musomi-manager.jar /opt/musomi/app/musomi-manager.jar.bak

# Stop the app
sudo systemctl stop musomi.service

# Replace JAR
cp "$JAR_FILE" /opt/musomi/app/musomi-manager.jar
chown musomi:musomi /opt/musomi/app/musomi-manager.jar

# Start the app
sudo systemctl start musomi.service

# Wait for startup
sleep 10

# Health check
if curl -sf http://localhost:8080/actuator/health > /dev/null; then
  echo "Deployment successful"
  rm /opt/musomi/app/musomi-manager.jar.bak
else
  echo "Health check failed, rolling back"
  sudo systemctl stop musomi.service
  mv /opt/musomi/app/musomi-manager.jar.bak /opt/musomi/app/musomi-manager.jar
  sudo systemctl start musomi.service
  exit 1
fi
```

Deploy from local machine:

```bash
scp backend/target/musomi-manager-1.0.0.jar musomi@school-server:/tmp/
ssh musomi@school-server "sudo /opt/musomi/scripts/deploy.sh /tmp/musomi-manager-1.0.0.jar"
```

---

## 12. Health Check Script

`/opt/musomi/scripts/health-check.sh`:

```bash
#!/bin/bash

# Backend health
if curl -sf http://localhost:8080/actuator/health > /dev/null; then
  echo "Backend: OK"
else
  echo "Backend: DOWN"
  # Log and optionally alert
fi

# Disk space
DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
if [ "$DISK_USAGE" -gt 85 ]; then
  echo "Disk: WARNING ($DISK_USAGE%)"
else
  echo "Disk: OK ($DISK_USAGE%)"
fi

# PostgreSQL
if pg_isready -h localhost -U musomi > /dev/null 2>&1; then
  echo "PostgreSQL: OK"
else
  echo "PostgreSQL: DOWN"
fi

# Tunnel
if systemctl is-active --quiet cloudflared; then
  echo "Tunnel: OK"
else
  echo "Tunnel: DOWN"
fi
```

Run every 5 minutes:

```
*/5 * * * * /opt/musomi/scripts/health-check.sh >> /opt/musomi/logs/health.log 2>&1
```

---

## 13. Monitoring and Logs

### Application logs

Written to `/opt/musomi/logs/app.log` by systemd.

Rotate with logrotate — `/etc/logrotate.d/musomi`:

```
/opt/musomi/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0640 musomi musomi
}
```

### System logs

```bash
sudo journalctl -u musomi.service -f      # app logs
sudo journalctl -u cloudflared -f         # tunnel logs
sudo tail -f /opt/musomi/logs/backup.log  # backup logs
sudo tail -f /opt/musomi/logs/health.log  # health logs
```

### Uptime monitoring (external, optional)

Use a free external service like:

- UptimeRobot — pings `https://school1.musomi.app/actuator/health` every 5 min
- Alerts via email/SMS on downtime

**This catches problems you don't see from inside the server.**

---

## 14. Common Prompts for DevOps AI

**Write a systemd service:**

```
Context: MASTER.md + DEVOPS.md

Write a systemd service file for [app name].
User: musomi, Port: 8080, Heap: 2 GB.
Follow the systemd pattern in DEVOPS.md.
```

**Write a backup script:**

```
Context: DEVOPS.md

Write a bash script that:
- Runs pg_dump on musomi_manager
- Compresses and stores locally
- Copies to external drive
- Deletes files older than 30 days
- Logs with timestamps

Follow the patterns in DEVOPS.md.
```

**Write a GitHub Actions workflow:**

```
Context: MASTER.md + DEVOPS.md

Write a CI workflow for [backend/desktop].
Runs on push to main and PRs.
Uses PostgreSQL service container.
Follow the pattern in DEVOPS.md.
```

**Configure Cloudflare Tunnel:**

```
Context: DEVOPS.md

Walk through setting up Cloudflare Tunnel for [school name].
Domain: school1.musomi.app
Local service: http://localhost:8080
Return the commands and config.
```

**Debug a deployment issue:**

```
Context: DEVOPS.md

Error: [paste]
Logs: [paste]

Explain cause, fix, prevention.
```

**Write a runbook:**

```
Context: DEVOPS.md

Write a runbook for "[procedure name]" — e.g., "Restore from backup",
"Deploy a new version", "Add a new school".
Include: prerequisites, steps, verification, rollback.
Follow the format in DEVOPS.md.
```

---

## 15. Definition of Done (DevOps)

A DevOps task is done when:

- [ ] Works on a fresh Ubuntu 22.04 server
- [ ] Documented in `docs/07-operations/`
- [ ] Tested end-to-end
- [ ] Backup verified
- [ ] Health check passes
- [ ] Logs are written and rotated
- [ ] Rollback procedure documented
- [ ] Runbook written
- [ ] Reviewed by another team member

---

## 16. School Onboarding Checklist

Before the pilot school goes live:

- [ ] Server hardware received and set up
- [ ] Ubuntu 22.04 installed
- [ ] Static IP or DHCP reservation configured
- [ ] Server joined to school LAN
- [ ] PostgreSQL installed and secured
- [ ] Java 21 installed
- [ ] Spring Boot JAR deployed
- [ ] systemd service running
- [ ] Cloudflare Tunnel configured
- [ ] `https://schoolname.musomi.app` accessible from internet
- [ ] External backup drive mounted
- [ ] Backup cron running
- [ ] First backup verified
- [ ] Health check script running
- [ ] Admin account created
- [ ] School settings configured (name, logo, grading scale)
- [ ] Students imported
- [ ] Teachers trained
- [ ] Students trained
- [ ] Support contact shared
- [ ] Go-live date confirmed

**Keep this checklist. Use it for every new school.**

---

## 17. What NOT to Do

- Don't use Docker in v1 — adds complexity, no benefit for one server
- Don't expose PostgreSQL to the internet — LAN only
- Don't open port 8080 to the internet — use Cloudflare Tunnel
- Don't run as root — use the `musomi` user
- Don't store passwords in plain text anywhere — use environment files with `chmod 600`
- Don't skip backups — test them monthly
- Don't skip the health check — silent failures are the worst kind
- Don't deploy on a Friday — deploy Monday–Thursday, and only when you can watch it
- Don't edit the JAR on the server — always deploy from source
- Don't hardcode the server URL in the desktop app — make it configurable
- Don't skip the restore test — a backup you can't restore is not a backup
- Don't forget to rotate logs — logs fill disks

---

## 18. Reference Documents

- `MASTER.md` — project context (paste first)
- `docs/specialists/DATABASE.md` — schema and migrations
- `docs/specialists/BACKEND.md` — how the backend runs
- `docs/07-operations/` — runbooks and procedures
- `docs/02-requirements/v1-requirements.md` — non-functional targets

---

## The One-Sentence Summary

**You are the DevOps Engineer on Musomi Manager. You set up the school server (Ubuntu + PostgreSQL + Java + Spring Boot + Cloudflare Tunnel), keep it running, back it up daily, and deploy new versions. One server, one database, no containers, no orchestration. Paste MASTER.md and DEVOPS.md into every AI session.**