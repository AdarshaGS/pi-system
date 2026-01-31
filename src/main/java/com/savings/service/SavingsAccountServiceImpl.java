package com.savings.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.security.AuthenticationHelper;
import com.savings.data.SavingsAccount;
import com.savings.data.SavingsAccountDTO;
import com.savings.repo.SavingsAccountRepository;
import com.savings.exception.DuplicateSavingsEntityException;
import com.savings.exception.SavingsEntityNotFoundException;

@Service
public class SavingsAccountServiceImpl implements SavingsAccountService {

    private final SavingsAccountRepository repository;
    private final AuthenticationHelper authenticationHelper;

    public SavingsAccountServiceImpl(final SavingsAccountRepository repository,
            final AuthenticationHelper authenticationHelper) {
        this.repository = repository;
        this.authenticationHelper = authenticationHelper;
    }

    @Override
    @Transactional
    public SavingsAccountDTO createSavingsAccountDetails(SavingsAccount savingsAccount) {
        authenticationHelper.validateUserAccess(savingsAccount.getUserId());
        try {
            validateSavingsAccountDetails(savingsAccount);
            this.repository.save(savingsAccount);
            return convertToDTO(savingsAccount);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSavingsEntityException("Savings Account", savingsAccount.getBankName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsAccountDTO retrieveSavingsAccountDetails(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        SavingsAccount savingsAccount = this.repository.findOneByUserId(userId);

        if (savingsAccount == null) {
            throw new SavingsEntityNotFoundException("Savings Account", userId, "userId");
        }

        return convertToDTO(savingsAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsAccountDTO> getAllSavingsAccounts(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return repository.findAllByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsAccountDTO getSavingsAccountById(Long id, Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return repository.findByIdAndUserId(id, userId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new SavingsEntityNotFoundException("Savings Account", id));
    }

    @Override
    @Transactional
    public SavingsAccountDTO updateSavingsAccount(Long id, Long userId, SavingsAccount savingsAccount) {
        authenticationHelper.validateUserAccess(userId);
        SavingsAccount existing = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SavingsEntityNotFoundException("Savings Account", id));

        // Update fields
        existing.setAccountHolderName(savingsAccount.getAccountHolderName());
        existing.setBankName(savingsAccount.getBankName());
        existing.setAmount(savingsAccount.getAmount());

        try {
            SavingsAccount updated = repository.save(existing);
            return convertToDTO(updated);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSavingsEntityException("Savings Account", savingsAccount.getBankName());
        }
    }

    @Override
    @Transactional
    public void deleteSavingsAccount(Long id, Long userId) {
        authenticationHelper.validateUserAccess(userId);
        SavingsAccount account = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SavingsEntityNotFoundException("Savings Account", id));
        repository.delete(account);
    }

    private SavingsAccountDTO convertToDTO(SavingsAccount account) {
        return SavingsAccountDTO.builder()
                .Id(account.getId())
                .accountHolderName(account.getAccountHolderName())
                .bankName(account.getBankName())
                .amount(account.getAmount())
                .build();
    }

    private void validateSavingsAccountDetails(SavingsAccount savingsAccount) {
        if (savingsAccount.getAccountHolderName() == null || savingsAccount.getAccountHolderName().isEmpty()) {
            throw new IllegalArgumentException("Account holder name cannot be null or empty");
        }
        if (savingsAccount.getBankName() == null || savingsAccount.getBankName().isEmpty()) {
            throw new IllegalArgumentException("Bank name cannot be null or empty");
        }
        if (savingsAccount.getAmount() == null || savingsAccount.getAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }

        if (savingsAccount.getId()!=null && repository.findByIdAndUserId(savingsAccount.getId(),savingsAccount.getUserId()).isPresent() ||
                    repository.findAllByUserId(savingsAccount.getUserId()).stream()
                            .anyMatch(a -> a.getBankName().equalsIgnoreCase(savingsAccount.getBankName()))) {
                throw new DuplicateSavingsEntityException("Savings Account", savingsAccount.getBankName());
            }
    }

}
