package com.vermeg.platform.supervision_platform.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ServerResponseDTO {

    private Long id;
    private String name;
    private String host;
    private String port;
    private String type;
    private String version;
    private String status;
    private LocalDateTime createdAt;
}

