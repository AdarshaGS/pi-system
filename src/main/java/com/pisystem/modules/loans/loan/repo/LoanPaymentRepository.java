package com.loan.repo;

import com.loan.data.LoanPayment;
import com.loan.data.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    List<LoanPayment> findByLoanId(Long loanId);

    List<LoanPayment> findByLoanIdOrderByPaymentDateDesc(Long loanId);

    List<LoanPayment> findByLoanIdAndPaymentStatus(Long loanId, PaymentStatus status);

    @Query("SELECT p FROM LoanPayment p WHERE p.loanId = :loanId AND p.paymentStatus = 'MISSED'")
    List<LoanPayment> findMissedPaymentsByLoanId(@Param("loanId") Long loanId);

    @Query("SELECT COUNT(p) FROM LoanPayment p WHERE p.loanId = :loanId AND p.paymentStatus = 'MISSED'")
    Long countMissedPaymentsByLoanId(@Param("loanId") Long loanId);

    @Query("SELECT p FROM LoanPayment p WHERE p.loanId = :loanId AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<LoanPayment> findPaymentsByDateRange(
        @Param("loanId") Long loanId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
}
