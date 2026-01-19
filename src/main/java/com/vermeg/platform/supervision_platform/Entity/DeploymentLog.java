package com.vermeg.platform.supervision_platform.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class DeploymentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogLevel level;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected DeploymentLog() {
        this.timestamp = LocalDateTime.now();
    }

    public DeploymentLog(Application application, String message, LogLevel level) {
        this.application = application;
        this.message = message;
        this.level = level;
        this.timestamp = LocalDateTime.now();
    }
}


