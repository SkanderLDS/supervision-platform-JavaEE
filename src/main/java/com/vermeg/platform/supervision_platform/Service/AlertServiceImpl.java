package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.AlertDTO;
import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.AlertRepository;
import com.vermeg.platform.supervision_platform.exception.AlertNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    public AlertServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /* =========================
       CHECK SERVER ALERTS
       Only creates alert if no unresolved alert of same message exists
       ========================= */
    @Override
    public void checkServerAlerts(Server server, ServerMetrics metrics) {
        if (metrics.getCpuUsage() > 80) {
            createAlertIfNotExists(
                    "High CPU usage: " + String.format("%.1f", metrics.getCpuUsage()) + "%",
                    AlertLevel.WARN,
                    server,
                    null
            );
        }

        if (metrics.getMemoryUsage() > 85) {
            createAlertIfNotExists(
                    "High memory usage: " + String.format("%.1f", metrics.getMemoryUsage()) + "%",
                    AlertLevel.WARN,
                    server,
                    null
            );
        }

        if (metrics.getDiskUsage() > 90) {
            createAlertIfNotExists(
                    "Disk almost full: " + String.format("%.1f", metrics.getDiskUsage()) + "%",
                    AlertLevel.CRITICAL,
                    server,
                    null
            );
        }
    }

    /* =========================
       APPLICATION FAILED ALERT
       ========================= */
    @Override
    public void applicationFailed(Application app, String reason) {
        createAlertIfNotExists(
                "Application failed: " + reason,
                AlertLevel.CRITICAL,
                app.getServer(),
                app
        );
    }

    /* =========================
       GET ALL ALERTS FOR SERVER
       ========================= */
    @Override
    public List<AlertDTO> getAlerts(Long serverId) {
        return alertRepository.findByServerIdOrderByCreatedAtDesc(serverId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /* =========================
       GET UNRESOLVED ALERTS FOR SERVER
       ========================= */
    @Override
    public List<AlertDTO> getUnresolvedAlerts(Long serverId) {
        return alertRepository.findByServerIdAndResolvedFalseOrderByCreatedAtDesc(serverId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /* =========================
       RESOLVE ALERT
       ========================= */
    @Override
    public void resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));
        alert.resolve();
        alertRepository.save(alert);
    }

    @Override
    public void createServerAlert(Server server, String message, AlertLevel level) {
        createAlertIfNotExists(message, level, server, null);
    }

    /* =========================
       HELPERS
       ========================= */
    private void createAlertIfNotExists(String message, AlertLevel level,
                                        Server server, Application app) {
        boolean exists = alertRepository
                .existsByServerIdAndMessageAndResolvedFalse(server.getId(), message);
        if (!exists) {
            Alert alert = Alert.builder()
                    .message(message)
                    .level(level)
                    .server(server)
                    .application(app)
                    .build();
            alertRepository.save(alert);
        }
    }

    private AlertDTO toDTO(Alert alert) {
        return AlertDTO.builder()
                .id(alert.getId())
                .message(alert.getMessage())
                .level(alert.getLevel().name())
                .resolved(alert.isResolved())
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .serverId(alert.getServer().getId())
                .serverName(alert.getServer().getName())
                .applicationId(alert.getApplication() != null
                        ? alert.getApplication().getId() : null)
                .applicationName(alert.getApplication() != null
                        ? alert.getApplication().getName() : null)
                .build();
    }
}