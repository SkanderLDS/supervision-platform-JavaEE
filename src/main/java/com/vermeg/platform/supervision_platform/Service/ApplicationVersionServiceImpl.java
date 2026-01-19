package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Entity.ApplicationType;
import com.vermeg.platform.supervision_platform.Entity.ApplicationVersion;
import com.vermeg.platform.supervision_platform.Entity.DeploymentStatus;
import com.vermeg.platform.supervision_platform.Repository.ApplicationVersionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ApplicationVersionServiceImpl implements ApplicationVersionService {
    private final ApplicationVersionRepository versionRepository;
    public ApplicationVersionServiceImpl(ApplicationVersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }


    @Override
    public ApplicationVersion deployNewVersion(
            Application application,
            String version,
            ApplicationType type
    ) {
        // 1️⃣ Create version (UNDEPLOYED by default)
        ApplicationVersion appVersion =
                new ApplicationVersion(application, version, type);

        // 2️⃣ Start deployment
        appVersion.markDeploying();

        // 3️⃣ Save
        return versionRepository.save(appVersion);
    }

    @Override
    public List<ApplicationVersion> getVersionsForApplication(Long applicationId) {
        return versionRepository
                .findByApplicationIdOrderByCreatedAtDesc(applicationId);
    }


}
