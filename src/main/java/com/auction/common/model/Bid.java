package com.auction.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Thực thể đại diện cho một lượt đặt giá (Bid) trong phiên đấu giá.
 * Phụ trách bởi TV1. Nằm trong common.model để cả Server và Client dùng chung.
 * Đảm bảo Serializable để TV2 có thể lưu trữ DAO và truyền nhận qua Socket.
 */
public class Bid implements Serializable, Comparable<Bid> {
    private static final long serialVersionUID = 1L;

    private final String bidId;
    private final String auctionId;
    private final String bidderId;
    private final double amount;
    private final LocalDateTime bidTime;

    public Bid(String bidId, String auctionId, String bidderId, double amount, LocalDateTime bidTime) {
        this.bidId = Objects.requireNonNull(bidId, "bidId không được null");
        this.auctionId = Objects.requireNonNull(auctionId, "auctionId không được null");
        this.bidderId = Objects.requireNonNull(bidderId, "bidderId không được null");
        this.amount = amount;
        this.bidTime = bidTime != null ? bidTime : LocalDateTime.now();
    }

    public String getBidId() {
        return bidId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    @Override
    public int compareTo(Bid other) {
        int priceCompare = Double.compare(this.amount, other.amount);
        if (priceCompare != 0) {
            return priceCompare;
        }
        return this.bidTime.compareTo(other.bidTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bid bid = (Bid) o;
        return Objects.equals(bidId, bid.bidId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bidId);
    }

    @Override
    public String toString() {
        return String.format("Bid{id='%s', bidder='%s', amount=%.2f, time=%s}",
                bidId, bidderId, amount, bidTime);
    }
}