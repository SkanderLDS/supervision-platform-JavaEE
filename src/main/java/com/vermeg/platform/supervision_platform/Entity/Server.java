package com.vermeg.platform.supervision_platform.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;
    @Column(nullable = false)
    private String host;
    @Column(nullable = false)
    private String port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerType type;
    @Column(nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerStatus status;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();



    public Server() {
        this.createdAt = LocalDateTime.now();this.status=ServerStatus.UP;
    }

    public Server(String name, String host,String port, ServerType type,String version, ServerStatus status) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.type = type;
        this.version = version;
        this.status = ServerStatus.UP;
        this.createdAt = LocalDateTime.now();
    }



}