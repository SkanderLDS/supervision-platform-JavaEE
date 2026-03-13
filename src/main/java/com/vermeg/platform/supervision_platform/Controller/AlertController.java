package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.AlertDTO;
import com.vermeg.platform.supervision_platform.Repository.AlertRepository;
import com.vermeg.platform.supervision_platform.Service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final AlertRepository alertRepository;

    public AlertController(AlertService alertService, AlertRepository alertRepository) {
        this.alertService = alertService;
        this.alertRepository = alertRepository;
    }

    @GetMapping("/servers/{serverId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_VIEWER')")
    public ResponseEntity<List<AlertDTO>> getAlerts(@PathVariable Long serverId) {
        return ResponseEntity.ok(alertService.getAlerts(serverId));
    }

    @GetMapping("/servers/{serverId}/unresolved")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_VIEWER')")
    public ResponseEntity<List<AlertDTO>> getUnresolvedAlerts(@PathVariable Long serverId) {
        return ResponseEntity.ok(alertService.getUnresolvedAlerts(serverId));
    }

    @GetMapping("/unresolved")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_VIEWER')")
    public ResponseEntity<List<AlertDTO>> getAllUnresolved() {
        return ResponseEntity.ok(alertRepository.findByResolvedFalse()
                .stream()
                .map(alert -> AlertDTO.builder()
                        .id(alert.getId())
                        .message(alert.getMessage())
                        .level(alert.getLevel().name())
                        .resolved(alert.isResolved())
                        .createdAt(alert.getCreatedAt())
                        .resolvedAt(alert.getResolvedAt())
                        .serverId(alert.getServer().getId())
                        .serverName(alert.getServer().getName())
                        .build())
                .toList());
    }

    @PutMapping("/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> resolveAlert(@PathVariable Long alertId) {
        alertService.resolveAlert(alertId);
        return ResponseEntity.ok().build();
    }
}