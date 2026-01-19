package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;
import com.vermeg.platform.supervision_platform.Entity.LogLevel;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.DeploymentLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeploymentLogServiceImpl implements DeploymentLogService {
    private final DeploymentLogRepository logRepository;



    public DeploymentLogServiceImpl(DeploymentLogRepository logRepository) {
        this.logRepository = logRepository;
    }


    @Override
    public void log (Application app, String message, LogLevel level) {
        // Implementation for logging deployment events
        DeploymentLog log = new DeploymentLog(app, message, level);
        logRepository.save(log);
    }

}
