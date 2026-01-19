package com.vermeg.platform.supervision_platform.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DeploymentLogResponseDTO {

    private String message;
    private String level;
    private LocalDateTime timestamp;
}
