package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.DeploymentLogResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.DeploymentAction;
import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;
import com.vermeg.platform.supervision_platform.Service.DeploymentLogQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deployment-logs")
public class DeploymentLogController {

    private final DeploymentLogQueryService queryService;

    public DeploymentLogController(DeploymentLogQueryService queryService) {
        this.queryService = queryService;
    }

    /* =========================
       Logs by application (ASC)
       ========================= */
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<DeploymentLog>> logsByApplication(
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                queryService.getLogsForApplication(applicationId)
        );
    }

    /* =========================
       Logs by application (DESC)
       ========================= */
    @GetMapping("/application/{applicationId}/latest")
    public ResponseEntity<List<DeploymentLog>> latestByApplication(
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                queryService.getLogsForApplicationDesc(applicationId)
        );
    }

    /* =========================
       Logs by action
       ========================= */
    @GetMapping("/application/{applicationId}/action/{action}")
    public ResponseEntity<List<DeploymentLog>> logsByAction(
            @PathVariable Long applicationId,
            @PathVariable DeploymentAction action
    ) {
        return ResponseEntity.ok(
                queryService.getLogsForApplicationByAction(
                        applicationId,
                        action
                )
        );
    }

    /* =========================
       Logs by version
       ========================= */
    @GetMapping("/application/{applicationId}/version/{version}")
    public ResponseEntity<List<DeploymentLog>> logsByVersion(
            @PathVariable Long applicationId,
            @PathVariable String version
    ) {
        return ResponseEntity.ok(
                queryService.getLogsForApplicationByVersion(
                        applicationId,
                        version
                )
        );
    }
}
