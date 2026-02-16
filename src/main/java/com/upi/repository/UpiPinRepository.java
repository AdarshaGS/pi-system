package com.upi.repository;

import com.upi.model.UpiPin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UpiPinRepository extends JpaRepository<UpiPin, Long> {
    Optional<UpiPin> findByUserId(Long userId);
}
