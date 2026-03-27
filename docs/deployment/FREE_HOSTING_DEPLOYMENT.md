# 🌐 Free Hosting Deployment Guide - PI System

> **Purpose**: Deploy PI System on free hosting platforms  
> **Cost**: $0/month (with limitations)  
> **Last Updated**: February 1, 2026  
> **Skill Level**: Beginner to Intermediate

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture Options](#architecture-options)
3. [Backend Hosting](#backend-hosting)
4. [Database Hosting](#database-hosting)
5. [Redis Hosting](#redis-hosting)
6. [Frontend Hosting](#frontend-hosting)
7. [Complete Deployment Steps](#complete-deployment-steps)
8. [Environment Configuration](#environment-configuration)
9. [Monitoring & Maintenance](#monitoring--maintenance)
10. [Limitations & Workarounds](#limitations--workarounds)
11. [Cost Upgrade Path](#cost-upgrade-path)

---

## 🎯 Overview

### What We'll Deploy
- **Backend**: Spring Boot application (Java 17)
- **Database**: MySQL 8
- **Cache**: Redis
- **Frontend**: React app (Vite)

### Free Hosting Options Available

| Service | Backend | Database | Frontend | Redis | Free Tier |
|---------|---------|----------|----------|-------|-----------|
| **Render** | ✅ | ✅ | ✅ | ❌ | 750 hrs/month |
| **Railway** | ✅ | ✅ | ❌ | ✅ | $5 credit/month |
| **Fly.io** | ✅ | ✅ | ✅ | ✅ | 3 VMs free |
| **Vercel** | ❌ | ❌ | ✅ | ❌ | Unlimited |
| **Netlify** | ❌ | ❌ | ✅ | ❌ | 100GB/month |
| **PlanetScale** | ❌ | ✅ MySQL | ❌ | ❌ | 5GB storage |
| **Supabase** | ❌ | ✅ PostgreSQL | ❌ | ❌ | 500MB |
| **Redis Cloud** | ❌ | ❌ | ❌ | ✅ | 30MB |

### Recommended Free Setup (Best Overall)

**Option 1: Render + Vercel (Easiest)**
- Backend + DB: Render
- Frontend: Vercel
- Redis: Redis Cloud (free 30MB)
- **Pros**: Simple, reliable, good free tier
- **Cons**: Cold starts on backend, small Redis

**Option 2: Fly.io (All-in-One)**
- Backend: Fly.io
- Database: Fly.io Postgres
- Frontend: Fly.io
- Redis: Fly.io Redis
- **Pros**: Everything in one place, fast
- **Cons**: More complex setup, need Docker knowledge

**Option 3: Railway (Developer Friendly)**
- Backend + DB + Redis: Railway
- Frontend: Vercel
- **Pros**: Great DX, easy to use
- **Cons**: Limited free credits ($5/month)

---

## 🚀 Architecture Options

### Architecture 1: Distributed (Recommended for Free)
```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Vercel    │────▶│  Render.com  │────▶│ PlanetScale │
│  (Frontend) │     │   (Backend)  │     │   (MySQL)   │
└─────────────┘     └──────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ Redis Cloud │
                    │   (Cache)   │
                    └─────────────┘
```

**Benefits**:
- Each service optimized for its purpose
- Free tiers don't overlap
- Easy to scale individual components

### Architecture 2: Monolithic (Simpler)
```
┌─────────────┐     ┌──────────────────────────┐
│   Vercel    │────▶│       Fly.io             │
│  (Frontend) │     │  ┌────────┬─────────┐    │
└─────────────┘     │  │ Backend│ MySQL   │    │
                    │  │ (Java) │ Database│    │
                    │  └────────┴─────────┘    │
                    │       │                   │
                    │       ▼                   │
                    │  ┌─────────┐             │
                    │  │  Redis  │             │
                    │  └─────────┘             │
                    └──────────────────────────┘
```

**Benefits**:
- Everything in one place
- Easier management
- Lower latency

---

## 🔧 Backend Hosting

### Option 1: Render.com (Recommended for Beginners)

#### Prerequisites
- GitHub account
- Project pushed to GitHub
- Dockerfile in project root

#### Step 1: Prepare Dockerfile
Create `Dockerfile` in project root:
```dockerfile
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Step 2: Create render.yaml
Create `render.yaml` in project root:
```yaml
services:
  - type: web
    name: pi-system-backend
    env: java
    buildCommand: ./gradlew clean build -x test
    startCommand: java -jar build/libs/*.jar
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: PORT
        value: 8082
      - key: SPRING_DATASOURCE_URL
        fromDatabase:
          name: pi-system-db
          property: connectionString
      - key: SPRING_DATASOURCE_USERNAME
        fromDatabase:
          name: pi-system-db
          property: user
      - key: SPRING_DATASOURCE_PASSWORD
        fromDatabase:
          name: pi-system-db
          property: password
      - key: SPRING_REDIS_HOST
        sync: false
      - key: SPRING_REDIS_PORT
        value: 6379
      - key: JWT_SECRET
        generateValue: true
        
databases:
  - name: pi-system-db
    databaseName: pisystem
    user: pisystem
```

#### Step 3: Deploy on Render
1. Go to https://render.com
2. Sign up with GitHub
3. Click "New +" → "Blueprint"
4. Connect your GitHub repository
5. Render will detect `render.yaml` and create services
6. Click "Apply" to deploy

**Free Tier Limits**:
- 750 hours/month (enough for 1 service)
- Spins down after 15 mins of inactivity
- Cold starts take 30-60 seconds
- 512 MB RAM

---

### Option 2: Railway.app

#### Step 1: Install Railway CLI
```bash
npm install -g @railway/cli
railway login
```

#### Step 2: Initialize Project
```bash
cd /path/to/pi-system
railway init
```

#### Step 3: Create Services
```bash
# Create MySQL
railway add --database mysql

# Create Redis
railway add --database redis

# Deploy backend
railway up
```

#### Step 4: Configure Environment
```bash
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set SPRING_DATASOURCE_URL=${{MYSQL_URL}}
railway variables set SPRING_REDIS_HOST=${{REDIS_HOST}}
```

**Free Tier Limits**:
- $5 credit/month
- ~500 hours of execution
- No sleep policy (always on)

---

### Option 3: Fly.io

#### Step 1: Install Fly CLI
```bash
# macOS
brew install flyctl

# Linux
curl -L https://fly.io/install.sh | sh

# Login
flyctl auth login
```

#### Step 2: Create fly.toml
Create `fly.toml` in project root:
```toml
app = "pi-system-backend"

[build]
  image = "openjdk:17-jdk-slim"

[env]
  PORT = "8082"
  SPRING_PROFILES_ACTIVE = "prod"

[[services]]
  http_checks = []
  internal_port = 8082
  protocol = "tcp"

  [[services.ports]]
    force_https = true
    handlers = ["http"]
    port = 80

  [[services.ports]]
    handlers = ["tls", "http"]
    port = 443
```

#### Step 3: Deploy
```bash
flyctl launch --no-deploy
flyctl deploy
```

#### Step 4: Create Database
```bash
flyctl postgres create --name pi-system-db
flyctl postgres attach pi-system-db
```

**Free Tier Limits**:
- 3 shared-cpu VMs with 256MB RAM each
- 3GB storage
- 160GB outbound data transfer

---

## 🗄️ Database Hosting

### Option 1: PlanetScale (MySQL - Recommended)

#### Step 1: Create Account
1. Go to https://planetscale.com
2. Sign up with GitHub
3. Create new database "pi-system"

#### Step 2: Get Connection String
```bash
# PlanetScale gives you a connection string like:
mysql://user:password@host.psdb.cloud/pisystem?sslaccept=strict
```

#### Step 3: Configure Application
Update `application-prod.yml`:
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    baseline-on-migrate: true
```

#### Step 4: Run Migrations
```bash
# Connect to PlanetScale
pscale connect pi-system main --port 3309

# Run migrations
./gradlew flywayMigrate
```

**Free Tier Limits**:
- 5GB storage
- 1 billion row reads/month
- 10 million row writes/month
- 1 production branch + 1 development branch

---

### Option 2: Supabase (PostgreSQL)

⚠️ **Note**: PI System uses MySQL. To use Supabase, you need to:
1. Convert all migrations from MySQL to PostgreSQL syntax
2. Change Hibernate dialect
3. Update data types (BIGINT AUTO_INCREMENT → BIGSERIAL)

**If you want PostgreSQL**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://db.xxx.supabase.co:5432/postgres
    username: postgres
    password: your-password
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

**Free Tier**:
- 500MB database
- Unlimited API requests
- 50MB file storage

---

### Option 3: Render Database

Render provides free PostgreSQL (not MySQL):
- 256MB RAM
- 1GB storage
- Expires after 90 days (need to renew)

**Setup via render.yaml** (already included above)

---

## 🔴 Redis Hosting

### Option 1: Redis Cloud (Recommended)

#### Step 1: Create Account
1. Go to https://redis.com/try-free/
2. Sign up for free account
3. Create new database

#### Step 2: Get Connection Details
```
Host: redis-xxxxx.c1.us-east-1-2.ec2.cloud.redislabs.com
Port: 12345
Password: your-password
```

#### Step 3: Configure Application
```yaml
spring:
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}
    ssl: true
```

**Free Tier**:
- 30MB storage
- 30 connections
- Shared instance

---

### Option 2: Upstash Redis

1. Go to https://upstash.com
2. Create Redis database
3. Get REST API URL

```yaml
# Use Upstash REST API
redis:
  url: ${UPSTASH_REDIS_REST_URL}
  token: ${UPSTASH_REDIS_REST_TOKEN}
```

**Free Tier**:
- 10,000 commands/day
- Max 256MB
- Global replication

---

### Option 3: Railway Redis

Included in Railway deployment:
```bash
railway add --database redis
```

Uses your $5/month credit.

---

## 🎨 Frontend Hosting

### Option 1: Vercel (Recommended)

#### Step 1: Prepare Frontend Build
```bash
cd frontend

# Create .env.production
echo "VITE_API_URL=https://your-backend.render.com/api/v1" > .env.production

# Build
npm run build
```

#### Step 2: Deploy via GitHub
1. Go to https://vercel.com
2. Sign up with GitHub
3. Click "New Project"
4. Import your repository
5. Configure:
   - Framework Preset: Vite
   - Root Directory: frontend
   - Build Command: `npm run build`
   - Output Directory: `dist`
6. Add environment variable: `VITE_API_URL`
7. Click "Deploy"

**Free Tier**:
- Unlimited sites
- 100GB bandwidth/month
- Automatic HTTPS
- Global CDN

---

### Option 2: Netlify

#### Step 1: Create netlify.toml
```toml
[build]
  base = "frontend"
  command = "npm run build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200

[build.environment]
  VITE_API_URL = "https://your-backend.render.com/api/v1"
```

#### Step 2: Deploy
```bash
# Install Netlify CLI
npm install -g netlify-cli

# Login
netlify login

# Deploy
cd frontend
netlify deploy --prod
```

**Free Tier**:
- 100GB bandwidth/month
- 300 build minutes/month
- Automatic HTTPS

---

### Option 3: GitHub Pages

⚠️ **Limitation**: Static only, good for demo but API calls need CORS configured

```bash
cd frontend
npm run build
npx gh-pages -d dist
```

---

## 📦 Complete Deployment Steps

### Deployment Scenario: Render + Vercel (Full Walkthrough)

#### Phase 1: Database Setup (15 minutes)

**Step 1: PlanetScale Database**
```bash
# Install PlanetScale CLI
brew install planetscale/tap/pscale

# Login
pscale auth login

# Create database
pscale database create pi-system --region us-east

# Create password
pscale password create pi-system main pisystem-user

# Note: Save the connection details shown
```

**Step 2: Redis Cloud**
1. Go to https://redis.com/try-free/
2. Create free account
3. Create database: "pi-system-redis"
4. Select AWS US-East-1
5. Copy host, port, and password

---

#### Phase 2: Backend Deployment (20 minutes)

**Step 1: Prepare Repository**
```bash
cd /path/to/pi-system

# Create production application properties
cat > src/main/resources/application-prod.yml << EOF
server:
  port: \${PORT:8082}

spring:
  datasource:
    url: \${DATABASE_URL}
    username: \${DATABASE_USERNAME}
    password: \${DATABASE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
  
  flyway:
    enabled: true
    baseline-on-migrate: true
  
  redis:
    host: \${REDIS_HOST}
    port: \${REDIS_PORT:6379}
    password: \${REDIS_PASSWORD}
    ssl: true

jwt:
  secret: \${JWT_SECRET}
  expiration: 86400000
EOF

# Commit and push
git add .
git commit -m "Add production configuration"
git push origin main
```

**Step 2: Create Dockerfile**
```dockerfile
# Multi-stage build
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
```

**Step 3: Deploy on Render**
1. Go to https://render.com
2. Click "New +" → "Web Service"
3. Connect GitHub repository
4. Configure:
   - **Name**: pi-system-backend
   - **Environment**: Docker
   - **Branch**: main
   - **Instance Type**: Free
5. Add Environment Variables:
   ```
   SPRING_PROFILES_ACTIVE=prod
   DATABASE_URL=mysql://xxx:xxx@xxx.psdb.cloud/pisystem?sslaccept=strict
   DATABASE_USERNAME=xxx
   DATABASE_PASSWORD=xxx
   REDIS_HOST=redis-xxxxx.c1.us-east-1-2.ec2.redislabs.com
   REDIS_PORT=12345
   REDIS_PASSWORD=xxx
   JWT_SECRET=[generate random 64-char string]
   PORT=8082
   ```
6. Click "Create Web Service"
7. Wait 5-10 minutes for build

**Step 4: Run Migrations**
```bash
# Connect to PlanetScale
pscale connect pi-system main --port 3309

# Update local application.yml temporarily
spring:
  datasource:
    url: jdbc:mysql://localhost:3309/pisystem

# Run migrations
./gradlew flywayMigrate

# Or run migrations script
./run-migrations.sh
```

---

#### Phase 3: Frontend Deployment (10 minutes)

**Step 1: Configure Frontend**
```bash
cd frontend

# Create .env.production
echo "VITE_API_URL=https://pi-system-backend.onrender.com/api/v1" > .env.production

# Test build locally
npm install
npm run build

# Test the build
npm run preview
```

**Step 2: Deploy on Vercel**
1. Go to https://vercel.com
2. Click "Add New..." → "Project"
3. Import your GitHub repository
4. Configure:
   - **Framework Preset**: Vite
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Install Command**: `npm install`
5. Environment Variables:
   ```
   VITE_API_URL=https://pi-system-backend.onrender.com/api/v1
   ```
6. Click "Deploy"
7. Wait 2-3 minutes

**Step 3: Custom Domain (Optional)**
1. In Vercel, go to Settings → Domains
2. Add your domain (e.g., pi-system.yourdomain.com)
3. Update DNS records as instructed

---

#### Phase 4: Testing (10 minutes)

**Test Backend**:
```bash
# Health check
curl https://pi-system-backend.onrender.com/actuator/health

# Register user
curl -X POST https://pi-system-backend.onrender.com/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!"
  }'

# Login
curl -X POST https://pi-system-backend.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!"
  }'
```

**Test Frontend**:
1. Open https://your-app.vercel.app
2. Try to register
3. Try to login
4. Check if data loads

---

## ⚙️ Environment Configuration

### Required Environment Variables

#### Backend (Render/Railway/Fly.io)
```bash
# Database
DATABASE_URL=jdbc:mysql://host:port/database?sslaccept=strict
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password

# Redis
REDIS_HOST=redis-xxxxx.cloud.redislabs.com
REDIS_PORT=12345
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET=your-super-secret-key-min-64-characters-long-for-security

# Spring
SPRING_PROFILES_ACTIVE=prod
PORT=8082

# Optional: External APIs
MFAPI_BASE_URL=https://api.mfapi.in
```

#### Frontend (Vercel/Netlify)
```bash
VITE_API_URL=https://your-backend.onrender.com/api/v1
```

### Generating JWT Secret
```bash
# Option 1: OpenSSL
openssl rand -base64 64

# Option 2: Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"

# Option 3: Online
# Visit: https://generate.plus/en/base64
```

---

## 📊 Monitoring & Maintenance

### Monitoring Tools (Free)

#### 1. UptimeRobot
```
Website: https://uptimerobot.com
- Monitor up to 50 websites
- 5-minute check intervals
- Email/SMS alerts
- Status pages
```

**Setup**:
1. Create account
2. Add monitor: `https://pi-system-backend.onrender.com/actuator/health`
3. Set alert contacts

#### 2. Better Stack (formerly Logtail)
```
Website: https://betterstack.com
- Free logging (1GB/month)
- Real-time log streaming
- Error tracking
```

#### 3. Sentry (Error Tracking)
```
Website: https://sentry.io
- 5,000 errors/month free
- Performance monitoring
- Release tracking
```

**Add to application-prod.yml**:
```yaml
sentry:
  dsn: ${SENTRY_DSN}
  traces-sample-rate: 1.0
```

### Health Checks

#### Backend Health Endpoint
```java
// Already included via Spring Boot Actuator
// Access at: /actuator/health
```

#### Ping Endpoint
Create simple ping controller:
```java
@RestController
@RequestMapping("/api/v1/ping")
public class PingController {
    @GetMapping
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
```

### Log Management

#### Render Logs
```bash
# View via CLI
render logs -s pi-system-backend

# Or via web dashboard
```

#### Export Logs
```bash
# Download last 1000 lines
render logs -s pi-system-backend -n 1000 > logs.txt
```

---

## ⚠️ Limitations & Workarounds

### Free Tier Limitations

| Issue | Impact | Workaround |
|-------|--------|------------|
| **Cold Starts** | 30-60s delay after inactivity | Use cron job to ping every 10 mins |
| **Limited RAM (512MB)** | May OOM with heavy load | Optimize queries, add pagination |
| **CPU Throttling** | Slower performance | Cache aggressively with Redis |
| **Storage Limits** | Can't store large files | Use S3 free tier for documents |
| **Bandwidth Limits** | May exceed if popular | Optimize image sizes, use CDN |
| **Request Timeout** | 30s max on free tier | Break long operations into jobs |

### Workaround 1: Prevent Cold Starts

**Create Cron Job** (free):
```bash
# Use cron-job.org or easycron.com
# Ping every 10 minutes:
GET https://pi-system-backend.onrender.com/api/v1/ping
```

**Or use UptimeRobot** (mentioned above)

### Workaround 2: Optimize Memory

**application-prod.yml**:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  datasource:
    hikari:
      maximum-pool-size: 5  # Reduced from default 10
      minimum-idle: 2
```

**Dockerfile**:
```dockerfile
# Add JVM memory flags
ENTRYPOINT ["java", "-Xmx400m", "-Xms256m", "-XX:+UseG1GC", "-jar", "app.jar"]
```

### Workaround 3: Handle Timeouts

For long operations (reports, calculations):
```java
@Service
public class AsyncTaskService {
    
    @Async
    public CompletableFuture<Report> generateReport(Long userId) {
        // Long operation runs in background
        Report report = performCalculation();
        return CompletableFuture.completedFuture(report);
    }
}
```

### Workaround 4: Database Connection Pooling

```yaml
spring:
  datasource:
    hikari:
      connectionTimeout: 20000
      maximumPoolSize: 5
      idleTimeout: 300000
      maxLifetime: 600000
```

---

## 💰 Cost Upgrade Path

### When to Upgrade?

**Signs you've outgrown free tier**:
- [ ] Cold starts affecting user experience
- [ ] Hitting RAM limits (OOM errors)
- [ ] Exceeding bandwidth limits
- [ ] Need more than 5GB database storage
- [ ] Need 24/7 uptime without cold starts
- [ ] More than 100 users actively using the app

### Paid Tier Pricing (as of Feb 2026)

#### Render
```
Starter: $7/month
- 512MB RAM
- No cold starts
- Always on

Standard: $25/month
- 2GB RAM
- Faster builds
- Priority support
```

#### Railway
```
Hobby: $20/month credit
- ~500 hours execution
- All services included
- No cold starts
```

#### Fly.io
```
Hobby: $1.94/month per VM
- 256MB RAM
- 1 shared CPU
- No cold starts

Dedicated CPU: ~$30/month
- 1 dedicated CPU
- 2GB RAM
```

#### PlanetScale
```
Scaler: $29/month
- 10GB storage
- Multiple branches
- No cold starts
```

#### Vercel
```
Pro: $20/month
- Unlimited bandwidth
- Advanced analytics
- Team features
```

### Recommended Upgrade Path

**Phase 1: Backend Only ($7/month)**
- Upgrade Render to Starter
- Keep everything else free
- **Impact**: No cold starts, better UX

**Phase 2: Database ($29/month total)**
- Add PlanetScale Scaler
- **Impact**: More storage, better performance

**Phase 3: Full Stack ($60/month total)**
- Railway Hobby ($20)
- PlanetScale Scaler ($29)
- Vercel Pro ($20)
- **Impact**: Professional-grade hosting

---

## 🔧 Troubleshooting

### Issue 1: Build Fails on Render
```bash
# Error: Out of memory during build

# Solution: Add build environment variable
GRADLE_OPTS=-Xmx2g -XX:MaxMetaspaceSize=512m

# Or use pre-built JAR
git add build/libs/*.jar
```

### Issue 2: Database Connection Fails
```bash
# Error: Unable to connect to database

# Check:
1. Is DATABASE_URL correct?
2. Is SSL enabled? (add ?sslaccept=strict)
3. Is IP whitelisted? (PlanetScale: enable "Allow all IPs")
4. Are credentials correct?

# Test connection:
mysql -h host -u user -p database
```

### Issue 3: Frontend Can't Connect to Backend
```bash
# Error: CORS policy blocked

# Solution: Update SecurityConfig.java
@Bean
public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("https://your-app.vercel.app");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
}
```

### Issue 4: Redis Connection Timeout
```bash
# Error: Redis connection timeout

# Solution: Increase timeout
spring:
  redis:
    timeout: 10000ms  # 10 seconds
    connect-timeout: 10000ms
```

### Issue 5: Migrations Not Running
```bash
# Check Flyway status
./gradlew flywayInfo

# Run migrations manually
./gradlew flywayMigrate

# Repair if needed
./gradlew flywayRepair
```

---

## 📚 Additional Resources

### Official Documentation
- [Render Docs](https://render.com/docs)
- [Railway Docs](https://docs.railway.app)
- [Fly.io Docs](https://fly.io/docs)
- [Vercel Docs](https://vercel.com/docs)
- [PlanetScale Docs](https://docs.planetscale.com)

### Community Resources
- [Awesome Free Hosting](https://github.com/cloudcommunity/free-hosting)
- [Free For Dev](https://free-for.dev)
- [r/webhosting](https://reddit.com/r/webhosting)

### Video Tutorials
- [Deploying Spring Boot on Render](https://www.youtube.com/results?search_query=deploy+spring+boot+render)
- [React on Vercel](https://www.youtube.com/results?search_query=deploy+react+vercel)

---

## ✅ Deployment Checklist

### Pre-Deployment
- [ ] Code pushed to GitHub
- [ ] Production configuration file created
- [ ] Dockerfile created and tested
- [ ] Environment variables documented
- [ ] Database schema exported
- [ ] Frontend build tested locally

### Backend Deployment
- [ ] Hosting service account created
- [ ] Database created and configured
- [ ] Redis instance created
- [ ] Backend deployed
- [ ] Environment variables set
- [ ] Migrations run successfully
- [ ] Health endpoint accessible
- [ ] Can register a test user
- [ ] Can login with test user

### Frontend Deployment
- [ ] Build configuration correct
- [ ] API URL environment variable set
- [ ] Deployment successful
- [ ] Can access the site
- [ ] Can connect to backend
- [ ] Authentication works
- [ ] All pages load correctly

### Post-Deployment
- [ ] Uptime monitoring configured
- [ ] Error tracking setup (Sentry)
- [ ] Backup strategy documented
- [ ] Custom domain configured (if applicable)
- [ ] SSL certificate active
- [ ] Performance tested
- [ ] Documentation updated with URLs

---

## 🎉 Success!

If you've followed this guide, you should now have:
- ✅ Backend running on free hosting
- ✅ Database on free MySQL hosting
- ✅ Redis cache on free tier
- ✅ Frontend on global CDN
- ✅ HTTPS enabled
- ✅ Monitoring setup
- ✅ $0/month cost

**Your PI System is now live!** 🚀

---

**Version**: 1.0.0  
**Last Updated**: February 1, 2026  
**Maintained By**: PI System DevOps Team  
**Questions?** Check [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for production deployment
