package com.pipes.exception;

/**
 * Thrown when a user tries to access a resource they do not own.
 * Satisfies R10: second custom exception, unchecked.
 */
public class PipesAccessDeniedException extends RuntimeException {

    public PipesAccessDeniedException(String message) {
        super(message);
    }
}
