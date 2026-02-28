package com.vermeg.platform.supervision_platform.DTO;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupervisionResultDTO {
    private Long serverId;
    private String serverName;
    private String status;
    private ServerMetricsDTO metrics;
    private List<ApplicationStatusDTO> applications;
    private int unresolvedAlertsCount;
    private LocalDateTime checkedAt;
}