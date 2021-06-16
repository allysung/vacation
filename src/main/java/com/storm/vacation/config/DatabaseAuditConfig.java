package com.storm.vacation.config;

import com.storm.vacation.audit.AuditAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * this class for Audit Config
 */
@EnableJpaAuditing
@Configuration
public class DatabaseAuditConfig {

  @Bean
  public AuditAware auditorAware() {
    return new AuditAware();
  }

}
