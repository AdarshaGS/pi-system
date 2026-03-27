package com.pisystem.modules.mutualfunds.service;

import java.util.List;
import com.pisystem.modules.mutualfunds.data.MutualFundHolding;

public interface MutualFundFetchService {
    List<MutualFundHolding> fetchPortfolio(Long userId);
}
