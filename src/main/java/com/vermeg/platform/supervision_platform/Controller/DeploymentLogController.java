package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.DeploymentLogDTO;
import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;
import com.vermeg.platform.supervision_platform.Repository.DeploymentLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deployment-logs")
public class DeploymentLogController {

    private final DeploymentLogRepository deploymentLogRepository;

    public DeploymentLogController(DeploymentLogRepository deploymentLogRepository) {
        this.deploymentLogRepository = deploymentLogRepository;
    }

    @GetMapping("/applications/{applicationId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_VIEWER')")
    public ResponseEntity<List<DeploymentLogDTO>> getLogsForApplication(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(
                deploymentLogRepository.findByApplicationIdOrderByTimestampDesc(applicationId)
                        .stream()
                        .map(this::toDTO)
                        .toList()
        );
    }

    private DeploymentLogDTO toDTO(DeploymentLog log) {
        return DeploymentLogDTO.builder()
                .id(log.getId())
                .action(log.getAction().name())
                .status(log.getStatus().name())
                .version(log.getVersion())
                .message(log.getMessage())
                .level(log.getLevel().name())
                .timestamp(log.getTimestamp())
                .applicationId(log.getApplication().getId())
                .applicationName(log.getApplication().getName())
                .isRollback(log.getMessage() != null &&
                        log.getMessage().toLowerCase().contains("rollback"))
                .build();
    }
}