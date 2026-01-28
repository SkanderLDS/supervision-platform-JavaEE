package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.DeploymentLogResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.DeploymentAction;
import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;

import java.util.List;
import java.util.Optional;

public interface DeploymentLogQueryService {

    List<DeploymentLog> getLogsForApplication(Long applicationId);

    List<DeploymentLog> getLogsForApplicationDesc(Long applicationId);

    List<DeploymentLog> getLogsForApplicationByAction(
            Long applicationId,
            DeploymentAction action
    );

    List<DeploymentLog> getLogsForApplicationByVersion(
            Long applicationId,
            String version
    );
}
