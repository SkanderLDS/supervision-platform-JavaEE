package com.vermeg.platform.supervision_platform.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WildFlyManagementClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wildfly.management.url}")
    private String managementUrl;

    @Value("${wildfly.management.username}")
    private String username;

    @Value("${wildfly.management.password}")
    private String password;

    public Map<String, Object> readDeployments() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);

        Map<String, Object> body = new HashMap<>();
        body.put("operation", "read-children-names");
        body.put("child-type", "deployment");

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                managementUrl + "/management",
                entity,
                Map.class
        );

        return response.getBody();
    }
}
