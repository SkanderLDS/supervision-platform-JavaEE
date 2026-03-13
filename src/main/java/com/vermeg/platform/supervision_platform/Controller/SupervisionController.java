package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.AlertDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerMetricsDTO;
import com.vermeg.platform.supervision_platform.DTO.SupervisionResultDTO;
import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;
import com.vermeg.platform.supervision_platform.Service.AlertService;
import com.vermeg.platform.supervision_platform.Service.ServerMetricsService;
import com.vermeg.platform.supervision_platform.Service.SupervisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supervision")
public class SupervisionController {

    private final SupervisionService supervisionService;
    private final ServerMetricsService metricsService;
    private final AlertService alertService;

    public SupervisionController(SupervisionService supervisionService,
                                 ServerMetricsService metricsService,
                                 AlertService alertService) {
        this.supervisionService = supervisionService;
        this.metricsService = metricsService;
        this.alertService = alertService;
    }

    @PostMapping("/servers/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<SupervisionResultDTO> superviseServer(@PathVariable Long id) {
        return ResponseEntity.ok(supervisionService.superviseServer(id));
    }

    @PostMapping("/servers")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<SupervisionResultDTO>> superviseAll() {
        return ResponseEntity.ok(supervisionService.superviseAllServers());
    }

    @GetMapping("/servers/{id}/metrics")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_VIEWER')")
    public ResponseEntity<ServerMetricsDTO> getLatestMetrics(@PathVariable Long id) {
        ServerMetrics metrics = metricsService.getLatestMetrics(id);
        return ResponseEntity.ok(toMetricsDTO(metrics));
    }

    @GetMapping("/servers/{id}/metrics/history")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_VIEWER')")
    public ResponseEntity<List<ServerMetricsDTO>> getMetricsHistory(@PathVariable Long id) {
        List<ServerMetricsDTO> history = metricsService.getMetricsHistory(id)
                .stream()
                .map(this::toMetricsDTO)
                .toList();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/servers/{id}/alerts")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_VIEWER')")
    public ResponseEntity<List<AlertDTO>> getAlerts(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlerts(id));
    }

    @GetMapping("/servers/{id}/alerts/unresolved")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_VIEWER')")
    public ResponseEntity<List<AlertDTO>> getUnresolvedAlerts(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getUnresolvedAlerts(id));
    }

    @PatchMapping("/alerts/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> resolveAlert(@PathVariable Long alertId) {
        alertService.resolveAlert(alertId);
        return ResponseEntity.ok().build();
    }

    /* =========================
       HELPER
       ========================= */
    private ServerMetricsDTO toMetricsDTO(ServerMetrics metrics) {
        return ServerMetricsDTO.builder()
                .id(metrics.getId())
                .serverId(metrics.getServer().getId())
                .serverName(metrics.getServer().getName())
                .cpuUsage(metrics.getCpuUsage())
                .memoryUsage(metrics.getMemoryUsage())
                .diskUsage(metrics.getDiskUsage())
                .collectedAt(metrics.getCollectedAt())
                .build();
    }
}