package com.vermeg.platform.supervision_platform.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import com.vermeg.platform.supervision_platform.Entity.Server;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class WildFlyManagementClientImpl implements WildFlyManagementClient {

    private static final int SSH_TIMEOUT = 10000;
    private static final int HTTP_TIMEOUT = 30000;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /* =========================
       DEPLOY via SCP (SSH)
       Professional: transfer WAR file directly to deployments folder
       WildFly auto-deploys via deployment scanner
       ========================= */
    @Override
    public void deploy(Server server, File warFile, String runtimeName) {
        // Step 1: Upload WAR via SCP
        String remotePath = getDeploymentsPath(server) + "/" + runtimeName;
        scpUpload(server, warFile, remotePath);

        // Step 2: Wait for WildFly to deploy and verify via Management API
        waitForDeploymentStatus(server, runtimeName, "OK", 60);
    }

    /* =========================
       UNDEPLOY via Management API
       Professional: direct API call with confirmation
       ========================= */
    @Override
    public void undeploy(Server server, String runtimeName) {
        Map<String, Object> operation = new HashMap<>();
        operation.put("operation", "undeploy");
        operation.put("address", buildDeploymentAddress(runtimeName));
        executeManagementOperation(server, operation);

        // Then remove the deployment entirely
        Map<String, Object> removeOp = new HashMap<>();
        removeOp.put("operation", "remove");
        removeOp.put("address", buildDeploymentAddress(runtimeName));
        executeManagementOperation(server, removeOp);
    }

    /* =========================
       START via Management API
       Professional: deploy operation re-enables a disabled deployment
       ========================= */
    @Override
    public void start(Server server, String runtimeName) {
        Map<String, Object> operation = new HashMap<>();
        operation.put("operation", "deploy");
        operation.put("address", buildDeploymentAddress(runtimeName));
        executeManagementOperation(server, operation);
    }

    /* =========================
       STOP via Management API
       Professional: undeploy disables without removing
       ========================= */
    @Override
    public void stop(Server server, String runtimeName) {
        Map<String, Object> operation = new HashMap<>();
        operation.put("operation", "undeploy");
        operation.put("address", buildDeploymentAddress(runtimeName));
        executeManagementOperation(server, operation);
    }

    /* =========================
       RESTART via Management API
       Professional: uses composite operation (atomic stop + start)
       ========================= */
    @Override
    public void restart(Server server, String runtimeName) {
        // WildFly composite operation — atomic, professional approach
        Map<String, Object> stopOp = new HashMap<>();
        stopOp.put("operation", "undeploy");
        stopOp.put("address", buildDeploymentAddress(runtimeName));

        Map<String, Object> startOp = new HashMap<>();
        startOp.put("operation", "deploy");
        startOp.put("address", buildDeploymentAddress(runtimeName));

        Map<String, Object> composite = new HashMap<>();
        composite.put("operation", "composite");
        composite.put("address", new Object[]{});
        composite.put("steps", new Object[]{stopOp, startOp});

        executeManagementOperation(server, composite);
    }

    /* =========================
       CHECK DEPLOYMENT STATUS via Management API
       Returns: OK, FAILED, NOT_FOUND
       ========================= */
    @Override
    public String getDeploymentStatus(Server server, String runtimeName) {
        try {
            Map<String, Object> operation = new HashMap<>();
            operation.put("operation", "read-attribute");
            operation.put("address", buildDeploymentAddress(runtimeName));
            operation.put("name", "status");

            Map response = executeManagementOperation(server, operation);
            Object result = response.get("result");
            return result != null ? result.toString() : "UNKNOWN";
        } catch (Exception e) {
            return "NOT_FOUND";
        }
    }

    /* =========================
       SCP UPLOAD via JSch SFTP
       ========================= */
    private void scpUpload(Server server, File localFile, String remotePath) {
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = createSshSession(server);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(SSH_TIMEOUT);

            try (FileInputStream fis = new FileInputStream(localFile)) {
                channel.put(fis, remotePath);
            }

        } catch (Exception e) {
            throw new RuntimeException("SCP upload failed: " + e.getMessage(), e);
        } finally {
            disconnectSftp(channel);
            disconnectSession(session);
        }
    }

    /* =========================
       MANAGEMENT API EXECUTION
       Centralized HTTP call to WildFly Management API
       ========================= */
    private Map executeManagementOperation(Server server, Map<String, Object> operation) {
        String url = buildManagementUrl(server);

        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                new AuthScope(server.getHost(), server.getManagementPort()),
                new UsernamePasswordCredentials(
                        server.getManagementUsername(),
                        server.getManagementPassword().toCharArray()
                )
        );

        HttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build();

        RestTemplate restTemplate = new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(httpClient)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(operation, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class
            );

            Map body = response.getBody();
            if (body == null) {
                throw new RuntimeException("Empty response from WildFly Management API");
            }

            String outcome = (String) body.get("outcome");
            if (!"success".equals(outcome)) {
                Object failureDesc = body.get("failure-description");
                throw new RuntimeException("WildFly operation failed: " + failureDesc);
            }

            return body;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Management API call failed: " + e.getMessage(), e);
        }
    }
    /* =========================
       WAIT FOR DEPLOYMENT STATUS
       Polls WildFly until deployment is OK or timeout
       Professional: ensures deployment completed before returning
       ========================= */
    private void waitForDeploymentStatus(Server server, String runtimeName,
                                         String expectedStatus, int timeoutSeconds) {
        long start = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                String status = getDeploymentStatus(server, runtimeName);

                if (expectedStatus.equals(status)) {
                    return; // Success
                }

                if ("FAILED".equals(status)) {
                    throw new RuntimeException("WildFly deployment failed for: " + runtimeName);
                }

                // Still deploying — wait and retry
                Thread.sleep(2000);

            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Error checking deployment status", e);
            }
        }

        throw new RuntimeException("Deployment timed out after "
                + timeoutSeconds + " seconds for: " + runtimeName);
    }

    /* =========================
       SSH SESSION FACTORY
       ========================= */
    private Session createSshSession(Server server) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(
                server.getSshUsername(),
                server.getHost(),
                server.getSshPort()
        );
        session.setPassword(server.getSshPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(SSH_TIMEOUT);
        return session;
    }

    /* =========================
       HELPERS
       ========================= */
    private String buildManagementUrl(Server server) {
        return "http://" + server.getHost()
                + ":" + server.getManagementPort()
                + "/management";
    }

    private String getDeploymentsPath(Server server) {
        return server.getServerHomePath() + "/standalone/deployments";
    }

    private Object[] buildDeploymentAddress(String runtimeName) {
        Map<String, String> deploymentAddress = new HashMap<>();
        deploymentAddress.put("deployment", runtimeName);
        return new Object[]{deploymentAddress};
    }

    private void disconnectSftp(ChannelSftp channel) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
    }

    private void disconnectSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}