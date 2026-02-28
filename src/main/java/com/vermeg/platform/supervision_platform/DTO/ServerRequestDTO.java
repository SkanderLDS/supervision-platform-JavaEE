package com.vermeg.platform.supervision_platform.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServerRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Host is required")
    private String host;

    @NotBlank(message = "SSH username is required")
    private String sshUsername;

    @NotBlank(message = "SSH password is required")
    private String sshPassword;

    @Positive(message = "SSH port must be positive")
    private int sshPort = 22;

    @Positive(message = "Port must be positive")
    private int port;

    @Positive(message = "Management port must be positive")
    private int managementPort;

    @NotBlank(message = "Management username is required")
    private String managementUsername;

    @NotBlank(message = "Management password is required")
    private String managementPassword;

    @NotBlank(message = "Server home path is required")
    private String serverHomePath;

    @NotNull(message = "Server type is required")
    private String type;

    @NotBlank(message = "Version is required")
    private String version;

    @NotNull(message = "Environment is required")
    private String environment;
}