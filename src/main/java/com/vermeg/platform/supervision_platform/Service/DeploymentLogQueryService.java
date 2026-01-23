package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.DeploymentLogResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;

import java.util.List;

public interface DeploymentLogQueryService {

    List<DeploymentLogResponseDTO> getLogsForApplication(Long applicationId);
    List<DeploymentLog> getApplicationDeploymentHistory(Long applicationId);
}
