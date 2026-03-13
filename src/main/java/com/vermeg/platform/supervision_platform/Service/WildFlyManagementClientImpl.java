package com.vermeg.platform.supervision_platform.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import java.io.ByteArrayOutputStream;
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

        validateArtifact(warFile);
        String deploymentsPath = getDeploymentsPath(server);
        executeSSHCommand(server, "rm -f " + deploymentsPath + "/" + runtimeName + ".deployed");
        executeSSHCommand(server, "rm -f " + deploymentsPath + "/" + runtimeName + ".failed");
        executeSSHCommand(server, "rm -f " + deploymentsPath + "/" + runtimeName + ".pending");
        executeSSHCommand(server, "rm -f " + deploymentsPath + "/" + runtimeName + ".dodeploy");
        executeSSHCommand(server, "rm -f " + deploymentsPath + "/" + runtimeName);
        String remotePath = deploymentsPath + "/" + runtimeName;
        scpUpload(server, warFile, remotePath);
        executeSSHCommand(server, "touch " + deploymentsPath + "/" + runtimeName + ".dodeploy");
        waitForDeploymentStatus(server, runtimeName, "OK", 60);
    }

    /* =========================
       VALIDATE ARTIFACT
       Checks if WAR/EAR is a valid ZIP file before uploading
       ========================= */
    private void validateArtifact(File artifact) {
        if (!artifact.exists()) {
            throw new RuntimeException("Artifact file not found: " + artifact.getPath());
        }
        if (artifact.length() == 0) {
            throw new RuntimeException("Artifact file is empty: " + artifact.getName());
        }
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(artifact)) {
            // Valid ZIP/WAR/EAR file — has at least one entry
            if (zip.size() == 0) {
                throw new RuntimeException("Artifact file is empty ZIP: " + artifact.getName());
            }
        } catch (java.util.zip.ZipException e) {
            throw new RuntimeException("Invalid WAR/EAR file — not a valid ZIP archive: "
                    + artifact.getName(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate artifact: " + e.getMessage(), e);
        }
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

        String deploymentsPath = getDeploymentsPath(server);
        String failedMarker = deploymentsPath + "/" + runtimeName + ".failed";
        String deployedMarker = deploymentsPath + "/" + runtimeName + ".deployed";

        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                // Check for .failed marker file via SSH
                if (fileExistsOnServer(server, failedMarker)) {
                    executeSSHCommand(server, "rm -f " + failedMarker);
                    throw new RuntimeException(
                            "WildFly deployment failed for: " + runtimeName);
                }

                // Check for .deployed marker — but also verify Management API
                if (fileExistsOnServer(server, deployedMarker)) {
                    // Double check via Management API
                    String apiStatus = getDeploymentStatus(server, runtimeName);
                    if ("OK".equals(apiStatus)) {
                        return; // Truly successful
                    }
                    if ("FAILED".equals(apiStatus)) {
                        executeSSHCommand(server, "rm -f " + deployedMarker);
                        throw new RuntimeException(
                                "WildFly deployment failed for: " + runtimeName);
                    }
                    // Status not yet determined — wait more
                }

                // Check Management API status directly
                String status = getDeploymentStatus(server, runtimeName);
                if ("FAILED".equals(status)) {
                    throw new RuntimeException(
                            "WildFly deployment failed for: " + runtimeName);
                }
                if (expectedStatus.equals(status)) {
                    return; // Success
                }

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



    private boolean fileExistsOnServer(Server server, String filePath) {
        try {
            String result = executeSSHCommand(server,
                    "test -f " + filePath + " && echo EXISTS || echo NOT_FOUND");
            return result.trim().equals("EXISTS");
        } catch (Exception e) {
            return false;
        }
    }

    /* =========================
       EXECUTE SSH COMMAND
       ========================= */
    private String executeSSHCommand(Server server, String command) {
        Session session = null;
        ChannelExec channel = null;
        try {
            session = createSshSession(server);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            channel.setOutputStream(outputStream);
            channel.connect(SSH_TIMEOUT);

            // Wait for command to complete
            while (!channel.isClosed()) {
                Thread.sleep(500);
            }

            return outputStream.toString();
        } catch (Exception e) {
            throw new RuntimeException("SSH command failed: " + e.getMessage(), e);
        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
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