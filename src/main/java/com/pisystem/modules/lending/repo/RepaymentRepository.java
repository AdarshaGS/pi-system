package com.pisystem.modules.lending.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pisystem.modules.lending.data.Repayment;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
}
