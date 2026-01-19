package com.vermeg.platform.supervision_platform.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ServerMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double cpuUsage;
    private double memoryUsage;
    private double diskUsage;

    private LocalDateTime collectedAt;

    @OneToOne
    @JoinColumn(name = "server_id")
    private Server server;

    protected ServerMetrics() {
        this.collectedAt = LocalDateTime.now();
    }

    public ServerMetrics(Server server,
                         double cpu,
                         double memory,
                         double disk) {
        this.server = server;
        this.cpuUsage = cpu;
        this.memoryUsage = memory;
        this.diskUsage = disk;
        this.collectedAt = LocalDateTime.now();
    }
}
