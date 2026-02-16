package com.upi.service;

import com.upi.dto.QRGenerateRequest;
import com.upi.dto.QRScanRequest;
import com.upi.model.UpiId;
import com.upi.dto.QRGenerateResponse;
import com.upi.dto.QRScanResponse;
import com.upi.repository.UpiIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.upi.exception.UpiIdNotFoundException;

@Service
@Transactional
public class QRCodeService {
    @Autowired
    private UpiIdRepository upiIdRepository;

    /**
     * Scans QR code data and extracts UPI payment information.
     * @param request The QR scan request containing the QR data.
     * @return A response with extracted UPI ID, amount, and remarks.
     * @throws UpiIdNotFoundException if the UPI ID embedded in the QR data is not found.
     */
    public QRScanResponse scanQr(QRScanRequest request) {
        String qrData = request.getQrData();
        
        // For demo, assume qrData directly contains the UPI ID.
        // In a real application, this would involve parsing a UPI deep link (e.g., upi://pay?pa=...)
        // and extracting parameters like pa (payee address), am (amount), tn (transaction note), etc.
        UpiId upiId = upiIdRepository.findByUpiId(qrData)
                .orElseThrow(() -> new UpiIdNotFoundException("UPI ID '" + qrData + "' not found in QR data."));
        
        return QRScanResponse.builder()
                .upiId(upiId.getUpiId())
                .amount(BigDecimal.valueOf(100.0)) // Placeholder: In a real app, parse actual amount from QR data
                .remarks("Scanned QR")
                .status("success")
                .message("QR code scanned successfully.")
                .build();
    }

    /**
     * Generates a UPI QR code string based on the provided details.
     * @param request The request containing UPI ID, amount, merchant name, and remarks.
     * @return A response with the generated QR data string.
     * @throws UpiIdNotFoundException if the specified UPI ID does not exist.
     */
    public QRGenerateResponse generateQR(QRGenerateRequest request) {
        String upiId = request.getUpiId();
        BigDecimal amount = request.getAmount() != null ? BigDecimal.valueOf(request.getAmount()) : null;
        String merchantName = request.getMerchantName();
        String remarks = request.getRemarks();

        upiIdRepository.findByUpiId(upiId)
                .orElseThrow(() -> new UpiIdNotFoundException("UPI ID " + upiId + " not found."));

        // Generate UPI payment string (standard UPI format)
        // Format: upi://pay?pa=<UPI_ID>&pn=<NAME>&am=<AMOUNT>&tn=<NOTE>
        StringBuilder qrString = new StringBuilder("upi://pay?");
        qrString.append("pa=").append(upiId);

        if (merchantName != null && !merchantName.isEmpty()) {
            qrString.append("&pn=").append(merchantName.replace(" ", "%20"));
        }

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            qrString.append("&am=").append(amount);
        }

        if (remarks != null && !remarks.isEmpty()) {
            qrString.append("&tn=").append(remarks.replace(" ", "%20"));
        }
        
        return QRGenerateResponse.builder()
                .status("success")
                .message("QR code generated successfully.")
                .qrData(qrString.toString())
                .upiId(upiId)
                .amount(amount)
                .merchantName(merchantName)
                .remarks(remarks)
                .build();
    }
}
