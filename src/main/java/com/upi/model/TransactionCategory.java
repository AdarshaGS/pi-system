package com.upi.model;

/**
 * Represents the category or nature of a UPI transaction.
 * This is distinct from `transactionType` which might denote P2P, P2M, etc.
 */
public enum TransactionCategory {
    SEND,
    RECEIVE,
    REQUEST,
    REFUND
}