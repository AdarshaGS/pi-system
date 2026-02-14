package com.upi.controller;


import com.upi.dto.BankAccountLinkRequest;
import com.upi.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/upi/bank")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @PostMapping("/link")
    public ResponseEntity<?> linkBankAccount(@RequestBody BankAccountLinkRequest request) {
        return ResponseEntity.ok(bankAccountService.linkBankAccount(request));
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestParam String accountId) {
        return ResponseEntity.ok(bankAccountService.getBalance(accountId));
    }
}
