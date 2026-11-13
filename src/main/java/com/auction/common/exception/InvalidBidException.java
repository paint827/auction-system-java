package com.auction.common.exception;

/**
 * Ném ra khi lượt đặt giá (Bid) không hợp lệ:
 * - Giá đặt nhỏ hơn hoặc bằng giá hiện tại
 * - Bước giá không đủ theo quy định (stepPrice)
 * - Người bán tự đặt giá cho sản phẩm của chính mình
 * Phụ trách bởi TV1.
 */
public class InvalidBidException extends AuctionException {
    private static final long serialVersionUID = 1L;

    public InvalidBidException(String message) {
        super(message);
    }
}
