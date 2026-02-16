package com.upi.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.upi.dto.BankAccountLinkRequest;
import com.upi.model.BankAccount;
import com.upi.repository.BankAccountRepository;
import com.users.data.Users;
import com.users.exception.UserNotFoundException;
import com.users.repo.UsersRepository;
import com.upi.dto.BankAccountLinkResponse;
import com.upi.dto.BankAccountBalanceResponse;
import com.upi.exception.BankAccountNotFoundException;

@Service
@Transactional
public class BankAccountService {
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private UsersRepository userRepository;

    /**
     * Links a new bank account to a user.
     * @param request The request containing bank account details and user ID.
     * @return A response indicating the status of the bank account linking.
     * @throws UserNotFoundException if the specified user does not exist.
     */
    public BankAccountLinkResponse linkBankAccount(BankAccountLinkRequest request) {
        String userId = request.getUserId();
        String accountNumber = request.getAccountNumber();
        String ifscCode = request.getIfscCode();
        String bankName = request.getBankName();
        BigDecimal balance = request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO;

        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));

        BankAccount account = BankAccount.builder()
                .user(user)
                .accountNumber(accountNumber)
                .ifscCode(ifscCode)
                .bankName(bankName)
                .balance(balance) // Initialize balance for new account
                .build();

        bankAccountRepository.save(account);
        
        return BankAccountLinkResponse.builder()
                .bankAccountId(account.getId())
                .status("linked")
                .message("Bank account linked successfully.")
                .build();
    }

    /**
     * Retrieves the balance for a given bank account.
     * @param accountId The ID of the bank account.
     * @return A response containing the account balance.
     * @throws BankAccountNotFoundException if the specified bank account does not exist.
     */
    public BankAccountBalanceResponse getBalance(String accountId) {
        // Assuming BankAccount model's balance field is BigDecimal
        BankAccount account = bankAccountRepository.findById(Long.parseLong(accountId))
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account with ID " + accountId + " not found."));
        
        return BankAccountBalanceResponse.builder()
                .balance(account.getBalance())
                .status("success")
                .message("Balance retrieved successfully.")
                .build();
    }
}
