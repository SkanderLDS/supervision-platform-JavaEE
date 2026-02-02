package com.vermeg.platform.supervision_platform.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vermeg.platform.supervision_platform.Entity.Server;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class WildFlyManagementClientImpl
        implements WildFlyManagementClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void deploy(Server server, File warFile, String runtimeName) {
        try {
            byte[] bytes = Files.readAllBytes(warFile.toPath());
            String content = Base64.getEncoder().encodeToString(bytes);
            Map<String, Object> operation = new HashMap<>();
            operation.put("operation", "add");
            operation.put("address", new Object[]{
                    new Object[]{"deployment", runtimeName}
            });
            operation.put("content", new Object[]{
                    Map.of("bytes", content)
            });
            operation.put("enabled", true);
            execute(server, operation);
        } catch (Exception e) {
            throw new RuntimeException("WildFly deploy failed", e);
        }
    }

    @Override
    public void undeploy(Server server, String runtimeName) {

        Map<String, Object> operation = new HashMap<>();
        operation.put("operation", "remove");
        operation.put("address", new Object[]{
                new Object[]{"deployment", runtimeName}});
        execute(server, operation);

    }

    @Override
    public void start(Server server, String runtimeName) {

        Map<String, Object> operation = new HashMap<>();
        operation.put("operation", "deploy");
        operation.put("address", new Object[]{
                new Object[]{"deployment", runtimeName}
        });

        execute(server, operation);
    }

    @Override
    public void stop(Server server, String runtimeName) {

        Map<String, Object> operation = new HashMap<>();
        operation.put("operation", "undeploy");
        operation.put("address", new Object[]{
                new Object[]{"deployment", runtimeName}
        });
        execute(server, operation);
    }

    private void execute(Server server, Map<String, Object> operation) {

        String url = "http://" + server.getHost()
                + ":" + server.getManagementPort()
                + "/management";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(server.getManagementUsername(), server.getManagementPassword()
        );
        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(operation, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, request, Map.class
        );
        if (response.getBody() == null
                || !"success".equals(response.getBody().get("outcome"))) {
            throw new RuntimeException("WildFly management operation failed: " + response.getBody());
        }
    } //i have verified with the "encadreur" and he told me that SSH also for deployments and etc... so not API management , we can work with SSH
}
