package com.example.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Catalog application.
 * This class is responsible for bootstrapping the Spring Boot application.
 */
@SpringBootApplication
public class Application {

    /**
     * The main method that starts the Spring Boot application.
     *
     * @param args command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}