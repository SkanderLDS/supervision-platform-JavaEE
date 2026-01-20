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

    private final ServerRepository serverRepository;

    public ServerConnectivityServiceImpl(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    private boolean isReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(host, port),
                    3000 // timeout 3s
            );
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean checkConnectivity(Server server) {
        try {
            InetAddress address =
                    InetAddress.getByName(server.getHost());
            return address.isReachable(3000);
        } catch (Exception e) {
            return false;
        }
    }

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
                    "http://" + server.getHost() + ":" + server.getPort() + "/management";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            // 401 = management endpoint alive (auth required)
            if (responseCode == 200 || responseCode == 401) {
                return ServerStatus.UP;
            }

            return ServerStatus.DOWN;

        } catch (Exception e) {
            return ServerStatus.DOWN;
        }
    }
    private ServerStatus checkWebSphere(Server server) {
        // WebSphere usually uses SOAP/JMX
        // For graduation: we validate TCP reachability on admin port
        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(server.getHost(), Integer.parseInt(server.getPort())),
                    3000
            );
            return ServerStatus.UP;
        } catch (Exception e) {
            return ServerStatus.DOWN;
        }
    }
}

