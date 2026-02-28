package com.vermeg.platform.supervision_platform.exception;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(Long id) {
        super("Application not found with id: " + id);
    }
}
