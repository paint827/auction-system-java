package com.auction.client.model;

import java.io.Serializable;
import java.util.UUID;

public class AuctionItemDisplay implements Serializable {
    private final UUID auctionId;
    private final String code;          // Mã phiên cố định: A001, A002...
    private final String title;
    private final String category;      // ELECTRONICS, ART, VEHICLE
    private final String sellerName;
    private final long startingPrice;
    private long currentPrice;          // Cập nhật khi có lượt đặt giá mới
    private final long stepPrice;
    private String status;              // OPEN, RUNNING, FINISHED
    private long remainingSeconds;      // Giảm dần theo thời gian thực
    private final String endTimeString; // Ví dụ: "14/11/2026 21:30"
    private int bidCount;               // Tăng dần theo số lượt đặt giá
    private String leadingBidder;       // Cập nhật người dẫn đầu mới
    private final String winnerName;    // Người thắng cuộc
    private final String description;
    private final String specificInfo;  // Thuộc tính đặc thù của TV4

    public AuctionItemDisplay(UUID auctionId, String code, String title, String category, String sellerName,
                              long startingPrice, long currentPrice, long stepPrice,
                              String status, long remainingSeconds, String endTimeString,
                              int bidCount, String leadingBidder, String winnerName,
                              String description, String specificInfo) {
        this.auctionId = auctionId;
        this.code = code;
        this.title = title;
        this.category = category;
        this.sellerName = sellerName;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.stepPrice = stepPrice;
        this.status = status;
        this.remainingSeconds = remainingSeconds;
        this.endTimeString = endTimeString;
        this.bidCount = bidCount;
        this.leadingBidder = leadingBidder;
        this.winnerName = winnerName;
        this.description = description;
        this.specificInfo = specificInfo;
    }

    public UUID getAuctionId() { return auctionId; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getSellerName() { return sellerName; }
    public long getStartingPrice() { return startingPrice; }

    public long getCurrentPrice() { return currentPrice; }
    public long getStepPrice() { return stepPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getRemainingSeconds() { return remainingSeconds; }
    public void decrementRemainingSeconds() {
        if (this.remainingSeconds > 0) {
            this.remainingSeconds--;
        }
    }
    public void addRemainingSeconds(long seconds) {
        this.remainingSeconds += seconds;
    }

    public int getBidCount() { return bidCount; }
    public String getLeadingBidder() { return leadingBidder; }

    /**
     * Cập nhật phiên khi có người đặt giá thành công
     */
    public void recordNewBid(long newPrice, String bidder) {
        this.currentPrice = newPrice;
        this.leadingBidder = bidder;
        this.bidCount++;
    }

    public String getEndTimeString() { return endTimeString; }
    public String getWinnerName() { return winnerName; }
    public String getDescription() { return description; }
    public String getSpecificInfo() { return specificInfo; }
}
