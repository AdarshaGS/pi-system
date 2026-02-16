package com.upi.repository;

import com.upi.model.UpiId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UpiIdRepository extends JpaRepository<UpiId, Long> {

    // Change return type to Optional<UpiId> to handle cases where UPI ID is not found gracefully
    Optional<UpiId> findByUpiId(String upiId);

    List<UpiId> findByUserId(Long userId);
}