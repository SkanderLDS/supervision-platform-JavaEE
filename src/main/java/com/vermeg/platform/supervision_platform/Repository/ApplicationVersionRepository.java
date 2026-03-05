package com.vermeg.platform.supervision_platform.Repository;

import com.vermeg.platform.supervision_platform.Entity.ApplicationVersion;
import com.vermeg.platform.supervision_platform.Entity.DeploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationVersionRepository  extends JpaRepository<ApplicationVersion, Long> {
    List<ApplicationVersion> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
    Optional<ApplicationVersion> findTopByApplicationIdAndStatusOrderByDeployedAtDesc(
            Long applicationId, DeploymentStatus status);
    Optional<ApplicationVersion> findTopByApplicationServerIdAndStatusAndDeployedAtAfterOrderByDeployedAtDesc(
            Long serverId, DeploymentStatus status, LocalDateTime after);
}
