package com.jetmessage.model;

public class JetException extends RuntimeException {
    private static final long serialVersionUID = 8674679614878026923L;

    public JetException(String message) {
        super(message);
    }

    public JetException(String message, Throwable cause) {
        super(String.format("%s <- %s", message, cause.toString()), cause);
    }
}
