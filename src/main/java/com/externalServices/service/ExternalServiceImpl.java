package com.externalServices.service;

import com.externalServices.data.ExternalServiceEntity;
import com.externalServices.repo.ExternalServiceRepository;

public class ExternalServiceImpl implements ExternalService {

    private final ExternalServiceRepository externalServiceRepository;

    public ExternalServiceImpl(ExternalServiceRepository externalServiceRepository) {
        this.externalServiceRepository = externalServiceRepository;
    }

    @Override
    public ExternalServiceEntity getExternalServiceByName(String serviceName) {
        return externalServiceRepository.findByServiceName(serviceName);
    }

    
}
