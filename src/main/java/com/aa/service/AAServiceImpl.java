package com.aa.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aa.adapter.AccountAggregatorAdapter;
import com.aa.data.ConsentRequest;
import com.aa.data.ConsentResponse;
import com.aa.data.ConsentStatusResponse;
import com.aa.data.EncryptedFIPayload;
import com.aa.data.FIRequest;

@Service
public class AAServiceImpl implements AAService {

    private final AccountAggregatorAdapter accountAggregatorAdapter;

    public AAServiceImpl(@Qualifier("mockAA") AccountAggregatorAdapter accountAggregatorAdapter) {
        this.accountAggregatorAdapter = accountAggregatorAdapter;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, String>> getConsentTemplates() {
        return accountAggregatorAdapter.getConsentTemplates();
    }

    @Override
    @Transactional
    public ConsentResponse createConsent(ConsentRequest request) {
        return accountAggregatorAdapter.createConsent(request);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsentStatusResponse getConsentStatus(String consentId) {
        return accountAggregatorAdapter.getConsentStatus(consentId);
    }

    @Override
    @Transactional(readOnly = true)
    public EncryptedFIPayload fetchFinancialInformation(FIRequest request) {
        return accountAggregatorAdapter.fetchFinancialInformation(request);
    }

    @Override
    @Transactional
    public void revokeConsent(String consentId) {
        accountAggregatorAdapter.revokeConsent(consentId);
    }

}
