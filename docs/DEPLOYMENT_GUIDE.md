# Deployment Guide

**Last Updated**: February 1, 2026  
**Status**: Production Ready  
**Environment**: AWS / Docker / Kubernetes

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Build & Package](#build--package)
4. [Docker Deployment](#docker-deployment)
5. [Kubernetes Deployment](#kubernetes-deployment)
6. [Database Migration](#database-migration)
7. [Monitoring & Logging](#monitoring--logging)
8. [Rollback Procedures](#rollback-procedures)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### System Requirements
- **Java**: 17+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Docker**: 20.10+
- **Kubernetes**: 1.24+ (optional)
- **Git**: 2.30+

### Access Requirements
- GitHub repository access
- AWS credentials (for cloud deployment)
- Database admin credentials
- Docker Hub / Container Registry access

---

## Environment Setup

### Environment Variables

#### Backend (.env)
```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=pi-system
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRATION=86400000

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# External APIs
MUTUAL_FUND_API_URL=https://api.mfapi.in

# Email (Optional)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
```

#### Frontend (.env)
```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=Pi Finance System
VITE_APP_VERSION=1.0.0
```

### Application Profiles

#### application-dev.yml (Development)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pi-system
    username: root
    password: root
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    
logging:
  level:
    root: INFO
    com.pifinance: DEBUG
```

#### application-prod.yml (Production)
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    
logging:
  level:
    root: WARN
    com.pifinance: INFO
  file:
    name: /var/log/pi-system/application.log
```

---

## Build & Package

### Backend Build

#### Using Gradle
```bash
# Clean and build
./gradlew clean build

# Build without tests (faster)
./gradlew clean build -x test

# Build JAR only
./gradlew bootJar

# Output: build/libs/pifinance-1.0.0.jar
```

#### Using Maven
```bash
# Clean and build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Output: target/pifinance-1.0.0.jar
```

### Frontend Build

```bash
cd frontend

# Install dependencies
npm install

# Build for production
npm run build

# Output: dist/
```

### Verify Build

```bash
# Check JAR file
ls -lh build/libs/

# Check frontend build
ls -lh frontend/dist/

# Test JAR execution
java -jar build/libs/pifinance-1.0.0.jar --spring.profiles.active=prod
```

---

## Docker Deployment

### Dockerfile (Backend)

```dockerfile
# Multi-stage build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./
COPY src src
RUN ./gradlew clean build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Dockerfile (Frontend)

```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: pifinance-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: pi-system
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: pifinance-backend
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: pifinance
      DB_USERNAME: root
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
    volumes:
      - ./logs:/var/log/pifinance
    restart: unless-stopped

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: pifinance-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped

volumes:
  mysql-data:
```

### Deploy with Docker Compose

```bash
# Set environment variables
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret

# Build and start all services
docker-compose up -d --build

# Check status
docker-compose ps

# View logs
docker-compose logs -f backend

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

---

## Kubernetes Deployment

### ConfigMap

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: pifinance-config
  namespace: production
data:
  application.yml: |
    spring:
      profiles:
        active: prod
      datasource:
        url: jdbc:mysql://mysql-service:3306/pifinance
      flyway:
        enabled: true
    logging:
      level:
        root: INFO
```

### Secret

```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: pifinance-secret
  namespace: production
type: Opaque
stringData:
  db-password: your_database_password
  jwt-secret: your_jwt_secret_key
```

### MySQL Deployment

```yaml
# mysql-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: production
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: pifinance-secret
              key: db-password
        - name: MYSQL_DATABASE
          value: pifinance
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
      volumes:
      - name: mysql-storage
        persistentVolumeClaim:
          claimName: mysql-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
  namespace: production
spec:
  selector:
    app: mysql
  ports:
  - port: 3306
    targetPort: 3306
```

### Backend Deployment

```yaml
# backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pifinance-backend
  namespace: production
spec:
  replicas: 3
  selector:
    matchLabels:
      app: pifinance-backend
  template:
    metadata:
      labels:
        app: pifinance-backend
    spec:
      containers:
      - name: backend
        image: pifinance/backend:1.0.0
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: prod
        - name: DB_HOST
          value: mysql-service
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: pifinance-secret
              key: db-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: pifinance-secret
              key: jwt-secret
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
---
apiVersion: v1
kind: Service
metadata:
  name: backend-service
  namespace: production
spec:
  selector:
    app: pifinance-backend
  ports:
  - port: 8080
    targetPort: 8080
  type: ClusterIP
```

### Ingress

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: pifinance-ingress
  namespace: production
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - api.pifinance.com
    secretName: pifinance-tls
  rules:
  - host: api.pifinance.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: backend-service
            port:
              number: 8080
```

### Deploy to Kubernetes

```bash
# Create namespace
kubectl create namespace production

# Apply configurations
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f mysql-deployment.yaml
kubectl apply -f backend-deployment.yaml
kubectl apply -f ingress.yaml

# Check deployments
kubectl get deployments -n production
kubectl get pods -n production
kubectl get services -n production

# View logs
kubectl logs -f deployment/pifinance-backend -n production

# Scale deployment
kubectl scale deployment pifinance-backend --replicas=5 -n production
```

---

## Database Migration

### Pre-Deployment Checks

```bash
# Backup database
mysqldump -u root -p pifinance > backup_$(date +%Y%m%d_%H%M%S).sql

# Verify migration files
ls -l src/main/resources/db/migration/

# Test migrations locally
./gradlew flywayMigrate -Dflyway.url=jdbc:mysql://localhost:3306/pifinance_test
```

### Flyway Migration

Migrations are automatically run on application startup. To run manually:

```bash
# Using Gradle
./gradlew flywayMigrate

# Using Flyway CLI
flyway -url=jdbc:mysql://localhost:3306/pifinance \
       -user=root \
       -password=password \
       migrate

# Check migration status
./gradlew flywayInfo

# Repair failed migrations
./gradlew flywayRepair
```

### Rollback Procedure

```sql
-- Check flyway_schema_history
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;

-- Rollback to specific version (manual)
-- 1. Restore database backup
mysql -u root -p pifinance < backup_20260201.sql

-- 2. Delete failed migration records
DELETE FROM flyway_schema_history WHERE version = 'X';
```

---

## Monitoring & Logging

### Spring Actuator Endpoints

```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

#### Health Check
```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

### Logging Configuration

```xml
<!-- logback-spring.xml -->
<configuration>
    <property name="LOG_PATH" value="/var/log/pifinance"/>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/application-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### Prometheus Metrics

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'pifinance'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

---

## Rollback Procedures

### Application Rollback

#### Docker Compose
```bash
# Tag current version
docker tag pifinance/backend:latest pifinance/backend:1.0.1

# Rollback to previous version
docker-compose down
docker pull pifinance/backend:1.0.0
docker-compose up -d
```

#### Kubernetes
```bash
# Rollback to previous deployment
kubectl rollout undo deployment/pifinance-backend -n production

# Rollback to specific revision
kubectl rollout undo deployment/pifinance-backend --to-revision=2 -n production

# Check rollout status
kubectl rollout status deployment/pifinance-backend -n production

# View rollout history
kubectl rollout history deployment/pifinance-backend -n production
```

### Database Rollback

```bash
# Restore from backup
mysql -u root -p pifinance < backup_20260201.sql

# Or use Flyway repair (for failed migrations only)
./gradlew flywayRepair
```

---

## Troubleshooting

### Common Issues

#### Application Won't Start

**Check logs:**
```bash
# Docker
docker logs pifinance-backend

# Kubernetes
kubectl logs deployment/pifinance-backend -n production

# Local
tail -f /var/log/pifinance/application.log
```

**Common causes:**
- Database connection failure
- Missing environment variables
- Port already in use
- Insufficient memory

#### Database Connection Issues

```bash
# Test MySQL connection
mysql -h localhost -u root -p -e "SELECT 1"

# Check MySQL service
sudo systemctl status mysql

# Verify credentials
echo $DB_PASSWORD
```

#### High Memory Usage

```bash
# Check Java heap usage
jmap -heap <pid>

# Adjust JVM options
export JAVA_OPTS="-Xmx1024m -Xms512m"

# Restart application
docker-compose restart backend
```

#### Slow Performance

```bash
# Check database indexes
SHOW INDEX FROM expenses;

# Analyze slow queries
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

# Monitor application metrics
curl http://localhost:8080/actuator/metrics
```

---

## Deployment Checklist

### Pre-Deployment
- [ ] Code reviewed and approved
- [ ] All tests passing
- [ ] Database backup taken
- [ ] Environment variables configured
- [ ] Migration scripts tested
- [ ] Rollback plan prepared
- [ ] Monitoring alerts configured

### During Deployment
- [ ] Deploy to staging first
- [ ] Run smoke tests
- [ ] Check application logs
- [ ] Verify database migrations
- [ ] Test critical endpoints
- [ ] Monitor error rates

### Post-Deployment
- [ ] Verify health check endpoints
- [ ] Check application metrics
- [ ] Review error logs
- [ ] Test key features
- [ ] Notify stakeholders
- [ ] Update documentation
- [ ] Tag release in Git

---

## Maintenance Windows

### Scheduled Maintenance
- **Frequency**: Monthly (First Sunday, 2:00 AM - 4:00 AM)
- **Duration**: 2 hours
- **Activities**: Database maintenance, backup verification, updates

### Emergency Maintenance
- Critical security patches: Immediate
- Hotfixes: Within 4 hours
- Non-critical updates: Next maintenance window

---

## Contact & Support

### Deployment Team
- **DevOps Lead**: devops@pifinance.com
- **On-Call Engineer**: oncall@pifinance.com
- **Emergency Hotline**: +1-XXX-XXX-XXXX

### Escalation Path
1. On-call engineer (immediate)
2. DevOps lead (15 minutes)
3. Engineering manager (30 minutes)
4. CTO (1 hour)

---

**Document Owner**: DevOps Team  
**Review Cycle**: Quarterly  
**Next Review**: May 1, 2026
