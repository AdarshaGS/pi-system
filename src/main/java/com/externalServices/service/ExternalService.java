package com.externalServices.service;

import java.util.List;

import com.externalServices.data.ExternalServicePropertiesEntity;

public interface ExternalService {

    List<ExternalServicePropertiesEntity> getExternalServicePropertiesByServiceName(String serviceName);

}
