package com.tys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TysApplication {

    public static void main(String[] args) {
        SpringApplication.run(TysApplication.class, args);
    }
}
