package com.vermeg.platform.supervision_platform.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusDTO {
    private Long id;
    private String name;
    private String runtimeName;
    private String status;
    private String currentVersion;
}