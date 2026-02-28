package com.vermeg.platform.supervision_platform.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationVersionResponseDTO {

    private Long id;
    private String version;
    private String type;
    private String status;
    private String artifactPath;
    private Long applicationId;
    private String applicationName;
    private LocalDateTime deployedAt;
    private LocalDateTime createdAt;
}