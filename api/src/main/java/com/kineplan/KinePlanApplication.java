package com.kineplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.kineplan.cabinet.infrastructure.SubscriptionPlanProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(SubscriptionPlanProperties.class)
public class KinePlanApplication {
    public static void main(String[] args) {
        SpringApplication.run(KinePlanApplication.class, args);
    }
}