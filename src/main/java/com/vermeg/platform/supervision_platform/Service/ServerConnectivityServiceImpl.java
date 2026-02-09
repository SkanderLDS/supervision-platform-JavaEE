package com.vermeg.platform.supervision_platform.Service;

import com.jcraft.jsch.*;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;

@Service
public class ServerConnectivityServiceImpl implements ServerConnectivityService {

    /* =========================
       SSH CONNECTIVITY
       ========================= */
    @Override
    public ServerStatus checkSsh(Server server) {

        Session session = null;

        try {
            JSch jsch = new JSch();

            session = jsch.getSession(
                    server.getSshUsername(),
                    server.getHost(),
                    server.getSshPort()
            );

            session.setPassword(server.getSshPassword());
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect(5000); // 5 seconds timeout

            session.disconnect();

            return ServerStatus.UP;

        } catch (Exception e) {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            return ServerStatus.DOWN;
        }
    }

    /* =========================
       APPLICATION SERVER CONNECTIVITY
       ========================= */
    @Override
    public ServerStatus checkApplicationServer(Server server) {

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(server.getHost(), server.getManagementPort()),
                    3000
            );

            return ServerStatus.UP;

        } catch (Exception e) {
            return ServerStatus.DOWN;
        }
    }

    /* =========================
       GLOBAL CHECK
       ========================= */
    @Override
    public ServerStatus checkGlobal(Server server) {

        ServerStatus sshStatus = checkSsh(server);
        ServerStatus appStatus = checkApplicationServer(server);

        if (sshStatus == ServerStatus.UP && appStatus == ServerStatus.UP) {
            return ServerStatus.UP;
        }

        return ServerStatus.DOWN;
    }
}
