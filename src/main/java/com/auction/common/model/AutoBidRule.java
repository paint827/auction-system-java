package com.auction.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Đại diện cho một quy tắc tự động đặt giá (Auto-bidding).
 * Khi có bidder khác đưa giá cao hơn, hệ thống tự động đặt giá thay bidder này
 * với mức giá = currentPrice + stepPrice cho đến khi chạm trần maxAmount.
 * 
 * Phụ trách bởi TV1. Nằm trong common.model để Client có thể gửi đăng ký rule lên Server.
 */
public class AutoBidRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String ruleId;
    private final String auctionId;
    private final String bidderId;
    private final double maxAmount;
    private final LocalDateTime createdAt;

    public AutoBidRule(String ruleId, String auctionId, String bidderId, double maxAmount) {
        this.ruleId = Objects.requireNonNull(ruleId, "ruleId không được null");
        this.auctionId = Objects.requireNonNull(auctionId, "auctionId không được null");
        this.bidderId = Objects.requireNonNull(bidderId, "bidderId không được null");
        if (maxAmount <= 0) {
            throw new IllegalArgumentException("Hạn mức tối đa (maxAmount) phải lớn hơn 0");
        }
        this.maxAmount = maxAmount;
        this.createdAt = LocalDateTime.now();
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("AutoBidRule{ruleId='%s', bidder='%s', max=%.2f}",
                ruleId, bidderId, maxAmount);
    }
}