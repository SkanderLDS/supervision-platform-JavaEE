package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.ApplicationRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ApplicationResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerSummaryDTO;
import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.ApplicationRepository;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import org.springframework.stereotype.Service;

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
    @Override
    public ApplicationResponseDTO create(ApplicationRequestDTO dto) {

        Server server = serverRepository.findById(dto.getServerId()).orElseThrow(() -> new RuntimeException("Server not found"));
        Application app = new Application(
                dto.getName(),
                null,
                dto.getRuntimeName(),
                dto.getArtifactName(),
                ApplicationType.valueOf(dto.getType()),
                dto.getContextPath(),
                server
        );

        Application saved = applicationRepository.save(app);
        return toDTO(saved);
    }

    @Override
    public List<ApplicationResponseDTO> getAll() {
        return applicationRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }
    @Override
    public ApplicationResponseDTO getById(Long id) {
        Application app = applicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        return toDTO(app);
    }
    @Override
    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new RuntimeException("Application not found");
        }applicationRepository.deleteById(id);
    }

    private ApplicationResponseDTO toDTO(Application app) {

        ServerSummaryDTO serverDTO = null;

        if (app.getServer() != null) {
            serverDTO = new ServerSummaryDTO(app.getServer().getId(), app.getServer().getName(),
                    app.getServer().getType().name(),
                    app.getServer().getStatus().name() );
        }
        return new ApplicationResponseDTO(app.getId(), app.getName(), app.getCurrentVersion(),
                app.getType().name(), app.getContextPath(), app.getStatus().name(),
                app.getLastDeployedAt(),
                serverDTO
        );
    }
}
