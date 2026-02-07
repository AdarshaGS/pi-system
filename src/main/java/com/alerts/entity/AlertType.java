package com.alerts.entity;

/**
 * Types of alerts supported by the system
 */
public enum AlertType {
    STOCK_PRICE,          // Price hits target or changes by percentage
    STOCK_VOLUME,         // Volume spike detection
    EMI_DUE,              // Loan EMI payment due
    POLICY_EXPIRY,        // Insurance policy expiring
    PREMIUM_DUE,          // Insurance premium payment due
    TAX_DEADLINE,         // Tax filing or payment deadline
    PORTFOLIO_DRIFT,      // Portfolio allocation drift
    NEGATIVE_RETURNS,     // Portfolio negative returns alert
    SECTOR_CONCENTRATION  // Sector concentration risk
}
