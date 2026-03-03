package com.vermeg.platform.supervision_platform.Repository;
import com.vermeg.platform.supervision_platform.Entity.AlertRule;
import com.vermeg.platform.supervision_platform.Entity.AlertRuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    List<AlertRule> findByServerIdAndEnabledTrue(Long serverId);
    List<AlertRule> findByServerId(Long serverId);
    List<AlertRule> findByServerIdAndTypeAndEnabledTrue(Long serverId, AlertRuleType type);
}
