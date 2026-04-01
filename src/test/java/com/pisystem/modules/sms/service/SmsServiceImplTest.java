package com.pisystem.modules.sms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pisystem.modules.budget.data.Expense;
import com.pisystem.modules.budget.data.Income;
import com.pisystem.modules.budget.service.BudgetService;
import com.pisystem.modules.sms.data.ParsedSMSData;
import com.pisystem.modules.sms.data.SMSImportRequest;
import com.pisystem.modules.sms.data.SMSImportResponse;
import com.pisystem.modules.sms.data.SMSTransaction;
import com.pisystem.modules.sms.repo.SMSTransactionRepository;
import com.pisystem.modules.upi.repository.BankAccountRepository;
import com.pisystem.modules.upi.service.BankAccountService;

/**
 * Comprehensive test suite for SmsServiceImpl.importMessages()
 * Tests cover:
 * - Happy path scenarios (DEBIT, CREDIT, self-transfer)
 * - Edge cases (duplicates, empty lists, null values)
 * - Error handling
 * - Budget integration (Income/Expense creation)
 * - Bank account updates
 * - Batch operations
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SmsServiceImplTest {

    @Mock
    private SMSParserService parserService;

    @Mock
    private SMSTransactionRepository repository;

    @Mock
    private BudgetService budgetService;

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private SmsServiceImpl smsService;

    private Long userId;
    private SMSImportRequest request;

    @BeforeEach
    void setUp() {
        userId = 1L;
        // Default: no duplicates, no user bank accounts
        when(repository.findExistingMessages(anyLong(), ArgumentMatchers.<String>anyList()))
                .thenReturn(Collections.emptyList());
        when(bankAccountRepository.findAccountNumbersByUserId(anyLong()))
                .thenReturn(Collections.emptyList());
    }

    // ==================== SCENARIO 1: New DEBIT Transaction ====================
    @Test
    void testImportMessages_NewDebitTransaction_CreatesExpenseAndTransaction() {
        // Given: A new DEBIT transaction SMS
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.500 debited from A/c XX1234 on 12-03-2026 at AMAZON. Avl Bal: Rs.10,000",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(500),
                LocalDate.of(2026, 3, 12),
                LocalTime.of(14, 30),
                SMSTransaction.TransactionType.DEBIT,
                "AMAZON",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.95
        );

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Verify transaction saved and expense created
        assertEquals(1, response.getTotalMessages());
        assertEquals(1, response.getTransactions().size());
        assertEquals(0, response.getErrors().size());

        SMSImportResponse.TransactionSummary summary = response.getTransactions().get(0);
        assertEquals(1L, summary.getTransactionId());
        assertEquals(SMSTransaction.ParseStatus.SUCCESS, summary.getStatus());
        assertEquals("TRANSACTION", summary.getMessageType());
        assertTrue(summary.isAddedToBudget(), "DEBIT transaction should be added to budget as expense");

        // Verify expense was created
        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(budgetService, times(1)).addExpense(expenseCaptor.capture());
        Expense capturedExpense = expenseCaptor.getValue();
        assertEquals(BigDecimal.valueOf(500), capturedExpense.getAmount());
        assertEquals("AMAZON", capturedExpense.getDescription());

        // Verify bank account was updated
        verify(bankAccountService, times(1)).addOrUpdateBankAccount(userId, "XX1234");

        // Verify no income was created
        verify(budgetService, never()).addIncome(any());
    }

    // ==================== SCENARIO 2: New CREDIT Transaction ====================
    @Test
    void testImportMessages_NewCreditTransaction_CreatesIncomeAndTransaction() {
        // Given: A new CREDIT transaction SMS
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.5000 credited to A/c XX1234 on 12-03-2026. Ref: SAL2026",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(5000),
                LocalDate.of(2026, 3, 12),
                LocalTime.of(9, 0),
                SMSTransaction.TransactionType.CREDIT,
                "Salary Credit",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.98
        );

        SMSTransaction savedTransaction = createSMSTransaction(2L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Verify transaction saved and income created
        assertEquals(1, response.getTotalMessages());
        assertEquals(1, response.getTransactions().size());
        assertTrue(response.getTransactions().get(0).isAddedToBudget(),
                "CREDIT transaction should be added to budget as income");

        // Verify income was created
        ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
        verify(budgetService, times(1)).addIncome(incomeCaptor.capture());
        Income capturedIncome = incomeCaptor.getValue();
        assertEquals(BigDecimal.valueOf(5000), capturedIncome.getAmount());
        assertEquals("Salary Credit", capturedIncome.getDescription());
        assertEquals("SMS_PARSED", capturedIncome.getSource());

        // Verify no expense was created
        verify(budgetService, never()).addExpense(any());
    }

    // ==================== SCENARIO 3: Self-Transfer Detection ====================
    @Test
    void testImportMessages_SelfTransfer_BothAccountsBelongToUser() {
        // Given: A transfer between two user accounts
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.1000 debited from A/c XX1234 and credited to A/c XX5678 via UPI. Ref:123456",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(1000),
                LocalDate.of(2026, 3, 12),
                LocalTime.of(10, 0),
                SMSTransaction.TransactionType.DEBIT,
                "Self Transfer",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.92
        );
        parsedData.setFromAccount("XX1234");
        parsedData.setToAccount("XX5678");

        // Both accounts belong to user
        when(bankAccountRepository.findAccountNumbersByUserId(userId))
                .thenReturn(List.of("XX1234", "XX5678"));

        SMSTransaction savedTransaction = createSMSTransaction(3L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setFromAccount("XX1234");
        savedTransaction.setToAccount("XX5678");
        savedTransaction.setMessageType("SELF_TRANSFER");
        savedTransaction.setCategory("self_transfer");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should detect as self-transfer and NOT add to budget
        assertEquals(1, response.getTransactions().size());
        SMSImportResponse.TransactionSummary summary = response.getTransactions().get(0);
        assertEquals("SELF_TRANSFER", summary.getMessageType());
        assertFalse(summary.isAddedToBudget(), "Self-transfers should not be added to budget");

        // Verify no income or expense created
        verify(budgetService, never()).addIncome(any());
        verify(budgetService, never()).addExpense(any());
    }

    // ==================== SCENARIO 4: Regular Transfer (Not Self-Transfer) ====================
    @Test
    void testImportMessages_RegularTransfer_OnlyOneAccountBelongsToUser() {
        // Given: Transfer where only source account belongs to user
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.1000 debited from A/c XX1234 and credited to XX9999 via UPI. Ref:789012",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(1000),
                LocalDate.of(2026, 3, 12),
                LocalTime.of(10, 0),
                SMSTransaction.TransactionType.DEBIT,
                "UPI Transfer",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.90
        );
        parsedData.setFromAccount("XX1234");
        parsedData.setToAccount("XX9999"); // Not user's account

        // Only source account belongs to user
        when(bankAccountRepository.findAccountNumbersByUserId(userId))
                .thenReturn(List.of("XX1234"));

        SMSTransaction savedTransaction = createSMSTransaction(4L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should treat as regular transaction and create expense
        assertEquals(1, response.getTransactions().size());
        SMSImportResponse.TransactionSummary summary = response.getTransactions().get(0);
        assertEquals("TRANSACTION", summary.getMessageType());
        assertTrue(summary.isAddedToBudget(), "Regular transfer should be added to budget");

        // Verify expense was created
        verify(budgetService, times(1)).addExpense(any(Expense.class));
    }

    // ==================== SCENARIO 5: Duplicate Messages ====================
    @Test
    void testImportMessages_DuplicateMessages_SkipsProcessing() {
        // Given: Messages that already exist in database
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.500 debited from A/c XX1234",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        // Mock: Message already exists
        when(repository.findExistingMessages(userId, List.of(smsMessage.getContent())))
                .thenReturn(List.of(smsMessage.getContent()));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should skip without processing
        assertEquals(1, response.getTotalMessages());
        assertEquals(0, response.getTransactions().size());
        assertEquals(0, response.getErrors().size());

        // Verify no database operations
        verify(repository, never()).saveAll(anyList());
        verify(budgetService, never()).addIncome(any());
        verify(budgetService, never()).addExpense(any());
        verify(parserService, never()).parseSMS(anyString());
    }

    // ==================== SCENARIO 6: Multiple Messages (Mixed Types) ====================
    @Test
    void testImportMessages_MultipleMessages_ProcessesBatch() {
        // Given: Multiple messages with different types
        SMSImportRequest.SMSMessage debitMsg = new SMSImportRequest.SMSMessage(
                "Rs.200 debited from A/c XX1234 at STARBUCKS. Avl Bal: Rs.5000",
                "HDFCBK",
                1710234567000L
        );
        SMSImportRequest.SMSMessage creditMsg = new SMSImportRequest.SMSMessage(
                "Rs.3000 credited to A/c XX1234 on 12-Mar-2026. Ref:DIV2026",
                "HDFCBK",
                1710234568000L
        );
        SMSImportRequest.SMSMessage oldDebitMsg = new SMSImportRequest.SMSMessage(
                "Rs.150 debited from A/c XX1234 at GROCERY STORE", // Different content, marked as duplicate
                "HDFCBK",
                1710234569000L
        );

        request = new SMSImportRequest(userId, List.of(debitMsg, creditMsg, oldDebitMsg));

        // Mock: Third message already exists in DB
        when(repository.findExistingMessages(userId, 
                List.of(debitMsg.getContent(), creditMsg.getContent(), oldDebitMsg.getContent())))
                .thenReturn(List.of(oldDebitMsg.getContent()));

        // Mock parsed data for debit
        ParsedSMSData debitParsed = createParsedData(
                BigDecimal.valueOf(200),
                LocalDate.of(2026, 3, 12),
                LocalTime.of(14, 30),
                SMSTransaction.TransactionType.DEBIT,
                "STARBUCKS",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.93
        );

        // Mock parsed data for credit
        ParsedSMSData creditParsed = createParsedData(
                BigDecimal.valueOf(3000),
                LocalDate.of(2026, 3, 12),
                LocalTime.of(15, 0),
                SMSTransaction.TransactionType.CREDIT,
                "Dividend",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.88
        );

        when(parserService.parseSMS(debitMsg.getContent())).thenReturn(debitParsed);
        when(parserService.parseSMS(creditMsg.getContent())).thenReturn(creditParsed);
        // No mock for oldDebitMsg since it should be skipped

        SMSTransaction savedDebit = createSMSTransaction(1L, userId, debitMsg.getContent(), debitParsed);
        savedDebit.setMessageType("TRANSACTION");
        SMSTransaction savedCredit = createSMSTransaction(2L, userId, creditMsg.getContent(), creditParsed);
        savedCredit.setMessageType("TRANSACTION");

        when(repository.saveAll(ArgumentMatchers.<SMSTransaction>anyList()))
                .thenReturn(List.of(savedDebit, savedCredit));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should process 2 messages (skip duplicate)
        assertEquals(3, response.getTotalMessages());
        assertEquals(2, response.getTransactions().size());
        assertEquals(0, response.getErrors().size());

        // Verify both income and expense created
        verify(budgetService, times(1)).addExpense(any(Expense.class));
        verify(budgetService, times(1)).addIncome(any(Income.class));

        // Verify batch save
        verify(repository, times(1)).saveAll(ArgumentMatchers.<SMSTransaction>anyList());
    }

    // ==================== SCENARIO 7: Non-Transaction Messages ====================
    @Test
    void testImportMessages_NonTransactionMessage_SavesButDoesNotAddToBudget() {
        // Given: A balance inquiry or mandate alert (non-transaction)
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Your A/c XX1234 will be debited on 15-03-2026 for Auto-debit mandate",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                null, // No amount
                null,
                null,
                null,
                null,
                "XX1234",
                SMSTransaction.ParseStatus.PARTIAL,
                0.60
        );

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("MANDATE_ALERT");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Transaction saved but not added to budget
        assertEquals(1, response.getTotalMessages());
        assertEquals(1, response.getTransactions().size());
        assertFalse(response.getTransactions().get(0).isAddedToBudget(),
                "Non-transaction messages should not be added to budget");

        // Verify no budget operations
        verify(budgetService, never()).addIncome(any());
        verify(budgetService, never()).addExpense(any());

        // Verify transaction still saved
        verify(repository, times(1)).saveAll(anyList());
    }

    // ==================== SCENARIO 8: Parsing Error ====================
    @Test
    void testImportMessages_ParsingError_CapturesError() {
        // Given: A message that causes parsing exception
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Corrupt SMS message @#$%",
                "UNKNOWN",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        when(parserService.parseSMS(smsMessage.getContent()))
                .thenThrow(new RuntimeException("Invalid SMS format"));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should capture error
        assertEquals(1, response.getTotalMessages());
        assertEquals(0, response.getTransactions().size());
        assertEquals(1, response.getErrors().size());

        SMSImportResponse.ErrorDetail error = response.getErrors().get(0);
        assertTrue(error.getMessage().contains("Corrupt SMS"));
        assertEquals("Invalid SMS format", error.getError());

        // Verify no database operations
        verify(repository, never()).saveAll(anyList());
        verify(budgetService, never()).addIncome(any());
        verify(budgetService, never()).addExpense(any());
    }

    // ==================== SCENARIO 9: Empty Message List ====================
    @Test
    void testImportMessages_EmptyMessageList_ReturnsEmptyResponse() {
        // Given: Empty message list
        request = new SMSImportRequest(userId, Collections.emptyList());

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should return empty response
        assertEquals(0, response.getTotalMessages());
        assertEquals(0, response.getTransactions().size());
        assertEquals(0, response.getErrors().size());

        // Verify no processing
        verify(parserService, never()).parseSMS(anyString());
        verify(repository, never()).saveAll(anyList());
    }

    // ==================== SCENARIO 10: Transaction Without Amount ====================
    @Test
    void testImportMessages_TransactionWithoutAmount_SavesButDoesNotAddToBudget() {
        // Given: Transaction detected but no amount parsed
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Your transaction at MERCHANT was successful",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                null, // No amount
                LocalDate.of(2026, 3, 12),
                null,
                SMSTransaction.TransactionType.DEBIT,
                "MERCHANT",
                "XX1234",
                SMSTransaction.ParseStatus.PARTIAL,
                0.70
        );

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should save but not add to budget
        assertEquals(1, response.getTransactions().size());
        assertFalse(response.getTransactions().get(0).isAddedToBudget());

        // Verify no budget operations (because amount is null)
        verify(budgetService, never()).addIncome(any());
        verify(budgetService, never()).addExpense(any());
    }

    // ==================== SCENARIO 11: Transaction Without Account Number ====================
    @Test
    void testImportMessages_TransactionWithoutAccountNumber_DoesNotUpdateBankAccount() {
        // Given: Transaction without account number
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.300 spent at COFFEE SHOP using card XX9876",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(300),
                LocalDate.of(2026, 3, 12),
                LocalTime.of(16, 0),
                SMSTransaction.TransactionType.DEBIT,
                "COFFEE SHOP",
                null, // No account number
                SMSTransaction.ParseStatus.SUCCESS,
                0.85
        );
        parsedData.setCardNumber("XX9876");

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should process but not update bank account
        assertEquals(1, response.getTransactions().size());
        assertTrue(response.getTransactions().get(0).isAddedToBudget());

        // Verify expense created but bank account NOT updated
        verify(budgetService, times(1)).addExpense(any(Expense.class));
        verify(bankAccountService, never()).addOrUpdateBankAccount(anyLong(), anyString());
    }

    // ==================== SCENARIO 12: Transaction With Unknown Type ====================
    @Test
    void testImportMessages_TransactionWithUnknownType_SavesButDoesNotAddToBudget() {
        // Given: Transaction with null/unknown type - even though message looks like transaction
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.100 debited from A/c XX1234 at MERCHANT. Ref:ABC123",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(100),
                LocalDate.of(2026, 3, 12),
                null,
                null, // Unknown type (parser couldn't determine DEBIT/CREDIT)
                "MERCHANT",
                "XX1234",
                SMSTransaction.ParseStatus.PARTIAL,
                0.50
        );

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(ArgumentMatchers.<SMSTransaction>anyList()))
                .thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should save but not create income/expense because type is unknown
        assertEquals(1, response.getTransactions().size());
        assertFalse(response.getTransactions().get(0).isAddedToBudget(),
                "Unknown transaction type should not be added to budget");

        // No income or expense because type is unknown
        verify(budgetService, never()).addIncome(any());
        verify(budgetService, never()).addExpense(any());
    }

    // ==================== SCENARIO 13: Low Confidence Transaction ====================
    @Test
    void testImportMessages_LowConfidenceTransaction_StillProcesses() {
        // Given: Transaction with low confidence score
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.50 debited",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(50),
                LocalDate.now(),
                null,
                SMSTransaction.TransactionType.DEBIT,
                "Unknown",
                null,
                SMSTransaction.ParseStatus.PARTIAL,
                0.40 // Low confidence
        );

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("LOW_CONFIDENCE_TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should still save (but message type reflects low confidence)
        assertEquals(1, response.getTransactions().size());
        assertEquals(SMSTransaction.ParseStatus.PARTIAL, response.getTransactions().get(0).getStatus());
        assertEquals(0.40, response.getTransactions().get(0).getConfidence());
    }

    // ==================== SCENARIO 14: Partial Success (Some Errors) ====================
    @Test
    void testImportMessages_PartialSuccess_SomeMessagesFailAndSomeSucceed() {
        // Given: Mix of valid and invalid messages
        SMSImportRequest.SMSMessage validMsg = new SMSImportRequest.SMSMessage(
                "Rs.100 debited from A/c XX1234",
                "HDFCBK",
                1710234567000L
        );
        SMSImportRequest.SMSMessage invalidMsg = new SMSImportRequest.SMSMessage(
                "Invalid message",
                "UNKNOWN",
                1710234568000L
        );

        request = new SMSImportRequest(userId, List.of(validMsg, invalidMsg));

        ParsedSMSData validParsed = createParsedData(
                BigDecimal.valueOf(100),
                LocalDate.of(2026, 3, 12),
                LocalTime.of(12, 0),
                SMSTransaction.TransactionType.DEBIT,
                "SHOP",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.90
        );

        when(parserService.parseSMS(validMsg.getContent())).thenReturn(validParsed);
        when(parserService.parseSMS(invalidMsg.getContent()))
                .thenThrow(new RuntimeException("Parse failed"));

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, validMsg.getContent(), validParsed);
        savedTransaction.setMessageType("TRANSACTION");

        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should have both success and error
        assertEquals(2, response.getTotalMessages());
        assertEquals(1, response.getTransactions().size());
        assertEquals(1, response.getErrors().size());

        assertEquals("Parse failed", response.getErrors().get(0).getError());

        // Verify partial processing
        verify(repository, times(1)).saveAll(anyList());
        verify(budgetService, times(1)).addExpense(any());
    }

    // ==================== SCENARIO 15: All Messages Are Duplicates ====================
    @Test
    void testImportMessages_AllDuplicates_NoProcessing() {
        // Given: All messages already exist
        SMSImportRequest.SMSMessage msg1 = new SMSImportRequest.SMSMessage("Message 1", "BANK", 1L);
        SMSImportRequest.SMSMessage msg2 = new SMSImportRequest.SMSMessage("Message 2", "BANK", 2L);
        request = new SMSImportRequest(userId, List.of(msg1, msg2));

        when(repository.findExistingMessages(userId, List.of("Message 1", "Message 2")))
                .thenReturn(List.of("Message 1", "Message 2"));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should skip all
        assertEquals(2, response.getTotalMessages());
        assertEquals(0, response.getTransactions().size());
        assertEquals(0, response.getErrors().size());

        verify(parserService, never()).parseSMS(anyString());
        verify(repository, never()).saveAll(anyList());
    }

    // ==================== SCENARIO 16: Self-Transfer with Null Accounts ====================
    @Test
    void testImportMessages_SelfTransferCheck_HandlesNullAccounts() {
        // Given: Transaction with null fromAccount or toAccount
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.100 debited",
                "BANK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(100),
                LocalDate.now(),
                LocalTime.now(),
                SMSTransaction.TransactionType.DEBIT,
                "MERCHANT",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.80
        );
        parsedData.setFromAccount(null); // Null from account
        parsedData.setToAccount(null); // Null to account

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(ArgumentMatchers.<SMSTransaction>anyList()))
                .thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should process normally (not detect as self-transfer)
        assertEquals(1, response.getTransactions().size());
        assertEquals("TRANSACTION", response.getTransactions().get(0).getMessageType());
        assertTrue(response.getTransactions().get(0).isAddedToBudget());
    }

    // ==================== SCENARIO 17: Bank Account Cache Usage ====================
    @Test
    void testImportMessages_UsesCache_ForMultipleTransactions() {
        // Given: Multiple transactions from same user
        SMSImportRequest.SMSMessage msg1 = new SMSImportRequest.SMSMessage(
                "Rs.100 debited from A/c XX1234 and credited to A/c XX5678 via UPI",
                "HDFCBK",
                1L
        );
        SMSImportRequest.SMSMessage msg2 = new SMSImportRequest.SMSMessage(
                "Rs.200 debited from A/c XX1234 and credited to A/c XX5678 via UPI",
                "HDFCBK",
                2L
        );
        request = new SMSImportRequest(userId, List.of(msg1, msg2));

        // User has both accounts
        when(bankAccountRepository.findAccountNumbersByUserId(userId))
                .thenReturn(List.of("XX1234", "XX5678"));

        ParsedSMSData parsed1 = createParsedData(
                BigDecimal.valueOf(100),
                LocalDate.now(),
                LocalTime.now(),
                SMSTransaction.TransactionType.DEBIT,
                "Transfer",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.90
        );
        parsed1.setFromAccount("XX1234");
        parsed1.setToAccount("XX5678");

        ParsedSMSData parsed2 = createParsedData(
                BigDecimal.valueOf(200),
                LocalDate.now(),
                LocalTime.now(),
                SMSTransaction.TransactionType.DEBIT,
                "Transfer",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.90
        );
        parsed2.setFromAccount("XX1234");
        parsed2.setToAccount("XX5678");

        when(parserService.parseSMS(msg1.getContent())).thenReturn(parsed1);
        when(parserService.parseSMS(msg2.getContent())).thenReturn(parsed2);

        SMSTransaction saved1 = createSMSTransaction(1L, userId, msg1.getContent(), parsed1);
        saved1.setMessageType("SELF_TRANSFER");
        SMSTransaction saved2 = createSMSTransaction(2L, userId, msg2.getContent(), parsed2);
        saved2.setMessageType("SELF_TRANSFER");

        when(repository.saveAll(ArgumentMatchers.<SMSTransaction>anyList()))
                .thenReturn(List.of(saved1, saved2));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should query bank accounts only once (cache hit on second)
        verify(bankAccountRepository, times(1)).findAccountNumbersByUserId(userId);

        assertEquals(2, response.getTransactions().size());
        response.getTransactions().forEach(t ->
                assertEquals("SELF_TRANSFER", t.getMessageType())
        );
    }

    // ==================== SCENARIO 18: Transaction ID Mapping ====================
    @Test
    void testImportMessages_MapsTransactionIds_FromSavedEntities() {
        // Given: Multiple new transactions
        SMSImportRequest.SMSMessage msg1 = new SMSImportRequest.SMSMessage("Rs.100 debited", "BANK", 1L);
        SMSImportRequest.SMSMessage msg2 = new SMSImportRequest.SMSMessage("Rs.200 debited", "BANK", 2L);
        request = new SMSImportRequest(userId, List.of(msg1, msg2));

        ParsedSMSData parsed1 = createTransactionParsedData(100);
        ParsedSMSData parsed2 = createTransactionParsedData(200);

        when(parserService.parseSMS(msg1.getContent())).thenReturn(parsed1);
        when(parserService.parseSMS(msg2.getContent())).thenReturn(parsed2);

        SMSTransaction saved1 = createSMSTransaction(101L, userId, msg1.getContent(), parsed1);
        saved1.setMessageType("TRANSACTION");
        SMSTransaction saved2 = createSMSTransaction(102L, userId, msg2.getContent(), parsed2);
        saved2.setMessageType("TRANSACTION");

        when(repository.saveAll(ArgumentMatchers.<SMSTransaction>anyList()))
                .thenReturn(List.of(saved1, saved2));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Transaction IDs should be mapped correctly
        assertEquals(2, response.getTransactions().size());
        assertEquals(101L, response.getTransactions().get(0).getTransactionId());
        assertEquals(102L, response.getTransactions().get(1).getTransactionId());
    }

    // ==================== SCENARIO 19: Message Truncation ====================
    @Test
    void testImportMessages_TruncatesLongMessages_InResponse() {
        // Given: Very long SMS message
        String longMessage = "Rs.500 debited from A/c XX1234 on 12-03-2026 at AMAZON for purchase reference 1234567890ABCDEF with description: Lorem ipsum dolor sit amet";
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(longMessage, "BANK", 1L);
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createTransactionParsedData(500);
        when(parserService.parseSMS(longMessage)).thenReturn(parsedData);

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, longMessage, parsedData);
        savedTransaction.setMessageType("TRANSACTION");
        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Message should be truncated to 50 chars in response
        String truncatedMessage = response.getTransactions().get(0).getMessage();
        assertTrue(truncatedMessage.length() <= 53, "Message should be truncated to 50 + '...'");
        assertTrue(truncatedMessage.endsWith("..."), "Truncated message should end with '...'");
    }

    // ==================== SCENARIO 20: Expense Auto-Categorization ====================
    @Test
    void testImportMessages_ExpenseCreation_AppliesAutoCategorizationAndCustomCategory() {
        // Given: DEBIT transaction that triggers expense creation
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.1500 debited from A/c XX1234 at AMAZON",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(1500),
                LocalDate.of(2026, 3, 12),
                LocalTime.of(14, 30),
                SMSTransaction.TransactionType.DEBIT,
                "AMAZON",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.95
        );
        parsedData.setReferenceNumber("REF123456");

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(ArgumentMatchers.<SMSTransaction>anyList()))
                .thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Verify expense has proper fields
        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(budgetService, times(1)).addExpense(expenseCaptor.capture());

        Expense expense = expenseCaptor.getValue();
        assertEquals(userId, expense.getUserId());
        assertEquals(BigDecimal.valueOf(1500), expense.getAmount());
        assertEquals(LocalDate.of(2026, 3, 12), expense.getExpenseDate());
        assertEquals("AMAZON", expense.getDescription());
        assertTrue(expense.getNotes().contains("REF123456"), "Notes should contain reference number");
        assertEquals("Shopping", expense.getCustomCategoryName(), 
                "AMAZON should be auto-categorized as Shopping");
    }

    // ==================== SCENARIO 21: Income Creation Fields ====================
    @Test
    void testImportMessages_IncomeCreation_HasCorrectFields() {
        // Given: CREDIT transaction
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.50000 credited to A/c XX1234 - Salary",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(50000),
                LocalDate.of(2026, 3, 1),
                LocalTime.of(0, 0),
                SMSTransaction.TransactionType.CREDIT,
                "EMPLOYER INC",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.97
        );

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(ArgumentMatchers.<SMSTransaction>anyList()))
                .thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Verify income has correct fields
        ArgumentCaptor<Income> incomeCaptor = ArgumentCaptor.forClass(Income.class);
        verify(budgetService, times(1)).addIncome(incomeCaptor.capture());

        Income income = incomeCaptor.getValue();
        assertEquals(userId, income.getUserId());
        assertEquals(BigDecimal.valueOf(50000), income.getAmount());
        assertEquals(LocalDate.of(2026, 3, 1), income.getDate());
        assertEquals("SMS_PARSED", income.getSource());
        assertEquals("EMPLOYER INC", income.getDescription());
    }

    // ==================== SCENARIO 22: Self-Transfer - Empty Account Strings ====================
    @Test
    void testImportMessages_SelfTransferCheck_HandlesEmptyAccountStrings() {
        // Given: Transaction with empty (not null) account strings
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.100 debited from A/c XX1234 at MERCHANT. Avl Bal: Rs.9900",
                "HDFCBK",
                1L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createTransactionParsedData(100);
        parsedData.setFromAccount(""); // Empty string
        parsedData.setToAccount(""); // Empty string

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(anyList())).thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should not detect as self-transfer (empty strings)
        assertEquals("TRANSACTION", response.getTransactions().get(0).getMessageType());
    }

    // ==================== SCENARIO 23: Balance Inquiry Message ====================
    @Test
    void testImportMessages_BalanceInquiry_SavesWithoutBudgetEntry() {
        // Given: Balance inquiry SMS
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Your A/c XX1234 current balance is Rs.15,000",
                "HDFCBK",
                1710234567000L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = ParsedSMSData.builder()
                .balance(BigDecimal.valueOf(15000))
                .accountNumber("XX1234")
                .parseStatus(SMSTransaction.ParseStatus.PARTIAL)
                .confidence(0.85)
                .build();

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("BALANCE_INQUIRY");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(ArgumentMatchers.<SMSTransaction>anyList()))
                .thenReturn(List.of(savedTransaction));

        // When: Import messages
        SMSImportResponse response = smsService.importMessages(request);

        // Then: Should save but not create budget entries
        assertEquals(1, response.getTransactions().size());
        assertFalse(response.getTransactions().get(0).isAddedToBudget());
        verify(budgetService, never()).addIncome(any());
        verify(budgetService, never()).addExpense(any());
    }

    // ==================== SCENARIO 24: Transaction Without Merchant ====================
    @Test
    void testImportMessages_TransactionWithoutMerchant_UsesDefaultDescription() {
        // Given: Transaction with null merchant
        SMSImportRequest.SMSMessage smsMessage = new SMSImportRequest.SMSMessage(
                "Rs.250 debited from A/c XX1234",
                "BANK",
                1L
        );
        request = new SMSImportRequest(userId, List.of(smsMessage));

        ParsedSMSData parsedData = createParsedData(
                BigDecimal.valueOf(250),
                LocalDate.now(),
                LocalTime.now(),
                SMSTransaction.TransactionType.DEBIT,
                null, // No merchant
                "XX1234",
                SMSTransaction.ParseStatus.PARTIAL,
                0.75
        );

        SMSTransaction savedTransaction = createSMSTransaction(1L, userId, smsMessage.getContent(), parsedData);
        savedTransaction.setMessageType("TRANSACTION");

        when(parserService.parseSMS(smsMessage.getContent())).thenReturn(parsedData);
        when(repository.saveAll(ArgumentMatchers.<SMSTransaction>anyList()))
                .thenReturn(List.of(savedTransaction));

        // When: Import messages
        smsService.importMessages(request);

        // Then: Expense uses default description
        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(budgetService, times(1)).addExpense(expenseCaptor.capture());
        assertEquals("SMS Transaction", expenseCaptor.getValue().getDescription());
    }

    // ==================== SCENARIO 25: High Volume Batch Processing ====================
    @Test
    void testImportMessages_HighVolumeBatch_ProcessesEfficiently() {
        // Given: 100 messages in one batch
        List<SMSImportRequest.SMSMessage> messages = new ArrayList<>();
        List<SMSTransaction> savedTransactions = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            messages.add(new SMSImportRequest.SMSMessage(
                    "Rs." + (i * 10) + " debited from A/c XX1234 at MERCHANT. Avl Bal: Rs.10000",
                    "HDFCBK",
                    (long) i
            ));

            ParsedSMSData parsed = createTransactionParsedData(i * 10);
            when(parserService.parseSMS(messages.get(i - 1).getContent())).thenReturn(parsed);

            SMSTransaction saved = createSMSTransaction((long) i, userId, messages.get(i - 1).getContent(), parsed);
            saved.setMessageType("TRANSACTION");
            savedTransactions.add(saved);
        }

        request = new SMSImportRequest(userId, messages);
        when(repository.saveAll(ArgumentMatchers.<SMSTransaction>anyList()))
                .thenReturn(savedTransactions);

        // When: Import large batch
        SMSImportResponse response = smsService.importMessages(request);

        // Then: All processed in single batch
        assertEquals(100, response.getTotalMessages());
        assertEquals(100, response.getTransactions().size());
        assertEquals(0, response.getErrors().size());

        // Verify single batch save (not 100 individual saves)
        verify(repository, times(1)).saveAll(ArgumentMatchers.<SMSTransaction>anyList());

        // Verify 100 expenses created (all are DEBIT)
        verify(budgetService, times(100)).addExpense(any(Expense.class));
    }

    // ==================== Helper Methods ====================

    private ParsedSMSData createParsedData(
            BigDecimal amount,
            LocalDate date,
            LocalTime time,
            SMSTransaction.TransactionType type,
            String merchant,
            String accountNumber,
            SMSTransaction.ParseStatus status,
            Double confidence
    ) {
        return ParsedSMSData.builder()
                .amount(amount)
                .transactionDate(date)
                .transactionTime(time)
                .transactionType(type)
                .merchant(merchant)
                .accountNumber(accountNumber)
                .parseStatus(status)
                .confidence(confidence)
                .build();
    }

    private ParsedSMSData createTransactionParsedData(int amount) {
        return createParsedData(
                BigDecimal.valueOf(amount),
                LocalDate.now(),
                LocalTime.now(),
                SMSTransaction.TransactionType.DEBIT,
                "MERCHANT",
                "XX1234",
                SMSTransaction.ParseStatus.SUCCESS,
                0.90
        );
    }

    private SMSTransaction createSMSTransaction(Long id, Long userId, String message, ParsedSMSData parsed) {
        return SMSTransaction.builder()
                .id(id)
                .userId(userId)
                .originalMessage(message)
                .amount(parsed.getAmount())
                .transactionDate(parsed.getTransactionDate())
                .transactionTime(parsed.getTransactionTime())
                .transactionType(parsed.getTransactionType())
                .merchant(parsed.getMerchant())
                .accountNumber(parsed.getAccountNumber())
                .parseStatus(parsed.getParseStatus())
                .parseConfidence(parsed.getConfidence())
                .fromAccount(parsed.getFromAccount())
                .toAccount(parsed.getToAccount())
                .build();
    }
}
