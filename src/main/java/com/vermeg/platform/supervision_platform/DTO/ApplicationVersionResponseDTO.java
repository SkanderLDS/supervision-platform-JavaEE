package com.vermeg.platform.supervision_platform.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApplicationVersionResponseDTO {

    private Long id;
    private String version;
    private String type;
    private String status;
    private LocalDateTime deployedAt;
    private LocalDateTime createdAt;
}
