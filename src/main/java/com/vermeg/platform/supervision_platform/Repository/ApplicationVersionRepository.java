package com.vermeg.platform.supervision_platform.Repository;

import com.vermeg.platform.supervision_platform.Entity.ApplicationVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationVersionRepository  extends JpaRepository<ApplicationVersion, Long> {
    List<ApplicationVersion> findApplicationIdOrderByDeployedAtDesc(Long applicationId);
}
