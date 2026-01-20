package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.AlertRepository;

public class AlertServiceImpl implements AlertService{
    private final AlertRepository alertRepository;

    public AlertServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }
    @Override
    public void checkServerAlerts(Server server, ServerMetrics metrics) {

        if (metrics.getCpuUsage() > 80) {
            alertRepository.save(
                    new Alert("High CPU usage", AlertLevel.WARN, server)
            );
        }

        if (metrics.getMemoryUsage() > 85) {
            alertRepository.save(
                    new Alert("High memory usage", AlertLevel.WARN, server)
            );
        }

        if (metrics.getDiskUsage() > 90) {
            alertRepository.save(
                    new Alert("Disk almost full", AlertLevel.CRITICAL, server)
            );
        }
    }

    @Override
    public void applicationFailed(Application app, String reason) {
        alertRepository.save(
                new Alert("Application failed: " + reason,
                        AlertLevel.CRITICAL,
                        app.getServer())
        );
    }
}
