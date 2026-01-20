package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;

public interface AlertService {
    void checkServerAlerts(Server server, ServerMetrics metrics);
    void applicationFailed(Application app, String reason);
}
