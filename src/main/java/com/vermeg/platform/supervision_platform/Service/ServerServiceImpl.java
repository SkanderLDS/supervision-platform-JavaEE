package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.ServerRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerSummaryDTO;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import com.vermeg.platform.supervision_platform.exception.ServerNotFoundException;
import com.vermeg.platform.supervision_platform.mapper.ServerMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;
    private final ServerConnectivityService connectivityService;
    private final ServerMapper serverMapper;

    public ServerServiceImpl(ServerRepository serverRepository,
                             ServerConnectivityService connectivityService,
                             ServerMapper serverMapper) {
        this.serverRepository = serverRepository;
        this.connectivityService = connectivityService;
        this.serverMapper = serverMapper;
    }

    @Override
    public ServerResponseDTO create(ServerRequestDTO dto) {
        Server server = serverMapper.toEntity(dto);
        return serverMapper.toResponseDTO(serverRepository.save(server));
    }

    @Override
    public ServerResponseDTO update(Long id, ServerRequestDTO dto) {
        Server server = findServer(id);
        serverMapper.updateEntityFromDTO(server, dto);
        return serverMapper.toResponseDTO(serverRepository.save(server));
    }

    @Override
    public void delete(Long id) {
        if (!serverRepository.existsById(id)) {
            throw new ServerNotFoundException(id);
        }
        serverRepository.deleteById(id);
    }

    @Override
    public List<ServerResponseDTO> getAll() {
        return serverRepository.findAll()
                .stream()
                .map(serverMapper::toResponseDTO)
                .toList();
    }

    @Override
    public ServerResponseDTO getById(Long id) {
        return serverMapper.toResponseDTO(findServer(id));
    }

    @Override
    public ServerSummaryDTO getSummaryById(Long id) {
        return serverMapper.toSummaryDTO(findServer(id));
    }

    @Override
    public ServerStatus checkSshConnectivity(Long serverId) {
        Server server = findServer(serverId);
        ServerStatus status = connectivityService.checkSsh(server);
        updateStatus(server, status);
        return status;
    }

    @Override
    public ServerStatus checkApplicationServerConnectivity(Long serverId) {
        Server server = findServer(serverId);
        ServerStatus status = connectivityService.checkApplicationServer(server);
        updateStatus(server, status);
        return status;
    }

    @Override
    public ServerStatus checkGlobalConnectivity(Long serverId) {
        Server server = findServer(serverId);
        ServerStatus status = connectivityService.checkGlobal(server);
        updateStatus(server, status);
        return status;
    }

    private void updateStatus(Server server, ServerStatus status) {
        server.setStatus(status);
        server.setLastCheckedAt(LocalDateTime.now());
        serverRepository.save(server);
    }

    private Server findServer(Long id) {
        return serverRepository.findById(id)
                .orElseThrow(() -> new ServerNotFoundException(id));
    }
}