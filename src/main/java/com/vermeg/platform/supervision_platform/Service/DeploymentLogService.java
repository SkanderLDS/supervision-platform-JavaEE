package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Entity.LogLevel;

public interface DeploymentLogService {
    void log(Application app, String message, LogLevel level);

}
