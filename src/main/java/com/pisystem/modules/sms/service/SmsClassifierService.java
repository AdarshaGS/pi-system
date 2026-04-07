package com.pisystem.modules.sms.service;

import com.pisystem.modules.sms.data.SmsClassificationResult;

/**
 * Contract for the SMS classification engine.
 * Determines whether a raw SMS is a financial transaction, promotional,
 * OTP, service message, or unknown.
 */
public interface SmsClassifierService {

    /**
     * Classify a single raw SMS string.
     *
     * @param rawSms the original SMS body text
     * @return classification result with type, confidence, and reason
     */
    SmsClassificationResult classify(String rawSms);
}
