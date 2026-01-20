package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;
import com.vermeg.platform.supervision_platform.Repository.ServerMetricsRepository;
import org.springframework.stereotype.Service;

@Service
public class ServerMetricsServiceImpl implements ServerMetricsService {
    private final ServerMetricsRepository repository;

    public ServerMetricsServiceImpl(ServerMetricsRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServerMetrics collectMetrics(Server server) {

        // 🔧 Simulation (REALISTIC ranges)
        double cpu = 10 + Math.random() * 80;
        double memory = 20 + Math.random() * 70;
        double disk = 30 + Math.random() * 60;

        ServerMetrics metrics =
                new ServerMetrics(server, cpu, memory, disk);

        return repository.save(metrics);
    }

    @Override
    public ServerMetrics getLatestMetrics(Long serverId) {
        return repository.findTopByServerIdOrderByCollectedAtDesc(serverId)
                .orElseThrow(() ->
                        new RuntimeException("No metrics available"));

    }
}
