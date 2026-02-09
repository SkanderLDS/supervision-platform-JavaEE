package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;
import com.vermeg.platform.supervision_platform.Entity.ServerStatus;

public interface ServerConnectivityService {

    ServerStatus checkSsh(Server server);

    ServerStatus checkApplicationServer(Server server);

    ServerStatus checkGlobal(Server server);
}
