package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.ApplicationRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ApplicationResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Service.ApplicationService;
import com.vermeg.platform.supervision_platform.Service.DeploymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final DeploymentService deploymentService;

    public ApplicationController(ApplicationService applicationService,
                                 DeploymentService deploymentService) {
        this.applicationService = applicationService;
        this.deploymentService = deploymentService;
    }

    // ✅ CREATE
    @PostMapping
    public ApplicationResponseDTO create(@RequestBody ApplicationRequestDTO dto) {
        return applicationService.create(dto);
    }

    // ✅ READ ALL
    @GetMapping
    public List<ApplicationResponseDTO> getAll() {
        return applicationService.getAll();
    }

    // ✅ READ ONE
    @GetMapping("/{id}")
    public ApplicationResponseDTO getById(@PathVariable Long id) {
        return applicationService.getById(id);
    }

    // ✅ DEPLOY
    @PostMapping("/{id}/deploy")
    public ResponseEntity<String> deploy(@PathVariable Long id) {
        deploymentService.deployApplication(id);
        return ResponseEntity.ok("Deployment started for application " + id);
    }

    // ✅ REDEPLOY
    @PostMapping("/{id}/redeploy")
    public ResponseEntity<String> redeploy(@PathVariable Long id) {
        deploymentService.redeployApplication(id);
        return ResponseEntity.ok("Redeployment started for application " + id);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
    /// /////////////////////////////////////////////////////////////////
    ///
    ///
    @PostMapping("/{id}/start")
    public ResponseEntity<String> start(@PathVariable Long id) {
        deploymentService.startApplication(id);
        return ResponseEntity.ok("Application started");
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<String> stop(@PathVariable Long id) {
        deploymentService.stopApplication(id);
        return ResponseEntity.ok("Application stopped");
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<String> restart(@PathVariable Long id) {
        deploymentService.restartApplication(id);
        return ResponseEntity.ok("Application restarted");
    }
}



