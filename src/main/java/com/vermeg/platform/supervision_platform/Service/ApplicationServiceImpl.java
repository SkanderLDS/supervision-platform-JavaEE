package com.vermeg.platform.supervision_platform.Service;
import com.vermeg.platform.supervision_platform.DTO.ApplicationRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ApplicationResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerSummaryDTO;
import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import com.vermeg.platform.supervision_platform.exception.ApplicationNotFoundException;
import com.vermeg.platform.supervision_platform.exception.ServerNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ServerRepository serverRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  ServerRepository serverRepository) {
        this.applicationRepository = applicationRepository;
        this.serverRepository = serverRepository;
    }
    @Override
    public ApplicationResponseDTO create(ApplicationRequestDTO dto) {
        Server server = serverRepository.findById(dto.getServerId())
                .orElseThrow(() -> new ServerNotFoundException(dto.getServerId()));
        Application app = Application.builder()
                .name(dto.getName())
                .currentVersion(dto.getCurrentVersion())
                .runtimeName(dto.getRuntimeName())
                .artifactName(dto.getArtifactName())
                .type(ApplicationType.valueOf(dto.getType()))
                .contextPath(dto.getContextPath())
                .server(server)
                .build();
        return toDTO(applicationRepository.save(app));
    }
    @Override
    public ApplicationResponseDTO update(Long id, ApplicationRequestDTO dto) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        app.setName(dto.getName());
        app.setCurrentVersion(dto.getCurrentVersion());
        app.setRuntimeName(dto.getRuntimeName());
        app.setArtifactName(dto.getArtifactName());
        app.setType(ApplicationType.valueOf(dto.getType()));
        app.setContextPath(dto.getContextPath());
        return toDTO(applicationRepository.save(app));
    }

    @Override
    public void delete(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new ApplicationNotFoundException(id);
        }
        applicationRepository.deleteById(id);
    }

    @Override
    public ApplicationResponseDTO getById(Long id) {
        return toDTO(applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id)));
    }

    @Override
    public List<ApplicationResponseDTO> getAll() {
        return applicationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<ApplicationResponseDTO> getByServerId(Long serverId) {
        if (!serverRepository.existsById(serverId)) {
            throw new ServerNotFoundException(serverId);
        }
        return applicationRepository.findByServerId(serverId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private ApplicationResponseDTO toDTO(Application app) {
        ServerSummaryDTO serverDTO = null;
        if (app.getServer() != null) {
            serverDTO = new ServerSummaryDTO(
                    app.getServer().getId(),
                    app.getServer().getName(),
                    app.getServer().getType().name(),
                    app.getServer().getStatus().name()
            );
        }
        return new ApplicationResponseDTO(
                app.getId(),
                app.getName(),
                app.getCurrentVersion(),
                app.getRuntimeName(),
                app.getArtifactName(),
                app.getType().name(),
                app.getContextPath(),
                app.getStatus().name(),
                app.getLastDeployedAt(),
                app.getCreatedAt(),
                serverDTO
        );
    }
}