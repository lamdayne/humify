package com.lamdayne.humify.auth.config;

import com.lamdayne.humify.auth.security.rls.RlsDataSourceWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean("originalDataSource")
    @ConfigurationProperties("spring.datasource")
    public DataSource originalDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    public DataSource dataSource(
            @Qualifier("originalDataSource") DataSource originalDataSource
    ) {
        return new RlsDataSourceWrapper(originalDataSource);
    }

}
