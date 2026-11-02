package vn.auction.common.dto;

import java.io.Serializable;
import java.util.UUID;
import vn.auction.common.protocol.EventType;

/**
 * Sự kiện thời gian thực gửi tới các Client trong phòng đấu giá.
 */
public record RoomEvent(
    EventType type,
    UUID auctionId,
    long version,
    AuctionSnapshot snapshot
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

