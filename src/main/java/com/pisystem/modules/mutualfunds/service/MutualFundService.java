package com.investments.mutualfunds.service;

import java.util.List;
import com.investments.mutualfunds.data.MutualFundHolding;
import com.investments.mutualfunds.data.MutualFundInsights;
import com.investments.mutualfunds.data.MutualFundSummary;
import com.externalServices.mutualfund.dto.MFLatestNAVResponse;
import com.externalServices.mutualfund.dto.MFNAVHistoryResponse;
import com.externalServices.mutualfund.dto.MFSchemeListItem;
import com.externalServices.mutualfund.dto.MFSchemeSearchResult;

public interface MutualFundService {
    // Portfolio management APIs
    List<MutualFundHolding> getHoldings(Long userId);
    MutualFundSummary getSummary(Long userId);
    MutualFundInsights getInsights(Long userId);
    
    // External API - Scheme discovery
    List<MFSchemeSearchResult> searchSchemes(String query);
    List<MFSchemeListItem> listAllSchemes(Integer limit, Integer offset);
    
    // External API - NAV data
    MFLatestNAVResponse getLatestNAV(Long schemeCode);
    MFNAVHistoryResponse getNAVHistory(Long schemeCode);
    MFNAVHistoryResponse getNAVHistory(Long schemeCode, String startDate, String endDate);
}
