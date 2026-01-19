package com.vermeg.platform.supervision_platform.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerRequestDTO {
    private String name;
    private String host;
    private int port;
    private String type;
    private String version;
    private String environment;
}
