package vn.auction.common.protocol;

import vn.auction.common.dto.AuctionSnapshot;

/**
 * Observer nhận thông báo khi trạng thái hoặc giá của phiên đấu giá thay đổi.
 */
@FunctionalInterface
public interface AuctionObserver {
    void onChanged(AuctionSnapshot snapshot);
}

