package com.externalServices.service;

import java.util.List;

import com.externalServices.data.ExternalServiceEntity;
import com.externalServices.data.ExternalServicePropertiesEntity;

public interface ExternalService {

    List<ExternalServiceEntity> getAllServices();

    List<ExternalServicePropertiesEntity> getExternalServicePropertiesByServiceName(String serviceName);

    ExternalServiceEntity createService(String serviceName);

    ExternalServicePropertiesEntity createServiceProperty(Long serviceId, String propertyName, String propertyValue);

    ExternalServicePropertiesEntity updateServiceProperty(Long propertyId, String newValue);

}
