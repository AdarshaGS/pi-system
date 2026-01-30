package com.savings.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.users.repo.UsersRepository;

import com.savings.data.SavingsAccount;
import com.savings.data.SavingsAccountDTO;
import com.savings.service.SavingsAccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/savings-accounts")
@Tag(name = "Savings Account Management", description = "APIs for managing user savings accounts, FD/RD, and bank balances")
@AllArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SavingsController {

    private final SavingsAccountService savingsAccountService;
    private final UsersRepository usersRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return null;
        return usersRepository.findByEmail(authentication.getName())
                .map(user -> user.getId())
                .orElse(null);
    }

    @PostMapping
    @Operation(summary = "Create Savings Account Details", description = "Creates Savings Account Details for a user.")
    @ApiResponse(responseCode = "201", description = "Successfully Created Savings Account")
    @PreAuthorize("@userSecurity.hasUserId(#savingsAccount.userId)")
    public ResponseEntity<SavingsAccountDTO> postSavingsAccountDetails(
            @Valid @RequestBody SavingsAccount savingsAccount) {
        SavingsAccountDTO created = this.savingsAccountService.createSavingsAccountDetails(savingsAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all Savings Accounts", description = "Retrieves all Savings Accounts for the current user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all savings accounts")
    public List<SavingsAccountDTO> getAllSavingsAccounts(
            @RequestParam(value = "userId", required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : getCurrentUserId();
        return this.savingsAccountService.getAllSavingsAccounts(targetUserId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Savings Account by ID", description = "Retrieves a specific Savings Account by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved Savings Account")
    public SavingsAccountDTO getSavingsAccountById(@PathVariable("id") Long id,
            @RequestParam(value = "userId", required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : getCurrentUserId();
        return this.savingsAccountService.getSavingsAccountById(id, targetUserId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Savings Account", description = "Updates an existing Savings Account")
    @ApiResponse(responseCode = "200", description = "Successfully updated Savings Account")
    public SavingsAccountDTO updateSavingsAccount(
            @PathVariable("id") Long id,
            @RequestParam(value = "userId", required = false) Long userId,
            @Valid @RequestBody SavingsAccount savingsAccount) {
        Long targetUserId = (userId != null) ? userId
                : (savingsAccount.getUserId() != null ? savingsAccount.getUserId() : getCurrentUserId());
        return this.savingsAccountService.updateSavingsAccount(id, targetUserId, savingsAccount);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Savings Account", description = "Deletes a Savings Account")
    @ApiResponse(responseCode = "204", description = "Successfully deleted Savings Account")
    public org.springframework.http.ResponseEntity<Void> deleteSavingsAccount(@PathVariable("id") Long id,
            @RequestParam(value = "userId", required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : getCurrentUserId();
        this.savingsAccountService.deleteSavingsAccount(id, targetUserId);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
