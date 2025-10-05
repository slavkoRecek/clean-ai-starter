# Database Connection Guide - JetBrains IDE

## Overview

The Chirp Backend PostgreSQL database is deployed on Fly.io's private network and is **not publicly accessible**. To connect from your JetBrains IDE, you need to create a secure tunnel through Fly.io's infrastructure.

## Credentials Reference

⚠️ **Important:** Database passwords and sensitive credentials are stored in `fly_deploy_notes.txt` (gitignored file).  
This file contains the actual passwords needed for database connections.

## Database Details

- **Database Name:** chirp_backend
- **Username:** chirp_backend
- **Host (Internal):** clean-ai-starter-db.flycast:5432
- **Password:** See `fly_deploy_notes.txt` (gitignored file with credentials)

## Connection Method: Fly Proxy Tunnel

### Step 1: Create Proxy Tunnel

```bash
# Create secure tunnel to database using local port 5444
fly proxy 5444:5432 -a clean-ai-starter-db
```

This command:
- Maps your local port `5444` to the database's internal port `5432`
- Must remain running while you use the database connection
- Handles authentication and secure tunneling automatically

### Step 2: Get Database Password

```bash
# Check if password secret is set
fly secrets list -a clean-ai-starter

# If you need to find the original password, check database attachment logs
# Or connect via fly console to verify
fly postgres connect -a clean-ai-starter-db
```

### Step 3: Configure JetBrains IDE

1. **Ensure proxy tunnel is running** (from Step 1)

2. **Open Database Tool Window** in your JetBrains IDE

3. **Add New Data Source:**
   - Type: PostgreSQL
   - Host: `127.0.0.1` ⚠️ **Important: Use IPv4, NOT localhost**
   - Port: `5444`
   - Database: `chirp_backend`
   - Username: `chirp_backend`
   - Password: `<see fly_deploy_notes.txt>`

4. **Test Connection** - should show successful connection

5. **Apply/OK** to save the configuration

## Connection Configuration Summary

```
Host: 127.0.0.1  ⚠️ CRITICAL: Use IPv4, NOT localhost
Port: 5444
Database: chirp_backend
Username: chirp_backend
Password: <see fly_deploy_notes.txt>
SSL: Not required (tunnel handles encryption)
```



## Verification Steps

### 1. Test Tunnel Connection
```bash
# Start proxy tunnel
fly proxy 5444:5432 -a clean-ai-starter-db

# In another terminal, test connection (if you have psql installed)
PGPASSWORD=<password_from_fly_deploy_notes> psql -h 127.0.0.1 -p 5444 -U chirp_backend -d chirp_backend
```


## Troubleshooting

### Issue: Connection Refused
**Solution:** Ensure the proxy tunnel is running
```bash
# Check if tunnel is active
ps aux | grep "fly proxy"

# Restart tunnel if needed
fly proxy 5444:5432 -a clean-ai-starter-db
```

### Issue: Authentication Failed
**Most Common Cause:** Using `localhost` instead of `127.0.0.1`

**Solutions:**
1. **Use IPv4 address:** Replace `localhost` with `127.0.0.1` in connection settings
2. **Verify credentials:** Check `fly_deploy_notes.txt` for the `chirp_backend` user password
3. **Check tunnel is running:** `ps aux | grep "fly proxy"`

```bash
# Test with correct IPv4 address
PGPASSWORD=<password_from_fly_deploy_notes> psql -h 127.0.0.1 -p 5444 -U chirp_backend -d chirp_backend

# If still failing, check database users via fly console
fly postgres connect -a clean-ai-starter-db -c "SELECT usename FROM pg_user;"
```

### Issue: Database Not Found
**Solution:** Verify database exists and you're using correct name
```bash
# List databases via fly console
fly postgres connect -a clean-ai-starter-db -c "\l"
```

### Issue: Port Already in Use
**Solution:** Use different local port or kill existing process
```bash
# Check what's using port 5444
lsof -i :5444

# Use different port
fly proxy 5445:5432 -a clean-ai-starter-db
```

## Critical Connection Notes

### IPv6 vs IPv4 Issue ⚠️
- **Always use `127.0.0.1`** instead of `localhost`
- `localhost` resolves to IPv6 `::1` which causes authentication failures
- The fly proxy tunnel works reliably with IPv4 addresses
- This is the most common cause of connection failures

### Verified Working Credentials
- **Username:** `chirp_backend`
- **Password:** See `fly_deploy_notes.txt` (gitignored)
- **Database:** `chirp_backend`
- **Host:** `127.0.0.1` (NOT localhost)

## Security Notes

- **Private Network:** Database is only accessible via Fly.io internal network
- **No Public IP:** No direct internet access to database
- **Encrypted Tunnel:** All traffic through proxy is encrypted
- **Access Control:** Only authenticated Fly.io users can create tunnels
- **Automatic Cleanup:** Tunnel closes when you stop the proxy command

## Connection Commands Reference

```bash
# Start tunnel (primary method)
fly proxy 5444:5432 -a clean-ai-starter-db

# Test connection (WORKING COMMAND)
PGPASSWORD=<password_from_fly_deploy_notes> psql -h 127.0.0.1 -p 5444 -U chirp_backend -d chirp_backend

# List all tables
PGPASSWORD=<password_from_fly_deploy_notes> psql -h 127.0.0.1 -p 5444 -U chirp_backend -d chirp_backend -c "\dt"

# Direct console access
fly postgres connect -a clean-ai-starter-db

# Check database status
fly status -a clean-ai-starter-db

# View database logs
fly logs -a clean-ai-starter-db

# List database machines
fly machine list -a clean-ai-starter-db
```

Remember: The proxy tunnel must remain active for the duration of your IDE connection. Run it in a separate terminal tab/window and keep it running while working with the database.
