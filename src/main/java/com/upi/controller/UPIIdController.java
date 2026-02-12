package com.upi.controller;

import com.upi.service.UPIIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/upi/ids")
public class UPIIdController {

    @Autowired
    private UPIIdService upiIdService;

    @PostMapping
    public ResponseEntity<?> createUpiId(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String upiId = (String) payload.get("upiId");
        Map<String, Object> result = upiIdService.createUpiId(userId, upiId);
        return ResponseEntity.ok(result);
    }
}
