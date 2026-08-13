package com.aidocumentreader.backend.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("An account already exists with this email address.");
    }
}