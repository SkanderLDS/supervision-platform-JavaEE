package com.vermeg.platform.supervision_platform.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String version;

    @Enumerated(EnumType.STRING)
    private ApplicationType type;

    private String contextPath;

    @Enumerated(EnumType.STRING)
    private DeploymentStatus status;

    @ManyToOne
    @JoinColumn(name = "server_id")
    private Server server;

    private LocalDateTime lastDeployedAt;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    private List<DeploymentLog> deploymentLogs = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    private List<ApplicationVersion> versions = new ArrayList<>();

    public Application() {
        this.createdAt = LocalDateTime.now();
    }

    public Application(String name, String version,
                       ApplicationType type,
                       String contextPath,
                       Server server) {

        this.name = name;
        this.version = version;
        this.type = type;
        this.contextPath = contextPath;
        this.server = server;

        this.status = DeploymentStatus.UNDEPLOYED;
    }

    public void markDeploying() {
        this.status = DeploymentStatus.IN_PROGRESS;
    }

    public void markDeployed() {
        this.status = DeploymentStatus.DEPLOYED;
        this.lastDeployedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = DeploymentStatus.FAILED;
    }

    public void start() {
        if (this.status != DeploymentStatus.DEPLOYED &&
                this.status != DeploymentStatus.STOPPED) {
            throw new IllegalStateException("Application cannot be started");
        }
        this.status = DeploymentStatus.DEPLOYED;
    }

    public void stop() {
        if (this.status != DeploymentStatus.DEPLOYED) {
            throw new IllegalStateException("Application is not running");
        }
        this.status = DeploymentStatus.STOPPED;
    }

    public void restart() {
        stop();
        start();
    }
}

