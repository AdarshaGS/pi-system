# 📊 Monitoring Configuration

This directory contains all configuration files for Prometheus and Grafana monitoring setup.

## 📁 Directory Structure

```
monitoring/
├── prometheus.yml                    # Prometheus scrape configuration
├── grafana/
│   ├── dashboards/
│   │   ├── dashboard-provider.yml   # Dashboard provisioning config
│   │   └── pi-system-dashboard.json # Main application dashboard
│   └── datasources/
│       └── prometheus.yml            # Prometheus datasource config
```

## 🚀 Quick Start

### Start Monitoring Stack

```bash
# From project root
docker-compose up -d prometheus grafana

# Check status
docker-compose ps
```

### Access Services

- **Grafana**: http://localhost:3000 (admin / admin123)
- **Prometheus**: http://localhost:9090

## 📊 Dashboard Panels

The PI System dashboard includes:

1. **HTTP Requests Rate** - Requests per second by endpoint
2. **95th Percentile Response Time** - Response time health indicator
3. **JVM Memory Usage** - Heap and non-heap memory
4. **CPU Usage** - System and process CPU
5. **User Registrations** - Total registrations count
6. **User Logins** - Total logins count
7. **Portfolio Transactions** - Transaction count
8. **Budget Exceeded** - Budget alert count
9. **Database Query Performance** - Query execution times
10. **Database Connection Pool** - HikariCP connections

## 🔧 Configuration

### Prometheus

- **Scrape Interval**: 10 seconds
- **Metrics Path**: `/actuator/prometheus`
- **Target**: `app:8080`

### Grafana

- **Admin User**: admin
- **Admin Password**: admin123 (change in production!)
- **Default Datasource**: Prometheus
- **Auto-refresh**: Every 10 seconds

## 📖 Documentation

For detailed documentation, see [MONITORING_GUIDE.md](../docs/MONITORING_GUIDE.md)

## 🔐 Security Notes

**⚠️ Important for Production:**

1. Change default Grafana password
2. Restrict Prometheus/Grafana access to internal network
3. Enable HTTPS/TLS
4. Add authentication to actuator endpoints
5. Use secrets management for credentials

## 🛠️ Troubleshooting

### Prometheus Can't Scrape Metrics

```bash
# Check if application is running
curl http://localhost:8080/actuator/health

# Check Prometheus targets
# Visit http://localhost:9090/targets
```

### Grafana Can't Connect to Prometheus

```bash
# Test network connectivity
docker exec grafana ping prometheus

# Verify datasource in Grafana UI
# Configuration → Data Sources
```

## 📝 Adding Custom Metrics

See [MONITORING_GUIDE.md](../docs/MONITORING_GUIDE.md#custom-metrics-usage) for examples.

## 🔄 Updates

Last Updated: February 2, 2026
Version: 1.0.0
