package com.pisystem.modules.stocks.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ResilienceConfigEntity {

    @Id
    private String id;
    private int limitForPeriod;
    private int timeoutDuration;
    private int maxAttempts;
    private long waitDuration;
}
