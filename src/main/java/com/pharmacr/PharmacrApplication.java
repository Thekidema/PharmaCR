package com.pharmacr;

import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PharmacrApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.of("es", "CR"));
        SpringApplication.run(PharmacrApplication.class, args);
    }
}
