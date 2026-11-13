package com.auction.server.core.state;

import com.auction.common.exception.AuctionClosedException;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;

import java.time.LocalDateTime;

/**
 * Tráº¡ng thÃ¡i RUNNING: PhiÃªn Ä‘ang diá»…n ra sÃ´i ná»•i.
 * Cho phÃ©p Ä‘áº·t giÃ¡ há»£p lá»‡ vÃ  chá»‘t phiÃªn khi háº¿t thá»i gian.
 * Phá»¥ trÃ¡ch bá»Ÿi TV1.
 */
public class RunningState implements AuctionState {
    private static final long serialVersionUID = 1L;

    @Override
    public AuctionStatus getStatus() {
        return AuctionStatus.RUNNING;
    }

    @Override
    public void handleBid(Auction auction, Bid bid) throws InvalidBidException, AuctionClosedException {
        // 1. Quy táº¯c: NgÆ°á»i bÃ¡n khÃ´ng Ä‘Æ°á»£c tá»± Ä‘áº·t giÃ¡ cho sáº£n pháº©m cá»§a mÃ¬nh
        if (bid.getBidderId().equals(auction.getSellerId())) {
            throw new InvalidBidException("NgÆ°á»i bÃ¡n khÃ´ng Ä‘Æ°á»£c phÃ©p Ä‘áº·t giÃ¡ cho sáº£n pháº©m cá»§a chÃ­nh mÃ¬nh!");
        }

        // 2. Quy táº¯c bÆ°á»›c giÃ¡ vÃ  giÃ¡ tá»‘i thiá»ƒu
        double currentPrice = auction.getCurrentPrice();
        double stepPrice = auction.getStepPrice();

        if (auction.getBids().isEmpty()) {
            // LÆ°á»£t Ä‘áº·t Ä‘áº§u tiÃªn: GiÃ¡ pháº£i lá»›n hÆ¡n hoáº·c báº±ng giÃ¡ khá»Ÿi Ä‘iá»ƒm
            if (bid.getAmount() < auction.getStartingPrice()) {
                throw new InvalidBidException(String.format(
                        "LÆ°á»£t Ä‘áº·t giÃ¡ Ä‘áº§u tiÃªn pháº£i >= giÃ¡ khá»Ÿi Ä‘iá»ƒm (%.2f). GiÃ¡ báº¡n Ä‘áº·t: %.2f",
                        auction.getStartingPrice(), bid.getAmount()));
            }
        } else {
            // CÃ¡c lÆ°á»£t Ä‘áº·t tiáº¿p theo: Pháº£i >= giÃ¡ hiá»‡n táº¡i + bÆ°á»›c giÃ¡
            double minAcceptablePrice = currentPrice + stepPrice;
            if (bid.getAmount() < minAcceptablePrice) {
                throw new InvalidBidException(String.format(
                        "GiÃ¡ Ä‘áº·t (%.2f) pháº£i lá»›n hÆ¡n hoáº·c báº±ng giÃ¡ hiá»‡n táº¡i (%.2f) + bÆ°á»›c giÃ¡ (%.2f) = %.2f",
                        bid.getAmount(), currentPrice, stepPrice, minAcceptablePrice));
            }
            // KhÃ´ng Ä‘Æ°á»£c tá»± nÃ¢ng giÃ¡ chÃ­nh mÃ¬nh náº¿u Ä‘ang lÃ  ngÆ°á»i giá»¯ giÃ¡ cao nháº¥t
            if (auction.getHighestBid() != null && auction.getHighestBid().getBidderId().equals(bid.getBidderId())) {
                throw new InvalidBidException("Báº¡n Ä‘ang lÃ  ngÆ°á»i giá»¯ má»©c giÃ¡ cao nháº¥t hiá»‡n táº¡i!");
            }
        }

        // Há»£p lá»‡ -> Ghi nháº­n lÆ°á»£t Ä‘áº·t giÃ¡
        auction.addBid(bid);
    }

    @Override
    public boolean handleSettle(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        // Náº¿u chÆ°a Ä‘áº¿n giá» káº¿t thÃºc, khÃ´ng lÃ m gÃ¬ cáº£
        if (now.isBefore(auction.getEndTime())) {
            return false;
        }

        // ÄÃ£ háº¿t giá» -> Tiáº¿n hÃ nh chá»‘t phiÃªn
        if (auction.getHighestBid() != null) {
            auction.setWinnerId(auction.getHighestBid().getBidderId());
        } else {
            // KhÃ´ng cÃ³ ai Ä‘áº·t giÃ¡ trong suá»‘t phiÃªn
            auction.setWinnerId(null);
        }

        // Chuyá»ƒn sang FinishedState
        auction.changeState(new FinishedState());
        return true;
    }

    @Override
    public void handleCancel(Auction auction) throws AuctionClosedException {
        throw new AuctionClosedException("KhÃ´ng thá»ƒ há»§y phiÃªn Ä‘áº¥u giÃ¡ khi phiÃªn Ä‘ang diá»…n ra (RUNNING)!");
    }

    @Override
    public void handlePay(Auction auction) throws AuctionClosedException {
        throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ Ä‘ang diá»…n ra, chÆ°a thá»ƒ thanh toÃ¡n!");
    }

    @Override
    public String toString() {
        return "RUNNING";
    }
}
