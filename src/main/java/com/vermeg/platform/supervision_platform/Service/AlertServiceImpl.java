package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.AlertDTO;
import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.AlertRepository;
import com.vermeg.platform.supervision_platform.Repository.AlertRuleRepository;
import com.vermeg.platform.supervision_platform.exception.AlertNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final EmailService emailService;

    public AlertServiceImpl(AlertRepository alertRepository,
                            AlertRuleRepository alertRuleRepository,
                            EmailService emailService) {
        this.alertRepository = alertRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.emailService = emailService;
    }

    /* =========================
       CHECK SERVER ALERTS
       Evaluates configured alert rules, falls back to hardcoded thresholds
       ========================= */
    @Override
    public void checkServerAlerts(Server server, ServerMetrics metrics) {
        List<AlertRule> rules = alertRuleRepository.findByServerIdAndEnabledTrue(server.getId());

        if (rules.isEmpty()) {
            // Fallback to hardcoded thresholds if no rules configured
            if (metrics.getCpuUsage() > 90) {
                createAlertIfNotExists(
                        "Critical CPU usage: " + String.format("%.1f", metrics.getCpuUsage()) + "%",
                        AlertLevel.CRITICAL, server, null, null);
            } else if (metrics.getCpuUsage() > 80) {
                createAlertIfNotExists(
                        "High CPU usage: " + String.format("%.1f", metrics.getCpuUsage()) + "%",
                        AlertLevel.WARN, server, null, null);
            }
            if (metrics.getMemoryUsage() > 90) {
                createAlertIfNotExists(
                        "Critical memory usage: " + String.format("%.1f", metrics.getMemoryUsage()) + "%",
                        AlertLevel.CRITICAL, server, null, null);
            } else if (metrics.getMemoryUsage() > 85) {
                createAlertIfNotExists(
                        "High memory usage: " + String.format("%.1f", metrics.getMemoryUsage()) + "%",
                        AlertLevel.WARN, server, null, null);
            }
            if (metrics.getDiskUsage() > 95) {
                createAlertIfNotExists(
                        "Critical disk usage: " + String.format("%.1f", metrics.getDiskUsage()) + "%",
                        AlertLevel.CRITICAL, server, null, null);
            } else if (metrics.getDiskUsage() > 90) {
                createAlertIfNotExists(
                        "Disk almost full: " + String.format("%.1f", metrics.getDiskUsage()) + "%",
                        AlertLevel.WARN, server, null, null);
            }
            return;
        }

        // Evaluate each configured rule
        for (AlertRule rule : rules) {
            double currentValue = switch (rule.getType()) {
                case CPU_USAGE -> metrics.getCpuUsage();
                case MEMORY_USAGE -> metrics.getMemoryUsage();
                case DISK_USAGE -> metrics.getDiskUsage();
                default -> 0.0;
            };

            boolean triggered = currentValue > rule.getThreshold();

            if (triggered) {
                String message = rule.getType().name().replace("_", " ") +
                        " alert: " + String.format("%.1f", currentValue) +
                        "% exceeds threshold of " + rule.getThreshold() + "%";

                AlertLevel level = rule.getLevel();
                String notificationEmail = rule.isEmailNotification()
                        ? rule.getNotificationEmail() : null;

                createAlertIfNotExists(message, level, server, null, notificationEmail);
            }
        }
    }

    /* =========================
       APPLICATION FAILED
       ========================= */
    @Override
    public void applicationFailed(Application app, String reason) {
        createAlertIfNotExists(
                "Application failed: " + reason,
                AlertLevel.CRITICAL,
                app.getServer(),
                app,
                null
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

    /* =========================
       CREATE SERVER ALERT (manual)
       ========================= */
    @Override
    public void createServerAlert(Server server, String message, AlertLevel level) {
        createAlertIfNotExists(message, level, server, null, null);
    }

    /* =========================
       HELPERS
       ========================= */
    private void createAlertIfNotExists(String message, AlertLevel level,
                                        Server server, Application app,
                                        String notificationEmail) {
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

            if (level == AlertLevel.WARN || level == AlertLevel.CRITICAL) {
                try {
                    if (notificationEmail != null && !notificationEmail.isBlank()) {
                        emailService.sendAlertEmail(alert, notificationEmail);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to send alert email: " + e.getMessage());
                }
            }
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