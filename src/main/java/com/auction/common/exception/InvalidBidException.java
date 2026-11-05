package com.auction.common.exception;

import com.auction.common.protocol.ErrorCode;

/**
 * Ném ra khi giá đấu không hợp lệ hoặc thấp hơn quy định.
 */
public class InvalidBidException extends AuctionException {
    public InvalidBidException(String message) {
        super(ErrorCode.INVALID_BID, message);
    }
}

