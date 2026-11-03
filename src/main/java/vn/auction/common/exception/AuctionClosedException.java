package vn.auction.common.exception;

import vn.auction.common.protocol.ErrorCode;

/**
 * Ném ra khi người dùng thao tác trên phiên đấu giá đã kết thúc hoặc bị hủy.
 */
public class AuctionClosedException extends AuctionException {
    public AuctionClosedException(String message) {
        super(ErrorCode.AUCTION_CLOSED, message);
    }
}

