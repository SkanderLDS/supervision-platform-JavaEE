package com.vermeg.platform.supervision_platform.Controller;
import com.vermeg.platform.supervision_platform.DTO.AppLogDTO;
import com.vermeg.platform.supervision_platform.Entity.LogLevel;
import com.vermeg.platform.supervision_platform.Service.LogCollectionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogCollectionService logCollectionService;

    public LogController(LogCollectionService logCollectionService) {
        this.logCollectionService = logCollectionService;
    }
    @PostMapping("/servers/{serverId}/collect")
    public ResponseEntity<List<AppLogDTO>> collectLogs(
            @PathVariable Long serverId) {
        return ResponseEntity.ok(logCollectionService.collectLogs(serverId));
    }
    @GetMapping("/servers/{serverId}/latest")
    public ResponseEntity<List<AppLogDTO>> getLatestLogs(
            @PathVariable Long serverId) {
        return ResponseEntity.ok(logCollectionService.getLatestLogs(serverId));
    }
    /* SEARCH LOGS WITH FILTERS(Multi-criteria: level, date range, keyword)*/
    @GetMapping("/servers/{serverId}/search")
    public ResponseEntity<List<AppLogDTO>> searchLogs(
            @PathVariable Long serverId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String keyword) {

        LogLevel logLevel = null;
        if (level != null && !level.isBlank()) {
            try {
                logLevel = LogLevel.valueOf(level.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid level — ignore and search without level filter
            }
        }
        return ResponseEntity.ok(logCollectionService.searchLogs(
                serverId, logLevel, from, to, keyword));
    }
}
