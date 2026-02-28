package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.SupervisionResultDTO;

import java.util.List;

public interface SupervisionService {
    SupervisionResultDTO superviseServer(Long serverId);
    List<SupervisionResultDTO> superviseAllServers();
}