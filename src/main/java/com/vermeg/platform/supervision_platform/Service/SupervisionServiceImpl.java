package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerMetrics;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SupervisionServiceImpl implements SupervisionService {

    private final ServerRepository serverRepository;
    private final ServerConnectivityService connectivityService;
    private final ServerMetricsService metricsService;
    private final AlertService alertService;

    public SupervisionServiceImpl(ServerRepository serverRepository, ServerConnectivityService connectivityService, ServerMetricsService metricsService, AlertService alertService) {
        this.serverRepository = serverRepository;
        this.connectivityService = connectivityService;
        this.metricsService = metricsService;
        this.alertService = alertService;
    }


    @Override
    @Transactional
    public ServerStatus superviseServer(Long serverId) {

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        ServerStatus status = connectivityService.checkServer(server);
        server.setStatus(status);
        serverRepository.save(server);
        return status;

    }
}


