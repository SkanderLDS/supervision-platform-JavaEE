package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.AlertDTO;
import com.vermeg.platform.supervision_platform.DTO.ApplicationStatusDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerMetricsDTO;
import com.vermeg.platform.supervision_platform.DTO.SupervisionResultDTO;
import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import com.vermeg.platform.supervision_platform.exception.ServerNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SupervisionServiceImpl implements SupervisionService {

    private final ServerRepository serverRepository;
    private final ServerConnectivityService connectivityService;
    private final ServerMetricsService metricsService;
    private final AlertService alertService;
    private final WildFlyManagementClient wildFlyClient;
    private final ApplicationRepository applicationRepository;

    public SupervisionServiceImpl(ServerRepository serverRepository,
                                  ServerConnectivityService connectivityService,
                                  ServerMetricsService metricsService,
                                  AlertService alertService,WildFlyManagementClient wildFlyClient,
                                  ApplicationRepository applicationRepository) {
        this.serverRepository = serverRepository;
        this.connectivityService = connectivityService;
        this.metricsService = metricsService;
        this.alertService = alertService;
        this.wildFlyClient = wildFlyClient;
        this.applicationRepository = applicationRepository;
    }

    /* =========================
       SUPERVISE SINGLE SERVER
       1. Check connectivity
       2. Collect metrics if UP
       3. Check for alerts
       4. Return full supervision result
       ========================= */
    @Override
    public SupervisionResultDTO superviseServer(Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ServerNotFoundException(serverId));

        // Step 1 — Check connectivity
        ServerStatus status = connectivityService.checkGlobal(server);
        server.setStatus(status);
        server.setLastCheckedAt(LocalDateTime.now());
        serverRepository.save(server);

        ServerMetricsDTO metricsDTO = null;
        int unresolvedCount = 0;
        List<ApplicationStatusDTO> appStatuses = List.of();

        // Step 2 — Collect metrics only if server is UP
        if (status == ServerStatus.UP) {

            // Collect metrics
            try {
                ServerMetrics metrics = metricsService.collectMetrics(server);
                metricsDTO = ServerMetricsDTO.builder()
                        .id(metrics.getId())
                        .serverId(server.getId())
                        .serverName(server.getName())
                        .cpuUsage(metrics.getCpuUsage())
                        .memoryUsage(metrics.getMemoryUsage())
                        .diskUsage(metrics.getDiskUsage())
                        .collectedAt(metrics.getCollectedAt())
                        .build();

                // Step 3 — Check thresholds and create alerts if needed
                alertService.checkServerAlerts(server, metrics);

            } catch (Exception e) {
                alertService.createServerAlert(server,
                        "Metrics collection failed: " + e.getMessage(),
                        AlertLevel.WARN);
            }

            // Step 4 — Check application statuses
            server.getApplications().forEach(app -> {
                try {
                    String appStatus = wildFlyClient.getDeploymentStatus(
                            server, app.getRuntimeName());
                    if ("FAILED".equals(appStatus)) {
                        app.markFailed();
                        alertService.applicationFailed(app,
                                "Application is in FAILED state on WildFly");
                        applicationRepository.save(app);
                    } else if ("OK".equals(appStatus)) {
                        app.markDeployed();
                        applicationRepository.save(app);
                    }
                } catch (Exception e) {
                    alertService.createServerAlert(server,
                            "Could not check status of: " + app.getName(),
                            AlertLevel.WARN);
                }
            });

            // Collect application statuses for result
            appStatuses = server.getApplications()
                    .stream()
                    .map(app -> ApplicationStatusDTO.builder()
                            .id(app.getId())
                            .name(app.getName())
                            .runtimeName(app.getRuntimeName())
                            .status(app.getStatus().name())
                            .currentVersion(app.getCurrentVersion())
                            .build())
                    .toList();

            // Step 5 — Count unresolved alerts
            List<AlertDTO> unresolvedAlerts = alertService.getUnresolvedAlerts(serverId);
            unresolvedCount = unresolvedAlerts.size();
        }

        return SupervisionResultDTO.builder()
                .serverId(server.getId())
                .serverName(server.getName())
                .status(status.name())
                .metrics(metricsDTO)
                .applications(appStatuses)
                .unresolvedAlertsCount(unresolvedCount)
                .checkedAt(LocalDateTime.now())
                .build();
    }

    /* =========================
       SUPERVISE ALL SERVERS
       Runs supervision on every active server
       ========================= */
    @Override
    public List<SupervisionResultDTO> superviseAllServers() {
        return serverRepository.findAll()
                .stream()
                .filter(Server::isActive)
                .map(server -> superviseServer(server.getId()))
                .toList();
    }
}