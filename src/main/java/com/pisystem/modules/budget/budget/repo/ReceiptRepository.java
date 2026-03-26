package com.budget.repo;

import com.budget.data.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    
    List<Receipt> findByExpenseId(Long expenseId);
    
    void deleteByExpenseId(Long expenseId);
}
