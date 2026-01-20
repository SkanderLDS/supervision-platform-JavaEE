package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;

public interface ServerConnectivityService {
    boolean checkConnectivity(Server server);
    ServerStatus checkServer(Server server);
}
