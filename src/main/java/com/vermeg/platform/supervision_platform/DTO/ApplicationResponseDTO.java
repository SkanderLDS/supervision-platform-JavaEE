package com.vermeg.platform.supervision_platform.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ApplicationResponseDTO {

    private Long id;
    private String name;
    private String version;
    private String type;
    private String contextPath;
    private String deploymentStatus;
    private LocalDateTime lastDeployedAt;

    private ServerSummaryDTO server;
}

