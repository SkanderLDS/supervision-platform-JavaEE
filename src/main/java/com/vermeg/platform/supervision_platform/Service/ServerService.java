package com.vermeg.platform.supervision_platform.Service;


import com.vermeg.platform.supervision_platform.DTO.ServerRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerSummaryDTO;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ServerService {
    ServerResponseDTO create(ServerRequestDTO dto);

    List<ServerResponseDTO> getAll();

    ServerResponseDTO getById(Long id);

    ServerSummaryDTO getSummaryById(Long id);

    void delete(Long id);

    ServerStatus checkConnectivity(Long serverId);
}
