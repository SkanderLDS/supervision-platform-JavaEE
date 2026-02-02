package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;

public interface ServerMetricsService {
 ServerMetrics collectMetrics(Server server);
 ServerMetrics getLatestMetrics(Long serverId);
}
