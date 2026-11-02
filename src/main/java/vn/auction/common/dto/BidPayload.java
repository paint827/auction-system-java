package vn.auction.common.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Payload đặt giá đấu. Không chứa bidderId (Server tự gán theo session).
 */
public record BidPayload(
    UUID auctionId,
    long amount
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

