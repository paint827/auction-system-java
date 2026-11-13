package com.auction.server.core.state;

import com.auction.common.exception.AuctionClosedException;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;

/**
 * Tráº¡ng thÃ¡i PAID: NgÆ°á»i tháº¯ng Ä‘Ã£ thanh toÃ¡n thÃ nh cÃ´ng.
 * PhiÃªn Ä‘áº¥u giÃ¡ hoÃ n táº¥t vÃ²ng Ä‘á»i.
 * Phá»¥ trÃ¡ch bá»Ÿi TV1.
 */
public class PaidState implements AuctionState {
    private static final long serialVersionUID = 1L;

    @Override
    public AuctionStatus getStatus() {
        return AuctionStatus.PAID;
    }

    @Override
    public void handleBid(Auction auction, Bid bid) throws InvalidBidException, AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ Ä‘Ã£ hoÃ n táº¥t thanh toÃ¡n, khÃ´ng thá»ƒ Ä‘áº·t giÃ¡!");
    }

    @Override
    public boolean handleSettle(Auction auction) {
        return false;
    }

    @Override
    public void handleCancel(Auction auction) throws AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ Ä‘Ã£ thanh toÃ¡n, khÃ´ng thá»ƒ há»§y!");
    }

    @Override
    public void handlePay(Auction auction) throws AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ nÃ y Ä‘Ã£ Ä‘Æ°á»£c thanh toÃ¡n tá»« trÆ°á»›c!");
    }

    @Override
    public String toString() {
        return "PAID";
    }
}
