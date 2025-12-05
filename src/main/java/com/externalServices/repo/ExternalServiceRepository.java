package com.externalServices.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.externalServices.data.ExternalServiceEntity;

public interface ExternalServiceRepository extends JpaRepository<ExternalServiceEntity, Long> {

    ExternalServiceEntity findByServiceName(String serviceName);
    
}
