package com.vermeg.platform.supervision_platform.DTO;
import lombok.*;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppLogDTO {
    private Long id;
    private String level;
    private String message;
    private String category;
    private String threadName;
    private LocalDateTime timestamp;
    private LocalDateTime collectedAt;
    private Long serverId;
    private String serverName;
}
