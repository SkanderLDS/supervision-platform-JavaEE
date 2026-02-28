package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;
import com.vermeg.platform.supervision_platform.DTO.ServerMetricsDTO;

import java.util.List;

public interface ServerMetricsService {
 ServerMetrics collectMetrics(Server server);
 ServerMetrics collectMetricsById(Long serverId);
 ServerMetrics getLatestMetrics(Long serverId);
 List<ServerMetrics> getMetricsHistory(Long serverId);
}