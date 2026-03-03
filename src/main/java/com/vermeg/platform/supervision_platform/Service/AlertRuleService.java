package com.vermeg.platform.supervision_platform.Service;
import com.vermeg.platform.supervision_platform.DTO.AlertRuleRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.AlertRuleResponseDTO;
import java.util.List;

public interface AlertRuleService {
    AlertRuleResponseDTO createRule(AlertRuleRequestDTO dto);
    AlertRuleResponseDTO updateRule(Long id, AlertRuleRequestDTO dto);
    void deleteRule(Long id);
    void enableRule(Long id);
    void disableRule(Long id);
    List<AlertRuleResponseDTO> getRulesForServer(Long serverId);
    void evaluateRulesForServer(Long serverId);
}