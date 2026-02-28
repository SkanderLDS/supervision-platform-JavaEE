package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.AlertDTO;
import com.vermeg.platform.supervision_platform.Entity.AlertLevel;
import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;

import java.util.List;

public interface AlertService {
    void checkServerAlerts(Server server, ServerMetrics metrics);
    void applicationFailed(Application app, String reason);
    List<AlertDTO> getAlerts(Long serverId);
    List<AlertDTO> getUnresolvedAlerts(Long serverId);
    void resolveAlert(Long alertId);
    void createServerAlert(Server server, String message, AlertLevel level);
}