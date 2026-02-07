package com.alerts.entity;

/**
 * Types of notifications
 */
public enum NotificationType {
    INFO,               // General information
    SUCCESS,            // Success message
    WARNING,            // Warning message
    ERROR,              // Error message
    ALERT,              // Alert triggered
    REMINDER,           // Reminder notification
    LENDING_DUE,        // Lending payment due today
    LENDING_OVERDUE     // Lending payment overdue
}
