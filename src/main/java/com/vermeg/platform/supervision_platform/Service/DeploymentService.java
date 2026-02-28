package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.ApplicationVersion;
import java.io.File;

public interface DeploymentService {
    ApplicationVersion deploy(Long versionId, File artifact);
    ApplicationVersion redeploy(Long versionId, File artifact);
    void start(Long versionId);
    void stop(Long versionId);
    void restart(Long versionId);
}