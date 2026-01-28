package com.vermeg.platform.supervision_platform.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequestDTO {
    private String name;
    private String currentVersion;
    private String runtimeName;   // ← WildFly runtime name
    private String artifactName;  // ← WAR / EAR filename
    private String type;
    private String contextPath;
    private Long serverId;
}