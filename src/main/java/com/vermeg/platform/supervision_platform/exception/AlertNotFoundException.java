package com.vermeg.platform.supervision_platform.exception;

public class AlertNotFoundException extends RuntimeException {
    public AlertNotFoundException(Long id) {
        super("Alert not found with id: " + id);
    }
}