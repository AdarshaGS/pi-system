package com.externalServices.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.externalServices.data.ExternalServiceEntity;
import com.externalServices.service.ExternalService;

@RestController
@RequestMapping("/api/external-services")
public class ExternalServiceAPIResource {

    private final ExternalService externalService;

    public ExternalServiceAPIResource(final ExternalService externalService) {
        this.externalService = externalService;
    }
    

    @GetMapping("/{serviceName}")
    public ExternalServiceEntity getExternalService(@PathVariable String serviceName) {
        return this.externalService.getExternalServiceByName(serviceName);
    }
}
