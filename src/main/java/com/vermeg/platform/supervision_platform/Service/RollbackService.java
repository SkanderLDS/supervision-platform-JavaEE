package com.vermeg.platform.supervision_platform.Service;
import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.ApplicationVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Optional;

@Service
public class RollbackService {

    private final ApplicationVersionRepository versionRepository;
    private final ApplicationRepository applicationRepository;
    private final AlertService alertService;
    private final WildFlyManagementClient wildFlyClient;
    private final DeploymentLogService deploymentLogService;

    public RollbackService(ApplicationVersionRepository versionRepository,
                           ApplicationRepository applicationRepository,
                           AlertService alertService,
                           WildFlyManagementClient wildFlyClient,
                           DeploymentLogService deploymentLogService) {
        this.versionRepository = versionRepository;
        this.applicationRepository = applicationRepository;
        this.alertService = alertService;
        this.wildFlyClient = wildFlyClient;
        this.deploymentLogService = deploymentLogService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void attemptRollback(Application app, ApplicationVersion failedVersion) {
        try {
            Optional<ApplicationVersion> lastSuccessful = versionRepository
                    .findTopByApplicationIdAndStatusOrderByDeployedAtDesc(
                            app.getId(), DeploymentStatus.DEPLOYED);

            if (lastSuccessful.isEmpty() ||
                    lastSuccessful.get().getId().equals(failedVersion.getId())) {
                alertService.createServerAlert(
                        app.getServer(),
                        "Deployment failed for " + app.getName()
                                + " version " + failedVersion.getVersion()
                                + " — No previous version available for rollback",
                        AlertLevel.CRITICAL
                );
                return;
            }

            ApplicationVersion rollbackVersion = lastSuccessful.get();
            File rollbackArtifact = new File(rollbackVersion.getArtifactPath());

            if (!rollbackArtifact.exists()) {
                alertService.createServerAlert(
                        app.getServer(),
                        "Deployment failed for " + app.getName()
                                + " — Rollback artifact not found: " + rollbackVersion.getArtifactPath(),
                        AlertLevel.CRITICAL
                );
                return;
            }

            deploymentLogService.log(
                    app,
                    DeploymentAction.DEPLOY,
                    DeploymentStatus.IN_PROGRESS,
                    rollbackVersion.getVersion(),
                    "Auto-rollback started to version " + rollbackVersion.getVersion()
            );

            wildFlyClient.deploy(app.getServer(), rollbackArtifact, app.getRuntimeName());

            rollbackVersion.markDeployed();
            app.markDeployed();
            app.setCurrentVersion(rollbackVersion.getVersion());

            versionRepository.save(rollbackVersion);
            applicationRepository.save(app);

            deploymentLogService.log(
                    app,
                    DeploymentAction.DEPLOY,
                    DeploymentStatus.DEPLOYED,
                    rollbackVersion.getVersion(),
                    "Auto-rollback successful to version " + rollbackVersion.getVersion()
            );

            alertService.createServerAlert(
                    app.getServer(),
                    "Auto-rollback executed for " + app.getName()
                            + " — Rolled back from version " + failedVersion.getVersion()
                            + " to version " + rollbackVersion.getVersion(),
                    AlertLevel.CRITICAL
            );

        } catch (Exception rollbackEx) {
            alertService.createServerAlert(
                    app.getServer(),
                    "Auto-rollback FAILED for " + app.getName()
                            + ": " + rollbackEx.getMessage(),
                    AlertLevel.CRITICAL
            );
        }
    }
}
