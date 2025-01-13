package com.nhd.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//import org.springframework.boot.actuate.health.Health;

@RestController
public class HealthCheckController {

//    private final LivenessIndicator livenessIndicator;
//
//    public HealthCheckController(LivenessIndicator livenessIndicator) {
//        this.livenessIndicator = livenessIndicator;
//    }

//    @GetMapping("/health/liveness")
//    public Health liveness() {
//        return Health.status(livenessIndicator.health().getStatus())
//                .withDetails(livenessIndicator.health().getDetails())
//                .build();
//    }

    @GetMapping("/health/readiness")
    public String readiness() {
        return "";
    }

}

//@Component
//class LivenessIndicator implements HealthIndicator {
//
//    @Override
//    public Health health() {
//        return Health.up().withDetail("livenessstate", "UP").build();
//    }
//
//}
