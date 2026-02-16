
package com.upi.service;

import com.upi.model.Transaction;
import com.upi.model.UpiId;
import com.upi.model.UpiPin;
import com.upi.model.BankAccount;
import com.upi.dto.UPITransactionRequest;
import com.upi.dto.UPICollectRequest;
import com.upi.dto.PinRequest;
import com.upi.dto.UPITransactionResponse;
import com.upi.repository.UpiIdRepository;
import com.upi.repository.UpiPinRepository;
import com.upi.repository.BankAccountRepository;
import com.upi.repository.TransactionRepository;
import com.upi.exception.UpiIdNotFoundException;
import com.upi.exception.BankAccountNotFoundException;
import com.upi.exception.InsufficientBalanceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.users.data.Users;
import com.upi.model.TransactionCategory;

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

    // Assuming TransactionType enum or String constants for transaction types
    public static final String TRANSACTION_TYPE_P2P = "P2P";
    public static final String TRANSACTION_TYPE_P2M = "P2M";
    public static final String TRANSACTION_TYPE_COLLECT_REQUEST = "COLLECT_REQUEST";
    public static final String TRANSACTION_TYPE_COLLECT_ACCEPT = "COLLECT_ACCEPT";
    public static final String TRANSACTION_TYPE_COLLECT_REJECT = "COLLECT_REJECT";

    @Transactional
    public UPITransactionResponse sendMoney(UPITransactionRequest request, String type) {
        String senderUpiId = request.getSenderUpiId();
        String receiverUpiId = request.getReceiverUpiId();
        BigDecimal amount = request.getAmount();
        String pin = request.getPin();
        String remarks = request.getRemarks();

        UpiId sender = upiIdRepository.findByUpiId(senderUpiId)
                .orElseThrow(() -> new UpiIdNotFoundException("Sender UPI ID " + senderUpiId + " not found."));
        UpiId receiver = upiIdRepository.findByUpiId(receiverUpiId)
                .orElseThrow(() -> new UpiIdNotFoundException("Receiver UPI ID " + receiverUpiId + " not found."));

        // Validate receiver is a merchant for P2M transactions
        if (TRANSACTION_TYPE_P2M.equals(type) && !receiver.isMerchant()) {
            return UPITransactionResponse.builder().status("failed").message("Receiver is not a registered merchant.").build();
        }

        // Validate PIN
        UpiPin upiPin = upiPinRepository.findByUserId(sender.getUser().getId()).orElse(null);
        if (upiPin == null || !org.springframework.security.crypto.bcrypt.BCrypt.checkpw(pin, upiPin.getPinHash())) { // Assuming BCrypt is used for PIN hashing
            return UPITransactionResponse.builder().status("failed").message("Invalid PIN.").build();
        }
        
        BankAccount senderAccount = getPrimaryBankAccount(sender.getUser())
                .orElseThrow(() -> new BankAccountNotFoundException("Sender has no primary bank account linked."));

        // Validate receiver's primary bank account BEFORE deducting funds
        BankAccount receiverAccount = getPrimaryBankAccount(receiver.getUser())
                .orElseThrow(() -> new BankAccountNotFoundException("Receiver has no primary bank account linked."));

        // Assuming BankAccount model's balance field is BigDecimal
        BigDecimal senderBalance = senderAccount.getBalance();
        if (senderBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in sender's account.");
        }

        // Deduct sender balance
        senderAccount.setBalance(senderBalance.subtract(amount));
        bankAccountRepository.save(senderAccount);

        // Calculate fee for P2M (MDR)
        BigDecimal fee = BigDecimal.ZERO;
        if (TRANSACTION_TYPE_P2M.equals(type)) {
            fee = amount.multiply(P2M_FEE_RATE);
        }

        // Credit receiver balance
        // Assuming BankAccount model's balance field is BigDecimal
        BigDecimal receiverBalance = receiverAccount.getBalance();
        receiverAccount.setBalance(receiverBalance.add(amount.subtract(fee)));
        bankAccountRepository.save(receiverAccount);

        // Create transaction record
        Transaction tx = new Transaction();
        tx.setSenderUpiId(senderUpiId);
        tx.setReceiverUpiId(receiverUpiId);
        tx.setAmount(amount);
        tx.setStatus("success");
        tx.setType(TransactionCategory.SEND); // Set the enum type
        // Assuming Transaction model has a 'transactionType' field
        tx.setTransactionType(type); 
        tx.setRemarks(remarks);
        tx.setCreatedAt(new java.util.Date());
        transactionRepository.save(tx);
        
        return UPITransactionResponse.builder()
                .transactionId(tx.getId())
                .status("success")
                .message("Money sent successfully.")
                .build();
    }

    @Transactional
    public UPITransactionResponse requestMoney(UPICollectRequest request, String type) {
        String requesterUpiId = request.getRequesterUpiId();
        String payerUpiId = request.getPayerUpiId();
        BigDecimal amount = request.getAmount();
        String remarks = request.getRemarks();

        UpiId requester = upiIdRepository.findByUpiId(requesterUpiId)
                .orElseThrow(() -> new UpiIdNotFoundException("Requester UPI ID " + requesterUpiId + " not found."));
        UpiId payer = upiIdRepository.findByUpiId(payerUpiId)
                .orElseThrow(() -> new UpiIdNotFoundException("Payer UPI ID " + payerUpiId + " not found."));

        // Validate requester is a merchant for P2M transactions
        if (TRANSACTION_TYPE_P2M.equals(type) && !requester.isMerchant()) {
            return UPITransactionResponse.builder().status("failed").message("Requester is not a registered merchant.").build();
        }

        Transaction tx = new Transaction();
        // Assuming Transaction model has a 'transactionType' field
        tx.setTransactionType(TRANSACTION_TYPE_COLLECT_REQUEST);
        tx.setSenderUpiId(payerUpiId);
        tx.setReceiverUpiId(requesterUpiId);
        tx.setAmount(amount);
        tx.setType(TransactionCategory.REQUEST); // Set the enum type
        tx.setStatus("pending");
        tx.setRemarks(remarks);
        tx.setCreatedAt(new java.util.Date());
        transactionRepository.save(tx);
        
        return UPITransactionResponse.builder()
                .requestId(tx.getId())
                .status("pending")
                .message("Money request sent successfully.")
                .build();
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
    public UPITransactionResponse acceptRequest(Long requestId, PinRequest request) {
        String pin = request.getPin();

        Transaction tx = transactionRepository.findById(requestId)
                .orElseThrow(() -> new UpiIdNotFoundException("Request with ID " + requestId + " not found."));
        
        if (!"pending".equals(tx.getStatus())) {
            return UPITransactionResponse.builder().status("failed").message("Request is not pending.").build();
        }
        
        UpiId payer = upiIdRepository.findByUpiId(tx.getSenderUpiId())
                .orElseThrow(() -> new UpiIdNotFoundException("Payer UPI ID " + tx.getSenderUpiId() + " not found."));

        // Check if the transaction is P2M based on receiver's merchant status
        // Using the stored transaction type if available, otherwise inferring
        UpiId receiver = upiIdRepository.findByUpiId(tx.getReceiverUpiId())
                .orElseThrow(() -> new UpiIdNotFoundException("Receiver UPI ID " + tx.getReceiverUpiId() + " not found."));
        
        boolean isP2M = TRANSACTION_TYPE_P2M.equals(tx.getTransactionType()) || receiver.isMerchant();

        UpiPin upiPin = upiPinRepository.findByUserId(payer.getUser().getId()).orElse(null);
        if (upiPin == null || !org.springframework.security.crypto.bcrypt.BCrypt.checkpw(pin, upiPin.getPinHash())) {
            return UPITransactionResponse.builder().status("failed").message("Invalid PIN.").build();
        }
        
        BankAccount payerAccount = getPrimaryBankAccount(payer.getUser())
                .orElseThrow(() -> new BankAccountNotFoundException("Payer has no primary bank account linked."));

        // Credit receiver balance - check existence first
        BankAccount receiverAccount = getPrimaryBankAccount(receiver.getUser())
                .orElseThrow(() -> new BankAccountNotFoundException("Receiver has no primary bank account linked."));

        // Assuming BankAccount model's balance field is BigDecimal
        BigDecimal payerBalance = payerAccount.getBalance();
        if (payerBalance.compareTo(tx.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in payer's account.");
        }

        // Deduct payer balance
        payerAccount.setBalance(payerBalance.subtract(tx.getAmount()));
        bankAccountRepository.save(payerAccount);

        // Calculate fee for P2M
        BigDecimal fee = BigDecimal.ZERO;
        if (isP2M) {
            fee = tx.getAmount().multiply(P2M_FEE_RATE);
        }
        
        // Assuming BankAccount model's balance field is BigDecimal
        BigDecimal receiverBalance = receiverAccount.getBalance();
        receiverAccount.setBalance(receiverBalance.add(tx.getAmount().subtract(fee)));
        bankAccountRepository.save(receiverAccount);

        tx.setStatus("success");
        tx.setType(TransactionCategory.SEND); // Update type from REQUEST to SEND
        transactionRepository.save(tx);
        
        return UPITransactionResponse.builder()
                .status("success")
                .message("Request accepted and payment completed.")
                .transactionId(tx.getId())
                .build();
    }

    @Transactional
    public UPITransactionResponse rejectRequest(Long requestId) {
        Transaction tx = transactionRepository.findById(requestId)
                .orElseThrow(() -> new UpiIdNotFoundException("Request with ID " + requestId + " not found."));
        
        if (!"pending".equals(tx.getStatus())) {
            return UPITransactionResponse.builder().status("failed").message("Request is not pending.").build();
        }
        
        tx.setStatus("rejected");
        tx.setType(TransactionCategory.REQUEST); // Type remains REQUEST, status changes
        // Assuming Transaction model has a 'transactionType' field
        tx.setTransactionType(TRANSACTION_TYPE_COLLECT_REJECT);
        transactionRepository.save(tx);
        
        return UPITransactionResponse.builder()
                .status("rejected")
                .message("Request rejected.")
                .transactionId(tx.getId())
                .build();
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
    
    /**
     * Helper method to find the primary bank account for a given user.
     * @param user The user for whom to find the primary bank account.
     * @return An Optional containing the primary BankAccount if found, otherwise empty.
     */
    private Optional<BankAccount> getPrimaryBankAccount(Users user) {
        // Assuming BankAccount has an 'isPrimary' field and a getter getIsPrimary()
        return user.getBankAccounts().stream().filter(BankAccount::getIsPrimary).findFirst();
    }
}
