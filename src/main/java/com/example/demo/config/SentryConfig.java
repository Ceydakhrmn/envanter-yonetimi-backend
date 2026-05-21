package com.example.demo.config;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentryConfig {

    @Value("${SENTRY_DSN:}")
    private String dsn;

    @PostConstruct
    public void init() {
        if (dsn == null || dsn.isBlank()) return;

        Sentry.init(options -> {
            options.setDsn(dsn);
            options.setTracesSampleRate(0.2);
            options.setDebug(false);
        });
    }
}
