package com.vermeg.platform.supervision_platform.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleRequestDTO {
    @NotBlank(message = "Rule name is required")
    private String name;
    @NotNull(message = "Rule type is required")
    private String type;
    @NotNull(message = "Threshold is required")
    @Positive(message = "Threshold must be positive")
    private double threshold;
    @NotNull(message = "Alert level is required")
    private String level;
    private boolean emailNotification;
    private String notificationEmail;
    @NotNull(message = "Server ID is required")
    private Long serverId;
}