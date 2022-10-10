package com.mybank.dbs.payments.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

// tag::add-import-annotation[]
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class Application extends SpringBootServletInitializer {

    // end::add-import-annotation[]
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}