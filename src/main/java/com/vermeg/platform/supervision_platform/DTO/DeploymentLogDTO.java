package com.vermeg.platform.supervision_platform.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeploymentLogDTO {
    private Long id;
    private String action;
    private String status;
    private String version;
    private String message;
    private String level;
    private LocalDateTime timestamp;
    private Long applicationId;
    private String applicationName;
    private boolean isRollback;
}