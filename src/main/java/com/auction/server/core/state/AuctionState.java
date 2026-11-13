package com.auction.server.core.state;

import com.auction.common.exception.AuctionClosedException;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;

import java.io.Serializable;

/**
 * State Interface trong State Pattern.
 * Äá»‹nh nghÄ©a cÃ¡c hÃ nh vi phá»¥ thuá»™c vÃ o tráº¡ng thÃ¡i cá»§a phiÃªn Ä‘áº¥u giÃ¡:
 * - handleBid: Xá»­ lÃ½ Ä‘áº·t giÃ¡
 * - handleSettle: Xá»­ lÃ½ chá»‘t phiÃªn khi háº¿t giá»
 * - handleCancel: Xá»­ lÃ½ há»§y phiÃªn
 * - handlePay: Xá»­ lÃ½ thanh toÃ¡n
 * 
 * Phá»¥ trÃ¡ch bá»Ÿi TV1.
 */
public interface AuctionState extends Serializable {

    /**
     * Tráº£ vá» enum tráº¡ng thÃ¡i tÆ°Æ¡ng á»©ng Ä‘á»ƒ phá»¥c vá»¥ hiá»ƒn thá»‹ vÃ  Serialization.
     */
    AuctionStatus getStatus();

    /**
     * Xá»­ lÃ½ hÃ nh vi Ä‘áº·t giÃ¡.
     */
    void handleBid(Auction auction, Bid bid) throws InvalidBidException, AuctionClosedException;

    /**
     * Xá»­ lÃ½ hÃ nh vi káº¿t thÃºc/chá»‘t phiÃªn.
     */
    boolean handleSettle(Auction auction);

    /**
     * Xá»­ lÃ½ hÃ nh vi há»§y phiÃªn.
     */
    void handleCancel(Auction auction) throws AuctionClosedException;

    /**
     * Xá»­ lÃ½ hÃ nh vi thanh toÃ¡n Ä‘Æ¡n hÃ ng thÃ nh cÃ´ng.
     */
    void handlePay(Auction auction) throws AuctionClosedException;
}
