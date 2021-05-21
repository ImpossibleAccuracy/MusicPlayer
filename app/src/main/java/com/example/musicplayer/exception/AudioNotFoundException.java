package com.example.musicplayer.exception;

public class AudioNotFoundException extends RuntimeException {
    public AudioNotFoundException() {
    }
    public AudioNotFoundException(String message) {
        super(message);
    }
    public AudioNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public AudioNotFoundException(Throwable cause) {
        super(cause);
    }
}
