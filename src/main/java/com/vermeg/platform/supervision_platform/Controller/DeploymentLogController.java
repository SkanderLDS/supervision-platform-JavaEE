package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;
import com.vermeg.platform.supervision_platform.DTO.DeploymentLogResponseDTO;
import com.vermeg.platform.supervision_platform.Service.DeploymentLogQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class DeploymentLogController {

    private final DeploymentLogQueryService logQueryService;

    public DeploymentLogController(DeploymentLogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @GetMapping("/application/{id}")
    public List<DeploymentLogResponseDTO> getLogs(@PathVariable Long id) {
        return logQueryService.getLogsForApplication(id);

    }
}
