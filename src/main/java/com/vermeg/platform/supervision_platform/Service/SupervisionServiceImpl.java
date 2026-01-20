package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
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

    public SupervisionServiceImpl(ServerRepository serverRepository,
                                  ServerConnectivityService connectivityService,
                                  ServerMetricsService metricsService) {
        this.serverRepository = serverRepository;
        this.connectivityService = connectivityService;
        this.metricsService = metricsService;
    }



    @Override
    public void superviseServer(Long serverId) {

        Server server = serverRepository.findById(serverId).orElseThrow(() -> new RuntimeException("Server not found"));
        boolean reachable = connectivityService.checkConnectivity(server);
        if (!reachable) {
            server.setStatus(ServerStatus.DOWN);
            serverRepository.save(server);
            return;}
        server.setStatus(ServerStatus.UP);
        metricsService.collectMetrics(server);
        serverRepository.save(server);
    }
}


