package com.pisystem.modules.stocks.networth.service;

import com.pisystem.modules.stocks.networth.data.AssetLiabilityTemplateDTO;
import com.pisystem.modules.stocks.networth.data.NetWorthDTO;

public interface NetWorthReadService {
    NetWorthDTO getNetWorth(Long userId);

    AssetLiabilityTemplateDTO getEntityTemplates();
}
