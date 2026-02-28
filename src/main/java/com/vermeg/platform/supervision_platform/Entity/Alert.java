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
@Table(
        name = "alerts",
        indexes = {
                @Index(name = "idx_alert_server", columnList = "server_id"),
                @Index(name = "idx_alert_created_at", columnList = "createdAt"),
                @Index(name = "idx_alert_resolved", columnList = "resolved")
        }
)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertLevel level;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private boolean resolved = false;

    private LocalDateTime resolvedAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    /* =======================
       State transitions
       ======================= */
    public void resolve() {
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
    }
}