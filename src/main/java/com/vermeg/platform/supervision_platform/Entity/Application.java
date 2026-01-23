package com.vermeg.platform.supervision_platform.Entity;

import com.vermeg.platform.supervision_platform.Entity.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

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
    private DeploymentStatus status;
    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id")
    private Server server;

    private LocalDateTime lastDeployedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeploymentLog> deploymentLogs = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationVersion> versions = new ArrayList<>();


    protected Application() {
        this.createdAt = LocalDateTime.now();
        this.status = DeploymentStatus.UNDEPLOYED;
    }

    public Application(String name,
                       String version,
                       String runtimeName,
                       String artifactName,
                       ApplicationType type,
                       String contextPath,
                       Server server) {

        this.name = name;
        this.version = version;
        this.runtimeName = runtimeName;
        this.artifactName = artifactName;
        this.type = type;
        this.contextPath = contextPath;
        this.server = server;

        this.createdAt = LocalDateTime.now();
        this.status = DeploymentStatus.UNDEPLOYED;
    }

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
