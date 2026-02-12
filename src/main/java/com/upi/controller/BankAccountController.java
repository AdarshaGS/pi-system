package com.upi.controller;

import com.upi.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/upi/bank")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @PostMapping("/link")
    public ResponseEntity<?> linkBankAccount(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String accountNumber = (String) payload.get("accountNumber");
        String ifscCode = (String) payload.get("ifscCode");
        String bankName = (String) payload.get("bankName");
        return ResponseEntity.ok(bankAccountService.linkBankAccount(userId, accountNumber, ifscCode, bankName));
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestParam String accountId) {
        return ResponseEntity.ok(bankAccountService.getBalance(accountId));
    }
}
