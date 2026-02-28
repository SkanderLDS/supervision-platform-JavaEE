package com.vermeg.platform.supervision_platform.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeploymentStatus status = DeploymentStatus.UNDEPLOYED;

    // Path where the artifact is stored on the platform server
    @Column(nullable = false)
    private String artifactPath;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime deployedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

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

    public void markStopped() {
        this.status = DeploymentStatus.STOPPED;
    }
}