package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.ApplicationVersionRepository;
import com.vermeg.platform.supervision_platform.exception.ApplicationNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
@Transactional
public class DeploymentServiceImpl implements DeploymentService {

    private final ApplicationVersionRepository versionRepository;
    private final ApplicationRepository applicationRepository;
    private final DeploymentLogService deploymentLogService;
    private final WildFlyManagementClient wildFlyClient;
    private final AlertService alertService;

    public DeploymentServiceImpl(
            ApplicationVersionRepository versionRepository,
            ApplicationRepository applicationRepository,
            DeploymentLogService deploymentLogService,
            WildFlyManagementClient wildFlyClient,
            AlertService alertService
    ) {
        this.versionRepository = versionRepository;
        this.applicationRepository = applicationRepository;
        this.deploymentLogService = deploymentLogService;
        this.wildFlyClient = wildFlyClient;
        this.alertService = alertService;
    }

    /* =========================
       DEPLOY
       ========================= */
    @Override
    public ApplicationVersion deploy(Long versionId, File artifact) {
        ApplicationVersion version = findVersion(versionId);
        Application app = version.getApplication();
        Server server = app.getServer();

        try {
            version.markDeploying();
            app.markDeploying();
            log(version, DeploymentAction.DEPLOY, DeploymentStatus.IN_PROGRESS,
                   "Deployment started for version " + version.getVersion());

            wildFlyClient.deploy(server, artifact, app.getRuntimeName());

            version.markDeployed();
            app.markDeployed();
            app.setCurrentVersion(version.getVersion());

            log(version, DeploymentAction.DEPLOY, DeploymentStatus.DEPLOYED,
                   "Deployment successful for version " + version.getVersion());

            applicationRepository.save(app);
            return versionRepository.save(version);

        } catch (Exception e) {
            version.markFailed();
            app.markFailed();
            log(version, DeploymentAction.DEPLOY, DeploymentStatus.FAILED,
                    "Deployment failed: " + e.getMessage());
            versionRepository.save(version);
            applicationRepository.save(app);

            // Auto-rollback to last successful version
            attemptRollback(app, version);

            throw new RuntimeException("Deployment failed: " + e.getMessage(), e);
        }
    }

    /* =========================
       REDEPLOY — fixed duplicate log bug
       ========================= */
    @Override
    public ApplicationVersion redeploy(Long versionId, File artifact) {
        ApplicationVersion version = findVersion(versionId);
        Application app = version.getApplication();

        try {
            version.markDeploying();
            app.markDeploying();
            log(version, DeploymentAction.REDEPLOY, DeploymentStatus.IN_PROGRESS,
                    "Redeployment started for version " + version.getVersion());

            wildFlyClient.undeploy(app.getServer(), app.getRuntimeName());
            wildFlyClient.deploy(app.getServer(), artifact, app.getRuntimeName());

            version.markDeployed();
            app.markDeployed();
            app.setCurrentVersion(version.getVersion());

            log(version, DeploymentAction.REDEPLOY, DeploymentStatus.DEPLOYED,
                    "Redeployment successful for version " + version.getVersion());

            applicationRepository.save(app);
            return versionRepository.save(version);

        } catch (Exception e) {
            version.markFailed();
            app.markFailed();
            log(version, DeploymentAction.REDEPLOY, DeploymentStatus.FAILED,
                   "Redeployment failed: " + e.getMessage());
            versionRepository.save(version);
            applicationRepository.save(app);
            throw new RuntimeException("Redeployment failed: " + e.getMessage(), e);
        }
    }

    /* =========================
       START
       ========================= */
    @Override
    public void start(Long versionId) {
        ApplicationVersion version = findVersion(versionId);
        Application app = version.getApplication();

        try {
            wildFlyClient.start(app.getServer(), app.getRuntimeName());
            app.start();
            version.markDeployed();
            log(version, DeploymentAction.START, DeploymentStatus.DEPLOYED,
                    "Application started successfully");
            applicationRepository.save(app);
            versionRepository.save(version);
        } catch (Exception e) {
            log(version, DeploymentAction.START, DeploymentStatus.FAILED,
                   "Start failed: " + e.getMessage());
            throw new RuntimeException("Start failed: " + e.getMessage(), e);
        }
    }

    /* =========================
       STOP
       ========================= */
    @Override
    public void stop(Long versionId) {
        ApplicationVersion version = findVersion(versionId);
        Application app = version.getApplication();

        try {
            wildFlyClient.stop(app.getServer(), app.getRuntimeName());
            app.stop();
            version.markStopped();
            log(version, DeploymentAction.STOP, DeploymentStatus.STOPPED,
                   "Application stopped successfully");
            applicationRepository.save(app);
            versionRepository.save(version);
        } catch (Exception e) {
            log(version, DeploymentAction.STOP, DeploymentStatus.FAILED,
                    "Stop failed: " + e.getMessage());
            throw new RuntimeException("Stop failed: " + e.getMessage(), e);
        }
    }

    /* =========================
       RESTART
       ========================= */
    @Override
    public void restart(Long versionId) {
        ApplicationVersion version = findVersion(versionId);
        Application app = version.getApplication();

        try {
            wildFlyClient.restart(app.getServer(), app.getRuntimeName());
            app.stop();
            app.start();
            version.markDeployed();
            log(version, DeploymentAction.REDEPLOY, DeploymentStatus.DEPLOYED, "Application restarted successfully");
            applicationRepository.save(app);
            versionRepository.save(version);
        } catch (Exception e) {
            log(version, DeploymentAction.REDEPLOY, DeploymentStatus.FAILED, "Restart failed: " + e.getMessage());
            throw new RuntimeException("Restart failed: " + e.getMessage(), e);
        }
    }

    /* =========================
       HELPERS
       ========================= */
    private ApplicationVersion findVersion(Long id) {
        return versionRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    private void log(ApplicationVersion version,
                     DeploymentAction action,
                     DeploymentStatus status,
                     String message) {
        deploymentLogService.log(
                version.getApplication(),
                action,
                status,
                version.getVersion(),
                message
        );
    }

    /* =========================
   AUTO-ROLLBACK
   Finds last successfully deployed version and redeploys it
   ========================= */
    private void attemptRollback(Application app, ApplicationVersion failedVersion) {
        try {
            // Find last successfully deployed version (excluding the failed one)
            Optional<ApplicationVersion> lastSuccessful = versionRepository
                    .findTopByApplicationIdAndStatusOrderByDeployedAtDesc(
                            app.getId(), DeploymentStatus.DEPLOYED);

            if (lastSuccessful.isEmpty() ||
                    lastSuccessful.get().getId().equals(failedVersion.getId())) {
                // No previous successful version to rollback to
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

            // Get the artifact file from the rollback version
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

            // Perform rollback deployment
            log(rollbackVersion, DeploymentAction.DEPLOY, DeploymentStatus.IN_PROGRESS,
                    "Auto-rollback started to version " + rollbackVersion.getVersion());

            wildFlyClient.deploy(app.getServer(), rollbackArtifact, app.getRuntimeName());

            rollbackVersion.markDeployed();
            app.markDeployed();
            app.setCurrentVersion(rollbackVersion.getVersion());

            versionRepository.save(rollbackVersion);
            applicationRepository.save(app);

            log(rollbackVersion, DeploymentAction.DEPLOY, DeploymentStatus.DEPLOYED,
                    "Auto-rollback successful to version " + rollbackVersion.getVersion());

            // Create CRITICAL alert notifying about the rollback
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