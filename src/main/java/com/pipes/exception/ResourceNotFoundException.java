package com.pipes.exception;

/**
 * Thrown when a requested resource is not found.
 * Unchecked — callers decide whether to handle or propagate.
 * Satisfies R10: custom exception class.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s with id '%s' was not found.", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() { return resourceType; }
    public Object getResourceId()   { return resourceId; }
}
