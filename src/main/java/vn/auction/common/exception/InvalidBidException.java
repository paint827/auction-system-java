package vn.auction.common.exception;

import vn.auction.common.protocol.ErrorCode;

/**
 * Ném ra khi giá đấu không hợp lệ hoặc thấp hơn quy định.
 */
public class InvalidBidException extends AuctionException {
    public InvalidBidException(String message) {
        super(ErrorCode.INVALID_BID, message);
    }
}

