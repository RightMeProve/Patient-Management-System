package com.pm.analyticsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Analytics Service.
 * 
 * @SpringBootApplication is a convenience annotation that adds:
 * - @Configuration: Tags the class as a source of bean definitions.
 * - @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings.
 * - @ComponentScan: Tells Spring to look for other components, configurations, and services in the com.pm.analyticsservice package.
 */
@SpringBootApplication
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        // SpringApplication.run bootstraps the application, starting the Spring application context
        // and embedded web server (if configured).
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }

}
