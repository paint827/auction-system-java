package com.auction.common.exception;

/**
 * Lớp ngoại lệ cơ sở cho toàn bộ hệ thống đấu giá.
 * Phụ trách bởi TV1.
 */
public class AuctionException extends Exception {
    private static final long serialVersionUID = 1L;

    public AuctionException(String message) {
        super(message);
    }

    public AuctionException(String message, Throwable cause) {
        super(message, cause);
    }
}
