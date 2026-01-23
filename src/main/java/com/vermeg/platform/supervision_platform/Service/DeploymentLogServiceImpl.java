package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.DeploymentLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeploymentLogServiceImpl implements DeploymentLogService {

    private final DeploymentLogRepository deploymentLogRepository;

    public DeploymentLogServiceImpl(DeploymentLogRepository deploymentLogRepository) {
        this.deploymentLogRepository = deploymentLogRepository;
    }

    @Override
    public void log(Application application,
                    DeploymentAction action,
                    DeploymentStatus status,
                    String version,
                    String message) {

        LogLevel level = resolveLevel(status);

        DeploymentLog log = new DeploymentLog(
                application,
                action,
                status,
                version,
                message,
                level
        );

        deploymentLogRepository.save(log);
    }

    /* =========================
       INTERNAL LOG LEVEL RULES
       ========================= */
    private LogLevel resolveLevel(DeploymentStatus status) {
        return switch (status) {
            case FAILED -> LogLevel.ERROR;
            case IN_PROGRESS -> LogLevel.INFO;
            case STOPPED, UNDEPLOYED -> LogLevel.WARN;
            default -> LogLevel.INFO;
        };
    }
}
