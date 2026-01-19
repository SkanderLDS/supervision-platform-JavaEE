package com.vermeg.platform.supervision_platform.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ApplicationVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String version; // ex: 1.0.0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationType type; // WAR / EAR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeploymentStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deployedAt;

    // 🔗 Relation métier
    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    protected ApplicationVersion() {
        this.createdAt = LocalDateTime.now();
        this.status = DeploymentStatus.UNDEPLOYED;
    }

    public ApplicationVersion(Application application, String version, ApplicationType type) {
        this.application = application;
        this.version = version;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.status = DeploymentStatus.UNDEPLOYED;
    }
    public void markDeploying() {
        this.status = DeploymentStatus.IN_PROGRESS;
    }

    public void markDeployed() {
        this.status = DeploymentStatus.DEPLOYED;
        this.deployedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = DeploymentStatus.FAILED;
    }
}
