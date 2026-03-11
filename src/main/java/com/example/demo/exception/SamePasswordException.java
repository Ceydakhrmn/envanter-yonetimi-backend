package com.example.demo.exception;

/**
 * Thrown when user tries to set the same password as the current one.
 */
public class SamePasswordException extends RuntimeException {
    public SamePasswordException(String message) {
        super(message);
    }
}
