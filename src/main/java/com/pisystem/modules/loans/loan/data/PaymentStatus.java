package com.loan.data;

public enum PaymentStatus {
    PAID,      // Payment completed
    PENDING,   // Payment due but not yet paid
    MISSED,    // Payment missed/overdue
    SCHEDULED  // Future scheduled payment
}
