package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.ServerStatus;

public interface SupervisionService {
    void superviseServer(Long serverId);
    ServerStatus checkServer(Long serverId);
}
