# Scheduler Jobs / Scheduled Tasks

This document outlines all the scheduled background jobs recommended for the PI SYSTEM to maintain data freshness, system health, and optimal user experience.

---

## 📅 Recommended Scheduler Jobs

### **1. Stock Price Update Job**
**Frequency:** Every 15-30 minutes during market hours, once after market close  
**Cron Expression:** `0 */30 9-15 * * MON-FRI` (every 30 min, 9 AM - 3 PM, weekdays)  
**Post-Market:** `0 0 18 * * MON-FRI` (daily at 6:00 PM)

**Purpose:**
- Update real-time stock prices for all tracked securities
- Your portfolio analysis depends on current stock prices
- Batch update reduces API calls vs on-demand fetches
- Leverage existing price caching and fallback mechanisms
- Ensure accurate portfolio valuations for P&L calculations

**Implementation:**
```java
@Scheduled(cron = "0 */30 9-15 * * MON-FRI")
public void updateStockPricesDuringMarketHours() {
    // Batch update all user-tracked stocks
}

@Scheduled(cron = "0 0 18 * * MON-FRI")
public void endOfDayPriceUpdate() {
    // Final price update after market close
}
```

---

### **2. Portfolio Revaluation & Metrics Job**
**Frequency:** Daily at market close  
**Cron Expression:** `0 30 18 * * MON-FRI` (6:30 PM daily, after market close)

**Purpose:**
- Recalculate XIRR for all user portfolios automatically
- Update diversification scores and sector allocations
- Refresh risk metrics and portfolio insights
- Pre-compute heavy calculations for faster API responses
- Track daily portfolio value history for trend analysis
- Generate performance snapshots

**Benefits:**
- Reduces real-time computation load
- Faster API response times
- Historical data for charting and analytics

---

### **3. User Activity & Session Cleanup Job**
**Frequency:** Daily at midnight  
**Cron Expression:** `0 0 0 * * *` (12:00 AM daily)

**Purpose:**
- Clean up expired JWT tokens from audit tables
- Track daily/weekly/monthly active users
- Archive old user activity logs
- Maintain user login/logout statistics for admin dashboard
- Identify inactive accounts for potential re-engagement
- Generate daily active user metrics

**Data Retention:**
- Keep session data for 90 days
- Summarize older data before deletion

---

### **4. Database Maintenance & Optimization Job**
**Frequency:** Weekly  
**Cron Expression:** `0 0 2 * * SUN` (Sunday 2:00 AM)

**Purpose:**
- Vacuum/analyze database tables (PostgreSQL)
- Clean up old audit logs (older than 90 days)
- Archive historical transactional data
- Optimize and rebuild indexes
- Update database statistics
- Generate database health reports
- Check for table bloat

**Actions:**
- Run `VACUUM ANALYZE` on large tables
- Check index usage and remove unused indexes
- Archive old audit records

---

### **5. Loan EMI Reminder & Status Update Job**
**Frequency:** Daily morning  
**Cron Expression:** `0 0 9 * * *` (9:00 AM daily)

**Purpose:**
- Calculate upcoming EMI due dates (7/15/30 days ahead)
- Update loan outstanding amounts based on payments
- Track prepayments and recalculate remaining tenure
- Update user net worth based on loan balance changes
- Identify overdue payments
- Send notifications for pending payments (future feature)

**Impact:**
- Keeps loan data current
- Accurate net worth calculations
- Proactive user engagement

---

### **6. FD/RD Maturity Tracking Job**
**Frequency:** Daily morning  
**Cron Expression:** `0 0 8 * * *` (8:00 AM daily)

**Purpose:**
- Identify Fixed Deposits nearing maturity (7/15/30 days alerts)
- Update Recurring Deposit accumulated values with interest
- Calculate interest earned to date
- Update maturity amounts
- Send maturity notifications (future feature)
- Auto-update net worth calculations
- Flag matured deposits for user action

**Features:**
- Early maturity alerts
- Interest accrual tracking
- Post-maturity status updates

---

### **7. External API Health Check Job**
**Frequency:** Every 5 minutes  
**Cron Expression:** `0 */5 * * * *` (every 5 minutes)

**Purpose:**
- Monitor IndianAPI and other stock data provider health
- Track API success/failure rates
- Log response times and detect downtime
- Switch to fallback providers if primary fails
- Alert admins on prolonged outages
- Maintain API performance dashboard metrics
- Track rate limit consumption

**Monitoring Metrics:**
- Response time (p50, p95, p99)
- Success rate (%)
- Error rate by error type
- Uptime percentage
- Rate limit usage

---

### **8. Audit Log Archival Job**
**Frequency:** Monthly  
**Cron Expression:** `0 0 1 1 * *` (1st day of month at 1:00 AM)

**Purpose:**
- Archive audit logs older than 6 months
- Compress and move to cold storage or separate archive table
- Maintain audit trail for compliance requirements
- Free up primary database space
- Generate monthly audit summary reports for admins
- Ensure regulatory compliance

**Retention Policy:**
- Hot storage: Last 6 months
- Archive storage: 6 months to 7 years
- Critical audit events: Never delete

---

### **9. User Notification Digest Job** *(Future Feature)*
**Frequency:** Weekly  
**Cron Expression:** `0 0 8 * * MON` (Monday 8:00 AM)

**Purpose:**
- Send weekly portfolio performance summaries via email
- Alert on significant P&L changes (±10% or more)
- Notify about upcoming FD/RD maturities
- Share personalized investment insights and recommendations
- Re-engage inactive users with portfolio updates
- Highlight critical risk alerts

**Notification Types:**
- Portfolio performance digest
- Maturity alerts
- Risk warnings
- Milestone achievements

---

### **10. Data Backup & Export Job**
**Frequency:** Daily  
**Cron Expression:** `0 0 3 * * *` (3:00 AM daily)

**Purpose:**
- Automated database backups (full + incremental)
- Export critical user data for GDPR compliance
- Store backups in S3/cloud storage with encryption
- Test restore procedures periodically (weekly)
- Ensure disaster recovery readiness
- Maintain backup retention policy

**Backup Strategy:**
- Daily incremental backups
- Weekly full backups
- Monthly archive backups
- 30-day retention for daily backups

---

### **11. Sector & Market Data Refresh Job**
**Frequency:** Weekly  
**Cron Expression:** `0 0 20 * * SUN` (Sunday 8:00 PM)

**Purpose:**
- Update sector classifications and mappings
- Refresh market cap categories (Large/Mid/Small cap)
- Update benchmark indices (NIFTY, SENSEX, etc.)
- Sync new stock listings from exchanges
- Remove delisted stocks from reference data
- Update industry trends and sector weights

**Data Sources:**
- NSE/BSE listing data
- Sector classification databases
- Market cap updates

---

### **12. Third-Party API Audit Cleanup Job**
**Frequency:** Daily  
**Cron Expression:** `0 0 2 * * *` (2:00 AM daily)

**Purpose:**
- Clean up successful ThirdPartyAudit entries older than 30 days
- Retain failed API requests for longer analysis (90 days)
- Generate daily/weekly API usage reports
- Track API costs and rate limit consumption
- Identify problematic API patterns
- Monitor third-party service performance trends

**Retention:**
- Successful calls: 30 days
- Failed calls: 90 days
- Aggregated metrics: Forever

---

### **13. Insurance Premium Reminder Job**
**Frequency:** Daily  
**Cron Expression:** `0 0 10 * * *` (10:00 AM daily)

**Purpose:**
- Track insurance premium due dates
- Send upcoming renewal reminders (30/15/7/1 days before)
- Update policy status (active/lapsed/expired)
- Calculate total insurance coverage for net worth
- Impact net worth calculations when policies lapse
- Generate insurance portfolio summary

**Alerts:**
- Premium due reminders
- Policy lapse warnings
- Coverage gap notifications

---

### **14. Stale Data Detection Job**
**Frequency:** Hourly  
**Cron Expression:** `0 0 * * * *` (top of every hour)

**Purpose:**
- Identify stocks with outdated prices (>24 hours old)
- Mark portfolios needing urgent revaluation
- Trigger emergency price updates for stale data
- Alert users about data freshness issues in dashboard
- Update DataFreshness metadata
- Flag external API issues

**Thresholds:**
- Stale: Price older than 24 hours
- Critical: Price older than 3 days
- Emergency: No price available

---

### **15. Report Generation Job** *(Future Feature)*
**Frequency:** Monthly  
**Cron Expression:** `0 0 23 L * *` (Last day of month at 11:00 PM)

**Purpose:**
- Generate monthly portfolio performance reports (PDF)
- Create tax reports for realized gains (annual)
- Produce investment summary statements
- Export user data for tax filing
- Archive historical snapshots for compliance
- Generate wealth progression reports

**Report Types:**
- Monthly performance summary
- Quarterly portfolio review
- Annual tax statements
- Capital gains reports

---

## 🎯 Implementation Priority

### **Phase 1: Critical (Immediate Implementation)**
1. ✅ **Stock Price Update Job** - Core functionality for accurate valuations
2. ✅ **External API Health Check Job** - System reliability and monitoring
3. ✅ **Database Maintenance Job** - Performance and stability

### **Phase 2: High Priority (Next Sprint)**
4. ✅ **Portfolio Revaluation Job** - Performance optimization
5. ✅ **User Activity Cleanup Job** - Data hygiene
6. ✅ **Third-Party API Audit Cleanup Job** - Audit data management
7. ✅ **Stale Data Detection Job** - Data quality assurance

### **Phase 3: Medium Priority (Within 2 Months)**
8. ⏳ **Loan EMI Tracking Job** - Feature enhancement
9. ⏳ **FD/RD Maturity Job** - User engagement
10. ⏳ **Insurance Premium Job** - User engagement
11. ⏳ **Sector & Market Data Refresh Job** - Reference data accuracy

### **Phase 4: Future Enhancements**
12. 🔮 **User Notification Digest Job** - Requires email infrastructure
13. 🔮 **Report Generation Job** - Advanced feature
14. 🔮 **Data Backup Job** - DevOps infrastructure setup
15. 🔮 **Audit Log Archival Job** - Long-term data management

---

## 🛠️ Technical Implementation Guide

### Spring Boot @Scheduled Annotation

```java
package com.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StockPriceUpdateScheduler {
    
    private final StockPriceService stockPriceService;
    
    public StockPriceUpdateScheduler(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }
    
    // Every 30 minutes during market hours (9 AM - 3 PM, Mon-Fri)
    @Scheduled(cron = "0 */30 9-15 * * MON-FRI", zone = "Asia/Kolkata")
    public void updateStockPricesDuringMarketHours() {
        log.info("Starting scheduled stock price update during market hours");
        try {
            stockPriceService.batchUpdateAllTrackedStocks();
            log.info("Successfully completed stock price update");
        } catch (Exception e) {
            log.error("Error during scheduled stock price update", e);
        }
    }
    
    // Daily at 6:00 PM IST (after market close)
    @Scheduled(cron = "0 0 18 * * MON-FRI", zone = "Asia/Kolkata")
    public void endOfDayPriceUpdate() {
        log.info("Starting end-of-day stock price update");
        try {
            stockPriceService.finalizeEndOfDayPrices();
            log.info("Successfully completed end-of-day price update");
        } catch (Exception e) {
            log.error("Error during end-of-day price update", e);
        }
    }
}
```

### Enable Scheduling in Spring Boot

Add to your main application class or configuration:

```java
@SpringBootApplication
@EnableScheduling  // Enable scheduling support
public class PiSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(PiSystemApplication.class, args);
    }
}
```

### Configuration Properties

Add to `application.yml`:

```yaml
spring:
  task:
    scheduling:
      pool:
        size: 10  # Thread pool size for scheduled tasks
      thread-name-prefix: scheduler-
```

### Cron Expression Reference

```
# Format: second minute hour day month weekday
# Examples:
0 0 * * * *        # Every hour at minute 0
0 */30 * * * *     # Every 30 minutes
0 0 9 * * MON-FRI  # Every weekday at 9 AM
0 0 2 * * SUN      # Every Sunday at 2 AM
0 0 1 1 * *        # First day of every month at 1 AM
```

---

## 📊 Monitoring & Observability

### Key Metrics to Track
- Job execution time
- Job success/failure rate
- Job last execution timestamp
- Job queue length (if using async)
- Resource utilization during jobs

### Logging Best Practices
```java
@Slf4j
public class ExampleScheduler {
    
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledJob() {
        long startTime = System.currentTimeMillis();
        log.info("Starting scheduled job: {}", "JobName");
        
        try {
            // Job logic here
            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully completed job in {}ms", duration);
        } catch (Exception e) {
            log.error("Job failed with error", e);
            // Alert or metrics
        }
    }
}
```

---

## 🔐 Security Considerations

1. **Job Authentication**: Scheduled jobs run with system privileges - ensure proper security context
2. **Rate Limiting**: Respect external API rate limits in scheduled jobs
3. **Data Privacy**: Handle user data securely during batch operations
4. **Error Handling**: Graceful degradation on failures
5. **Audit Trail**: Log all scheduled job executions

---

## 🚀 Performance Optimization

1. **Batch Processing**: Process records in batches (e.g., 100 users at a time)
2. **Async Execution**: Use `@Async` for long-running jobs
3. **Database Connection Pooling**: Ensure adequate pool size
4. **Query Optimization**: Use indexes and efficient queries
5. **Caching**: Cache reference data during job execution
6. **Timeout Configuration**: Set appropriate timeouts

---

## 📝 Testing Scheduled Jobs

```java
@SpringBootTest
class StockPriceUpdateSchedulerTest {
    
    @Autowired
    private StockPriceUpdateScheduler scheduler;
    
    @Test
    void testEndOfDayPriceUpdate() {
        // Manually invoke scheduled method for testing
        scheduler.endOfDayPriceUpdate();
        
        // Verify expected behavior
        // Assert conditions
    }
}
```

---

## 🎯 Success Criteria

- ✅ All critical jobs (Phase 1) implemented and tested
- ✅ Jobs run reliably without failures
- ✅ Performance metrics monitored and optimized
- ✅ Error handling and alerting configured
- ✅ Data freshness improved across the system
- ✅ User experience enhanced with up-to-date data

---

**Last Updated:** 31 January 2026  
**Version:** 1.0  
**Status:** Planning & Design Phase
