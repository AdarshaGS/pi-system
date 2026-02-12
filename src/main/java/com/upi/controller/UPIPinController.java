package com.upi.controller;

import com.upi.service.UPIPinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/upi/pin")
public class UPIPinController {

    @Autowired
    private UPIPinService upiPinService;

    @PostMapping("/create")
    public ResponseEntity<?> createPin(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String newPin = (String) payload.get("newPin");
        return ResponseEntity.ok(upiPinService.createPin(userId, newPin));
    }

    @PostMapping("/change")
    public ResponseEntity<?> changePin(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String oldPin = (String) payload.get("oldPin");
        String newPin = (String) payload.get("newPin");
        return ResponseEntity.ok(upiPinService.changePin(userId, oldPin, newPin));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPin(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String newPin = (String) payload.get("newPin");
        return ResponseEntity.ok(upiPinService.resetPin(userId, newPin));
    }
}
