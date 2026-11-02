package vn.auction.common.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Bản sao an toàn và bất biến trạng thái phiên đấu giá để truyền qua mạng.
 */
public record AuctionSnapshot(
    UUID auctionId,
    String status,
    long price,
    UUID winnerId,
    Instant endAt,
    Instant serverNow,
    long remainingSeconds,
    long version
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

