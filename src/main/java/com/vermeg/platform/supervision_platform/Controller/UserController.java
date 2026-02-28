package com.vermeg.platform.supervision_platform.Controller;
import com.vermeg.platform.supervision_platform.DTO.AuditLogDTO;
import com.vermeg.platform.supervision_platform.DTO.UserRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.UserResponseDTO;
import com.vermeg.platform.supervision_platform.Service.AuditLogService;
import com.vermeg.platform.supervision_platform.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public UserController(UserService userService,
                          AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> assignRole(
            @PathVariable Long id,
            @RequestParam String roleName) {
        userService.assignRole(id, roleName);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAll());
    }
    @GetMapping("/{id}/audit-logs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsByUser(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getByUser(id));
    }
}
