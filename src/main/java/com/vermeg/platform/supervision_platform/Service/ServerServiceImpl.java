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
@Transactional
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;
    private final ServerConnectivityService connectivityService;

    public ServerServiceImpl(ServerRepository serverRepository,
                             ServerConnectivityService connectivityService) {
        this.serverRepository = serverRepository;
        this.connectivityService = connectivityService;
    }

    /* =========================
       CRUD
       ========================= */

    @Override
    public ServerResponseDTO create(ServerRequestDTO dto) {

        Server server = new Server();
        server.setName(dto.getName());
        server.setHost(dto.getHost());
        server.setSshUsername(dto.getSshUsername());
        server.setSshPassword(dto.getSshPassword());
        server.setSshPort(dto.getSshPort());
        server.setPort(dto.getPort());
        server.setManagementPort(dto.getManagementPort());
        server.setManagementUsername(dto.getManagementUsername());
        server.setManagementPassword(dto.getManagementPassword());
        server.setType(ServerType.valueOf(dto.getType()));
        server.setVersion(dto.getVersion());
        server.setEnvironment(Environment.valueOf(dto.getEnvironment()));

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
    public ServerResponseDTO update(Long id, ServerRequestDTO dto) {

        Server server = findServer(id);

        server.setName(dto.getName());
        server.setHost(dto.getHost());

        // SSH
        server.setSshUsername(dto.getSshUsername());
        server.setSshPassword(dto.getSshPassword());
        server.setSshPort(dto.getSshPort());

        // Application ports
        server.setPort(dto.getPort());
        server.setManagementPort(dto.getManagementPort());

        // Management credentials
        server.setManagementUsername(dto.getManagementUsername());
        server.setManagementPassword(dto.getManagementPassword());

        server.setType(ServerType.valueOf(dto.getType()));
        server.setVersion(dto.getVersion());
        server.setEnvironment(Environment.valueOf(dto.getEnvironment()));

        return toResponseDTO(serverRepository.save(server));
    }
    @Override
    public ServerStatus checkSshConnectivity(Long serverId) {

        Server server = findServer(serverId);
        ServerStatus status = connectivityService.checkSsh(server);

        server.setStatus(status);
        serverRepository.save(server);
        return status;
    }

    @Override
    public ServerStatus checkApplicationServerConnectivity(Long serverId) {

        Server server = findServer(serverId);
        ServerStatus status = connectivityService.checkApplicationServer(server);
        server.setStatus(status);
        serverRepository.save(server);
        return status;
    }

    @Override
    public ServerStatus checkGlobalConnectivity(Long serverId) {
        Server server = findServer(serverId);
        ServerStatus status = connectivityService.checkGlobal(server);
        server.setStatus(status);
        serverRepository.save(server);
        return status;
    }
    private Server findServer(Long id) {
        return serverRepository.findById(id).orElseThrow(() -> new RuntimeException("Server not found"));
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






