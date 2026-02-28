package com.vermeg.platform.supervision_platform.Entity;

import com.vermeg.platform.supervision_platform.Config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"host", "environment"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String host;

    // SSH
    @Column(nullable = false)
    private String sshUsername;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String sshPassword;

    @Builder.Default
    @Column(nullable = false)
    private int sshPort = 22;

    // App server
    @Column(nullable = false)
    private int port;

    @Column(nullable = false)
    private int managementPort;

    @Column(nullable = false)
    private String managementUsername;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String managementPassword;

    @Column(nullable = false)
    private String serverHomePath;

    // Enums
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerType type;

    @Column(nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ServerStatus status = ServerStatus.UNKNOWN;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // Timestamps
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastCheckedAt;

    // Relations
    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Application> applications = new ArrayList<>();
}
//    public Server(
//            String name,
//            String host,
//            String sshUsername,
//            String sshPassword,
//            int sshPort,
//            int port,
//            int managementPort,
//            String managementUsername,
//            String managementPassword,
//            ServerType type,
//            String version,
//            Environment environment
//    ) {
//        this.name = name;
//        this.host = host;
//        this.sshUsername = sshUsername;
//        this.sshPassword = sshPassword;
//        this.sshPort = sshPort;
//        this.port = port;
//        this.managementPort = managementPort;
//        this.managementUsername = managementUsername;
//        this.managementPassword = managementPassword;
//        this.type = type;
//        this.version = version;
//        this.environment = environment;
//        this.status = ServerStatus.UNKNOWN;
//        this.createdAt = LocalDateTime.now();
//    }


