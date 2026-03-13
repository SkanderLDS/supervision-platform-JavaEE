package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.ApplicationRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ApplicationResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.Application;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ApplicationService {
     ApplicationResponseDTO create(ApplicationRequestDTO dto);
     ApplicationResponseDTO update(Long id, ApplicationRequestDTO dto);
     void delete(Long id);
     ApplicationResponseDTO getById(Long id);
     List<ApplicationResponseDTO> getAll();
     List<ApplicationResponseDTO> getByServerId(Long serverId);
}

