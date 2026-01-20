package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;
import com.vermeg.platform.supervision_platform.Repository.ServerMetricsRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

@Service
public class ServerMetricsServiceImpl implements ServerMetricsService {
    private final ServerMetricsRepository repository;

    public ServerMetricsServiceImpl(ServerMetricsRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServerMetrics collectMetrics(Server server) {

        OperatingSystemMXBean osBean =
                ManagementFactory.getOperatingSystemMXBean();

        com.sun.management.OperatingSystemMXBean sunOsBean =
                (com.sun.management.OperatingSystemMXBean) osBean;

        double cpu = sunOsBean.getSystemCpuLoad() * 100;
        double memory =
                (1 - ((double) sunOsBean.getFreePhysicalMemorySize()
                        / sunOsBean.getTotalPhysicalMemorySize())) * 100;

        File root = new File("/");
        double disk =
                (1 - ((double) root.getFreeSpace() / root.getTotalSpace())) * 100;

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
