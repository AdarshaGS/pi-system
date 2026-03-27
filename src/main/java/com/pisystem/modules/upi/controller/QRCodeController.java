package com.pisystem.modules.upi.controller;

import com.pisystem.modules.upi.dto.QRGenerateRequest;
import com.pisystem.modules.upi.dto.QRScanRequest;
import com.pisystem.modules.upi.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/upi/qr")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    @PostMapping("/scan")
    public ResponseEntity<?> scanQr(@RequestBody QRScanRequest request) {
        return ResponseEntity.ok(qrCodeService.scanQr(request));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateQr(@RequestBody QRGenerateRequest request) {
        return ResponseEntity.ok(qrCodeService.generateQR(request));
    }
}
