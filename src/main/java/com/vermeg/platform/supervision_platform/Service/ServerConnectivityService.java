package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.ServerStatus;

public interface ServerConnectivityService {
    ServerStatus checkConnectivity(Long ServerId);
}
