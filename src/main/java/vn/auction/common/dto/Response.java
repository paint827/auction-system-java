package vn.auction.common.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Gói tin phản hồi từ Server về Client.
 */
public record Response(
    UUID requestId,
    boolean success,
    Serializable data,
    String errorCode,
    String message
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public static Response ok(UUID requestId, Serializable data) {
        return new Response(requestId, true, data, null, null);
    }

    public static Response error(UUID requestId, String errorCode, String message) {
        return new Response(requestId, false, null, errorCode, message);
    }
}

