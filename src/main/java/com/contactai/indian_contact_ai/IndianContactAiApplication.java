package com.contactai.indian_contact_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IndianContactAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(IndianContactAiApplication.class, args);
    }
}