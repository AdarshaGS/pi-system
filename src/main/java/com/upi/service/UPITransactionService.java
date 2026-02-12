
package com.upi.service;

import com.upi.model.Transaction;
import com.upi.model.UpiId;
import com.upi.model.UpiPin;
import com.upi.model.BankAccount;
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

    @Autowired
    private UpiIdRepository upiIdRepository;
    private UpiPinRepository upiPinRepository;
    private BankAccountRepository bankAccountRepository;
    private TransactionRepository transactionRepository;

    @Transactional
    public Map<String, Object> sendMoney(String senderUpiId, String receiverUpiId, BigDecimal amount, String pin,
            String remarks) {
        Map<String, Object> response = new HashMap<>();
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
        if (senderAccount.getBalance() < amount.doubleValue()) {
            response.put("status", "failed");
            response.put("message", "Insufficient balance");
            return response;
        }
        // Deduct sender balance
        senderAccount.setBalance(senderAccount.getBalance() - amount.doubleValue());
        bankAccountRepository.save(senderAccount);
        // Credit receiver balance
        BankAccount receiverAccount = receiver.getUser().getBankAccounts().stream().filter(BankAccount::getIsPrimary)
                .findFirst().orElse(null);
        if (receiverAccount != null) {
            receiverAccount.setBalance(receiverAccount.getBalance() + amount.doubleValue());
            bankAccountRepository.save(receiverAccount);
        }
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
    public Map<String, Object> requestMoney(String requesterUpiId, String payerUpiId, BigDecimal amount,
            String remarks) {
        Map<String, Object> response = new HashMap<>();
        UpiId requester = upiIdRepository.findByUpiId(requesterUpiId);
        UpiId payer = upiIdRepository.findByUpiId(payerUpiId);
        if (requester == null || payer == null) {
            response.put("status", "failed");
            response.put("message", "UPI ID not found");
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
    public Map<String, Object> acceptRequest(Long requestId, String pin) {
        Map<String, Object> response = new HashMap<>();
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
        if (payerAccount.getBalance() < tx.getAmount().doubleValue()) {
            response.put("status", "failed");
            response.put("message", "Insufficient balance");
            return response;
        }
        // Deduct payer balance
        payerAccount.setBalance(payerAccount.getBalance() - tx.getAmount().doubleValue());
        bankAccountRepository.save(payerAccount);
        // Credit receiver balance
        UpiId receiver = upiIdRepository.findByUpiId(tx.getReceiverUpiId());
        BankAccount receiverAccount = receiver.getUser().getBankAccounts().stream().filter(BankAccount::getIsPrimary)
                .findFirst().orElse(null);
        if (receiverAccount != null) {
            receiverAccount.setBalance(receiverAccount.getBalance() + tx.getAmount().doubleValue());
            bankAccountRepository.save(receiverAccount);
        }
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
