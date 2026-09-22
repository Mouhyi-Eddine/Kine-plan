package com.kineplan.shared.infrastructure.tenancy;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TenantDataSourceConfiguration {
    @Bean
    @Primary
    DataSource tenantAwareDataSource(DataSourceProperties properties) {
        return new TenantAwareDataSource(properties.initializeDataSourceBuilder().build());
    }
}