package com.vermeg.platform.supervision_platform.Service;
import com.vermeg.platform.supervision_platform.DTO.AppLogDTO;
import com.vermeg.platform.supervision_platform.Entity.LogLevel;
import java.time.LocalDateTime;
import java.util.List;

public interface LogCollectionService {

    List<AppLogDTO> collectLogs(Long serverId);

    List<AppLogDTO> searchLogs(Long serverId, LogLevel level, LocalDateTime from, LocalDateTime to,
            String keyword
    );
    List<AppLogDTO> getLatestLogs(Long serverId);
}
