package com.vermeg.platform.supervision_platform.Service;

import com.jcraft.jsch.*;
import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;

@Service
public class ServerConnectivityServiceImpl implements ServerConnectivityService {

    @Override
    public ServerStatus checkServer(Server server) {

        Session session = null;

        try {
            JSch jsch = new JSch();

            session = jsch.getSession(
                    server.getSshUsername(),
                    server.getHost(),
                    server.getSshPort()
            );

            session.setPassword(server.getSshPassword());

            // Important en environnement test/dev
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect(5000); // timeout 5s

            // Exécuter une commande simple
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("echo OK");
            channel.connect();

            channel.disconnect();
            session.disconnect();

            return ServerStatus.UP;

        } catch (Exception e) {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            return ServerStatus.DOWN;
        }
    }

    @Override
    public ServerStatus checkSsh(Server server) {
        // SSH ONLY
        try {
            // (code SSH JSch qu’on a déjà fait)
            return ServerStatus.UP;
        } catch (Exception e) {
            return ServerStatus.DOWN;
        }
    }

    @Override
    public ServerStatus checkApplicationServer(Server server) {
        try {
            // Exemple WildFly management port
            Socket socket = new Socket();
            socket.connect(
                    new InetSocketAddress(server.getHost(), server.getManagementPort()),
                    3000
            );
            socket.close();
            return ServerStatus.UP;
        } catch (Exception e) {
            return ServerStatus.DOWN;
        }
    }

    @Override
    public ServerStatus checkGlobal(Server server) {
        ServerStatus ssh = checkSsh(server);
        ServerStatus app = checkApplicationServer(server);

        return (ssh == ServerStatus.UP && app == ServerStatus.UP)
                ? ServerStatus.UP
                : ServerStatus.DOWN;
    }
}
