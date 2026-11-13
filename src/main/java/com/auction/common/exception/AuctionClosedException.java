package com.auction.common.exception;

/**
 * Ném ra khi người dùng cố gắng đặt giá vào một phiên đấu giá
 * không ở trạng thái RUNNING (ví dụ: OPEN, FINISHED, PAID, CANCELED).
 * Phụ trách bởi TV1.
 */
public class AuctionClosedException extends AuctionException {
    private static final long serialVersionUID = 1L;

    public AuctionClosedException(String message) {
        super(message);
    }
}
