package com.vermeg.platform.supervision_platform.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String currentVersion;

    @Column(nullable = false)
    private String runtimeName;

    @Column(nullable = false)
    private String artifactName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationType type;

    private String contextPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeploymentStatus status = DeploymentStatus.UNDEPLOYED;

    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id")
    private Server server;

    private LocalDateTime lastDeployedAt;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeploymentLog> deploymentLogs = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApplicationVersion> versions = new ArrayList<>();

    /* =======================
       State transitions
       ======================= */
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

    public void stop() {
        if (this.status != DeploymentStatus.DEPLOYED) {
            throw new IllegalStateException("Application is not running");
        }
        this.status = DeploymentStatus.STOPPED;
    }

    public void start() {
        if (this.status != DeploymentStatus.STOPPED) {
            throw new IllegalStateException("Application is not stopped");
        }
        this.status = DeploymentStatus.DEPLOYED;
    }

    public void restart() {
        stop();
        start();
    }
}