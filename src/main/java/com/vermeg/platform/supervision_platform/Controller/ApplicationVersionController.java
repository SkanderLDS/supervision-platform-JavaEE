package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.ApplicationVersionResponseDTO;
import com.vermeg.platform.supervision_platform.Service.ApplicationVersionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/versions")
public class ApplicationVersionController {

    private final ApplicationVersionService versionService;

    public ApplicationVersionController(ApplicationVersionService versionService) {
        this.versionService = versionService;
    }
    @PostMapping
    public ResponseEntity<ApplicationVersionResponseDTO> createVersion(
            @PathVariable Long applicationId,
            @RequestParam String version,
            @RequestParam String type) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(versionService.createVersion(applicationId, version, type));
    }
    @GetMapping
    public ResponseEntity<List<ApplicationVersionResponseDTO>> getVersions(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(versionService.getVersionsForApplication(applicationId));
    }
}