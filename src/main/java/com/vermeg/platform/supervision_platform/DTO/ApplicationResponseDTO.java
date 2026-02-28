package com.vermeg.platform.supervision_platform.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponseDTO {

    private Long id;
    private String name;
    private String currentVersion;
    private String runtimeName;
    private String artifactName;
    private String type;
    private String contextPath;
    private String deploymentStatus;
    private LocalDateTime lastDeployedAt;
    private LocalDateTime createdAt;
    private ServerSummaryDTO server;
}