package com.vermeg.platform.supervision_platform.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
    private Long id;
    private String action;
    private String entity;
    private String entityId;
    private String details;
    private String performedBy;
    private LocalDateTime createdAt;
}
