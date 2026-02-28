package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.ApplicationVersionRepository;
import com.vermeg.platform.supervision_platform.exception.ApplicationNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Transactional
public class DeploymentServiceImpl implements DeploymentService {

    private final ApplicationVersionRepository versionRepository;
    private final ApplicationRepository applicationRepository;
    private final DeploymentLogService deploymentLogService;
    private final WildFlyManagementClient wildFlyClient;

    public DeploymentServiceImpl(
            ApplicationVersionRepository versionRepository,
            ApplicationRepository applicationRepository,
            DeploymentLogService deploymentLogService,
            WildFlyManagementClient wildFlyClient
    ) {
        this.versionRepository = versionRepository;
        this.applicationRepository = applicationRepository;
        this.deploymentLogService = deploymentLogService;
        this.wildFlyClient = wildFlyClient;
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
}