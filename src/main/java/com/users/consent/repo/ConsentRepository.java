package com.users.consent.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.users.consent.data.Consent;

public interface ConsentRepository extends JpaRepository<Consent, Long>, JpaSpecificationExecutor<Consent> {

    // @Query(value = "SELECT * FROM user_consent uc WHERE uc.user_id =:userId",
    // nativeQuery = true)
    // Consent findOneByUserId(@Param("userId") Long userId);

}
