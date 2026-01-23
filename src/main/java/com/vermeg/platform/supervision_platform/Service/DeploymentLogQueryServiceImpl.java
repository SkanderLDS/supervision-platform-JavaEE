package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.DeploymentLogResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;
import com.vermeg.platform.supervision_platform.Repository.DeploymentLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeploymentLogQueryServiceImpl implements DeploymentLogQueryService {

    private final DeploymentLogRepository logRepository;

    public DeploymentLogQueryServiceImpl(DeploymentLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public List<DeploymentLogResponseDTO> getLogsForApplication(Long applicationId) {
        return logRepository.findByApplicationIdOrderByTimestampAsc(applicationId)
                .stream()
                .map(log -> new DeploymentLogResponseDTO(
                        log.getMessage(),
                        log.getLevel().name(),
                        log.getTimestamp()
                ))
                .toList();
    }

    @Override
    public List<DeploymentLog> getApplicationDeploymentHistory(Long applicationId) {
        return logRepository
                .findByApplicationIdOrderByTimestampDesc(applicationId);
    }
}
