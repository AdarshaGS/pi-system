package com.savings.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.savings.data.FixedDeposit;
import com.savings.data.FixedDepositDTO;
import com.savings.repo.FixedDepositRepository;
import com.savings.exception.DuplicateSavingsEntityException;
import com.savings.exception.SavingsEntityNotFoundException;

@Service
public class FixedDepositServiceImpl implements FixedDepositService {

    private final FixedDepositRepository repository;

    public FixedDepositServiceImpl(FixedDepositRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public FixedDepositDTO createFixedDeposit(FixedDeposit fixedDeposit) {
        // Calculate maturity date
        LocalDate maturityDate = fixedDeposit.getStartDate().plusMonths(fixedDeposit.getTenureMonths());
        fixedDeposit.setMaturityDate(maturityDate);

        // Calculate maturity amount using compound interest formula
        // A = P(1 + r/n)^(nt) where n=4 (quarterly compounding)
        BigDecimal principal = fixedDeposit.getPrincipalAmount();
        BigDecimal rate = fixedDeposit.getInterestRate().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        int months = fixedDeposit.getTenureMonths();
        double years = months / 12.0;
        int n = 4; // quarterly compounding

        double maturityAmountValue = principal.doubleValue() *
                Math.pow(1 + (rate.doubleValue() / n), n * years);

        BigDecimal maturityAmount = BigDecimal.valueOf(maturityAmountValue)
                .setScale(2, RoundingMode.HALF_UP);

        fixedDeposit.setMaturityAmount(maturityAmount);
        fixedDeposit.setStatus("ACTIVE");

        try {
            FixedDeposit saved = repository.save(fixedDeposit);
            return convertToDTO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSavingsEntityException("Fixed Deposit", fixedDeposit.getBankName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FixedDepositDTO getFixedDeposit(Long id, Long userId) {
        FixedDeposit fd = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SavingsEntityNotFoundException("Fixed Deposit", id));
        return convertToDTO(fd);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FixedDepositDTO> getAllFixedDeposits(Long userId) {
        return repository.findAllByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }   

    @Override
    @Transactional
    public FixedDepositDTO updateFixedDeposit(Long id, Long userId, FixedDeposit fixedDeposit) {
        FixedDeposit existing = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SavingsEntityNotFoundException("Fixed Deposit", id));

        // Update fields
        existing.setBankName(fixedDeposit.getBankName());
        existing.setAccountNumber(fixedDeposit.getAccountNumber());
        existing.setPrincipalAmount(fixedDeposit.getPrincipalAmount());
        existing.setInterestRate(fixedDeposit.getInterestRate());
        existing.setTenureMonths(fixedDeposit.getTenureMonths());
        existing.setStartDate(fixedDeposit.getStartDate());
        existing.setStatus(fixedDeposit.getStatus());

        // Recalculate maturity date and amount
        LocalDate maturityDate = existing.getStartDate().plusMonths(existing.getTenureMonths());
        existing.setMaturityDate(maturityDate);

        BigDecimal principal = existing.getPrincipalAmount();
        BigDecimal rate = existing.getInterestRate().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        int months = existing.getTenureMonths();
        double years = months / 12.0;
        int n = 4;

        double maturityAmountValue = principal.doubleValue() *
                Math.pow(1 + (rate.doubleValue() / n), n * years);

        existing.setMaturityAmount(BigDecimal.valueOf(maturityAmountValue)
                .setScale(2, RoundingMode.HALF_UP));

        try {
            FixedDeposit updated = repository.save(existing);
            return convertToDTO(updated);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSavingsEntityException("Fixed Deposit", fixedDeposit.getBankName());
        }
    }

    @Override
    @Transactional
    public void deleteFixedDeposit(Long id, Long userId) {
        FixedDeposit fd = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SavingsEntityNotFoundException("Fixed Deposit", id));
        repository.delete(fd);
    }

    private FixedDepositDTO convertToDTO(FixedDeposit fd) {
        return FixedDepositDTO.builder()
                .id(fd.getId())
                .userId(fd.getUserId())
                .bankName(fd.getBankName())
                .accountNumber(fd.getAccountNumber())
                .principalAmount(fd.getPrincipalAmount())
                .interestRate(fd.getInterestRate())
                .tenureMonths(fd.getTenureMonths())
                .maturityAmount(fd.getMaturityAmount())
                .startDate(fd.getStartDate())
                .maturityDate(fd.getMaturityDate())
                .status(fd.getStatus())
                .build();
    }
}
