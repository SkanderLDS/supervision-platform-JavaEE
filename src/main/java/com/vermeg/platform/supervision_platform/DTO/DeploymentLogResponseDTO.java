package com.vermeg.platform.supervision_platform.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentLogResponseDTO {

    private Long id;
    private String applicationName;
    private String serverName;
    private String action;
    private String status;
    private String version;
    private String message;
    private String level;
    private LocalDateTime timestamp;
}