package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.ApplicationVersionResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.ApplicationVersionRepository;
import com.vermeg.platform.supervision_platform.exception.ApplicationNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ApplicationVersionServiceImpl implements ApplicationVersionService {

    private final ApplicationVersionRepository versionRepository;
    private final ApplicationRepository applicationRepository;

    public ApplicationVersionServiceImpl(ApplicationVersionRepository versionRepository,
                                         ApplicationRepository applicationRepository) {
        this.versionRepository = versionRepository;
        this.applicationRepository = applicationRepository;
    }

    @Value("${app.artifacts.path}")
    private String artifactsPath;

    public ApplicationVersionResponseDTO createVersion(Long applicationId, String version, String type) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        // Save first to get the ID
        ApplicationVersion appVersion = ApplicationVersion.builder()
                .application(application)
                .version(version)
                .type(ApplicationType.valueOf(type))
                .artifactPath("pending") // temporary
                .build();

        ApplicationVersion saved = versionRepository.save(appVersion);

        // Now set the permanent artifact path using the generated ID
        String artifactPath = artifactsPath + "/version-" + saved.getId() + ".war";
        saved.setArtifactPath(artifactPath);
        versionRepository.save(saved);

        return toDTO(saved);
    }

    @Override
    public ApplicationVersion deployNewVersion(
            Application application,
            String version,
            ApplicationType type
    ) {
        ApplicationVersion appVersion = ApplicationVersion.builder()
                .application(application)
                .version(version)
                .type(type)
                .build();

        appVersion.markDeploying();
        application.markDeploying();
        applicationRepository.save(application);
        return versionRepository.save(appVersion);
    }

    @Override
    public void markVersionDeployed(ApplicationVersion version) {
        version.markDeployed();
        Application app = version.getApplication();
        app.markDeployed();
        app.setCurrentVersion(version.getVersion());
        applicationRepository.save(app);
        versionRepository.save(version);
    }

    @Override
    public void markVersionFailed(ApplicationVersion version) {
        version.markFailed();
        Application app = version.getApplication();
        app.markFailed();
        applicationRepository.save(app);
        versionRepository.save(version);
    }

    @Override
    public List<ApplicationVersionResponseDTO> getVersionsForApplication(Long applicationId) {
        return versionRepository
                .findByApplicationIdOrderByCreatedAtDesc(applicationId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ================= MAPPER =================
    private ApplicationVersionResponseDTO toDTO(ApplicationVersion version) {
        return new ApplicationVersionResponseDTO(
                version.getId(),
                version.getVersion(),
                version.getType().name(),
                version.getStatus().name(),
                version.getArtifactPath(),
                version.getApplication().getId(),
                version.getApplication().getName(),
                version.getDeployedAt(),
                version.getCreatedAt()
        );
    }
}