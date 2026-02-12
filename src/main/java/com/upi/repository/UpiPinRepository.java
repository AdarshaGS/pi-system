package com.upi.repository;

import com.upi.model.UpiPin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpiPinRepository extends JpaRepository<UpiPin, Long> {
    UpiPin findByUserId(Long userId);
}
