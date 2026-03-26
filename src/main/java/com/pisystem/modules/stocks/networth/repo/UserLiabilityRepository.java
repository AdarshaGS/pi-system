package com.investments.stocks.networth.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.investments.stocks.networth.data.UserLiability;

@Repository
public interface UserLiabilityRepository extends JpaRepository<UserLiability, Long> {
    List<UserLiability> findByUserId(Long userId);
}
