
package com.upi.service;

import com.upi.model.Transaction;
import com.upi.model.UpiId;
import com.upi.model.UpiPin;
import com.upi.model.BankAccount;
import com.upi.dto.UPITransactionRequest;
import com.upi.dto.UPICollectRequest;
import com.upi.dto.PinRequest;
import com.upi.repository.UpiIdRepository;
import com.upi.repository.UpiPinRepository;
import com.upi.repository.BankAccountRepository;
import com.upi.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class UPITransactionService {

    private static final BigDecimal P2M_FEE_RATE = new BigDecimal("0.015"); // 1.5% MDR

    @Autowired
    private UpiIdRepository upiIdRepository;
    @Autowired
    private UpiPinRepository upiPinRepository;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public Map<String, Object> sendMoney(UPITransactionRequest request, String type) {
        Map<String, Object> response = new HashMap<>();
        String senderUpiId = request.getSenderUpiId();
        String receiverUpiId = request.getReceiverUpiId();
        BigDecimal amount = request.getAmount();
        String pin = request.getPin();
        String remarks = request.getRemarks();

        // Validate sender UPI ID
        UpiId sender = upiIdRepository.findByUpiId(senderUpiId);
        if (sender == null) {
            response.put("status", "failed");
            response.put("message", "Sender UPI ID not found");
            return response;
        }
        // Validate receiver UPI ID
        UpiId receiver = upiIdRepository.findByUpiId(receiverUpiId);
        if (receiver == null) {
            response.put("status", "failed");
            response.put("message", "Receiver UPI ID not found");
            return response;
        }

        // Validate receiver is a merchant for P2M transactions
        if ("P2M".equals(type) && !receiver.isMerchant()) {
            response.put("status", "failed");
            response.put("message", "Receiver is not a registered merchant");
            return response;
        }

        // Validate PIN
        UpiPin upiPin = upiPinRepository.findByUserId(sender.getUser().getId());
        if (upiPin == null || !org.springframework.security.crypto.bcrypt.BCrypt.checkpw(pin, upiPin.getPinHash())) {
            response.put("status", "failed");
            response.put("message", "Invalid PIN");
            return response;
        }
        // Check sender's primary bank account and balance
        BankAccount senderAccount = sender.getUser().getBankAccounts().stream().filter(BankAccount::getIsPrimary)
                .findFirst().orElse(null);
        if (senderAccount == null) {
            response.put("status", "failed");
            response.put("message", "No primary bank account linked");
            return response;
        }

        // Validate receiver's primary bank account BEFORE deducting funds
        BankAccount receiverAccount = receiver.getUser().getBankAccounts().stream()
                .filter(BankAccount::getIsPrimary)
                .findFirst().orElse(null);
        if (receiverAccount == null) {
            response.put("status", "failed");
            response.put("message", "Receiver has no primary bank account linked");
            return response;
        }

        BigDecimal senderBalance = BigDecimal.valueOf(senderAccount.getBalance());
        if (senderBalance.compareTo(amount) < 0) {
            response.put("status", "failed");
            response.put("message", "Insufficient balance");
            return response;
        }

        // Deduct sender balance
        senderAccount.setBalance(senderBalance.subtract(amount).doubleValue());
        bankAccountRepository.save(senderAccount);

        // Calculate fee for P2M (MDR)
        BigDecimal fee = BigDecimal.ZERO;
        if ("P2M".equals(type)) {
            fee = amount.multiply(P2M_FEE_RATE);
        }

        // Credit receiver balance
        BigDecimal receiverBalance = BigDecimal.valueOf(receiverAccount.getBalance());
        receiverAccount.setBalance(receiverBalance.add(amount.subtract(fee)).doubleValue());
        bankAccountRepository.save(receiverAccount);

        // Create transaction record
        Transaction tx = new Transaction();
        tx.setSenderUpiId(senderUpiId);
        tx.setReceiverUpiId(receiverUpiId);
        tx.setAmount(amount);
        tx.setStatus("success");
        tx.setRemarks(remarks);
        tx.setCreatedAt(new java.util.Date());
        transactionRepository.save(tx);
        response.put("transactionId", tx.getId());
        response.put("status", "success");
        response.put("message", "Money sent successfully");
        return response;
    }

    @Transactional
    public Map<String, Object> requestMoney(UPICollectRequest request, String type) {
        Map<String, Object> response = new HashMap<>();
        String requesterUpiId = request.getRequesterUpiId();
        String payerUpiId = request.getPayerUpiId();
        BigDecimal amount = request.getAmount();
        String remarks = request.getRemarks();

        UpiId requester = upiIdRepository.findByUpiId(requesterUpiId);
        UpiId payer = upiIdRepository.findByUpiId(payerUpiId);
        if (requester == null || payer == null) {
            response.put("status", "failed");
            response.put("message", "UPI ID not found");
            return response;
        }

        // Validate requester is a merchant for P2M transactions
        if ("P2M".equals(type) && !requester.isMerchant()) {
            response.put("status", "failed");
            response.put("message", "Requester is not a registered merchant");
            return response;
        }

        Transaction tx = new Transaction();
        tx.setSenderUpiId(payerUpiId);
        tx.setReceiverUpiId(requesterUpiId);
        tx.setAmount(amount);
        tx.setStatus("pending");
        tx.setRemarks(remarks);
        tx.setCreatedAt(new java.util.Date());
        transactionRepository.save(tx);
        response.put("requestId", tx.getId());
        response.put("status", "pending");
        response.put("message", "Money request sent");
        return response;
    }

    public java.util.List<Map<String, Object>> getTransactionHistory(String upiId) {
        java.util.List<Transaction> txs = transactionRepository.findBySenderUpiIdOrReceiverUpiId(upiId, upiId);
        java.util.List<Map<String, Object>> history = new java.util.ArrayList<>();
        for (Transaction tx : txs) {
            Map<String, Object> map = new HashMap<>();
            map.put("transactionId", tx.getId());
            map.put("type", upiId.equals(tx.getSenderUpiId()) ? "debit" : "credit");
            map.put("amount", tx.getAmount());
            map.put("status", tx.getStatus());
            map.put("date", tx.getCreatedAt());
            map.put("remarks", tx.getRemarks());
            history.add(map);
        }
        return history;
    }

    public Map<String, Object> getTransactionStatus(Long transactionId) {
        Map<String, Object> status = new HashMap<>();
        Transaction tx = transactionRepository.findById(transactionId).orElse(null);
        if (tx == null) {
            status.put("status", "not_found");
            status.put("message", "Transaction not found");
            return status;
        }
        status.put("transactionId", tx.getId());
        status.put("status", tx.getStatus());
        return status;
    }

    public Map<String, Object> getTransactionReceipt(Long transactionId) {
        Map<String, Object> receipt = new HashMap<>();
        Transaction tx = transactionRepository.findById(transactionId).orElse(null);
        if (tx == null) {
            receipt.put("status", "not_found");
            receipt.put("message", "Transaction not found");
            return receipt;
        }
        receipt.put("transactionId", tx.getId());
        receipt.put("amount", tx.getAmount());
        receipt.put("senderUpiId", tx.getSenderUpiId());
        receipt.put("receiverUpiId", tx.getReceiverUpiId());
        receipt.put("date", tx.getCreatedAt());
        receipt.put("status", tx.getStatus());
        receipt.put("remarks", tx.getRemarks());
        receipt.put("receiptUrl", "https://example.com/receipt/" + tx.getId());
        return receipt;
    }

    @Transactional
    public Map<String, Object> acceptRequest(Long requestId, PinRequest request) {
        Map<String, Object> response = new HashMap<>();
        String pin = request.getPin();

        Transaction tx = transactionRepository.findById(requestId).orElse(null);
        if (tx == null || !"pending".equals(tx.getStatus())) {
            response.put("status", "failed");
            response.put("message", "Request not found or not pending");
            return response;
        }
        UpiId payer = upiIdRepository.findByUpiId(tx.getSenderUpiId());
        if (payer == null) {
            response.put("status", "failed");
            response.put("message", "Payer UPI ID not found");
            return response;
        }

        // Check if the transaction is P2M based on receiver's merchant status
        // (In a real system, the type would be stored in the Transaction entity)
        UpiId receiver = upiIdRepository.findByUpiId(tx.getReceiverUpiId());
        boolean isP2M = receiver != null && receiver.isMerchant();

        UpiPin upiPin = upiPinRepository.findByUserId(payer.getUser().getId());
        if (upiPin == null || !org.springframework.security.crypto.bcrypt.BCrypt.checkpw(pin, upiPin.getPinHash())) {
            response.put("status", "failed");
            response.put("message", "Invalid PIN");
            return response;
        }
        BankAccount payerAccount = payer.getUser().getBankAccounts().stream().filter(BankAccount::getIsPrimary)
                .findFirst().orElse(null);
        if (payerAccount == null) {
            response.put("status", "failed");
            response.put("message", "No primary bank account linked");
            return response;
        }

        // Credit receiver balance - check existence first
        BankAccount receiverAccount = receiver.getUser().getBankAccounts().stream()
                .filter(BankAccount::getIsPrimary)
                .findFirst().orElse(null);
        if (receiverAccount == null) {
            response.put("status", "failed");
            response.put("message", "Receiver has no primary bank account linked");
            return response;
        }

        BigDecimal payerBalance = BigDecimal.valueOf(payerAccount.getBalance());
        if (payerBalance.compareTo(tx.getAmount()) < 0) {
            response.put("status", "failed");
            response.put("message", "Insufficient balance");
            return response;
        }

        // Deduct payer balance
        payerAccount.setBalance(payerBalance.subtract(tx.getAmount()).doubleValue());
        bankAccountRepository.save(payerAccount);

        // Calculate fee for P2M
        BigDecimal fee = BigDecimal.ZERO;
        if (isP2M) {
            fee = tx.getAmount().multiply(P2M_FEE_RATE);
        }

        BigDecimal receiverBalance = BigDecimal.valueOf(receiverAccount.getBalance());
        receiverAccount.setBalance(receiverBalance.add(tx.getAmount().subtract(fee)).doubleValue());
        bankAccountRepository.save(receiverAccount);

        tx.setStatus("success");
        transactionRepository.save(tx);
        response.put("status", "success");
        response.put("message", "Request accepted and payment completed");
        response.put("transactionId", tx.getId());
        return response;
    }

    @Transactional
    public Map<String, Object> rejectRequest(Long requestId) {
        Map<String, Object> response = new HashMap<>();
        Transaction tx = transactionRepository.findById(requestId).orElse(null);
        if (tx == null || !"pending".equals(tx.getStatus())) {
            response.put("status", "failed");
            response.put("message", "Request not found or not pending");
            return response;
        }
        tx.setStatus("rejected");
        transactionRepository.save(tx);
        response.put("status", "rejected");
        response.put("message", "Request rejected");
        response.put("transactionId", tx.getId());
        return response;
    }

    public java.util.List<Map<String, Object>> getPendingRequests(String upiId) {
        java.util.List<Transaction> pendingTxs = transactionRepository.findBySenderUpiIdAndStatus(upiId, "pending");
        java.util.List<Map<String, Object>> requests = new java.util.ArrayList<>();
        for (Transaction tx : pendingTxs) {
            Map<String, Object> map = new HashMap<>();
            map.put("requestId", tx.getId());
            map.put("requesterUpiId", tx.getReceiverUpiId());
            map.put("amount", tx.getAmount());
            map.put("remarks", tx.getRemarks());
            map.put("createdAt", tx.getCreatedAt());
            requests.add(map);
        }
        return requests;
    }
}
