package com.pipes.util;

/**
 * Generic API response wrapper (R2 — Generics).
 *
 * <T> is the type of the payload. Using a type-bound could make sense here
 * (e.g. {@code <T extends Serializable>}), but a simple open generic keeps
 * the wrapper maximally reusable across DTOs, lists, and primitive values.
 *
 * @param <T> type of the wrapped data
 */
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // ── Static factory methods ────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean isSuccess()  { return success; }
    public String getMessage()  { return message; }
    public T getData()          { return data; }
}
