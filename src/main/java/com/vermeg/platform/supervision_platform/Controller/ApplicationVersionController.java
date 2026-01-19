package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.ApplicationVersionResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.ApplicationVersion;
import com.vermeg.platform.supervision_platform.Service.ApplicationVersionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/versions")
public class ApplicationVersionController {

    private final ApplicationVersionService versionService;

    public ApplicationVersionController(ApplicationVersionService versionService) {
        this.versionService = versionService;
    }

    @GetMapping
    public List<ApplicationVersionResponseDTO> getVersions(
            @PathVariable Long applicationId
    ) {
        return versionService.getVersionsForApplication(applicationId);
    }

    private ApplicationVersionResponseDTO toDTO(ApplicationVersion v) {
        return new ApplicationVersionResponseDTO(
                v.getId(),
                v.getVersion(),
                v.getType().name(),
                v.getStatus().name(),
                v.getDeployedAt(),
                v.getCreatedAt()
        );
    }
}
