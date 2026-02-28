package com.vermeg.platform.supervision_platform.Repository;

import com.vermeg.platform.supervision_platform.Entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByServerIdOrderByCreatedAtDesc(Long serverId);
    List<Alert> findByServerIdAndResolvedFalseOrderByCreatedAtDesc(Long serverId);
    boolean existsByServerIdAndMessageAndResolvedFalse(Long serverId, String message);
}