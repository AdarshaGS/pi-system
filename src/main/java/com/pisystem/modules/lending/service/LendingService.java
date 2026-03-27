package com.pisystem.modules.lending.service;

import java.util.List;

import com.pisystem.modules.lending.data.LendingDTO;
import com.pisystem.modules.lending.data.RepaymentDTO;

public interface LendingService {
    LendingDTO createLending(LendingDTO lendingDTO);

    List<LendingDTO> getUserLendings(Long userId);

    LendingDTO getLendingById(Long id);

    LendingDTO addRepayment(Long lendingId, RepaymentDTO repaymentDTO);

    LendingDTO updateLending(Long id, LendingDTO lendingDTO);

    LendingDTO closeLending(Long id);

    void sendPaymentReminder(Long lendingId);
}
