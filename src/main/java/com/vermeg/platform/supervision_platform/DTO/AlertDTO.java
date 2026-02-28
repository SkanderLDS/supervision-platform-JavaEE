package com.vermeg.platform.supervision_platform.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDTO {
    private Long id;
    private String message;
    private String level;
    private boolean resolved;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Long serverId;
    private String serverName;
    private Long applicationId;
    private String applicationName;
}