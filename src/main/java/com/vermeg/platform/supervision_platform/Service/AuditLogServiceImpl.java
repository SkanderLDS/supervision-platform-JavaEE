package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.AuditLogDTO;
import com.vermeg.platform.supervision_platform.Entity.AuditLog;
import com.vermeg.platform.supervision_platform.Entity.User;
import com.vermeg.platform.supervision_platform.Repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;
    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    @Override
    public void log(String action, String entity,
                    String entityId, String details, User performedBy) {
        AuditLog log = AuditLog.builder().action(action).entity(entity).entityId(entityId).details(details)
                .performedBy(performedBy).build();
        auditLogRepository.save(log);
    }

    @Override
    public List<AuditLogDTO> getAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<AuditLogDTO> getByUser(Long userId) {
        return auditLogRepository.findByPerformedByIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .toList();
    }
    private AuditLogDTO toDTO(AuditLog log) {
        return AuditLogDTO.builder().id(log.getId()).action(log.getAction()).entity(log.getEntity())
                .entityId(log.getEntityId()).details(log.getDetails()).performedBy(log.getPerformedBy() != null
                        ? log.getPerformedBy().getUsername() : "system").createdAt(log.getCreatedAt())
                .build();
    }
}
