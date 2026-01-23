package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Entity.DeploymentAction;
import com.vermeg.platform.supervision_platform.Entity.DeploymentStatus;
import com.vermeg.platform.supervision_platform.Entity.LogLevel;

public interface DeploymentLogService {

    void log(Application application,
             DeploymentAction action,
             DeploymentStatus status,
             String version,
             String message);
}
