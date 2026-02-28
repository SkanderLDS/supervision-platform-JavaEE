package com.vermeg.platform.supervision_platform.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerMetricsDTO {
    private Long id;
    private Long serverId;
    private String serverName;
    private double cpuUsage;
    private double memoryUsage;
    private double diskUsage;
    private LocalDateTime collectedAt;
}