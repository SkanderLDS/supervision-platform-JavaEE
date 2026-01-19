package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.ApplicationVersionResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Entity.ApplicationType;
import com.vermeg.platform.supervision_platform.Entity.ApplicationVersion;

import java.util.List;

public interface ApplicationVersionService {

    ApplicationVersion deployNewVersion(
            Application application,
            String version,
            ApplicationType type
    );

    List<ApplicationVersionResponseDTO> getVersionsForApplication(Long applicationId);
}
