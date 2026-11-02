package vn.auction.common.dto;

import java.io.Serializable;
import java.util.UUID;
import vn.auction.common.protocol.Command;

/**
 * Gói tin yêu cầu từ Client gửi lên Server.
 */
public record Request(
    UUID requestId,
    Command command,
    Serializable payload
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

