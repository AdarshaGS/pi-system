package com.investments.stocks.networth.service;

import com.investments.stocks.networth.data.AssetLiabilityTemplateDTO;
import com.investments.stocks.networth.data.NetWorthDTO;

public interface NetWorthReadService {
    NetWorthDTO getNetWorth(Long userId);

    AssetLiabilityTemplateDTO getEntityTemplates();
}
