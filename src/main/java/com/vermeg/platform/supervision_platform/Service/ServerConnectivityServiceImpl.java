package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.net.*;

@Service
@Transactional
public class ServerConnectivityServiceImpl implements ServerConnectivityService {

    @Override
    public ServerStatus checkServer(Server server) {

        try {
            switch (server.getType()) {

                case WILDFLY:
                case JBOSS:
                    return checkWildFly(server);

                case WEBSPHERE:
                    return checkWebSphere(server);

                default:
                    return ServerStatus.UNKNOWN;
            }
        } catch (Exception e) {
            return ServerStatus.DOWN;
        }
    }

    private ServerStatus checkWildFly(Server server) {
        try {
            String urlStr =
                    "http://" + server.getHost() + ":" + server.getPort() + "/";

            HttpURLConnection conn =
                    (HttpURLConnection) new URL(urlStr).openConnection();

            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            // 200 or 302 are both OK for WildFly
            if (responseCode == 200 || responseCode == 302) {
                return ServerStatus.UP;
            }

            return ServerStatus.DOWN;

        } catch (Exception e) {
            return ServerStatus.DOWN;
        }
    }

    private ServerStatus checkWebSphere(Server server) {
        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(server.getHost(), server.getPort()), 3000
            );
            return ServerStatus.UP;
        } catch (Exception e) {
            return ServerStatus.DOWN;
        }
    }
}


