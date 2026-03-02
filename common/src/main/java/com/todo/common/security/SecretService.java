package com.todo.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;


public class SecretService {
    private static final Logger log = LoggerFactory.getLogger(SecretService.class);

    public static String getSecret(String key) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isEmpty())
            log.info("Using environment variable for secret: {}",key);
        return envValue;
    }
}
