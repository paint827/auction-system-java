package vn.auction.common.protocol;

/**
 * Loại sự kiện phát thanh (broadcast) thời gian thực trong phòng đấu giá.
 */
public enum EventType {
    BID_PLACED,
    TIMER_TICK,
    AUCTION_EXTENDED,
    AUCTION_FINISHED,
    AUCTION_CANCELED,
    STATE_CHANGED
}

