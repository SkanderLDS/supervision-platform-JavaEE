package com.vermeg.platform.supervision_platform.Controller;
import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Service.DeploymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/deployments")
public class DeploymentController {

    private final DeploymentService deploymentService;

    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @PostMapping("/{applicationId}/deploy")
    public ResponseEntity<Application> deploy(@PathVariable Long applicationId,
                                              @RequestParam("file") MultipartFile file)
            throws IOException {

        File artifact = toTempFile(file);
        Application app = deploymentService.deployApplication(applicationId, artifact);
        return ResponseEntity.ok(app);
    }

    @PostMapping("/{applicationId}/redeploy")
    public ResponseEntity<Application> redeploy(@PathVariable Long applicationId,
                                                @RequestParam("file") MultipartFile file)
            throws IOException {

        File artifact = toTempFile(file);
        Application app = deploymentService.redeployApplication(applicationId, artifact);
        return ResponseEntity.ok(app);
    }

    @PostMapping("/{applicationId}/start")
    public ResponseEntity<Application> start(@PathVariable Long applicationId) {

        Application app = deploymentService.startApplication(applicationId);
        return ResponseEntity.ok(app);
    }

    @PostMapping("/{applicationId}/stop")
    public ResponseEntity<Application> stop(@PathVariable Long applicationId) {

        Application app = deploymentService.stopApplication(applicationId);
        return ResponseEntity.ok(app);
    }

    // ---------------- PRIVATE UTILS ----------------

    private File toTempFile(MultipartFile multipartFile) throws IOException {

        Path tempFile = Files.createTempFile("deploy-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile.toFile());
        return tempFile.toFile();
    }
}
