package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.ServerRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import com.vermeg.platform.supervision_platform.Service.ServerService;
import org.springframework.http.ResponseEntity;
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
    @PutMapping("/{id}")
    public ResponseEntity<ServerResponseDTO> update(
            @PathVariable Long id,
            @RequestBody ServerRequestDTO dto
    ) {
        return ResponseEntity.ok(serverService.update(id, dto));
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        serverService.delete(id);
    }
    @PostMapping("/{id}/check/ssh")
    public ResponseEntity<String> checkSsh(@PathVariable Long id) {
        ServerStatus status = serverService.checkSshConnectivity(id);
        return ResponseEntity.ok("SSH Status: " + status);
    }
    @PostMapping("/{id}/check/app")
    public ResponseEntity<String> checkApp(@PathVariable Long id) {
        ServerStatus status = serverService.checkApplicationServerConnectivity(id);
        return ResponseEntity.ok("Application Server Status: " + status);
    }
    @PostMapping("/{id}/check/global")
    public ResponseEntity<String> checkGlobal(@PathVariable Long id) {
        ServerStatus status = serverService.checkGlobalConnectivity(id);
        return ResponseEntity.ok("Global Status: " + status);
    }
}
