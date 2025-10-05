# Fly.io Deployment Guide - Chirp Backend

## Overview

This document provides a comprehensive guide for deploying the Chirp Backend Kotlin/Quarkus application to Fly.io, including common issues encountered and their solutions.

## Deployment Summary

**Successfully deployed:** Chirp Backend to Fly.io  
**URL:** https://clean-ai-starter.fly.dev  
**Health Check:** https://clean-ai-starter.fly.dev/q/health  
**Organization:** chirpnotes  
**Region:** US West Coast (sjc)  

## Infrastructure Configuration

### Application Setup
- **App Name:** clean-ai-starter
- **Memory:** 512MB RAM
- **CPU:** 1 shared CPU
- **Docker:** JVM mode (Dockerfile.jvm)
- **Profile:** main (production profile)

### Database
- **Type:** PostgreSQL 17.2
- **Storage:** 1GB volume
- **Connection:** clean-ai-starter-db.flycast:5432
- **Database:** chirp_backend
- **User:** chirp_backend

### Object Storage
- **Provider:** Tigris (S3-compatible)
- **Bucket:** chirp-notes-bucket-main
- **Access:** Private

### Authentication
- **JWT:** RSA keys generated and configured
- **Secret Management:** Fly.io secrets for sensitive data

## Key Configuration Files

### 1. fly.toml
```toml
app = 'clean-ai-starter'
primary_region = 'sjc'

[build]
dockerfile = 'src/main/docker/Dockerfile.jvm'

[env]
QUARKUS_HTTP_HOST = "0.0.0.0"
QUARKUS_HTTP_PORT = "8080"
QUARKUS_FLYWAY_MIGRATE_AT_START = "true"
QUARKUS_PROFILE = "main"
APP_STORAGE_S3_BUCKET = "chirp-notes-bucket-main"

[http_service]
internal_port = 8080
force_https = true
auto_stop_machines = 'stop'
auto_start_machines = true
min_machines_running = 0

[[http_service.checks]]
grace_period = "10s"
interval = "30s"
method = "GET"
timeout = "5s"
path = "/q/health"

[[vm]]
memory = '512mb'
cpu_kind = 'shared'
cpus = 1
```

### 2. application-main.properties (Production Profile)
```properties
# Main profile for Fly.io deployment  
quarkus.datasource.jdbc.url=jdbc:postgresql://clean-ai-starter-db.flycast:5432/chirp_backend?sslmode=disable
quarkus.datasource.username=chirp_backend
quarkus.datasource.password=${POSTGRES_PASSWORD}

# Production logging
quarkus.log.level=INFO

# Production environment settings
app.jwt.verify.issuer=chirp-app
app.email.default-sender=chirp@info.com
```

### 3. Health Endpoint Dependency
Added to build.gradle.kts:
```kotlin
implementation("io.quarkus:quarkus-smallrye-health")
```

## Deployment Process

### Step 1: Initial Setup
```bash
# Install Fly CLI and authenticate
fly auth login

# Create app in organization
fly apps create clean-ai-starter --org chirpnotes

# Initialize fly.toml
fly launch --no-deploy --name clean-ai-starter --org chirpnotes --region sjc
```

### Step 2: Database Setup
```bash
# Create PostgreSQL cluster
fly postgres create --name clean-ai-starter-db --org chirpnotes --region sjc --vm-size shared-cpu-1x --volume-size 1

# Attach database to app
fly postgres attach clean-ai-starter-db --app clean-ai-starter
```

### Step 3: Object Storage Setup
```bash
# Create Tigris bucket
fly storage create --name chirp-notes-bucket-main --org chirpnotes
```

### Step 4: Secrets Configuration
```bash
# Generate and set JWT keys
# Set database password (use chirp_backend user password, NOT postgres password)
fly secrets set POSTGRES_PASSWORD="<chirp_backend_user_password>" --app clean-ai-starter

# Set AWS credentials for Tigris
fly secrets set AWS_ACCESS_KEY_ID="<tigris_access_key>" --app clean-ai-starter
fly secrets set AWS_SECRET_ACCESS_KEY="<tigris_secret_key>" --app clean-ai-starter
```

### Step 5: Build and Deploy
```bash
# Local build
./gradlew build

# Deploy to Fly.io
fly deploy
```

## Common Issues and Solutions

### Issue 1: Database Authentication Failures
**Error:** `FATAL: password authentication failed for user "chirp_backend"`

**Root Cause:** Using incorrect password (postgres superuser password instead of chirp_backend user password)

**Solution:**
1. When database is attached, Fly.io creates a dedicated user `chirp_backend` with a specific password
2. Use the `chirp_backend` user password, not the postgres superuser password
3. Check database attachment output for the correct password
4. Update secret: `fly secrets set POSTGRES_PASSWORD="<correct_chirp_backend_password>"`

### Issue 2: Database URL Format Issues
**Error:** PostgreSQL driver cannot parse Fly.io DATABASE_URL format

**Root Cause:** Fly.io DATABASE_URL format is not compatible with direct JDBC usage

**Solution:**
1. Create separate application profile (application-main.properties)
2. Use explicit JDBC URL format: `jdbc:postgresql://clean-ai-starter-db.flycast:5432/chirp_backend?sslmode=disable`
3. Set QUARKUS_PROFILE environment variable to activate profile

### Issue 4: Health Check Failures
**Error:** Health check on port 8080 has failed

**Root Cause:** Missing health endpoint implementation

**Solution:**
1. Add SmallRye Health extension to build.gradle.kts
2. Extension automatically provides `/q/health` endpoint
3. Includes database connectivity checks
4. No additional configuration required

### Issue 5: Test Configuration Conflicts
**Error:** Tests failing due to production configuration bleeding into test profile

**Root Cause:** Application configuration affecting test environment

**Solution:**
1. Keep test configuration (application.properties in test resources) separate
2. Use profile-specific configuration files
3. Ensure test configuration excludes production-specific components

## Verification Steps

### 1. Health Check
```bash
curl https://clean-ai-starter.fly.dev/q/health
```
Expected response:
```json
{
    "status": "UP",
    "checks": [
        {
            "name": "Database connections health check",
            "status": "UP",
            "data": {
                "<default>": "UP"
            }
        }
    ]
}
```

### 2. Application Logs
```bash
fly logs --app clean-ai-starter
```

## Cost Optimization Features

1. **Auto-stop/start machines:** Machines stop when idle, start on request
2. **Minimum machines:** 0 (full shutdown when not in use)
3. **Shared CPU:** Cost-effective for development workloads
4. **Small memory allocation:** 512MB optimized for Quarkus
5. **Small database volume:** 1GB for development use

## Future Improvements

1. **GitHub Actions:** Set up CI/CD pipeline for automated deployments
2. **Email Service:** Enable SES for email functionality
3. **Monitoring:** Add application monitoring and alerting
4. **Scaling:** Configure auto-scaling based on traffic
5. **Multiple Environments:** Set up staging environment

## Troubleshooting Commands

```bash
# View application logs
fly logs --app clean-ai-starter

# Check application status
fly status --app clean-ai-starter

# View secrets (names only)
fly secrets list --app clean-ai-starter

# Connect to database
fly postgres connect -a clean-ai-starter-db

# SSH into application machine
fly ssh console --app clean-ai-starter

# View machine details
fly machine list --app clean-ai-starter
```

## Key Learnings

1. **Database credentials:** Always use the app-specific user credentials, not superuser
2. **Profile management:** Use Quarkus profiles for environment-specific configuration
3. **Health checks:** Essential for proper deployment verification and monitoring
4. **Docker caching:** Be aware of caching issues during iterative deployment
5. **Configuration isolation:** Keep test and production configurations separate
