package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.ApplicationRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ApplicationResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.Application;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ApplicationService {

     ApplicationResponseDTO create(ApplicationRequestDTO dto);
     List<ApplicationResponseDTO> getAll();
     ApplicationResponseDTO getById(Long id);
     void deleteApplication(Long id);
}

