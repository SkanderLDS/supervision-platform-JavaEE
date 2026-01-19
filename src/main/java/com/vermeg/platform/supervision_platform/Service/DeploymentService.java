package com.vermeg.platform.supervision_platform.Service;

public interface DeploymentService {

    void deployApplication(Long applicationId);

    void redeployApplication(Long applicationId);

    void startApplication(Long applicationId);
    void stopApplication(Long applicationId);
    void restartApplication(Long applicationId);









}
