package com.pisystem.modules.lending.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pisystem.modules.lending.data.LendingRecord;
import com.pisystem.modules.lending.data.LendingStatus;

@Repository
public interface LendingRepository extends JpaRepository<LendingRecord, Long> {

    @Query("SELECT DISTINCT l FROM LendingRecord l LEFT JOIN FETCH l.repayments WHERE l.userId = :userId")
    List<LendingRecord> findByUserIdWithRepayments(@Param("userId") Long userId);

    @Query("SELECT l FROM LendingRecord l LEFT JOIN FETCH l.repayments WHERE l.id = :id")
    java.util.Optional<LendingRecord> findByIdWithRepayments(@Param("id") Long id);

    List<LendingRecord> findByStatusNotAndDueDateBefore(LendingStatus status, LocalDate date);

    List<LendingRecord> findByStatusNotAndDueDate(LendingStatus status, LocalDate date);

    List<LendingRecord> findByStatus(LendingStatus status);
}
