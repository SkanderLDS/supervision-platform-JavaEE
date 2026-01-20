package com.vermeg.platform.supervision_platform.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    @Enumerated(EnumType.STRING)
    private AlertLevel level;

    private LocalDateTime createdAt;

    @ManyToOne
    private Server server;

    @ManyToOne
    private Application application;

    protected Alert() {
        this.createdAt = LocalDateTime.now();
    }

    public Alert(String message, AlertLevel level, Server server) {
        this.message = message;
        this.level = level;
        this.server = server;
        this.createdAt = LocalDateTime.now();
    }
}
