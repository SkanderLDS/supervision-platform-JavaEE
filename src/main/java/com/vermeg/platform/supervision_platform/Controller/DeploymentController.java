package com.vermeg.platform.supervision_platform.Controller;
import com.vermeg.platform.supervision_platform.DTO.ApplicationVersionResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.ApplicationVersion;
import com.vermeg.platform.supervision_platform.Service.DeploymentService;
import com.vermeg.platform.supervision_platform.mapper.ApplicationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/versions")
public class DeploymentController {

    private final DeploymentService deploymentService;
    private final ApplicationMapper applicationMapper;

    @Value("${app.artifacts.path}")
    private String artifactsPath;

    public DeploymentController(DeploymentService deploymentService,
                                ApplicationMapper applicationMapper) {
        this.deploymentService = deploymentService;
        this.applicationMapper = applicationMapper;
    }

    @PostMapping("/{versionId}/deploy")
    public ResponseEntity<ApplicationVersionResponseDTO> deploy(
            @PathVariable Long versionId,
            @RequestParam("file") MultipartFile file) throws IOException {
        File artifact = saveArtifact(file, versionId);
        ApplicationVersion version = deploymentService.deploy(versionId, artifact);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationMapper.toVersionResponseDTO(version));
    }

    @PostMapping("/{versionId}/redeploy")
    public ResponseEntity<ApplicationVersionResponseDTO> redeploy(
            @PathVariable Long versionId,
            @RequestParam("file") MultipartFile file) throws IOException {
        File artifact = saveArtifact(file, versionId);
        ApplicationVersion version = deploymentService.redeploy(versionId, artifact);
        return ResponseEntity.ok(applicationMapper.toVersionResponseDTO(version));
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

    @PostMapping("/{versionId}/restart")
    public ResponseEntity<Void> restart(@PathVariable Long versionId) {
        deploymentService.restart(versionId);
        return ResponseEntity.ok().build();
    }

    /* =========================
       SAVE ARTIFACT PERMANENTLY
       Stores uploaded file in permanent artifacts directory
       ========================= */
    private File saveArtifact(MultipartFile multipartFile, Long versionId) throws IOException {
        // Create artifacts directory if it doesn't exist
        Path artifactDir = Paths.get(artifactsPath);
        if (!Files.exists(artifactDir)) {
            Files.createDirectories(artifactDir);
        }

        // Save file with version ID prefix to avoid conflicts
        String original = multipartFile.getOriginalFilename();
        String extension = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf("."))
                : ".war";

        String fileName = "version-" + versionId + extension;
        Path filePath = artifactDir.resolve(fileName);

        // Save permanently
        multipartFile.transferTo(filePath.toFile());

        return filePath.toFile();
    }
}