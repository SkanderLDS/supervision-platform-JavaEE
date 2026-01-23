package com.vermeg.platform.supervision_platform.Entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "deployment_log",
        indexes = {
                @Index(name = "idx_deployment_log_app", columnList = "application_id"),
                @Index(name = "idx_deployment_log_time", columnList = "timestamp")
        }
)
public class DeploymentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeploymentAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeploymentStatus status;

    @Column(length = 50)
    private String version;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LogLevel level;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public DeploymentLog(
            Application application,
            DeploymentAction action,
            DeploymentStatus status,
            String version,
            String message,
            LogLevel level
    ) {
        this.application = application;
        this.action = action;
        this.status = status;
        this.version = version;
        this.message = message;
        this.level = level;
        this.timestamp = LocalDateTime.now();
    }
}


