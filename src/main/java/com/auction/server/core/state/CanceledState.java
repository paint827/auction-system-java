package com.auction.server.core.state;

import com.auction.common.exception.AuctionClosedException;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;

/**
 * Tráº¡ng thÃ¡i CANCELED: PhiÃªn Ä‘áº¥u giÃ¡ Ä‘Ã£ bá»‹ há»§y bá» bá»Ÿi Seller (khi OPEN) hoáº·c Admin.
 * Phá»¥ trÃ¡ch bá»Ÿi TV1.
 */
public class CanceledState implements AuctionState {
    private static final long serialVersionUID = 1L;

    @Override
    public AuctionStatus getStatus() {
        return AuctionStatus.CANCELED;
    }

    @Override
    public void handleBid(Auction auction, Bid bid) throws InvalidBidException, AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ Ä‘Ã£ bá»‹ há»§y bá»!");
    }

    @Override
    public boolean handleSettle(Auction auction) {
        return false;
    }

    @Override
    public void handleCancel(Auction auction) throws AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ nÃ y Ä‘Ã£ Ä‘Æ°á»£c há»§y tá»« trÆ°á»›c!");
    }

    @Override
    public void handlePay(Auction auction) throws AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ Ä‘Ã£ bá»‹ há»§y, khÃ´ng thá»ƒ thanh toÃ¡n!");
    }

    @Override
    public String toString() {
        return "CANCELED";
    }
}
