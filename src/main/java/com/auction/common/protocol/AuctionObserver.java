package com.auction.common.protocol;

import com.auction.common.dto.AuctionSnapshot;

/**
 * Observer nhận thông báo khi trạng thái hoặc giá của phiên đấu giá thay đổi.
 */
@FunctionalInterface
public interface AuctionObserver {
    void onChanged(AuctionSnapshot snapshot);
}

