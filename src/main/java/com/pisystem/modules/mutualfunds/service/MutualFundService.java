package com.pisystem.modules.mutualfunds.service;

import java.util.List;
import com.pisystem.modules.mutualfunds.data.MutualFundHolding;
import com.pisystem.modules.mutualfunds.data.MutualFundInsights;
import com.pisystem.modules.mutualfunds.data.MutualFundSummary;
import com.pisystem.integrations.externalservices.mutualfund.dto.MFLatestNAVResponse;
import com.pisystem.integrations.externalservices.mutualfund.dto.MFNAVHistoryResponse;
import com.pisystem.integrations.externalservices.mutualfund.dto.MFSchemeListItem;
import com.pisystem.integrations.externalservices.mutualfund.dto.MFSchemeSearchResult;

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
