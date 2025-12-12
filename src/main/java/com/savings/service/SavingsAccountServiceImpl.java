package com.savings.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.savings.data.SavingsAccount;
import com.savings.data.SavingsAccountDTO;
import com.savings.repo.SavingsAccountRepository;

@Service
public class SavingsAccountServiceImpl implements SavingsAccountService {

    @Autowired
    private final SavingsAccountRepository repository;

    public SavingsAccountServiceImpl(final SavingsAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public SavingsAccount createSavingsAccountDetails(SavingsAccount savingsAccount) {
        this.repository.save(savingsAccount);
        return SavingsAccount.builder().Id(savingsAccount.getId()).build();
    }

    @Override
    public SavingsAccountDTO retrieveSavingsAccountDetails(Long userId) {
        SavingsAccount savingsAccount = this.repository.findOneByUserId(userId);

        if (savingsAccount == null) {
            throw new RuntimeException("Savings account not found for user ID: " + userId);
        }

        return SavingsAccountDTO.builder()
                .accountHolderName(savingsAccount.getAccountHolderName())
                .bankName(savingsAccount.getBankName())
                .amount(savingsAccount.getAmount())
                .build();
    }

}
