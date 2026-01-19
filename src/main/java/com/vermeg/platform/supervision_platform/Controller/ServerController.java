package com.vermeg.platform.supervision_platform.Controller;


import com.vermeg.platform.supervision_platform.DTO.ServerRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Service.ServerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @PostMapping
    public ServerResponseDTO create(@RequestBody ServerRequestDTO dto) {
        return serverService.create(dto);
    }

    @GetMapping
    public List<ServerResponseDTO> getAll() {
        return serverService.getAll();
    }

    @GetMapping("/{id}")
    public ServerResponseDTO getById(@PathVariable Long id) {
        return serverService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        serverService.delete(id);
    }
}
