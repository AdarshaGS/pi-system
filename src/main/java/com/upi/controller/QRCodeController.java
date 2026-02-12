package com.upi.controller;

import com.upi.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/upi/qr")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    @PostMapping("/scan")
    public ResponseEntity<?> scanQr(@RequestBody Map<String, Object> payload) {
        String qrData = (String) payload.get("qrData");
        return ResponseEntity.ok(qrCodeService.scanQr(qrData));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateQr(@RequestBody Map<String, Object> payload) {
        String upiId = (String) payload.get("upiId");
        Double amount = payload.get("amount") != null ? Double.parseDouble(payload.get("amount").toString()) : null;
        String merchantName = (String) payload.get("merchantName");
        String remarks = (String) payload.get("remarks");
        return ResponseEntity.ok(qrCodeService.generateQR(upiId, amount, merchantName, remarks));
    }
}
