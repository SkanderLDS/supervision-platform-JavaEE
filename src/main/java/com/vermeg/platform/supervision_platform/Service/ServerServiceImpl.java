package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.ServerRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerSummaryDTO;
import com.vermeg.platform.supervision_platform.Entity.Environment;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import com.vermeg.platform.supervision_platform.Entity.ServerType;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import com.vermeg.platform.supervision_platform.Service.ServerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;
    private final ServerConnectivityService connectivityService;

    public ServerServiceImpl(ServerRepository serverRepository,ServerConnectivityService connectivityService)
    {
        this.serverRepository = serverRepository;
        this.connectivityService = connectivityService;
    }

    @Override
    public ServerResponseDTO create(ServerRequestDTO dto) {
        Server server = new Server(
                dto.getName(),
                dto.getHost(),
                dto.getPort(),
                ServerType.valueOf(dto.getType()),
                dto.getVersion(),
                Environment.valueOf(dto.getEnvironment())
        );

        return toResponseDTO(serverRepository.save(server));
    }

    @Override
    public List<ServerResponseDTO> getAll() {
        return serverRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public ServerResponseDTO getById(Long id) {
        return toResponseDTO(
                serverRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Server not found"))
        );
    }

    @Override
    public ServerSummaryDTO getSummaryById(Long id) {
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        return new ServerSummaryDTO(
                server.getId(),
                server.getName(),
                server.getType().name(),
                server.getStatus().name()
        );
    }

    @Override
    public void delete(Long id) {
        if (!serverRepository.existsById(id)) {
            throw new RuntimeException("Server not found");
        }
        serverRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ServerStatus checkConnectivity(Long serverId) {

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        ServerStatus status = connectivityService.checkServer(server);

        server.setStatus(status);
        serverRepository.save(server);

        return status;
    }

    private ServerResponseDTO toResponseDTO(Server server) {
        return new ServerResponseDTO(
                server.getId(),
                server.getName(),
                server.getHost(),
                server.getPort(),
                server.getType().name(),
                server.getVersion(),
                server.getEnvironment().name(),
                server.getStatus().name(),
                server.getCreatedAt()
        );
    }
}






