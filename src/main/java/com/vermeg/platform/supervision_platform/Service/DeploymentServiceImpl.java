package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.ApplicationVersionRepository;
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
            log(version, DeploymentAction.DEPLOY, DeploymentStatus.IN_PROGRESS, "Deployment started");
            wildFlyClient.deploy(server, artifact, app.getRuntimeName());
            version.markDeployed();
            app.markDeployed();
            app.setCurrentVersion(version.getVersion());
            log(version, DeploymentAction.DEPLOY, DeploymentStatus.DEPLOYED, "Deployment successful");
            applicationRepository.save(app);
            return versionRepository.save(version);
        }catch (Exception e) {
            version.markFailed();
            app.markFailed();
            log(version, DeploymentAction.DEPLOY, DeploymentStatus.FAILED, e.getMessage());
            versionRepository.save(version);
            applicationRepository.save(app);
            throw e;
        }
    }
    @Override
    public ApplicationVersion redeploy(Long versionId, File artifact) {

        ApplicationVersion version = findVersion(versionId);
        Application app = version.getApplication();
        wildFlyClient.undeploy(app.getServer(), app.getRuntimeName());
        log(version, DeploymentAction.REDEPLOY,
                DeploymentStatus.IN_PROGRESS, "Redeployment started");
        return deploy(versionId, artifact);
    }
    @Override
    public void start(Long versionId) {
        ApplicationVersion version = findVersion(versionId);
        Application app = version.getApplication();
        wildFlyClient.start(app.getServer(), app.getRuntimeName());
        app.start();
        version.markDeployed();
        log(version, DeploymentAction.START, DeploymentStatus.DEPLOYED, "Application started");
        applicationRepository.save(app);
        versionRepository.save(version);
    }
    @Override
    public void stop(Long versionId) {
        ApplicationVersion version = findVersion(versionId);
        Application app = version.getApplication();
        wildFlyClient.stop(app.getServer(), app.getRuntimeName());
        app.stop();
        version.setStatus(DeploymentStatus.STOPPED);
        log(version, DeploymentAction.STOP, DeploymentStatus.STOPPED, "Application stopped");
        applicationRepository.save(app);
        versionRepository.save(version);
    }
    private ApplicationVersion findVersion(Long id) {
        return versionRepository.findById(id).orElseThrow(() -> new RuntimeException("ApplicationVersion not found"));
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

