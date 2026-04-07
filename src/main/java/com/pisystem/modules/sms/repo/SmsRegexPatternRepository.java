package com.pisystem.modules.sms.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pisystem.modules.sms.data.SmsRegexPattern;

public interface SmsRegexPatternRepository extends JpaRepository<SmsRegexPattern, Long> {

    List<SmsRegexPattern> findByIsActiveTrue();

    Optional<SmsRegexPattern> findByPatternKey(String patternKey);
}
