package com.auction.server.core.lock;

/**
 * @deprecated Đã được tích hợp trực tiếp ReentrantLock vào thực thể {@link com.auction.server.core.Auction}
 * theo đúng nguyên lý đóng gói (Encapsulation) và Slide 7 (Đa luồng - trang 44-46).
 */
@Deprecated
public final class AuctionLockManager {
    private AuctionLockManager() {}
}
