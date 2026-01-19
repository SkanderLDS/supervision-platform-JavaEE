package com.vermeg.platform.supervision_platform.Controller;


import com.vermeg.platform.supervision_platform.DTO.ServerRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import com.vermeg.platform.supervision_platform.Service.ServerConnectivityService;
import com.vermeg.platform.supervision_platform.Service.ServerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;
    private final ServerConnectivityService connectivityService ;

    public ServerController(ServerService serverService, ServerConnectivityService connectivityService) {
        this.serverService = serverService;
        this.connectivityService = connectivityService;
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

    @PostMapping("/{id}/check")
    public ResponseEntity<String> checkConnectivity(@PathVariable Long id) {
        ServerStatus status = connectivityService.checkConnectivity(id);
        return ResponseEntity.ok("Server status: " + status);
    }
}
