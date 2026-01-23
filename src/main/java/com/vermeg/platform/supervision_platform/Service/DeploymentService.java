package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Application;

import java.io.File;

public interface DeploymentService {

    Application deploy(Long applicationId, File warFile);

    Application redeploy(Long applicationId, File warFile);

    Application start(Long applicationId);

    Application stop(Long applicationId);

    Application undeploy(Long applicationId);
}

