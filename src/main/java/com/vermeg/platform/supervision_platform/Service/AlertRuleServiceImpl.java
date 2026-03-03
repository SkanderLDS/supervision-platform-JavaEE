package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.AlertRuleRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.AlertRuleResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.AlertRuleRepository;
import com.vermeg.platform.supervision_platform.Repository.AppLogRepository;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import com.vermeg.platform.supervision_platform.exception.ServerNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;
    private final ServerRepository serverRepository;
    private final ServerMetricsService metricsService;
    private final AlertService alertService;
    private final AppLogRepository appLogRepository;

    public AlertRuleServiceImpl(AlertRuleRepository alertRuleRepository,
                                ServerRepository serverRepository,
                                ServerMetricsService metricsService,
                                AlertService alertService,
                                AppLogRepository appLogRepository) {
        this.alertRuleRepository = alertRuleRepository;
        this.serverRepository = serverRepository;
        this.metricsService = metricsService;
        this.alertService = alertService;
        this.appLogRepository = appLogRepository;
    }

    /* =========================
       CREATE RULE
       ========================= */
    @Override
    public AlertRuleResponseDTO createRule(AlertRuleRequestDTO dto) {
        Server server = serverRepository.findById(dto.getServerId())
                .orElseThrow(() -> new ServerNotFoundException(dto.getServerId()));

        AlertRule rule = AlertRule.builder()
                .name(dto.getName())
                .type(AlertRuleType.valueOf(dto.getType()))
                .threshold(dto.getThreshold())
                .level(AlertLevel.valueOf(dto.getLevel()))
                .server(server)
                .build();

        return toDTO(alertRuleRepository.save(rule));
    }

    /* =========================
       UPDATE RULE
       ========================= */
    @Override
    public AlertRuleResponseDTO updateRule(Long id, AlertRuleRequestDTO dto) {
        AlertRule rule = findRule(id);
        rule.setName(dto.getName());
        rule.setType(AlertRuleType.valueOf(dto.getType()));
        rule.setThreshold(dto.getThreshold());
        rule.setLevel(AlertLevel.valueOf(dto.getLevel()));
        return toDTO(alertRuleRepository.save(rule));
    }

    /* =========================
       DELETE RULE
       ========================= */
    @Override
    public void deleteRule(Long id) {
        if (!alertRuleRepository.existsById(id)) {
            throw new RuntimeException("Alert rule not found with id: " + id);
        }
        alertRuleRepository.deleteById(id);
    }

    /* =========================
       ENABLE RULE
       ========================= */
    @Override
    public void enableRule(Long id) {
        AlertRule rule = findRule(id);
        rule.setEnabled(true);
        alertRuleRepository.save(rule);
    }

    /* =========================
       DISABLE RULE
       ========================= */
    @Override
    public void disableRule(Long id) {
        AlertRule rule = findRule(id);
        rule.setEnabled(false);
        alertRuleRepository.save(rule);
    }

    /* =========================
       GET RULES FOR SERVER
       ========================= */
    @Override
    public List<AlertRuleResponseDTO> getRulesForServer(Long serverId) {
        return alertRuleRepository.findByServerId(serverId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /* =========================
       EVALUATE RULES FOR SERVER
       Checks all enabled rules against current metrics
       ========================= */
    @Override
    public void evaluateRulesForServer(Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ServerNotFoundException(serverId));

        List<AlertRule> enabledRules = alertRuleRepository
                .findByServerIdAndEnabledTrue(serverId);

        if (enabledRules.isEmpty()) return;

        // Get latest metrics
        ServerMetrics metrics = null;
        try {
            metrics = metricsService.getLatestMetrics(serverId);
        } catch (Exception e) {
            // No metrics available yet
            return;
        }

        for (AlertRule rule : enabledRules) {
            evaluateRule(rule, server, metrics);
        }
    }

    /* =========================
       EVALUATE SINGLE RULE
       ========================= */
    private void evaluateRule(AlertRule rule, Server server, ServerMetrics metrics) {
        switch (rule.getType()) {
            case CPU_USAGE -> {
                if (metrics.getCpuUsage() > rule.getThreshold()) {
                    alertService.createServerAlert(server,
                            String.format("[Rule: %s] CPU usage %.1f%% exceeded threshold %.1f%%",
                                    rule.getName(), metrics.getCpuUsage(), rule.getThreshold()),
                            rule.getLevel());
                }
            }
            case MEMORY_USAGE -> {
                if (metrics.getMemoryUsage() > rule.getThreshold()) {
                    alertService.createServerAlert(server,
                            String.format("[Rule: %s] Memory usage %.1f%% exceeded threshold %.1f%%",
                                    rule.getName(), metrics.getMemoryUsage(), rule.getThreshold()),
                            rule.getLevel());
                }
            }
            case DISK_USAGE -> {
                if (metrics.getDiskUsage() > rule.getThreshold()) {
                    alertService.createServerAlert(server,
                            String.format("[Rule: %s] Disk usage %.1f%% exceeded threshold %.1f%%",
                                    rule.getName(), metrics.getDiskUsage(), rule.getThreshold()),
                            rule.getLevel());
                }
            }
            case ERROR_COUNT_PER_MINUTE -> {
                long errorCount = countErrorsLastMinute(server.getId());
                if (errorCount > rule.getThreshold()) {
                    alertService.createServerAlert(server,
                            String.format("[Rule: %s] %d errors in last minute exceeded threshold %.0f",
                                    rule.getName(), errorCount, rule.getThreshold()),
                            rule.getLevel());
                }
            }
        }
    }

    /* =========================
       COUNT ERRORS IN LAST MINUTE
       ========================= */
    private long countErrorsLastMinute(Long serverId) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        return appLogRepository
                .findByServerIdAndLevelOrderByTimestampDesc(serverId, LogLevel.ERROR)
                .stream()
                .filter(log -> log.getTimestamp().isAfter(oneMinuteAgo))
                .count();
    }

    /* =========================
       HELPERS
       ========================= */
    private AlertRule findRule(Long id) {
        return alertRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert rule not found with id: " + id));
    }

    private AlertRuleResponseDTO toDTO(AlertRule rule) {
        return AlertRuleResponseDTO.builder()
                .id(rule.getId())
                .name(rule.getName())
                .type(rule.getType().name())
                .threshold(rule.getThreshold())
                .level(rule.getLevel().name())
                .enabled(rule.isEnabled())
                .createdAt(rule.getCreatedAt())
                .serverId(rule.getServer().getId())
                .serverName(rule.getServer().getName())
                .build();
    }
}