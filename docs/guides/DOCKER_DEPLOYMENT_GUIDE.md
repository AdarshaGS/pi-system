# 🐳 Docker Deployment Guide - Pi-System

## 📋 Quick Start

### Option 1: Deploy Everything with Docker Compose (Recommended)

```bash
# 1. Navigate to project directory
cd /Users/adarshgs/Documents/Stocks/App/pi-system

# 2. Build and start all services
docker-compose up -d --build

# 3. Check status
docker-compose ps

# 4. View logs
docker-compose logs -f app
```

**This will start:**
- MySQL database (pi_system)
- Redis cache
- Pi-System backend application
- Prometheus monitoring
- Grafana dashboards

---

### Option 2: Build and Run Docker Image Only

```bash
# 1. Build the Docker image
docker build -t pi-system:latest .

# 2. Run with external database
docker run -d \
  --name pi-system-app \
  -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/pi_system?useSSL=false&serverTimezone=UTC" \
  -e DB_USER="root" \
  -e DB_PASSWORD="mysql" \
  -e REDIS_HOST="host.docker.internal" \
  -e REDIS_PORT="6379" \
  -e JWT_SECRET="your-secret-key" \
  -e JWT_EXPIRATION="86400000" \
  -e JWT_REFRESH_TOKEN_EXPIRATION="2592000000" \
  -e SPRING_PROFILES_ACTIVE="prod" \
  pi-system:latest

# 3. Check logs
docker logs -f pi-system-app
```

---

## 📦 Pre-Deployment Checklist

- [ ] Ensure `.env` file exists and is configured
- [ ] Database name is set to `pi_system` (with underscore)
- [ ] Docker and Docker Compose are installed
- [ ] Ports 3306, 6379, 8080, 9090, 3000 are available
- [ ] Project builds successfully: `./gradlew clean build`

---

## 🚀 Step-by-Step Deployment

### Step 1: Verify Configuration

```bash
# Check .env file
cat .env | grep DB_URL
# Should show: DB_URL=jdbc:mysql://localhost:3306/pi_system...

# Verify docker-compose.yml
grep "MYSQL_DATABASE" docker-compose.yml
# Should show: MYSQL_DATABASE: pi_system
```

### Step 2: Build the Application

```bash
# Option A: Let Docker handle the build (slower but isolated)
docker-compose build app

# Option B: Build locally first (faster for subsequent builds)
./gradlew clean build -x test
docker-compose build app
```

### Step 3: Start Services

```bash
# Start all services
docker-compose up -d

# Or start specific services
docker-compose up -d mysql redis  # Start dependencies first
docker-compose up -d app          # Then start app
```

### Step 4: Verify Deployment

```bash
# Check all containers are running
docker-compose ps

# Should show:
# pi-system-db     Up (healthy)
# redis            Up (healthy)
# pi-system-app    Up (healthy)
# prometheus       Up
# grafana          Up

# View application logs
docker-compose logs -f app

# Wait for message: "Started Application in X seconds"
```

### Step 5: Test the Application

```bash
# Health check
curl http://localhost:8080/actuator/health

# API test
curl http://localhost:8080/api/v1/subscription/tiers

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## 🔧 Common Commands

### Managing Services

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose stop

# Restart a service
docker-compose restart app

# View logs
docker-compose logs -f app        # Follow app logs
docker-compose logs --tail=100    # Last 100 lines

# Execute commands in container
docker-compose exec app sh        # Shell access
docker-compose exec mysql mysql -uroot -pmysql  # MySQL CLI
```

### Rebuilding

```bash
# Rebuild and restart
docker-compose up -d --build

# Force recreate containers
docker-compose up -d --force-recreate

# Rebuild without cache
docker-compose build --no-cache app
```

### Cleaning Up

```bash
# Stop and remove containers
docker-compose down

# Remove containers and volumes (⚠️ deletes data)
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Clean everything (⚠️ nuclear option)
docker system prune -a --volumes
```

---

## 🌐 Accessing Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Backend API** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **Grafana** | http://localhost:3000 | admin / admin123 |
| **Prometheus** | http://localhost:9090 | - |
| **MySQL** | localhost:3306 | root / mysql |
| **Redis** | localhost:6379 | - |

---

## 📊 Monitoring

### View Metrics

```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Health status
curl http://localhost:8080/actuator/health
```

### Grafana Dashboards

1. Open http://localhost:3000
2. Login with `admin` / `admin123`
3. Navigate to Dashboards
4. View pre-configured dashboards

---

## 🐛 Troubleshooting

### Container Won't Start

```bash
# Check logs for errors
docker-compose logs app

# Check container status
docker-compose ps

# Inspect container
docker inspect pi-system-app

# Check resource usage
docker stats
```

### Database Connection Issues

```bash
# Check MySQL is running
docker-compose ps mysql

# Test MySQL connection
docker-compose exec mysql mysql -uroot -pmysql -e "SHOW DATABASES;"

# Verify database exists
docker-compose exec mysql mysql -uroot -pmysql -e "USE pi_system; SHOW TABLES;"

# Check network connectivity
docker-compose exec app ping mysql
```

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port in docker-compose.yml
ports:
  - "8081:8080"  # External:Internal
```

### Build Failures

```bash
# Clean Gradle cache
./gradlew clean

# Rebuild without Docker cache
docker-compose build --no-cache app

# Check Dockerfile syntax
docker build -t test .
```

### Database Migration Issues

```bash
# Access app container
docker-compose exec app sh

# Check Flyway status
# (Inside container)
java -jar app.jar --spring.profiles.active=prod flyway.info

# Manual migration
docker-compose exec app java -jar app.jar --spring.profiles.active=prod flyway.migrate
```

---

## 🔐 Production Deployment

### Environment Variables

Create a `.env.production` file:

```env
# Database
DB_URL=jdbc:mysql://production-db:3306/pi_system?useSSL=true
DB_USER=pi_user
DB_PASSWORD=<strong-password>

# Redis
REDIS_HOST=production-redis
REDIS_PORT=6379

# JWT
JWT_SECRET=<generate-strong-secret>
JWT_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=<your-email>
MAIL_PASSWORD=<app-password>

# Profile
SPRING_PROFILES_ACTIVE=prod
```

### Security Best Practices

1. **Use secrets management**
   ```bash
   # Docker secrets
   docker secret create db_password db_password.txt
   ```

2. **Use private registry**
   ```bash
   # Tag image
   docker tag pi-system:latest registry.example.com/pi-system:v1.0.0
   
   # Push to registry
   docker push registry.example.com/pi-system:v1.0.0
   ```

3. **Enable HTTPS**
   - Add nginx reverse proxy
   - Configure SSL certificates
   - Update docker-compose.yml

4. **Limit resource usage**
   ```yaml
   services:
     app:
       deploy:
         resources:
           limits:
             cpus: '2'
             memory: 2G
           reservations:
             cpus: '1'
             memory: 1G
   ```

---

## 📤 Deploying to Cloud

### Docker Hub

```bash
# Login
docker login

# Tag image
docker tag pi-system:latest yourusername/pi-system:latest

# Push
docker push yourusername/pi-system:latest

# Pull on server
docker pull yourusername/pi-system:latest
```

### AWS ECS / ECR

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Tag
docker tag pi-system:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/pi-system:latest

# Push
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/pi-system:latest
```

### Google Cloud Run

```bash
# Build and push
gcloud builds submit --tag gcr.io/PROJECT-ID/pi-system

# Deploy
gcloud run deploy pi-system \
  --image gcr.io/PROJECT-ID/pi-system \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

### Railway / Render

```bash
# These platforms auto-detect Dockerfile
# Just connect your GitHub repo and deploy!
```

---

## 📈 Scaling

### Horizontal Scaling

```yaml
services:
  app:
    deploy:
      replicas: 3
    
  # Add load balancer
  nginx:
    image: nginx:alpine
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    ports:
      - "80:80"
    depends_on:
      - app
```

### Database Replication

```yaml
services:
  mysql-master:
    # Primary database
    
  mysql-slave:
    # Read replica
    environment:
      MYSQL_MASTER_HOST: mysql-master
```

---

## 🎯 Quick Reference

| Task | Command |
|------|---------|
| **Start** | `docker-compose up -d` |
| **Stop** | `docker-compose stop` |
| **Restart** | `docker-compose restart app` |
| **Logs** | `docker-compose logs -f app` |
| **Build** | `docker-compose build app` |
| **Clean** | `docker-compose down -v` |
| **Scale** | `docker-compose up -d --scale app=3` |
| **Update** | `docker-compose pull && docker-compose up -d` |

---

## ✅ Success Checklist

After deployment, verify:

- [ ] Application accessible at http://localhost:8080
- [ ] Swagger UI loads at http://localhost:8080/swagger-ui.html
- [ ] Health check returns `{"status":"UP"}`
- [ ] Database has tables (run migrations)
- [ ] Can create user and login
- [ ] Can access tier information endpoint
- [ ] Grafana dashboards display data
- [ ] Prometheus scraping metrics
- [ ] Logs show no errors

---

## 🆘 Getting Help

If issues persist:

1. Check logs: `docker-compose logs -f`
2. Verify configuration: `docker-compose config`
3. Test network: `docker-compose exec app ping mysql`
4. Check resources: `docker stats`
5. Review documentation in `docs/` folder

---

## 🎉 You're Ready!

Your pi-system is now containerized and ready to deploy anywhere Docker runs!

**Next Steps:**
1. Deploy locally: `docker-compose up -d`
2. Test all endpoints
3. Deploy to production
4. Set up CI/CD pipeline
5. Monitor and scale as needed
