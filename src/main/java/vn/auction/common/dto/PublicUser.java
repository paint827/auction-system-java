package vn.auction.common.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Thông tin người dùng công khai gửi qua mạng, tuyệt đối không có hash/salt.
 */
public record PublicUser(
    UUID id,
    String username,
    String role,
    boolean active
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

