package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.DeploymentLogResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.DeploymentAction;
import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;
import com.vermeg.platform.supervision_platform.Repository.DeploymentLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DeploymentLogQueryServiceImpl
        implements DeploymentLogQueryService {

    private final DeploymentLogRepository deploymentLogRepository;

    public DeploymentLogQueryServiceImpl(
            DeploymentLogRepository deploymentLogRepository
    ) {
        this.deploymentLogRepository = deploymentLogRepository;
    }

    @Override
    public List<DeploymentLog> getLogsForApplication(Long applicationId) {
        return deploymentLogRepository
                .findByApplicationIdOrderByTimestampAsc(applicationId);
    }

    @Override
    public List<DeploymentLog> getLogsForApplicationDesc(Long applicationId) {
        return deploymentLogRepository
                .findByApplicationIdOrderByTimestampDesc(applicationId);
    }

    @Override
    public List<DeploymentLog> getLogsForApplicationByAction(
            Long applicationId,
            DeploymentAction action
    ) {
        return deploymentLogRepository
                .findByApplicationIdAndActionOrderByTimestampDesc(
                        applicationId,
                        action
                );
    }

    @Override
    public List<DeploymentLog> getLogsForApplicationByVersion(
            Long applicationId,
            String version
    ) {
        return deploymentLogRepository
                .findByApplicationIdAndVersionOrderByTimestampDesc(
                        applicationId,
                        version
                );
    }
}

