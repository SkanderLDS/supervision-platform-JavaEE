package com.vermeg.platform.supervision_platform.Controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "Supervision Platform is running";
    }
}