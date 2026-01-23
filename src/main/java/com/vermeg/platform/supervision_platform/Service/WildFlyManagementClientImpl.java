package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class WildFlyManagementClientImpl implements WildFlyManagementClient {

    private final RestTemplate restTemplate;
    private final String username;
    private final String password;

    public WildFlyManagementClientImpl(
            RestTemplateBuilder builder,
            @Value("${wildfly.management.username}") String username,
            @Value("${wildfly.management.password}") String password
    ) {
        this.restTemplate = builder.build();
        this.username = username;
        this.password = password;
    }

    /* =========================
       DEPLOY
       ========================= */
    @Override
    public void deploy(Server server, File warFile, String runtimeName) {

        String url = managementUrl(server);

        Map<String, Object> body = Map.of(
                "operation", "add",
                "address", List.of("deployment", runtimeName),
                "content", List.of(Map.of(
                        "path", warFile.getAbsolutePath(),
                        "archive", true
                )),
                "enabled", true
        );

        execute(url, body);
    }

    /* =========================
       UNDEPLOY
       ========================= */
    @Override
    public void undeploy(Server server, String runtimeName) {

        String url = managementUrl(server);

        Map<String, Object> body = Map.of(
                "operation", "remove",
                "address", List.of("deployment", runtimeName)
        );

        execute(url, body);
    }

    /* =========================
       START
       ========================= */
    @Override
    public void start(Server server, String runtimeName) {

        String url = managementUrl(server);

        Map<String, Object> body = Map.of(
                "operation", "deploy",
                "address", List.of("deployment", runtimeName)
        );

        execute(url, body);
    }

    /* =========================
       STOP
       ========================= */
    @Override
    public void stop(Server server, String runtimeName) {

        String url = managementUrl(server);

        Map<String, Object> body = Map.of(
                "operation", "undeploy",
                "address", List.of("deployment", runtimeName)
        );

        execute(url, body);
    }

    /* =========================
       INTERNAL HELPERS
       ========================= */
    private String managementUrl(Server server) {
        return "http://" + server.getHost() + ":9990/management";
    }

    private void execute(String url, Map<String, Object> body) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        if (!"success".equals(response.getBody().get("outcome"))) {
            throw new RuntimeException("WildFly operation failed: " + response.getBody());
        }
    }
}