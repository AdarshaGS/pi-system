package com.upi.controller;

import com.upi.dto.PinRequest;
import com.upi.dto.UPICollectRequest;
import com.upi.dto.UPITransactionRequest;
import com.upi.dto.UPITransactionResponse;
import com.upi.service.UPITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/upi/transactions")
public class UPITransactionController {

    @Autowired
    private UPITransactionService upiTransactionService;

    @PostMapping("/p2p/send")
    public ResponseEntity<?> sendMoneyP2P(@RequestBody UPITransactionRequest request) {
        UPITransactionResponse result = upiTransactionService.sendMoney(request, "P2P");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/p2m/send")
    public ResponseEntity<?> sendMoneyP2M(@RequestBody UPITransactionRequest request) {
        UPITransactionResponse result = upiTransactionService.sendMoney(request, "P2M");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/p2p/request")
    public ResponseEntity<?> requestMoneyP2P(@RequestBody UPICollectRequest request) {
        UPITransactionResponse result = upiTransactionService.requestMoney(request, "P2P");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/p2m/request")
    public ResponseEntity<?> requestMoneyP2M(@RequestBody UPICollectRequest request) {
        UPITransactionResponse result = upiTransactionService.requestMoney(request, "P2M");
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
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, @RequestBody PinRequest request) {
        UPITransactionResponse result = upiTransactionService.acceptRequest(requestId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId) {
        UPITransactionResponse result = upiTransactionService.rejectRequest(requestId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<?> getPendingRequests(@RequestParam String upiId) {
        return ResponseEntity.ok(upiTransactionService.getPendingRequests(upiId));
    }
}
