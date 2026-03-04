package com.vermeg.platform.supervision_platform.Controller;
import com.vermeg.platform.supervision_platform.DTO.AlertRuleRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.AlertRuleResponseDTO;
import com.vermeg.platform.supervision_platform.Service.AlertRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alert-rules")
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    public AlertRuleController(AlertRuleService alertRuleService) {
        this.alertRuleService = alertRuleService;
    }

    /* =========================
       CREATE RULE — ADMIN/MANAGER only
       ========================= */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<AlertRuleResponseDTO> createRule(
            @Valid @RequestBody AlertRuleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alertRuleService.createRule(dto));
    }

    /* =========================
       UPDATE RULE — ADMIN/MANAGER only
       ========================= */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<AlertRuleResponseDTO> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody AlertRuleRequestDTO dto) {
        return ResponseEntity.ok(alertRuleService.updateRule(id, dto));
    }

    /* =========================
       DELETE RULE — ADMIN only
       ========================= */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        alertRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    /* =========================
       ENABLE RULE — ADMIN/MANAGER only
       ========================= */
    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> enableRule(@PathVariable Long id) {
        alertRuleService.enableRule(id);
        return ResponseEntity.ok().build();
    }

    /* =========================
       DISABLE RULE — ADMIN/MANAGER only
       ========================= */
    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> disableRule(@PathVariable Long id) {
        alertRuleService.disableRule(id);
        return ResponseEntity.ok().build();
    }

    /* =========================
       GET RULES FOR SERVER — ALL authenticated
       ========================= */
    @GetMapping("/servers/{serverId}")
    public ResponseEntity<List<AlertRuleResponseDTO>> getRulesForServer(
            @PathVariable Long serverId) {
        return ResponseEntity.ok(alertRuleService.getRulesForServer(serverId));
    }

    /* =========================
       EVALUATE RULES FOR SERVER — ADMIN/MANAGER only
       ========================= */
    @PostMapping("/servers/{serverId}/evaluate")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> evaluateRules(@PathVariable Long serverId) {
        alertRuleService.evaluateRulesForServer(serverId);
        return ResponseEntity.ok().build();
    }
}
