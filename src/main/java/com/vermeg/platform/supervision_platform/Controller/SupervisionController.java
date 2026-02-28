package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.AlertDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerMetricsDTO;
import com.vermeg.platform.supervision_platform.DTO.SupervisionResultDTO;
import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;
import com.vermeg.platform.supervision_platform.Service.AlertService;
import com.vermeg.platform.supervision_platform.Service.ServerMetricsService;
import com.vermeg.platform.supervision_platform.Service.SupervisionService;
import org.springframework.http.ResponseEntity;
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

    /* =========================
       SUPERVISE SINGLE SERVER
       Checks connectivity + collects metrics + checks alerts
       ========================= */
    @PostMapping("/servers/{id}")
    public ResponseEntity<SupervisionResultDTO> superviseServer(@PathVariable Long id) {
        return ResponseEntity.ok(supervisionService.superviseServer(id));
    }

    /* =========================
       SUPERVISE ALL SERVERS
       ========================= */
    @PostMapping("/servers")
    public ResponseEntity<List<SupervisionResultDTO>> superviseAll() {
        return ResponseEntity.ok(supervisionService.superviseAllServers());
    }

    /* =========================
       GET LATEST METRICS FOR SERVER
       ========================= */
    @GetMapping("/servers/{id}/metrics")
    public ResponseEntity<ServerMetricsDTO> getLatestMetrics(@PathVariable Long id) {
        ServerMetrics metrics = metricsService.getLatestMetrics(id);
        ServerMetricsDTO dto = ServerMetricsDTO.builder()
                .id(metrics.getId())
                .serverId(metrics.getServer().getId())
                .serverName(metrics.getServer().getName())
                .cpuUsage(metrics.getCpuUsage())
                .memoryUsage(metrics.getMemoryUsage())
                .diskUsage(metrics.getDiskUsage())
                .collectedAt(metrics.getCollectedAt())
                .build();
        return ResponseEntity.ok(dto);
    }

    /* =========================
       GET METRICS HISTORY FOR SERVER
       ========================= */
    @GetMapping("/servers/{id}/metrics/history")
    public ResponseEntity<List<ServerMetricsDTO>> getMetricsHistory(@PathVariable Long id) {
        List<ServerMetricsDTO> history = metricsService.getMetricsHistory(id)
                .stream()
                .map(metrics -> ServerMetricsDTO.builder()
                        .id(metrics.getId())
                        .serverId(metrics.getServer().getId())
                        .serverName(metrics.getServer().getName())
                        .cpuUsage(metrics.getCpuUsage())
                        .memoryUsage(metrics.getMemoryUsage())
                        .diskUsage(metrics.getDiskUsage())
                        .collectedAt(metrics.getCollectedAt())
                        .build())
                .toList();
        return ResponseEntity.ok(history);
    }

    /* =========================
       GET ALL ALERTS FOR SERVER
       ========================= */
    @GetMapping("/servers/{id}/alerts")
    public ResponseEntity<List<AlertDTO>> getAlerts(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlerts(id));
    }

    /* =========================
       GET UNRESOLVED ALERTS FOR SERVER
       ========================= */
    @GetMapping("/servers/{id}/alerts/unresolved")
    public ResponseEntity<List<AlertDTO>> getUnresolvedAlerts(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getUnresolvedAlerts(id));
    }

    /* =========================
       RESOLVE ALERT
       ========================= */
    @PatchMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<Void> resolveAlert(@PathVariable Long alertId) {
        alertService.resolveAlert(alertId);
        return ResponseEntity.ok().build();
    }
}