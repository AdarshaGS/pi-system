package com.lending.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alerts.entity.AlertChannel;
import com.alerts.entity.NotificationType;
import com.alerts.service.NotificationService;
import com.common.security.AuthenticationHelper;
import com.lending.data.LendingDTO;
import com.lending.data.LendingRecord;
import com.lending.data.LendingStatus;
import com.lending.data.Repayment;
import com.lending.data.RepaymentDTO;
import com.lending.repo.LendingRepository;
import com.lending.repo.RepaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LendingServiceImpl implements LendingService {

    private final LendingRepository lendingRepository;
    private final RepaymentRepository repaymentRepository;
    private final AuthenticationHelper authenticationHelper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public LendingDTO createLending(LendingDTO dto) {
        authenticationHelper.validateUserAccess(dto.getUserId());
        LendingRecord record = LendingRecord.builder()
                .userId(dto.getUserId())
                .borrowerName(dto.getBorrowerName())
                .borrowerContact(dto.getBorrowerContact())
                .amountLent(dto.getAmountLent())
                .amountRepaid(BigDecimal.ZERO)
                .outstandingAmount(dto.getAmountLent())
                .dateLent(dto.getDateLent())
                .dueDate(dto.getDueDate())
                .status(LendingStatus.PENDING)
                .notes(dto.getNotes())
                .build();

        return mapToDTO(lendingRepository.save(record));
    }

    @Override
    public List<LendingDTO> getUserLendings(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return lendingRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LendingDTO getLendingById(Long id) {
        LendingRecord record = lendingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lending record not found with id: " + id));
        authenticationHelper.validateUserAccess(record.getUserId());
        return mapToDTO(record);
    }

    @Override
    @Transactional
    public LendingDTO addRepayment(Long lendingId, RepaymentDTO repaymentDTO) {
        LendingRecord record = lendingRepository.findById(lendingId)
                .orElseThrow(() -> new RuntimeException("Lending record not found"));
        authenticationHelper.validateUserAccess(record.getUserId());

        if (repaymentDTO.getAmount().compareTo(record.getOutstandingAmount()) > 0) {
            throw new RuntimeException("Repayment amount cannot exceed outstanding amount");
        }

        Repayment repayment = Repayment.builder()
                .lendingRecord(record)
                .amount(repaymentDTO.getAmount())
                .repaymentDate(repaymentDTO.getRepaymentDate())
                .repaymentMethod(repaymentDTO.getRepaymentMethod())
                .notes(repaymentDTO.getNotes())
                .build();

        repaymentRepository.save(repayment);

        // Update record
        BigDecimal newAmountRepaid = record.getAmountRepaid().add(repaymentDTO.getAmount());
        record.setAmountRepaid(newAmountRepaid);
        record.setOutstandingAmount(record.getAmountLent().subtract(newAmountRepaid));

        // Update status
        if (record.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            record.setStatus(LendingStatus.PAID);
        } else {
            record.setStatus(LendingStatus.PARTIALLY_PAID);
        }

        return mapToDTO(lendingRepository.save(record));
    }

    @Override
    @Transactional
    public LendingDTO closeLending(Long id) {
        LendingRecord record = lendingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lending record not found"));
        authenticationHelper.validateUserAccess(record.getUserId());

        record.setAmountRepaid(record.getAmountLent());
        record.setOutstandingAmount(BigDecimal.ZERO);
        record.setStatus(LendingStatus.PAID);

        return mapToDTO(lendingRepository.save(record));
    }

    @Override
    @Transactional
    public LendingDTO updateLending(Long id, LendingDTO dto) {
        LendingRecord record = lendingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lending record not found"));
        authenticationHelper.validateUserAccess(record.getUserId());

        // Update basic information
        record.setBorrowerName(dto.getBorrowerName());
        record.setBorrowerContact(dto.getBorrowerContact());
        record.setDueDate(dto.getDueDate());
        record.setNotes(dto.getNotes());

        // Only update amount if no repayments have been made
        if (record.getAmountRepaid().compareTo(BigDecimal.ZERO) == 0) {
            record.setAmountLent(dto.getAmountLent());
            record.setOutstandingAmount(dto.getAmountLent());
        }

        return mapToDTO(lendingRepository.save(record));
    }

    private LendingDTO mapToDTO(LendingRecord record) {
        return LendingDTO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .borrowerName(record.getBorrowerName())
                .borrowerContact(record.getBorrowerContact())
                .amountLent(record.getAmountLent())
                .amountRepaid(record.getAmountRepaid())
                .outstandingAmount(record.getOutstandingAmount())
                .dateLent(record.getDateLent())
                .dueDate(record.getDueDate())
                .status(record.getStatus())
                .notes(record.getNotes())
                .repayments(record.getRepayments().stream()
                        .map(r -> RepaymentDTO.builder()
                                .id(r.getId())
                                .amount(r.getAmount())
                                .repaymentDate(r.getRepaymentDate())
                                .repaymentMethod(r.getRepaymentMethod())
                                .notes(r.getNotes())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public void sendPaymentReminder(Long lendingId) {
        LendingRecord record = lendingRepository.findById(lendingId)
                .orElseThrow(() -> new RuntimeException("Lending record not found"));
        authenticationHelper.validateUserAccess(record.getUserId());

        // Don't send reminder if already paid
        if (record.getStatus() == LendingStatus.PAID) {
            throw new RuntimeException("Cannot send reminder for fully paid lending");
        }

        String title = "Payment Reminder: " + record.getBorrowerName();
        String message = String.format(
            "Reminder: %s owes you ₹%.2f (Outstanding: ₹%.2f). Due date: %s. " +
            "Consider reaching out to the borrower for payment.",
            record.getBorrowerName(),
            record.getAmountLent(),
            record.getOutstandingAmount(),
            record.getDueDate()
        );

        notificationService.sendNotification(
            record.getUserId(),
            title,
            message,
            NotificationType.REMINDER,
            AlertChannel.IN_APP
        );
    }
}
