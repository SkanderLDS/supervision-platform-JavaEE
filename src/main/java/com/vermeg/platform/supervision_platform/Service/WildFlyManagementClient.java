package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.Entity.Server;

import java.io.File;


public interface WildFlyManagementClient {

    void deploy(Server server, File warFile, String runtimeName);

    void undeploy(Server server, String runtimeName);

    void start(Server server, String runtimeName);

    void stop(Server server, String runtimeName);

}
