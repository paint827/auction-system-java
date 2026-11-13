package com.auction.server.core.state;

import com.auction.common.exception.AuctionClosedException;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;

/**
 * Tráº¡ng thÃ¡i FINISHED: PhiÃªn Ä‘Ã£ háº¿t giá» vÃ  Ä‘Ã£ chá»‘t káº¿t quáº£ ngÆ°á»i tháº¯ng.
 * ÄÃ³ng hoÃ n toÃ n viá»‡c Ä‘áº·t giÃ¡. Chá» thanh toÃ¡n.
 * 
 * TÃNH CHáº¤T IDEMPOTENT:
 * Gá»i handleSettle() á»Ÿ tráº¡ng thÃ¡i nÃ y luÃ´n tráº£ vá» false vÃ  khÃ´ng lÃ m thay Ä‘á»•i tráº¡ng thÃ¡i,
 * Ä‘áº£m báº£o há»‡ thá»‘ng khÃ´ng bao giá» chá»‘t phiÃªn hoáº·c thanh toÃ¡n 2 láº§n khi timer gá»i Ä‘á»‹nh ká»³.
 * 
 * Phá»¥ trÃ¡ch bá»Ÿi TV1.
 */
public class FinishedState implements AuctionState {
    private static final long serialVersionUID = 1L;

    @Override
    public AuctionStatus getStatus() {
        return AuctionStatus.FINISHED;
    }

    @Override
    public void handleBid(Auction auction, Bid bid) throws InvalidBidException, AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ Ä‘Ã£ káº¿t thÃºc, khÃ´ng thá»ƒ Ä‘áº·t giÃ¡!");
    }

    @Override
    public boolean handleSettle(Auction auction) {
        // ÄÃ£ káº¿t thÃºc tá»« trÆ°á»›c -> Tráº£ vá» false ngay láº­p tá»©c (Idempotent)
        return false;
    }

    @Override
    public void handleCancel(Auction auction) throws AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ Ä‘Ã£ káº¿t thÃºc, khÃ´ng thá»ƒ há»§y!");
    }

    @Override
    public void handlePay(Auction auction) throws AuctionClosedException {
        if (auction.getWinnerId() == null) {
            throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ káº¿t thÃºc khÃ´ng cÃ³ ngÆ°á»i tháº¯ng, khÃ´ng thá»ƒ thanh toÃ¡n!");
        }
        auction.changeState(new PaidState());
    }

    @Override
    public String toString() {
        return "FINISHED";
    }
}
