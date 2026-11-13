package com.auction.common.model;

import java.io.Serializable;

/**
 * Đại diện cho 5 trạng thái trong vòng đời của một phiên đấu giá.
 * Phụ trách bởi TV1. Nằm trong common.model để cả Server và Client dùng chung.
 */
public enum AuctionStatus implements Serializable {
    OPEN,       // Phiên vừa tạo, chưa đến giờ mở, chưa cho đặt giá
    RUNNING,    // Đang diễn ra, cho phép đặt giá hợp lệ
    FINISHED,   // Đã hết giờ, đã chốt người thắng (hoặc không ai thắng), đóng nhận giá
    PAID,       // Người thắng đã thanh toán thành công
    CANCELED    // Bị hủy bởi Seller (khi OPEN) hoặc Admin
}