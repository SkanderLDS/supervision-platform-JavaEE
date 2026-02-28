package com.vermeg.platform.supervision_platform.Controller;

import com.vermeg.platform.supervision_platform.DTO.ServerRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.ServerSummaryDTO;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import com.vermeg.platform.supervision_platform.Service.ServerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @PostMapping
    public ResponseEntity<ServerResponseDTO> create(@Valid @RequestBody ServerRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serverService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<ServerResponseDTO>> getAll() {
        return ResponseEntity.ok(serverService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serverService.getById(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ServerSummaryDTO> getSummary(@PathVariable Long id) {
        return ResponseEntity.ok(serverService.getSummaryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServerResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ServerRequestDTO dto) {
        return ResponseEntity.ok(serverService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serverService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/check/ssh")
    public ResponseEntity<Map<String, String>> checkSsh(@PathVariable Long id) {
        ServerStatus status = serverService.checkSshConnectivity(id);
        return ResponseEntity.ok(Map.of("status", status.name()));
    }

    @PostMapping("/{id}/check/app")
    public ResponseEntity<Map<String, String>> checkApp(@PathVariable Long id) {
        ServerStatus status = serverService.checkApplicationServerConnectivity(id);
        return ResponseEntity.ok(Map.of("status", status.name()));
    }

    @PostMapping("/{id}/check/global")
    public ResponseEntity<Map<String, String>> checkGlobal(@PathVariable Long id) {
        ServerStatus status = serverService.checkGlobalConnectivity(id);
        return ResponseEntity.ok(Map.of("status", status.name()));
    }
}