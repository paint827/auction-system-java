package com.auction.client.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BidRecord implements Serializable {
    private final String time;
    private final String bidder;
    private final long amount;
    private String status; // "DẪN ĐẦU", "BỊ VƯỢT", "TRÚNG GIÁ"

    public BidRecord(String bidder, long amount, String status) {
        this.time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.bidder = bidder;
        this.amount = amount;
        this.status = status;
    }

    public BidRecord(String time, String bidder, long amount, String status) {
        this.time = time;
        this.bidder = bidder;
        this.amount = amount;
        this.status = status;
    }

    public String getTime() { return time; }
    public String getBidder() { return bidder; }
    public long getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
