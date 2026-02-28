package com.vermeg.platform.supervision_platform.Repository;
import com.vermeg.platform.supervision_platform.Entity.AppLog;
import com.vermeg.platform.supervision_platform.Entity.LogLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AppLogRepository extends JpaRepository<AppLog, Long> {
    // Filter by server
    List<AppLog> findByServerIdOrderByTimestampDesc(Long serverId);
    // Filter by server and level
    List<AppLog> findByServerIdAndLevelOrderByTimestampDesc(
            Long serverId, LogLevel level);
    // Filter by server and date range
    List<AppLog> findByServerIdAndTimestampBetweenOrderByTimestampDesc(
            Long serverId, LocalDateTime from, LocalDateTime to);
    // Multi-criteria search
    @Query("SELECT l FROM AppLog l WHERE l.server.id = :serverId " +
            "AND (:level IS NULL OR l.level = :level) " +
            "AND (:from IS NULL OR l.timestamp >= :from) " +
            "AND (:to IS NULL OR l.timestamp <= :to) " +
            "AND (:keyword IS NULL OR l.message LIKE %:keyword%) " +
            "ORDER BY l.timestamp DESC")
    List<AppLog> searchLogs(
            @Param("serverId") Long serverId,
            @Param("level") LogLevel level,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("keyword") String keyword
    );
}