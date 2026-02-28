package com.vermeg.platform.supervision_platform.mapper;

import com.vermeg.platform.supervision_platform.DTO.*;
import com.vermeg.platform.supervision_platform.Entity.*;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationResponseDTO toResponseDTO(Application app) {
        ApplicationResponseDTO dto = new ApplicationResponseDTO();
        dto.setId(app.getId());
        dto.setName(app.getName());
        dto.setCurrentVersion(app.getCurrentVersion());
        dto.setRuntimeName(app.getRuntimeName());
        dto.setArtifactName(app.getArtifactName());
        dto.setType(app.getType().name());
        dto.setContextPath(app.getContextPath());
        dto.setDeploymentStatus(app.getStatus().name());
        dto.setLastDeployedAt(app.getLastDeployedAt());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setServer(new ServerSummaryDTO(
                app.getServer().getId(),
                app.getServer().getName(),
                app.getServer().getType().name(),
                app.getServer().getStatus().name()
        ));
        return dto;
    }

    public ApplicationVersionResponseDTO toVersionResponseDTO(ApplicationVersion version) {
        ApplicationVersionResponseDTO dto = new ApplicationVersionResponseDTO();
        dto.setId(version.getId());
        dto.setVersion(version.getVersion());
        dto.setType(version.getType().name());
        dto.setStatus(version.getStatus().name());
        dto.setArtifactPath(version.getArtifactPath());
        dto.setApplicationId(version.getApplication().getId());
        dto.setApplicationName(version.getApplication().getName());
        dto.setDeployedAt(version.getDeployedAt());
        dto.setCreatedAt(version.getCreatedAt());
        return dto;
    }
}