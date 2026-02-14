package com.upi.controller;

import com.upi.dto.PinRequest;
import com.upi.service.UPIPinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/upi/pin")
public class UPIPinController {

    @Autowired
    private UPIPinService upiPinService;

    @PostMapping("/create")
    public ResponseEntity<?> createPin(@RequestBody PinRequest request) {
        return ResponseEntity.ok(upiPinService.createPin(request));
    }

    @PostMapping("/change")
    public ResponseEntity<?> changePin(@RequestBody PinRequest request) {
        return ResponseEntity.ok(upiPinService.changePin(request));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPin(@RequestBody PinRequest request) {
        return ResponseEntity.ok(upiPinService.resetPin(request));
    }
}
