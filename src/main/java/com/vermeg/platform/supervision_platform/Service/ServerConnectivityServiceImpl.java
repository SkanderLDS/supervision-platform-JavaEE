package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;
import com.vermeg.platform.supervision_platform.Repository.ServerRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

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
            InetAddress address = InetAddress.getByName(server.getHost());
            return address.isReachable(2000); // 2 seconds timeout
        } catch (Exception e) {
            return false;
        }
    }
}

