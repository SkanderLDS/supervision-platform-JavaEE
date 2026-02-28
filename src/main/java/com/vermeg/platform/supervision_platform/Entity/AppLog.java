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
        name = "app_logs",
        indexes = {
                @Index(name = "idx_applog_server", columnList = "server_id"),
                @Index(name = "idx_applog_level", columnList = "level"),
                @Index(name = "idx_applog_timestamp", columnList = "timestamp")
        }
)
public class AppLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogLevel level;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    @Column(columnDefinition = "TEXT")
    private String category;
    @Column(columnDefinition = "TEXT")
    private String threadName;
    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime collectedAt = LocalDateTime.now();
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;
}
