package com.upi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.upi.model.BankAccount;
import com.upi.repository.BankAccountRepository;
import com.users.data.Users;
import com.users.repo.UsersRepository;

@Service
public class BankAccountService {
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private UsersRepository userRepository;

    public Map<String, Object> linkBankAccount(String userId, String accountNumber, String ifscCode, String bankName) {
        Map<String, Object> response = new HashMap<>();
        Users user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null) {
            response.put("status", "failed");
            response.put("message", "User not found");
            return response;
        }
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setAccountNumber(accountNumber);
        account.setIfscCode(ifscCode);
        account.setBankName(bankName);
        account.setIsPrimary(false);
        bankAccountRepository.save(account);
        response.put("bankAccountId", account.getId());
        response.put("status", "linked");
        return response;
    }

    public Map<String, Object> getBalance(String accountId) {
        Map<String, Object> response = new HashMap<>();
        BankAccount account = bankAccountRepository.findById(Long.parseLong(accountId)).orElse(null);
        if (account == null) {
            response.put("status", "failed");
            response.put("message", "Account not found");
            return response;
        }
        // For demo, return static balance
        response.put("accountId", accountId);
        response.put("balance", 10000.00);
        return response;
    }
}
