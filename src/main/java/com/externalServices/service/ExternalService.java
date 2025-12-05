package com.externalServices.service;

import com.externalServices.data.ExternalServiceEntity;

public interface ExternalService {

    ExternalServiceEntity getExternalServiceByName(String serviceName);
    
}
