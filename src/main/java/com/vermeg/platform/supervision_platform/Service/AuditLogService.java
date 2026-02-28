package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.AuditLogDTO;
import com.vermeg.platform.supervision_platform.Entity.User;

import java.util.List;

public interface AuditLogService {
    void log(String action, String entity, String entityId, String details, User performedBy);
    List<AuditLogDTO> getAll();
    List<AuditLogDTO> getByUser(Long userId);
}
