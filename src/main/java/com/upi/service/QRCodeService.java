package com.upi.service;

import com.upi.model.UpiId;
import com.upi.repository.UpiIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {
    @Autowired
    private UpiIdRepository upiIdRepository;

    public Map<String, Object> scanQr(String qrData) {
        Map<String, Object> response = new HashMap<>();
        // For demo, assume qrData is UPI ID
        UpiId upiId = upiIdRepository.findByUpiId(qrData);
        if (upiId == null) {
            response.put("status", "failed");
            response.put("message", "UPI ID not found in QR");
            return response;
        }
        response.put("upiId", upiId.getUpiId());
        response.put("amount", 100.0); // In real app, parse amount from QR
        response.put("remarks", "Scanned QR");
        response.put("status", "success");
        return response;
    }

    public Map<String, Object> generateQR(String upiId, Double amount, String merchantName, String remarks) {
        Map<String, Object> response = new HashMap<>();

        // Validate UPI ID exists
        UpiId upi = upiIdRepository.findByUpiId(upiId);
        if (upi == null) {
            response.put("status", "failed");
            response.put("message", "UPI ID not found");
            return response;
        }

        // Generate UPI payment string (standard UPI format)
        // Format: upi://pay?pa=<UPI_ID>&pn=<NAME>&am=<AMOUNT>&tn=<NOTE>
        StringBuilder qrString = new StringBuilder("upi://pay?");
        qrString.append("pa=").append(upiId);

        if (merchantName != null && !merchantName.isEmpty()) {
            qrString.append("&pn=").append(merchantName.replace(" ", "%20"));
        }

        if (amount != null && amount > 0) {
            qrString.append("&am=").append(amount);
        }

        if (remarks != null && !remarks.isEmpty()) {
            qrString.append("&tn=").append(remarks.replace(" ", "%20"));
        }

        response.put("status", "success");
        response.put("qrData", qrString.toString());
        response.put("upiId", upiId);
        response.put("amount", amount);
        response.put("merchantName", merchantName);
        response.put("remarks", remarks);
        response.put("message", "QR code generated successfully");

        return response;
    }
}
