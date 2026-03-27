package com.pisystem.integrations.externalservices.service;

import java.util.List;

import com.pisystem.integrations.externalservices.data.ExternalServiceEntity;
import com.pisystem.integrations.externalservices.data.ExternalServicePropertiesEntity;

public interface ExternalService {

    List<ExternalServiceEntity> getAllServices();

    List<ExternalServicePropertiesEntity> getExternalServicePropertiesByServiceName(String serviceName);

    ExternalServiceEntity createService(String serviceName);

    ExternalServicePropertiesEntity createServiceProperty(Long serviceId, String propertyName, String propertyValue);

    ExternalServicePropertiesEntity updateServiceProperty(Long propertyId, String newValue);

}
