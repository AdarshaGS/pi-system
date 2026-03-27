package com.pisystem.integrations.externalservices.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pisystem.integrations.externalservices.data.ExternalServicePropertiesEntity;

public interface ExternalServicePropertiesRepository extends JpaRepository<ExternalServicePropertiesEntity, Long> {

    List<ExternalServicePropertiesEntity> findByExternalServiceId(Long externalServiceId);
}
