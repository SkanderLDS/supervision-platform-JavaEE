package com.vermeg.platform.supervision_platform.Service;

import com.jcraft.jsch.*;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;
import com.vermeg.platform.supervision_platform.Repository.ServerMetricsRepository;
import com.vermeg.platform.supervision_platform.exception.ServerNotFoundException;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ServerMetricsServiceImpl implements ServerMetricsService {

    private static final int SSH_TIMEOUT = 10000;

    private final ServerMetricsRepository metricsRepository;
    private final ServerRepository serverRepository;

    public ServerMetricsServiceImpl(ServerMetricsRepository metricsRepository,
                                    ServerRepository serverRepository) {
        this.metricsRepository = metricsRepository;
        this.serverRepository = serverRepository;
    }

    /* =========================
       COLLECT METRICS via SSH
       Reads real metrics from the remote server
       ========================= */
    @Override
    public ServerMetrics collectMetrics(Server server) {
        try {
            double cpu = collectCpuUsage(server);
            double memory = collectMemoryUsage(server);
            double disk = collectDiskUsage(server);

            ServerMetrics metrics = ServerMetrics.builder()
                    .server(server)
                    .cpuUsage(cpu)
                    .memoryUsage(memory)
                    .diskUsage(disk)
                    .build();

            return metricsRepository.save(metrics);

        } catch (Exception e) {
            throw new RuntimeException("Failed to collect metrics from server "
                    + server.getHost() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public ServerMetrics collectMetricsById(Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ServerNotFoundException(serverId));
        return collectMetrics(server);
    }

    @Override
    public ServerMetrics getLatestMetrics(Long serverId) {
        return metricsRepository
                .findTopByServerIdOrderByCollectedAtDesc(serverId)
                .orElseThrow(() -> new RuntimeException("No metrics available for server: " + serverId));
    }

    @Override
    public List<ServerMetrics> getMetricsHistory(Long serverId) {
        return metricsRepository.findByServerIdOrderByCollectedAtDesc(serverId);
    }

    /* =========================
       CPU USAGE
       Uses mpstat if available, falls back to /proc/stat
       ========================= */
    private double collectCpuUsage(Server server) throws Exception {
        // /proc/stat is the most reliable cross-distro approach
        String output = executeCommand(server,
                "top -bn1 | grep 'Cpu(s)' | awk '{print $2 + $4}'");
        return parseDouble(output.trim());
    }

    /* =========================
       MEMORY USAGE
       Uses free command — available on all Linux distros
       ========================= */
    private double collectMemoryUsage(Server server) throws Exception {
        String output = executeCommand(server,
                "free | awk '/Mem:/ {printf \"%.2f\", ($3/$2)*100}'");
        return parseDouble(output.trim());
    }

    /* =========================
       DISK USAGE
       Uses df for root partition
       ========================= */
    private double collectDiskUsage(Server server) throws Exception {
        String output = executeCommand(server,
                "df / | awk 'NR==2 {gsub(\"%\",\"\"); print $5}'");
        return parseDouble(output.trim());
    }

    /* =========================
       SSH COMMAND EXECUTION
       ========================= */
    private String executeCommand(Server server, String command) throws Exception {
        Session session = null;
        ChannelExec channel = null;

        try {
            session = createSession(server);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            channel.setOutputStream(outputStream);
            channel.setErrStream(errorStream);

            channel.connect(SSH_TIMEOUT);

            // Wait for command to complete
            while (!channel.isClosed()) {
                Thread.sleep(200);
            }

            int exitStatus = channel.getExitStatus();
            if (exitStatus != 0) {
                throw new RuntimeException("Command failed: " + errorStream);
            }

            return outputStream.toString().trim();

        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    /* =========================
       SSH SESSION FACTORY
       ========================= */
    private Session createSession(Server server) throws JSchException {
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
       SAFE DOUBLE PARSER
       ========================= */
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}