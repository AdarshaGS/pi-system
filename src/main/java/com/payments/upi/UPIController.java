package com.payments.upi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/upi")
public class UPIController {

    @Autowired
    private UPIService upiService;

    @PostMapping("/p2p/initiate")
    public ResponseEntity<String> initiatePaymentP2P(@RequestBody UPIRequest request) {
        String txnId = upiService.initiatePayment(request, "P2P");
        return ResponseEntity.ok("P2P UPI payment initiated. Transaction ID: " + txnId);
    }

    @PostMapping("/p2m/initiate")
    public ResponseEntity<String> initiatePaymentP2M(@RequestBody UPIRequest request) {
        String txnId = upiService.initiatePayment(request, "P2M");
        return ResponseEntity.ok("UPI payment initiated. Transaction ID: " + txnId);
    }

    @GetMapping("/status/{transactionId}")
    public ResponseEntity<String> getStatus(@PathVariable String transactionId) {
        String status = upiService.getUPIStatus(transactionId);
        return ResponseEntity.ok("Status: " + status);
    }
}
