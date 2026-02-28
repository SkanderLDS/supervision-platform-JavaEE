package com.vermeg.platform.supervision_platform.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Current version is required")
    private String currentVersion;

    @NotBlank(message = "Runtime name is required")
    private String runtimeName;

    @NotBlank(message = "Artifact name is required")
    private String artifactName;

    @NotNull(message = "Application type is required")
    private String type;

    private String contextPath;

    @NotNull(message = "Server ID is required")
    private Long serverId;
}