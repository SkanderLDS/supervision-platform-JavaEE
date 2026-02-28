package com.vermeg.platform.supervision_platform.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServerResponseDTO {

    private Long id;
    private String name;
    private String host;
    private String sshUsername;
    private String managementUsername;
    private int sshPort;
    private int port;
    private int managementPort;
    private String serverHomePath;
    private String type;
    private String version;
    private String environment;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastCheckedAt;
}