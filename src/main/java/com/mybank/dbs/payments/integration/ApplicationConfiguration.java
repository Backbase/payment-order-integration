package com.mybank.dbs.payments.integration;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    public static final String JSON_EXTENSION = ".json";
    public static final File ROOT_ORDER_FOLDER;

    static {
        try {
            ROOT_ORDER_FOLDER = Files.createTempDirectory("").toFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Autowired
    public void configureObjectMapper(ObjectMapper mapper) {
        mapper.registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    }

    @Bean
    public ObjectWriter objectWriter(ObjectMapper mapper) {
        return mapper.writer(new DefaultPrettyPrinter());
    }
}
