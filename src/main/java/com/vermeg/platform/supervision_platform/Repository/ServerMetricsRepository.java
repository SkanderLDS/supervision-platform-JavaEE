package com.vermeg.platform.supervision_platform.Repository;

import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServerMetricsRepository extends JpaRepository<ServerMetrics, Long> {
    Optional<ServerMetrics> findTopByServerIdOrderByCollectedAtDesc(Long serverId);

}
