package com.vermeg.platform.supervision_platform.Service;
import com.vermeg.platform.supervision_platform.DTO.AlertDTO;
import com.vermeg.platform.supervision_platform.DTO.ApplicationStatusDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerMetricsDTO;
import com.vermeg.platform.supervision_platform.DTO.SupervisionResultDTO;
import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.ApplicationVersionRepository;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import com.vermeg.platform.supervision_platform.exception.ServerNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupervisionServiceImpl implements SupervisionService {

    private static final int RECENT_DEPLOYMENT_WINDOW_MINUTES = 30;
    private static final double CRITICAL_CPU_THRESHOLD = 90.0;
    private static final double CRITICAL_MEMORY_THRESHOLD = 90.0;

    private final ServerRepository serverRepository;
    private final ServerConnectivityService connectivityService;
    private final ServerMetricsService metricsService;
    private final AlertService alertService;
    private final WildFlyManagementClient wildFlyClient;
    private final ApplicationRepository applicationRepository;
    private final ApplicationVersionRepository versionRepository;
    private final RollbackService rollbackService;

    public SupervisionServiceImpl(ServerRepository serverRepository,
                                  ServerConnectivityService connectivityService,
                                  ServerMetricsService metricsService,
                                  AlertService alertService,
                                  WildFlyManagementClient wildFlyClient,
                                  ApplicationRepository applicationRepository,
                                  ApplicationVersionRepository versionRepository,
                                  RollbackService rollbackService) {
        this.serverRepository = serverRepository;
        this.connectivityService = connectivityService;
        this.metricsService = metricsService;
        this.alertService = alertService;
        this.wildFlyClient = wildFlyClient;
        this.applicationRepository = applicationRepository;
        this.versionRepository = versionRepository;
        this.rollbackService = rollbackService;
    }

    /* =========================
       SUPERVISE SINGLE SERVER
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

                // Step 3 — Check thresholds and create alerts
                alertService.checkServerAlerts(server, metrics);

                // Step 4 — Runtime instability rollback check
                checkRuntimeInstability(server, metrics);

            } catch (Exception e) {
                alertService.createServerAlert(server,
                        "Metrics collection failed: " + e.getMessage(),
                        AlertLevel.WARN);
            }

            // Step 5 — Check application statuses
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

            // Step 6 — Count unresolved alerts
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
       ========================= */
    @Override
    public List<SupervisionResultDTO> superviseAllServers() {
        return serverRepository.findAll()
                .stream()
                .filter(Server::isActive)
                .map(server -> superviseServer(server.getId()))
                .toList();
    }
    /* =========================
       RUNTIME INSTABILITY ROLLBACK
       Checks if CRITICAL metrics coincide with a recent deployment
       If yes → auto-rollback to last stable version
       ========================= */
    private void checkRuntimeInstability(Server server, ServerMetrics metrics) {
        boolean isCritical = metrics.getCpuUsage() > CRITICAL_CPU_THRESHOLD
                || metrics.getMemoryUsage() > CRITICAL_MEMORY_THRESHOLD;

        if (!isCritical) return;

        // Check if a new version was deployed in the last 30 minutes
        LocalDateTime windowStart = LocalDateTime.now()
                .minusMinutes(RECENT_DEPLOYMENT_WINDOW_MINUTES);

        Optional<ApplicationVersion> recentDeployment = versionRepository
                .findTopByApplicationServerIdAndStatusAndDeployedAtAfterOrderByDeployedAtDesc(
                        server.getId(), DeploymentStatus.DEPLOYED, windowStart);

        if (recentDeployment.isEmpty()) return;

        ApplicationVersion unstableVersion = recentDeployment.get();
        Application app = unstableVersion.getApplication();

        // Avoid duplicate rollback alerts
        String alertMessage = "Runtime instability detected for " + app.getName()
                + " version " + unstableVersion.getVersion()
                + " — CPU: " + String.format("%.1f", metrics.getCpuUsage())
                + "%, Memory: " + String.format("%.1f", metrics.getMemoryUsage()) + "%";

        alertService.createServerAlert(server, alertMessage, AlertLevel.CRITICAL);

        // Trigger auto-rollback
        rollbackService.attemptRollback(app, unstableVersion);
    }
}