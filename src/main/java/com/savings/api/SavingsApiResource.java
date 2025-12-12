package com.savings.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.savings.data.SavingsAccount;
import com.savings.data.SavingsAccountDTO;
import com.savings.service.SavingsAccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("api/v1/savings-account")
@Tag(name = "Savings Account Management", description = "APIs for managing user savings accounts, FD/RD, and bank balances")
@AllArgsConstructor
public class SavingsApiResource {

    @Autowired
    private final SavingsAccountService savingsAccountService;

    @PostMapping
    @Operation(summary = "Create Savings Account Details", description = "Creates Savings Account Details for a user.")
    @ApiResponse(responseCode = "200", description = "Successfully Created Savings Account")
    public SavingsAccount postSavingsAccountDetails(@RequestBody SavingsAccount savingsAccount) {
        return this.savingsAccountService.createSavingsAccountDetails(savingsAccount);
    }

    @GetMapping()
    @Operation(summary = "Retrieve Savings Account Details", description = "Retrieves Savings Account Details for a user id.")
    @ApiResponse(responseCode = "200", description = "Fetched Details Successfully")
    public SavingsAccountDTO retrieveSavingsAccountDetails(@RequestParam("{userId}") final Long userId) {
        return this.savingsAccountService.retrieveSavingsAccountDetails(userId);
    }
}
