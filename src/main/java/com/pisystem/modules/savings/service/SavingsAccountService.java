package com.pisystem.modules.savings.service;

import java.util.List;

import com.pisystem.modules.savings.data.SavingsAccount;
import com.pisystem.modules.savings.data.SavingsAccountDTO;

public interface SavingsAccountService {

    SavingsAccountDTO createSavingsAccountDetails(SavingsAccount savingsAccount);

    SavingsAccountDTO retrieveSavingsAccountDetails(Long userId);

    List<SavingsAccountDTO> getAllSavingsAccounts(Long userId);

    SavingsAccountDTO getSavingsAccountById(Long id, Long userId);

    SavingsAccountDTO updateSavingsAccount(Long id, Long userId, SavingsAccount savingsAccount);

    void deleteSavingsAccount(Long id, Long userId);

}
