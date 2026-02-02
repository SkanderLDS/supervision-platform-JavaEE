package com.vermeg.platform.supervision_platform.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerRequestDTO {

    private String name;
    private String host;
    private String sshUsername;
    private String sshPassword;
    private int sshPort = 22;
    private int port;
    private int managementPort;
    private String managementUsername;
    private String managementPassword;
    private String type;        // WILDFLY, WEBSPHERE
    private String version;     // 26, 27...
    private String environment; // DEV, TEST, PROD

}

