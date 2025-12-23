package com.tax.service;

import com.tax.data.Tax;
import com.tax.data.TaxDTO;
import java.math.BigDecimal;

public interface TaxService {
    
    TaxDTO createTaxDetails(Tax tax);

    TaxDTO getTaxDetailsByUserId(Long userId, String financialYear);

    BigDecimal calculateTotalTaxLiability(Long userId);

    BigDecimal getOutstandingTaxLiability(Long userId);
}