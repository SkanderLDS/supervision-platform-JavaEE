package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.ApplicationRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ApplicationResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerSummaryDTO;
import com.vermeg.platform.supervision_platform.Entity.Application;
import com.vermeg.platform.supervision_platform.Entity.ApplicationType;
import com.vermeg.platform.supervision_platform.Entity.DeploymentStatus;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ServerRepository serverRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  ServerRepository serverRepository) {
        this.applicationRepository = applicationRepository;
        this.serverRepository = serverRepository;
    }

    // ================= CREATE =================
    @Override
    public ApplicationResponseDTO create(ApplicationRequestDTO dto) {

        Application app = new Application();
        app.setName(dto.getName());
        app.setVersion(dto.getVersion());
        app.setContextPath(dto.getContextPath());
        app.setType(ApplicationType.valueOf(dto.getType()));
        app.setStatus(DeploymentStatus.UNDEPLOYED);
        app.setCreatedAt(LocalDateTime.now());

        if (dto.getServerId() != null) {
            app.setServer(
                    serverRepository.findById(dto.getServerId())
                            .orElseThrow(() -> new RuntimeException("Server not found"))
            );
        }

        Application saved = applicationRepository.save(app);
        return toDTO(saved);
    }

    // ================= READ ALL =================
    @Override
    public List<ApplicationResponseDTO> getAll() {
        return applicationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ================= READ ONE =================
    @Override
    public ApplicationResponseDTO getById(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        return toDTO(app);
    }

    // ================= DELETE =================
    @Override
    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new RuntimeException("Application not found");
        }
        applicationRepository.deleteById(id);
    }

    // ================= MAPPER =================
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
                app.getVersion(),
                app.getType().name(),
                app.getContextPath(),
                app.getStatus().name(),
                app.getLastDeployedAt(),
                serverDTO
        );
    }
}
