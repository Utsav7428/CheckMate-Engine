package com.checkmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CheckmateEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(CheckmateEngineApplication.class, args);
    }
}
