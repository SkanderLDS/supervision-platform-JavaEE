package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.ApplicationRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ApplicationResponseDTO;
import com.vermeg.platform.supervision_platform.Service.ApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }


    @PostMapping
    public ApplicationResponseDTO create(
            @RequestBody ApplicationRequestDTO dto
    ) {
        return applicationService.create(dto);
    }


    @GetMapping
    public List<ApplicationResponseDTO> getAll() {
        return applicationService.getAll();
    }


    @GetMapping("/{id}")
    public ApplicationResponseDTO getById(
            @PathVariable Long id
    ) {
        return applicationService.getById(id);
    }
}
