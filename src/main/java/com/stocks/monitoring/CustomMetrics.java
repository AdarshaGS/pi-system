package com.stocks.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for business operations
 */
@Component
public class CustomMetrics {

    private final Counter userRegistrationCounter;
    private final Counter userLoginCounter;
    private final Counter portfolioTransactionCounter;
    private final Counter budgetExceededCounter;
    private final Counter loanPaymentCounter;
    private final Counter insuranceClaimCounter;
    private final Timer databaseQueryTimer;
    private final Timer externalApiTimer;
    
    public CustomMetrics(MeterRegistry registry) {
        // User authentication metrics
        this.userRegistrationCounter = Counter.builder("user.registrations.total")
                .description("Total number of user registrations")
                .register(registry);
        
        this.userLoginCounter = Counter.builder("user.logins.total")
                .description("Total number of successful user logins")
                .register(registry);
        
        // Portfolio metrics
        this.portfolioTransactionCounter = Counter.builder("portfolio.transactions.total")
                .description("Total number of portfolio transactions")
                .tag("type", "all")
                .register(registry);
        
        // Budget metrics
        this.budgetExceededCounter = Counter.builder("budget.exceeded.total")
                .description("Number of times budget was exceeded")
                .register(registry);
        
        // Loan metrics
        this.loanPaymentCounter = Counter.builder("loan.payments.total")
                .description("Total number of loan payments recorded")
                .register(registry);
        
        // Insurance metrics
        this.insuranceClaimCounter = Counter.builder("insurance.claims.total")
                .description("Total number of insurance claims filed")
                .register(registry);
        
        // Performance metrics
        this.databaseQueryTimer = Timer.builder("database.query.duration")
                .description("Database query execution time")
                .register(registry);
        
        this.externalApiTimer = Timer.builder("external.api.call.duration")
                .description("External API call duration")
                .register(registry);
    }
    
    // Counter methods
    public void incrementUserRegistration() {
        userRegistrationCounter.increment();
    }
    
    public void incrementUserLogin() {
        userLoginCounter.increment();
    }
    
    public void incrementPortfolioTransaction() {
        portfolioTransactionCounter.increment();
    }
    
    public void incrementBudgetExceeded() {
        budgetExceededCounter.increment();
    }
    
    public void incrementLoanPayment() {
        loanPaymentCounter.increment();
    }
    
    public void incrementInsuranceClaim() {
        insuranceClaimCounter.increment();
    }
    
    // Timer methods
    public Timer.Sample startDatabaseTimer() {
        return Timer.start();
    }
    
    public void recordDatabaseQuery(Timer.Sample sample) {
        sample.stop(databaseQueryTimer);
    }
    
    public Timer.Sample startExternalApiTimer() {
        return Timer.start();
    }
    
    public void recordExternalApiCall(Timer.Sample sample) {
        sample.stop(externalApiTimer);
    }
}
