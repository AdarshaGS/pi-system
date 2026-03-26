package com.externalServices.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.externalServices.data.ExternalServiceEntity;
import com.externalServices.data.ExternalServicePropertiesEntity;
import com.externalServices.repo.ExternalServicePropertiesRepository;
import com.externalServices.repo.ExternalServiceRepository;

@Service
public class ExternalServiceImpl implements ExternalService {

    private final ExternalServiceRepository externalServiceRepository;
    private final ExternalServicePropertiesRepository externalServicePropertiesRepository;

    public ExternalServiceImpl(ExternalServiceRepository externalServiceRepository,
            ExternalServicePropertiesRepository externalServicePropertiesRepository) {
        this.externalServiceRepository = externalServiceRepository;
        this.externalServicePropertiesRepository = externalServicePropertiesRepository;
    }

    @Override
    public List<ExternalServiceEntity> getAllServices() {
        return externalServiceRepository.findAll();
    }

    @Override
    public List<ExternalServicePropertiesEntity> getExternalServicePropertiesByServiceName(String serviceName) {
        List<ExternalServicePropertiesEntity> externalServiceProperties = null;
        ExternalServiceEntity entity = externalServiceRepository.findByServiceName(serviceName);
        if (entity != null) {
            externalServiceProperties = this.externalServicePropertiesRepository
                    .findByExternalServiceId(entity.getId());
        }
        return externalServiceProperties;
    }

    @Override
    public ExternalServiceEntity createService(String serviceName) {
        ExternalServiceEntity service = ExternalServiceEntity.builder()
                .serviceName(serviceName)
                .build();
        return externalServiceRepository.save(service);
    }

    @Override
    public ExternalServicePropertiesEntity createServiceProperty(Long serviceId, String propertyName, String propertyValue) {
        ExternalServiceEntity service = externalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + serviceId));
        
        ExternalServicePropertiesEntity property = ExternalServicePropertiesEntity.builder()
                .name(propertyName)
                .value(propertyValue)
                .externalService(service)
                .build();
        
        return externalServicePropertiesRepository.save(property);
    }

    @Override
    public ExternalServicePropertiesEntity updateServiceProperty(Long propertyId, String newValue) {
        ExternalServicePropertiesEntity property = externalServicePropertiesRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyId));
        
        property.setValue(newValue);
        return externalServicePropertiesRepository.save(property);
    }

}
