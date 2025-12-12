package com.savings.service;

import com.savings.data.SavingsAccount;
import com.savings.data.SavingsAccountDTO;


public interface SavingsAccountService {

    SavingsAccount createSavingsAccountDetails(SavingsAccount savingsAccount);

    SavingsAccountDTO retrieveSavingsAccountDetails(Long userId);

}
