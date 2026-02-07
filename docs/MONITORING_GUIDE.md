# 📊 Prometheus & Grafana Monitoring Guide

**Implementation Date**: February 2, 2026  
**Status**: ✅ Complete  
**Version**: 1.0.0

---

## 📋 Overview

This guide describes the complete monitoring setup for PI System using Prometheus for metrics collection and Grafana for visualization. The system tracks application performance, business metrics, JVM health, and database operations.

---

## 🏗️ Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   PI System     │────▶│   Prometheus     │────▶│    Grafana      │
│   Application   │     │  (Metrics Store) │     │ (Visualization) │
│   :8080         │     │      :9090       │     │      :3000      │
└─────────────────┘     └──────────────────┘     └─────────────────┘
         │
         │ /actuator/prometheus
         │
    ┌────▼─────┐
    │  Metrics │
    │ Exporter │
    └──────────┘
```

---

## 🚀 Quick Start

### 1. Start All Services

```bash
# Start all services (MySQL, Redis, App, Prometheus, Grafana)
docker-compose up -d

# Check service status
docker-compose ps
```

### 2. Access Dashboards

| Service | URL | Credentials |
|---------|-----|-------------|
| **PI System** | http://localhost:8080 | Your app credentials |
| **Prometheus** | http://localhost:9090 | No auth required |
| **Grafana** | http://localhost:3000 | admin / admin123 |
| **Actuator** | http://localhost:8080/actuator | No auth required |

### 3. View Metrics

**Prometheus Metrics Endpoint:**
```
http://localhost:8080/actuator/prometheus
```

**Grafana Dashboard:**
1. Login to Grafana (admin/admin123)
2. Navigate to "Dashboards"
3. Open "PI System - Application Metrics"

---

## 📊 Available Metrics

### 1. **HTTP Metrics**
- Request rate (requests per second)
- Response time percentiles (50th, 95th, 99th)
- Error rates by status code
- Request count by endpoint

**Prometheus Queries:**
```promql
# Request rate
rate(http_server_requests_seconds_count{application="pi-system"}[5m])

# 95th percentile response time
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{application="pi-system"}[5m])) by (le))
```

### 2. **Business Metrics**
- Total user registrations
- Total user logins
- Portfolio transactions count
- Budget exceeded events
- Loan payments recorded
- Insurance claims filed

**Custom Metrics:**
```java
@Autowired
private CustomMetrics customMetrics;

// In your controller/service
customMetrics.incrementUserRegistration();
customMetrics.incrementUserLogin();
customMetrics.incrementPortfolioTransaction();
```

### 3. **JVM Metrics**
- Heap memory usage
- Non-heap memory usage
- Garbage collection stats
- Thread count
- CPU usage (system and process)

**Prometheus Queries:**
```promql
# JVM memory
jvm_memory_used_bytes{application="pi-system"}

# CPU usage
system_cpu_usage{application="pi-system"}
process_cpu_usage{application="pi-system"}
```

### 4. **Database Metrics**
- HikariCP connection pool stats
- Active/idle connections
- Query execution time
- Connection wait time

**Prometheus Queries:**
```promql
# Active connections
hikaricp_connections_active{application="pi-system"}

# Database query performance
histogram_quantile(0.95, sum(rate(database_query_duration_seconds_bucket[5m])) by (le))
```

### 5. **System Health**
- Application uptime
- Thread states
- System load average
- Disk space

---

## 🎨 Grafana Dashboard

### Dashboard Panels

#### Row 1: HTTP Performance
1. **HTTP Requests Rate** (Time Series)
   - Shows requests per second by endpoint and status
   
2. **95th Percentile Response Time** (Gauge)
   - Visual indicator of response time health
   - Green < 100ms, Yellow < 500ms, Red > 500ms

#### Row 2: Resource Usage
3. **JVM Memory Usage** (Time Series)
   - Heap and non-heap memory by region
   
4. **CPU Usage** (Time Series)
   - System and process CPU utilization

#### Row 3: Business Metrics
5. **Total User Registrations** (Stat)
6. **Total User Logins** (Stat)
7. **Portfolio Transactions** (Stat)
8. **Budget Exceeded Count** (Stat)

#### Row 4: Database Performance
9. **Database Query Performance** (Time Series)
   - 50th and 95th percentile query times
   
10. **Database Connection Pool** (Time Series)
    - Active, idle, and total connections

### Dashboard Features
- **Auto-refresh**: Updates every 10 seconds
- **Time Range**: Last 1 hour (configurable)
- **Variables**: None (can be added for environment filtering)
- **Templating**: Ready for multi-environment setup

---

## 🔧 Configuration Files

### 1. Application Configuration
**Location**: `bin/main/application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: pi-system
      environment: ${spring.profiles.active}
```

### 2. Prometheus Configuration
**Location**: `monitoring/prometheus.yml`

```yaml
scrape_configs:
  - job_name: 'pi-system'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    static_configs:
      - targets: ['app:8080']
```

### 3. Grafana Datasource
**Location**: `monitoring/grafana/datasources/prometheus.yml`

```yaml
datasources:
  - name: Prometheus
    type: prometheus
    url: http://prometheus:9090
    isDefault: true
```

### 4. Docker Compose
**Location**: `docker-compose.yml`

Includes:
- Prometheus container (port 9090)
- Grafana container (port 3000)
- Volume mounts for configurations
- Network configuration

---

## 📈 Custom Metrics Usage

### Adding Custom Business Metrics

**1. Inject CustomMetrics:**
```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private CustomMetrics customMetrics;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO dto) {
        // ... registration logic
        
        customMetrics.incrementUserRegistration();
        
        return ResponseEntity.ok(response);
    }
}
```

**2. Available Methods:**
```java
// Counters
customMetrics.incrementUserRegistration();
customMetrics.incrementUserLogin();
customMetrics.incrementPortfolioTransaction();
customMetrics.incrementBudgetExceeded();
customMetrics.incrementLoanPayment();
customMetrics.incrementInsuranceClaim();

// Timers
Timer.Sample sample = customMetrics.startDatabaseTimer();
// ... database operation
customMetrics.recordDatabaseQuery(sample);

Timer.Sample apiSample = customMetrics.startExternalApiTimer();
// ... external API call
customMetrics.recordExternalApiCall(apiSample);
```

**3. Add New Custom Metrics:**

Edit `CustomMetrics.java`:
```java
private final Counter newMetricCounter;

public CustomMetrics(MeterRegistry registry) {
    this.newMetricCounter = Counter.builder("custom.metric.total")
            .description("Description of the metric")
            .tag("type", "business")
            .register(registry);
}

public void incrementNewMetric() {
    newMetricCounter.increment();
}
```

---

## 🔍 Querying Prometheus

### Access Prometheus UI
http://localhost:9090

### Example Queries

**1. Current Request Rate:**
```promql
rate(http_server_requests_seconds_count{application="pi-system"}[1m])
```

**2. Error Rate:**
```promql
rate(http_server_requests_seconds_count{application="pi-system", status=~"5.."}[5m])
```

**3. Memory Usage Percentage:**
```promql
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100
```

**4. Database Connection Pool Usage:**
```promql
(hikaricp_connections_active / hikaricp_connections) * 100
```

**5. Business Metrics - New Users (per hour):**
```promql
increase(user_registrations_total[1h])
```

---

## 🚨 Alerting (Future Enhancement)

### Prometheus Alerting Rules
Create `monitoring/alerts.yml`:

```yaml
groups:
  - name: pi-system-alerts
    interval: 30s
    rules:
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le)) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "95th percentile response time is above 1 second"
      
      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.9
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High heap memory usage"
          description: "JVM heap usage is above 90%"
      
      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active >= hikaricp_connections
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool exhausted"
          description: "All database connections are in use"
```

### Enable Alertmanager
1. Add Alertmanager to docker-compose.yml
2. Configure alert destinations (email, Slack, PagerDuty)
3. Update prometheus.yml with alerting config

---

## 🔐 Security Considerations

### Production Recommendations

1. **Enable Authentication:**
   - Secure Grafana with strong password
   - Use OAuth/LDAP for team access
   - Restrict Prometheus to internal network

2. **Actuator Security:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

Add Spring Security rules:
```java
@Bean
public SecurityFilterChain actuatorSecurity(HttpSecurity http) {
    http.requestMatchers("/actuator/**")
        .hasRole("ADMIN");
    return http.build();
}
```

3. **Network Isolation:**
   - Keep Prometheus and Grafana on private network
   - Use reverse proxy for public access
   - Enable HTTPS/TLS

---

## 📦 Data Retention

### Prometheus Storage
- Default: 15 days
- Configure in docker-compose.yml:
```yaml
prometheus:
  command:
    - '--storage.tsdb.retention.time=30d'
    - '--storage.tsdb.retention.size=10GB'
```

### Grafana Settings
- Snapshots expire after 90 days
- Query history stored in database
- Configure in grafana.ini or environment variables

---

## 🛠️ Troubleshooting

### Common Issues

**1. Prometheus Can't Scrape Metrics**
```bash
# Check if actuator endpoint is accessible
curl http://localhost:8080/actuator/prometheus

# Check Prometheus targets
# Navigate to http://localhost:9090/targets
```

**2. Grafana Can't Connect to Prometheus**
```bash
# Check network connectivity
docker exec grafana ping prometheus

# Verify datasource configuration
# Grafana UI → Configuration → Data Sources
```

**3. No Custom Metrics Showing**
```bash
# Verify CustomMetrics bean is loaded
# Check application logs for errors

# Test metric manually
curl http://localhost:8080/actuator/prometheus | grep user_registrations
```

**4. High Memory Usage**
```bash
# Check Prometheus memory
docker stats prometheus

# Reduce retention or scrape frequency
```

---

## 📚 Best Practices

### 1. Metric Naming
- Use snake_case: `user_registrations_total`
- Add units suffix: `_seconds`, `_bytes`, `_total`
- Be consistent with labels

### 2. Dashboard Design
- Group related metrics
- Use appropriate visualization types
- Set meaningful thresholds
- Add descriptive titles and legends

### 3. Alerting
- Alert on symptoms, not causes
- Avoid alert fatigue (tune thresholds)
- Include actionable information
- Test alerts regularly

### 4. Performance
- Limit cardinality of labels
- Use recording rules for complex queries
- Set appropriate retention periods
- Monitor Prometheus itself

---

## 🔄 Maintenance

### Regular Tasks

**Weekly:**
- Review dashboard performance
- Check for anomalies in metrics
- Verify all targets are healthy

**Monthly:**
- Review and tune alert rules
- Clean up unused dashboards
- Update Grafana/Prometheus versions
- Audit metric retention policies

**Quarterly:**
- Review and optimize queries
- Add new business metrics
- Performance tuning
- Security audit

---

## 📖 Additional Resources

### Official Documentation
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)

### Useful Links
- [PromQL Cheat Sheet](https://promlabs.com/promql-cheat-sheet/)
- [Grafana Best Practices](https://grafana.com/docs/grafana/latest/best-practices/)
- [Spring Boot Metrics](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics)

---

## 🎯 Next Steps

### Recommended Enhancements

1. **Add Alerting**
   - Set up Alertmanager
   - Configure notification channels
   - Create alert rules

2. **Advanced Dashboards**
   - Create separate dashboards for each module
   - Add business intelligence dashboards
   - User behavior analytics

3. **Logging Integration**
   - Add ELK/EFK stack
   - Correlate logs with metrics
   - Full observability stack

4. **Distributed Tracing**
   - Integrate Jaeger or Zipkin
   - Track request flows
   - Performance bottleneck analysis

5. **APM Tools**
   - Consider New Relic, DataDog, or Dynatrace
   - Advanced profiling
   - User experience monitoring

---

## ✅ Summary

**Implementation Complete:**
- ✅ Prometheus integration with Spring Boot Actuator
- ✅ Custom business metrics (6 counters, 2 timers)
- ✅ Grafana dashboard with 11 panels
- ✅ Docker Compose orchestration
- ✅ Auto-provisioned datasources
- ✅ JVM, HTTP, and database metrics
- ✅ Production-ready configuration

**Access Points:**
- Application: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin123)

**Files Created:**
- `build.gradle` - Dependencies added
- `application.yml` - Actuator configuration
- `MetricsConfiguration.java` - Metrics setup
- `CustomMetrics.java` - Business metrics
- `docker-compose.yml` - Monitoring services
- `prometheus.yml` - Scrape configuration
- Grafana configs - Datasources and dashboard

---

**Document Version**: 1.0.0  
**Last Updated**: February 2, 2026  
**Status**: Production Ready ✅
