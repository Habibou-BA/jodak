package com.jodak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de la plateforme JO (API REST + Web Service SOAP).
 */
@SpringBootApplication
public class JodakApplication {

    public static void main(String[] args) {
        SpringApplication.run(JodakApplication.class, args);
    }
}
