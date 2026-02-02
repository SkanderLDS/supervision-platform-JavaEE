package com.vermeg.platform.supervision_platform.Service;


import com.vermeg.platform.supervision_platform.DTO.ServerRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerSummaryDTO;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ServerService {

    /* =========================
       CRUD
       ========================= */
    ServerResponseDTO create(ServerRequestDTO dto);

    ServerResponseDTO update(Long id, ServerRequestDTO dto);

    void delete(Long id);

    List<ServerResponseDTO> getAll();

    ServerResponseDTO getById(Long id);

    ServerSummaryDTO getSummaryById(Long id);

    /* =========================
       CONNECTIVITY (A.1)
       ========================= */

    ServerStatus checkSshConnectivity(Long serverId);

    ServerStatus checkApplicationServerConnectivity(Long serverId);

    ServerStatus checkGlobalConnectivity(Long serverId);
}


