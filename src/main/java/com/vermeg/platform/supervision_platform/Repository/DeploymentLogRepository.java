package com.vermeg.platform.supervision_platform.Repository;

import com.vermeg.platform.supervision_platform.Entity.DeploymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeploymentLogRepository extends JpaRepository<DeploymentLog,Long> {
    List<DeploymentLog> findByApplicationIdOrderByTimestampAsc(Long applicationId);
    List<DeploymentLog> findByApplicationIdOrderByTimestampDesc(Long applicationId);


}
