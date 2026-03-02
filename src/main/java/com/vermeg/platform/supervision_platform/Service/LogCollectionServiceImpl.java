package com.vermeg.platform.supervision_platform.Service;

import com.jcraft.jsch.*;
import com.vermeg.platform.supervision_platform.DTO.AppLogDTO;
import com.vermeg.platform.supervision_platform.Entity.*;
import com.vermeg.platform.supervision_platform.Repository.AppLogRepository;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import com.vermeg.platform.supervision_platform.exception.ServerNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import specification.AppLogSpecification;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class LogCollectionServiceImpl implements LogCollectionService {

    private static final int SSH_TIMEOUT = 10000;
    private static final int MAX_LINES = 500;

    private final AppLogRepository appLogRepository;
    private final ServerRepository serverRepository;

    public LogCollectionServiceImpl(AppLogRepository appLogRepository,
                                    ServerRepository serverRepository) {
        this.appLogRepository = appLogRepository;
        this.serverRepository = serverRepository;
    }

    /* =========================
       COLLECT LOGS FROM REMOTE SERVER
       ========================= */
    @Override
    public List<AppLogDTO> collectLogs(Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ServerNotFoundException(serverId));

        String logFilePath = resolveLogFilePath(server);
        List<String> lines = readRemoteFile(server, logFilePath);
        List<AppLog> logs = parseLogLines(lines, server);

        appLogRepository.saveAll(logs);

        return logs.stream().map(this::toDTO).toList();
    }

    /* =========================
       SEARCH LOGS WITH FILTERS
       ========================= */
    @Override
    public List<AppLogDTO> searchLogs(Long serverId, LogLevel level,
                                      LocalDateTime from, LocalDateTime to,
                                      String keyword) {
        return appLogRepository
                .findAll(AppLogSpecification.filter(serverId, level, from, to, keyword))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /* =========================
       GET LATEST LOGS
       ========================= */
    @Override
    public List<AppLogDTO> getLatestLogs(Long serverId) {
        return appLogRepository
                .findByServerIdOrderByTimestampDesc(serverId)
                .stream()
                .limit(100)
                .map(this::toDTO)
                .toList();
    }

    /* =========================
       RESOLVE LOG FILE PATH
       Based on server type
       ========================= */
    private String resolveLogFilePath(Server server) {
        return switch (server.getType()) {
            case WILDFLY, JBOSS -> server.getServerHomePath()
                    + "/standalone/log/server.log";
            case WEBSPHERE -> server.getServerHomePath()
                    + "/profiles/AppSrv01/logs/server1/SystemOut.log";
            case TOMCAT -> server.getServerHomePath()
                    + "/logs/catalina.out";
            case GLASSFISH -> server.getServerHomePath()
                    + "/domains/domain1/logs/server.log";
        };
    }

    /* =========================
       PARSE LOG LINES
       Routes to correct parser based on server type
       ========================= */
    private List<AppLog> parseLogLines(List<String> lines, Server server) {
        return switch (server.getType()) {
            case WILDFLY, JBOSS -> parseWildFlyLogs(lines, server);
            case WEBSPHERE -> parseWebSphereLogs(lines, server);
            case TOMCAT -> parseTomcatLogs(lines, server);
            case GLASSFISH -> parseWildFlyLogs(lines, server);
        };
    }

    /* =========================
       WILDFLY / JBOSS LOG PARSER
       Format: 2026-02-28 13:33:09,645 INFO  [category] (thread) message
       ========================= */
    private List<AppLog> parseWildFlyLogs(List<String> lines, Server server) {
        List<AppLog> logs = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})\\s+(\\w+)\\s+\\[([^\\]]+)\\]\\s+\\(([^)]+)\\)\\s+(.+)$"
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

        for (String line : lines) {
            try {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    logs.add(AppLog.builder()
                            .level(parseLogLevel(matcher.group(2)))
                            .message(matcher.group(5))
                            .category(matcher.group(3))
                            .threadName(matcher.group(4))
                            .timestamp(LocalDateTime.parse(matcher.group(1), formatter))
                            .server(server)
                            .build());
                }
            } catch (DateTimeParseException e) {
                // Skip malformed lines
            }
        }
        return logs;
    }

    /* =========================
       WEBSPHERE LOG PARSER
       Format: [28/02/26 13:33:09:645 CET] 00000001 SystemOut O message
       ========================= */
    private List<AppLog> parseWebSphereLogs(List<String> lines, Server server) {
        List<AppLog> logs = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "^\\[(\\d{2}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3})\\s+\\w+\\]\\s+\\w+\\s+(\\w+)\\s+\\w+\\s+(.+)$"
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss:SSS");

        for (String line : lines) {
            try {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    logs.add(AppLog.builder()
                            .level(parseLogLevel(matcher.group(2)))
                            .message(matcher.group(3))
                            .category("WebSphere")
                            .threadName("unknown")
                            .timestamp(LocalDateTime.parse(matcher.group(1), formatter))
                            .server(server)
                            .build());
                }
            } catch (DateTimeParseException e) {
                // Skip malformed lines
            }
        }
        return logs;
    }

    /* =========================
       TOMCAT LOG PARSER
       Format: 28-Feb-2026 13:33:09.645 INFO [main] message
       ========================= */
    private List<AppLog> parseTomcatLogs(List<String> lines, Server server) {
        List<AppLog> logs = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "^(\\d{2}-\\w{3}-\\d{4} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+(\\w+)\\s+\\[([^\\]]+)\\]\\s+(.+)$"
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss.SSS");

        for (String line : lines) {
            try {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    logs.add(AppLog.builder()
                            .level(parseLogLevel(matcher.group(2)))
                            .message(matcher.group(4))
                            .category(matcher.group(3))
                            .threadName("unknown")
                            .timestamp(LocalDateTime.parse(matcher.group(1), formatter))
                            .server(server)
                            .build());
                }
            } catch (DateTimeParseException e) {
                // Skip malformed lines
            }
        }
        return logs;
    }

    /* =========================
       PARSE LOG LEVEL
       ========================= */
    private LogLevel parseLogLevel(String levelStr) {
        return switch (levelStr.toUpperCase()) {
            case "ERROR", "FATAL" -> LogLevel.ERROR;
            case "WARN", "WARNING" -> LogLevel.WARN;
            default -> LogLevel.INFO;
        };
    }

    /* =========================
       READ REMOTE FILE VIA SSH
       ========================= */
    private List<String> readRemoteFile(Server server, String filePath) {
        Session session = null;
        ChannelExec channel = null;
        List<String> lines = new ArrayList<>();

        try {
            session = createSession(server);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("tail -n " + MAX_LINES + " " + filePath);

            InputStream inputStream = channel.getInputStream();
            channel.connect(SSH_TIMEOUT);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read remote log file: "
                    + e.getMessage(), e);
        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }

        return lines;
    }

    /* =========================
       SSH SESSION FACTORY
       ========================= */
    private Session createSession(Server server) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(
                server.getSshUsername(),
                server.getHost(),
                server.getSshPort()
        );
        session.setPassword(server.getSshPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(SSH_TIMEOUT);
        return session;
    }

    /* =========================
       MAPPER
       ========================= */
    private AppLogDTO toDTO(AppLog log) {
        return AppLogDTO.builder()
                .id(log.getId())
                .level(log.getLevel().name())
                .message(log.getMessage())
                .category(log.getCategory())
                .threadName(log.getThreadName())
                .timestamp(log.getTimestamp())
                .collectedAt(log.getCollectedAt())
                .serverId(log.getServer().getId())
                .serverName(log.getServer().getName())
                .build();
    }
}