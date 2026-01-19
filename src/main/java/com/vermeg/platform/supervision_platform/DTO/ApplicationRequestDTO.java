package com.vermeg.platform.supervision_platform.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequestDTO {
    private String name;
    private String version;
    private String contextPath;
    private String type;
    private Long serverId;
}