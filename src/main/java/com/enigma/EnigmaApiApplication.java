package com.enigma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the browser game and its Java REST API. */
@SpringBootApplication
public class EnigmaApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnigmaApiApplication.class, args);
    }
}
