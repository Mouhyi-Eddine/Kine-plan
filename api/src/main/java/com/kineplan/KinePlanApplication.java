package com.kineplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KinePlanApplication {
    public static void main(String[] args) {
        SpringApplication.run(KinePlanApplication.class, args);
    }
}