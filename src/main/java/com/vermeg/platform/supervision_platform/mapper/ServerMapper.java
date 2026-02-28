package com.vermeg.platform.supervision_platform.mapper;

import com.vermeg.platform.supervision_platform.DTO.ServerRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerSummaryDTO;
import com.vermeg.platform.supervision_platform.Entity.Environment;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerType;
import org.springframework.stereotype.Component;

@Component
public class ServerMapper {

    public Server toEntity(ServerRequestDTO dto) {
        return Server.builder()
                .name(dto.getName())
                .host(dto.getHost())
                .sshUsername(dto.getSshUsername())
                .sshPassword(dto.getSshPassword())
                .sshPort(dto.getSshPort())
                .port(dto.getPort())
                .managementPort(dto.getManagementPort())
                .managementUsername(dto.getManagementUsername())
                .managementPassword(dto.getManagementPassword())
                .serverHomePath(dto.getServerHomePath())
                .type(ServerType.valueOf(dto.getType()))
                .version(dto.getVersion())
                .environment(Environment.valueOf(dto.getEnvironment()))
                .build();
    }

    public ServerResponseDTO toResponseDTO(Server server) {
        ServerResponseDTO dto = new ServerResponseDTO();
        dto.setId(server.getId());
        dto.setName(server.getName());
        dto.setHost(server.getHost());
        dto.setSshUsername(server.getSshUsername());
        dto.setManagementUsername(server.getManagementUsername());
        dto.setSshPort(server.getSshPort());
        dto.setPort(server.getPort());
        dto.setManagementPort(server.getManagementPort());
        dto.setServerHomePath(server.getServerHomePath());
        dto.setType(server.getType().name());
        dto.setVersion(server.getVersion());
        dto.setEnvironment(server.getEnvironment().name());
        dto.setStatus(server.getStatus().name());
        dto.setCreatedAt(server.getCreatedAt());
        dto.setLastCheckedAt(server.getLastCheckedAt());
        return dto;
    }

    public ServerSummaryDTO toSummaryDTO(Server server) {
        return new ServerSummaryDTO(
                server.getId(),
                server.getName(),
                server.getType().name(),
                server.getStatus().name()
        );
    }

    public void updateEntityFromDTO(Server server, ServerRequestDTO dto) {
        server.setName(dto.getName());
        server.setHost(dto.getHost());
        server.setSshUsername(dto.getSshUsername());
        server.setSshPassword(dto.getSshPassword());
        server.setSshPort(dto.getSshPort());
        server.setPort(dto.getPort());
        server.setManagementPort(dto.getManagementPort());
        server.setManagementUsername(dto.getManagementUsername());
        server.setManagementPassword(dto.getManagementPassword());
        server.setServerHomePath(dto.getServerHomePath());
        server.setType(ServerType.valueOf(dto.getType()));
        server.setVersion(dto.getVersion());
        server.setEnvironment(Environment.valueOf(dto.getEnvironment()));
    }
}