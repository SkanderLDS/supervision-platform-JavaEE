package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.Entity.ApplicationVersion;
import com.vermeg.platform.supervision_platform.Service.DeploymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/versions")
public class DeploymentController {

    private final DeploymentService deploymentService;
    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }
    @PostMapping("/{versionId}/deploy")
    public ResponseEntity<ApplicationVersion> deploy(
            @PathVariable Long versionId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        File artifact = toTempFile(file);
        ApplicationVersion version = deploymentService.deploy(versionId, artifact);
        return ResponseEntity.status(HttpStatus.CREATED).body(version);
    }
    @PostMapping("/{versionId}/redeploy")
    public ResponseEntity<ApplicationVersion> redeploy(
            @PathVariable Long versionId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        File artifact = toTempFile(file);
        ApplicationVersion version = deploymentService.redeploy(versionId, artifact);
        return ResponseEntity.ok(version);
    }
    @PostMapping("/{versionId}/start")
    public ResponseEntity<Void> start(@PathVariable Long versionId) {
        deploymentService.start(versionId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{versionId}/stop")
    public ResponseEntity<Void> stop(@PathVariable Long versionId) {
        deploymentService.stop(versionId);
        return ResponseEntity.ok().build();
    }
    private File toTempFile(MultipartFile multipartFile) throws IOException {
        File temp = File.createTempFile("deploy-", ".war");
        multipartFile.transferTo(temp);
        temp.deleteOnExit();
        return temp;
    }
}
