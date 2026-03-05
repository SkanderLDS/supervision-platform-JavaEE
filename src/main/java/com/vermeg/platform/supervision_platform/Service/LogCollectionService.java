package com.vermeg.platform.supervision_platform.Service;
import com.vermeg.platform.supervision_platform.DTO.AppLogDTO;
import com.vermeg.platform.supervision_platform.Entity.LogLevel;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.List;

public interface LogCollectionService {
    // Collect logs from remote server via SSH
    List<AppLogDTO> collectLogs(Long serverId);
    // Get logs with filters and pagination
    Page<AppLogDTO> searchLogs(Long serverId, LogLevel level, LocalDateTime from, LocalDateTime to,
            String keyword, int page, int size
    );
    // Get latest logs for a server
    List<AppLogDTO> getLatestLogs(Long serverId);
}
