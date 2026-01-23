package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.File;


@Service
@Transactional
public class DeploymentServiceImpl implements DeploymentService {

    private final ApplicationRepository applicationRepository;
    private final DeploymentLogService deploymentLogService;
    private final WildFlyManagementClient wildFlyClient;

    public DeploymentServiceImpl(
            ApplicationRepository applicationRepository,
            DeploymentLogService deploymentLogService,
            WildFlyManagementClient wildFlyClient
    ) {
        this.applicationRepository = applicationRepository;
        this.deploymentLogService = deploymentLogService;
        this.wildFlyClient = wildFlyClient;
    }

    /* =========================
       DEPLOY
       ========================= */
    @Override
    public Application deploy(Long applicationId, File warFile) {
        Application app = findApplication(applicationId);
        Server server = app.getServer();

        try {
            app.markDeploying();
            log(app, DeploymentAction.DEPLOY, DeploymentStatus.IN_PROGRESS,
                    "Deployment started");

            wildFlyClient.deploy(
                    server,
                    warFile,
                    app.getRuntimeName()
            );

            app.markDeployed();
            log(app, DeploymentAction.DEPLOY, DeploymentStatus.DEPLOYED,
                    "Deployment successful");

        } catch (Exception e) {
            app.markFailed();
            log(app, DeploymentAction.DEPLOY, DeploymentStatus.FAILED,
                    e.getMessage());
            throw e;
        }

        return applicationRepository.save(app);
    }

    /* =========================
       REDEPLOY
       ========================= */
    @Override
    public Application redeploy(Long applicationId, File warFile) {
        Application app = findApplication(applicationId);

        try {
            wildFlyClient.undeploy(app.getServer(), app.getRuntimeName());

            log(app, DeploymentAction.REDEPLOY, DeploymentStatus.IN_PROGRESS,
                    "Redeployment started");

            return deploy(applicationId, warFile);

        } catch (Exception e) {
            app.markFailed();
            log(app, DeploymentAction.REDEPLOY, DeploymentStatus.FAILED,
                    e.getMessage());
            throw e;
        }
    }

    /* =========================
       START
       ========================= */
    @Override
    public Application start(Long applicationId) {
        Application app = findApplication(applicationId);

        wildFlyClient.start(
                app.getServer(),
                app.getRuntimeName()
        );

        app.start();
        log(app, DeploymentAction.START, DeploymentStatus.DEPLOYED,
                "Application started");

        return applicationRepository.save(app);
    }

    /* =========================
       STOP
       ========================= */
    @Override
    public Application stop(Long applicationId) {
        Application app = findApplication(applicationId);

        wildFlyClient.stop(
                app.getServer(),
                app.getRuntimeName()
        );

        app.stop();
        log(app, DeploymentAction.STOP, DeploymentStatus.STOPPED,
                "Application stopped");

        return applicationRepository.save(app);
    }

    /* =========================
       UNDEPLOY
       ========================= */
    @Override
    public Application undeploy(Long applicationId) {
        Application app = findApplication(applicationId);

        wildFlyClient.undeploy(
                app.getServer(),
                app.getRuntimeName()
        );

        app.setStatus(DeploymentStatus.UNDEPLOYED);

        log(app, DeploymentAction.UNDEPLOY, DeploymentStatus.UNDEPLOYED,
                "Application undeployed");

        return applicationRepository.save(app);
    }

    /* =========================
       HELPERS
       ========================= */
    private Application findApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    private void log(Application app,
                     DeploymentAction action,
                     DeploymentStatus status,
                     String message) {

        deploymentLogService.log(
                app,
                action,
                status,
                app.getVersion(),
                message
        );
    }
}