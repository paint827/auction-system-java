package com.auction.server.core.state;

import com.auction.common.exception.AuctionClosedException;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;

import java.time.LocalDateTime;

/**
 * Tráº¡ng thÃ¡i OPEN: PhiÃªn Ä‘áº¥u giÃ¡ vá»«a Ä‘Æ°á»£c táº¡o, chÆ°a Ä‘áº¿n giá» má»Ÿ.
 * KhÃ´ng cho phÃ©p Ä‘áº·t giÃ¡. ÄÆ°á»£c phÃ©p há»§y phiÃªn.
 * 
 * Ãp dá»¥ng nguyÃªn lÃ½ OCP (Slide 6.2 trang 21-25).
 * Phá»¥ trÃ¡ch bá»Ÿi TV1.
 */
public class OpenState implements AuctionState {
    private static final long serialVersionUID = 1L;

    @Override
    public AuctionStatus getStatus() {
        return AuctionStatus.OPEN;
    }

    @Override
    public void handleBid(Auction auction, Bid bid) throws InvalidBidException, AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ chÆ°a má»Ÿ (báº¯t Ä‘áº§u lÃºc: " + auction.getStartTime() + "). ChÆ°a thá»ƒ Ä‘áº·t giÃ¡!");
    }

    @Override
    public boolean handleSettle(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        // Náº¿u Ä‘Ã£ quÃ¡ giá» káº¿t thÃºc luÃ´n -> chuyá»ƒn tháº³ng sang FINISHED
        if (!now.isBefore(auction.getEndTime())) {
            auction.setWinnerId(null);
            auction.changeState(new FinishedState());
            return true;
        }
        // Náº¿u Ä‘Ã£ Ä‘áº¿n hoáº·c quÃ¡ giá» má»Ÿ phiÃªn -> tá»± Ä‘á»™ng chuyá»ƒn sang RUNNING
        if (!now.isBefore(auction.getStartTime())) {
            auction.changeState(new RunningState());
            return true;
        }
        return false;
    }

    @Override
    public void handleCancel(Auction auction) {
        // Cho phÃ©p há»§y phiÃªn khi cÃ²n á»Ÿ tráº¡ng thÃ¡i OPEN
        auction.changeState(new CanceledState());
    }

    @Override
    public void handlePay(Auction auction) throws AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ chÆ°a diá»…n ra, khÃ´ng thá»ƒ thanh toÃ¡n!");
    }

    @Override
    public String toString() {
        return "OPEN";
    }
}
