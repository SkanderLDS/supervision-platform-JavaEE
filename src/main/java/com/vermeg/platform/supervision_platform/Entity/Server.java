package com.vermeg.platform.supervision_platform.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String host; // IP ou hostname
    @Column(nullable = false)
    private String sshUsername;
    @Column(nullable = false)
    private String sshPassword;
    @Column(nullable = false)
    private int sshPort = 22;
    @Column(nullable = false)
    private int port; // ex: 8080 / 9090
    @Column(nullable = false)
    private int managementPort; // ex: 9990 / 9991
    @Column(nullable = false)
    private String managementUsername;
    @Column(nullable = false)
    private String managementPassword;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerType type; // WILDFLY, WEBSPHERE...
    @Column(nullable = false)
    private String version; // WildFly 26, 27...
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment; // DEV, TEST, PROD
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();
    public Server() {
        this.createdAt = LocalDateTime.now();
        this.status = ServerStatus.UNKNOWN;
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
}

