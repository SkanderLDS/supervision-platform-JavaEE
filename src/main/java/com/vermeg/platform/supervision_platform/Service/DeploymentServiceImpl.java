package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Entity.ApplicationVersion;
import com.vermeg.platform.supervision_platform.Entity.LogLevel;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class DeploymentServiceImpl implements  DeploymentService {
    private final ApplicationRepository applicationRepository;
    private final DeploymentLogService logService;
    private final ApplicationVersionService applicationVersionService;


    public DeploymentServiceImpl (ApplicationRepository applicationRepository,DeploymentLogService logService,ApplicationVersionService applicationVersionService) {
        this.applicationRepository = applicationRepository;
        this.logService= logService;
        this.applicationVersionService= applicationVersionService;
    }

    @Override
    public void deployApplication(Long applicationId) {
        Application app = findApplication(applicationId);


        ApplicationVersion version =
                applicationVersionService.deployNewVersion(
                        app,
                        app.getVersion(),
                        app.getType()
                );


        app.markDeploying();
        logService.log(app, "Deployment started for version " + version.getVersion(), LogLevel.INFO);

        simulateStep(2000);
        logService.log(app, "Validating application package", LogLevel.INFO);

        simulateStep(1500);
        logService.log(app, "Uploading application to server", LogLevel.INFO);

        simulateStep(2500);
        logService.log(app, "Starting application on server", LogLevel.INFO);

        simulateStep(2000);

        
        app.markDeployed();
        version.markDeployed();

        logService.log(app, "Application deployed successfully", LogLevel.INFO);

        applicationRepository.save(app);
    }


    @Override
    public  void redeployApplication(Long applicationId) {
        deployApplication(applicationId);
    }

    @Override
    public void startApplication(Long applicationId) {
        Application app = findApplication(applicationId);

        app.start();
        logService.log(app, "Application started", LogLevel.INFO);
    }

    @Override
    public void stopApplication(Long applicationId) {
        Application app = findApplication(applicationId);

        app.stop();
        logService.log(app, "Application stopped", LogLevel.WARN);
    }

    @Override
    public void restartApplication(Long applicationId) {
        Application app = findApplication(applicationId);

        app.restart();
        logService.log(app, "Application restarted", LogLevel.INFO);

    }


    private void simulateStep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Application findApplication(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }
}
