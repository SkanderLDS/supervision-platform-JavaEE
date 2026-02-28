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
        name = "server_metrics",
        indexes = {
                @Index(name = "idx_metrics_server", columnList = "server_id"),
                @Index(name = "idx_metrics_collected_at", columnList = "collectedAt")
        }
)
public class ServerMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double cpuUsage;

    @Column(nullable = false)
    private double memoryUsage;

    @Column(nullable = false)
    private double diskUsage;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime collectedAt = LocalDateTime.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;
}