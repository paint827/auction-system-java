package com.auction.common.model;

import com.auction.common.exception.AuctionClosedException;
import com.auction.common.exception.InvalidBidException;
import com.auction.server.core.state.AuctionState;
import com.auction.server.core.state.FinishedState;
import com.auction.server.core.state.OpenState;
import com.auction.server.core.state.RunningState;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thực thể trung tâm đại diện cho một phiên đấu giá trực tuyến.
 * Thuộc com.auction.common.model theo đúng cây thư mục thống nhất của nhóm [TV1].
 * 
 * KIẾN THỨC BÁM SÁT GIÁO TRÌNH UET:
 * 1. Nguyên lý Đóng gói (Encapsulation - Slide 1-3 & Slide 6.2 SRP):
 *    - Toàn bộ dữ liệu phiên (giá, lượt đặt, người thắng, trạng thái) đều là private.
 * 2. Mẫu State Pattern (Áp dụng nguyên lý OCP - Slide 6.2 trang 21-25):
 *    - Thay vì dùng chuỗi dài if-else/switch (Code Smell: Conditional Complexity - Slide 6.1 trang 12),
 *      hệ thống ủy quyền hành vi kiểm tra và chuyển trạng thái cho đối tượng AuctionState.
 * 3. Khóa đồng thời ReentrantLock (Slide 7 trang 44-46):
 *    - Mỗi Auction sở hữu riêng một ReentrantLock(fairness = true).
 *    - Khóa theo từng đối tượng giúp nhiều phiên diễn ra song song mà không làm tắc nghẽn toàn hệ thống.
 * 
 * Phụ trách bởi TV1 (Core Engine & Concurrency).
 */
public class Auction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String sellerId;
    private final String itemId;
    private final String itemName;
    private final double startingPrice;
    private final double stepPrice;
    private final LocalDateTime startTime;

    // endTime có thể được gia hạn khi kích hoạt tính năng Anti-sniping
    private LocalDateTime endTime;

    // Biến lưu trữ giá hiện tại và các lượt đặt giá
    private double currentPrice;
    private Bid highestBid;
    private final List<Bid> bids;

    // Quản lý trạng thái theo State Pattern (Slide 6.2 OCP)
    private AuctionState state;
    private String winnerId;

    // Danh sách quy tắc Auto-bid của phiên này (Tuần 6 - 7)
    private final List<AutoBidRule> autoBidRules;

    // Khóa đồng thời bảo vệ phiên (Slide 7 trang 44-46)
    // Đánh dấu transient để không ghi ra file khi thực hiện Serialization (Slide 5.2)
    private transient ReentrantLock lock;

    public Auction(String auctionId, String sellerId, String itemId, String itemName,
                   double startingPrice, double stepPrice,
                   LocalDateTime startTime, LocalDateTime endTime) {
        this.auctionId = Objects.requireNonNull(auctionId, "auctionId không được null");
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId không được null");
        this.itemId = Objects.requireNonNull(itemId, "itemId không được null");
        this.itemName = itemName != null ? itemName : "Sản phẩm #" + itemId;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.stepPrice = stepPrice > 0 ? stepPrice : 1.0;
        this.startTime = startTime != null ? startTime : LocalDateTime.now();
        this.endTime = Objects.requireNonNull(endTime, "endTime không được null");
        this.bids = new ArrayList<>();
        this.autoBidRules = new ArrayList<>();
        this.winnerId = null;
        this.highestBid = null;
        this.lock = new ReentrantLock(true);

        // Khởi tạo trạng thái ban đầu: Chưa tới giờ mở là OPEN, còn lại là RUNNING
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.startTime)) {
            this.state = new OpenState();
        } else {
            this.state = new RunningState();
        }
    }

    public ReentrantLock getLock() {
        if (this.lock == null) {
            this.lock = new ReentrantLock(true);
        }
        return this.lock;
    }

    // ==================== STATE PATTERN DELEGATION ====================

    public void changeState(AuctionState newState) {
        this.state = newState;
    }

    public AuctionState getState() {
        return state;
    }

    public AuctionStatus getStatus() {
        return state != null ? state.getStatus() : AuctionStatus.OPEN;
    }

    public void placeBid(Bid bid) throws InvalidBidException, AuctionClosedException {
        getLock().lock();
        try {
            this.state.handleBid(this, bid);
        } finally {
            getLock().unlock();
        }
    }

    public boolean settle() {
        getLock().lock();
        try {
            return this.state.handleSettle(this);
        } finally {
            getLock().unlock();
        }
    }

    public void cancel() throws AuctionClosedException {
        getLock().lock();
        try {
            this.state.handleCancel(this);
        } finally {
            getLock().unlock();
        }
    }

    public void pay() throws AuctionClosedException {
        getLock().lock();
        try {
            this.state.handlePay(this);
        } finally {
            getLock().unlock();
        }
    }

    // ==================== AUTO-BID MANAGEMENT ====================

    public void addAutoBidRule(AutoBidRule rule) {
        if (rule == null) {
            return;
        }
        if (rule.getBidderId().equals(this.sellerId)) {
            throw new IllegalArgumentException("Người bán không thể đặt Auto-bid cho sản phẩm của mình!");
        }
        getLock().lock();
        try {
            this.autoBidRules.removeIf(r -> r.getBidderId().equals(rule.getBidderId()));
            this.autoBidRules.add(rule);
        } finally {
            getLock().unlock();
        }
    }

    public List<AutoBidRule> getAutoBidRules() {
        getLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(autoBidRules));
        } finally {
            getLock().unlock();
        }
    }

    // ==================== GETTERS ====================

    public String getAuctionId() {
        return auctionId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getStepPrice() {
        return stepPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public Bid getHighestBid() {
        return highestBid;
    }

    public List<Bid> getBids() {
        getLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(bids));
        } finally {
            getLock().unlock();
        }
    }

    public String getWinnerId() {
        return winnerId;
    }

    // ==================== MUTATORS ====================

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setHighestBid(Bid highestBid) {
        this.highestBid = highestBid;
        if (highestBid != null) {
            this.currentPrice = highestBid.getAmount();
        }
    }

    public void addBid(Bid bid) {
        this.bids.add(bid);
        setHighestBid(bid);
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public void extendEndTime(long extraSeconds) {
        if (extraSeconds > 0) {
            this.endTime = this.endTime.plusSeconds(extraSeconds);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Auction auction = (Auction) o;
        return Objects.equals(auctionId, auction.auctionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionId);
    }

    @Override
    public String toString() {
        return String.format("Auction{id='%s', item='%s', currentPrice=%.2f, status=%s, winner='%s'}",
                auctionId, itemName, currentPrice, getStatus(), winnerId != null ? winnerId : "Chưa có");
    }
}