package com.vermeg.platform.supervision_platform.Repository;
import com.vermeg.platform.supervision_platform.Entity.AppLog;
import com.vermeg.platform.supervision_platform.Entity.LogLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AppLogRepository extends JpaRepository<AppLog, Long>,
        JpaSpecificationExecutor<AppLog> {

    List<AppLog> findByServerIdOrderByTimestampDesc(Long serverId);
    List<AppLog> findByServerIdAndLevelOrderByTimestampDesc(Long serverId, LogLevel level);
    boolean existsByServerIdAndTimestampAndMessage(Long serverId, java.time.LocalDateTime timestamp, String message);
}