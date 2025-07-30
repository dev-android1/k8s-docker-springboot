package com.nagp.assignment;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger {

    @Autowired
    private Environment env;

    @PostConstruct
    public void logJdbcUrl() {
        System.out.println("ENV spring.datasource.url = " + env.getProperty("spring.datasource.url"));
        System.out.println("ENV DB_HOST = " + System.getenv("DB_HOST"));
    }
}