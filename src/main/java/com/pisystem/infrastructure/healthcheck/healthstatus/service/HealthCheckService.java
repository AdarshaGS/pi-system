package com.healthstatus.service;

import com.healthstatus.data.HealthStatus;

public interface HealthCheckService {

    /**
     * Get overall health status of the application including all components
     * 
     * @return HealthStatus object with aggregated health information
     */
    HealthStatus getOverallHealth();
}
