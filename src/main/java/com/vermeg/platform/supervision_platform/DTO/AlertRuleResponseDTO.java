package com.vermeg.platform.supervision_platform.DTO;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRuleResponseDTO {
    private Long id;
    private String name;
    private String type;
    private double threshold;
    private String level;
    private boolean enabled;
    private LocalDateTime createdAt;
    private Long serverId;
    private String serverName;
    private boolean emailNotification;
    private String notificationEmail;
}
