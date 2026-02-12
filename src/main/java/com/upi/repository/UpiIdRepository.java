package com.upi.repository;

import com.upi.model.UpiId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpiIdRepository extends JpaRepository<UpiId, Long> {
    UpiId findByUpiId(String upiId);
}
