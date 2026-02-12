package com.payments.upi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/upi")
public class UPIController {

    @Autowired
    private UPIService upiService;

    @PostMapping("/initiate")
    public ResponseEntity<String> initiatePayment(@RequestBody UPIRequest request) {
        String txnId = upiService.initiatePayment(request);
        return ResponseEntity.ok("UPI payment initiated. Transaction ID: " + txnId);
    }

    @GetMapping("/status/{transactionId}")
    public ResponseEntity<String> getStatus(@PathVariable String transactionId) {
        String status = upiService.getUPIStatus(transactionId);
        return ResponseEntity.ok("Status: " + status);
    }
}
