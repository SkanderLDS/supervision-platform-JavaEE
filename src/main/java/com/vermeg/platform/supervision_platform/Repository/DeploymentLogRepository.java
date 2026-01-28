package com.vermeg.platform.supervision_platform.Repository;

import com.vermeg.platform.supervision_platform.Entity.DeploymentAction;
import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentLogRepository extends JpaRepository<DeploymentLog,Long> {
    List<DeploymentLog> findByApplicationIdOrderByTimestampAsc(Long applicationId);
    List<DeploymentLog> findByApplicationIdOrderByTimestampDesc(Long applicationId);

    List<DeploymentLog> findByApplicationIdAndActionOrderByTimestampDesc(
            Long applicationId,
            DeploymentAction action
    );

    List<DeploymentLog> findByApplicationIdAndVersionOrderByTimestampDesc(
            Long applicationId,
            String version
    );
}
