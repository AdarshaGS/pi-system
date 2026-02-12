package com.upi.controller;

import com.upi.service.UPITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/upi/transactions")
public class UPITransactionController {

    @Autowired
    private UPITransactionService upiTransactionService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMoney(@RequestBody Map<String, Object> payload) {
        String senderUpiId = (String) payload.get("senderUpiId");
        String receiverUpiId = (String) payload.get("receiverUpiId");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String pin = (String) payload.get("pin");
        String remarks = (String) payload.getOrDefault("remarks", "");

        Map<String, Object> result = upiTransactionService.sendMoney(senderUpiId, receiverUpiId, amount, pin, remarks);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestMoney(@RequestBody Map<String, Object> payload) {
        String requesterUpiId = (String) payload.get("requesterUpiId");
        String payerUpiId = (String) payload.get("payerUpiId");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String remarks = (String) payload.getOrDefault("remarks", "");

        Map<String, Object> result = upiTransactionService.requestMoney(requesterUpiId, payerUpiId, amount, remarks);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory(@RequestParam String upiId) {
        return ResponseEntity.ok(upiTransactionService.getTransactionHistory(upiId));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getTransactionStatus(@RequestParam Long transactionId) {
        return ResponseEntity.ok(upiTransactionService.getTransactionStatus(transactionId));
    }

    @GetMapping("/receipt")
    public ResponseEntity<?> getTransactionReceipt(@RequestParam Long transactionId) {
        return ResponseEntity.ok(upiTransactionService.getTransactionReceipt(transactionId));
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, @RequestBody Map<String, Object> payload) {
        String pin = (String) payload.get("pin");
        Map<String, Object> result = upiTransactionService.acceptRequest(requestId, pin);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId) {
        Map<String, Object> result = upiTransactionService.rejectRequest(requestId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<?> getPendingRequests(@RequestParam String upiId) {
        return ResponseEntity.ok(upiTransactionService.getPendingRequests(upiId));
    }
}
