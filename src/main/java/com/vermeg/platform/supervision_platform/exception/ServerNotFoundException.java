package com.vermeg.platform.supervision_platform.exception;

public class ServerNotFoundException extends RuntimeException {
    public ServerNotFoundException(Long id) {
        super("Server not found with id: " + id);
    }
}