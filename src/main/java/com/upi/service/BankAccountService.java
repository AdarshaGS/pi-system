package com.upi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.upi.dto.BankAccountLinkRequest;
import com.upi.model.BankAccount;
import com.upi.repository.BankAccountRepository;
import com.users.data.Users;
import com.users.exception.UserNotFoundException;
import com.users.repo.UsersRepository;

@Service
public class BankAccountService {
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private UsersRepository userRepository;

    public Map<String, Object> linkBankAccount(BankAccountLinkRequest request) {
        Map<String, Object> response = new HashMap<>();
        String userId = request.getUserId();
        String accountNumber = request.getAccountNumber();
        String ifscCode = request.getIfscCode();
        String bankName = request.getBankName();

        Users user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null) {
            throw new UserNotFoundException();
        }
        BankAccount account = BankAccount.builder()
                .user(user)
                .accountNumber(accountNumber)
                .ifscCode(ifscCode)
                .bankName(bankName)
                .build();

        bankAccountRepository.save(account);
        response.put("bankAccountId", account.getId());
        response.put("status", "linked");
        return response;
    }

    public Map<String, Object> getBalance(String accountId) {
        Map<String, Object> response = new HashMap<>();
        BankAccount account = bankAccountRepository.findById(Long.parseLong(accountId)).orElse(null);
        if (account == null) {
            throw new UserNotFoundException();
        }
        response.put("balance", account.getBalance());
        return response;
    }
}
