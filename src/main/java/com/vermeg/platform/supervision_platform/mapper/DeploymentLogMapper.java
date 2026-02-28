package com.vermeg.platform.supervision_platform.mapper;

import com.vermeg.platform.supervision_platform.DTO.DeploymentLogResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;
import org.springframework.stereotype.Component;

@Component
public class DeploymentLogMapper {

    public DeploymentLogResponseDTO toResponseDTO(DeploymentLog log) {
        DeploymentLogResponseDTO dto = new DeploymentLogResponseDTO();
        dto.setId(log.getId());
        dto.setApplicationName(log.getApplication().getName());
        dto.setServerName(log.getApplication().getServer().getName());
        dto.setAction(log.getAction().name());
        dto.setStatus(log.getStatus().name());
        dto.setVersion(log.getVersion());
        dto.setMessage(log.getMessage());
        dto.setLevel(log.getLevel().name());
        dto.setTimestamp(log.getTimestamp());
        return dto;
    }
}