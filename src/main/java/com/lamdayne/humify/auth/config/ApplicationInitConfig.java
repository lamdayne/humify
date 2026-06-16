package com.lamdayne.humify.auth.config;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.auth.service.ApplicationInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j(topic = "APPLICATION_INIT")
@Configuration
@RequiredArgsConstructor
public class ApplicationInitConfig {

    private final ApplicationInitService applicationInitService;

    @Bean
    public ApplicationRunner initApplicationRunner() {
        return application -> {
            try {
                CompanyContext.setAdmin(true);
                applicationInitService.initAll();
            } finally {
                CompanyContext.clear();
            }
        };
    }

}
