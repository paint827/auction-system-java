package com.auction.server.service;

import com.auction.common.exception.AuctionClosedException;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.model.Auction;
import com.auction.common.model.Bid;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface Ä‘á»‹nh nghÄ©a há»£p Ä‘á»“ng nghiá»‡p vá»¥ (Service Contract) cho toÃ n bá»™ há»‡ thá»‘ng Ä‘áº¥u giÃ¡.
 * Ãp dá»¥ng nguyÃªn lÃ½ DIP (Dependency Inversion Principle - Slide 6.2 trang 29-32) vÃ 
 * Interface trong Java (Slide 4).
 * 
 * Phá»¥ trÃ¡ch bá»Ÿi TV1 (Core Engine & Concurrency).
 */
public interface AuctionService {

    /**
     * Táº¡o má»™t phiÃªn Ä‘áº¥u giÃ¡ má»›i vÃ  Ä‘Äƒng kÃ½ vÃ o AuctionManager.
     */
    Auction createAuction(String sellerId, String itemId, String itemName,
                          double startingPrice, double stepPrice,
                          LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Äáº·t giÃ¡ cho má»™t phiÃªn Ä‘áº¥u giÃ¡.
     * Äáº£m báº£o an toÃ n luá»“ng (Concurrency - Slide 7) chá»‘ng Race Condition vÃ  Lost Update.
     * Tá»± Ä‘á»™ng kÃ­ch hoáº¡t Anti-sniping vÃ  chuá»—i pháº£n há»“i Auto-bidding náº¿u cÃ³.
     * 
     * @param auctionId mÃ£ phiÃªn Ä‘áº¥u giÃ¡
     * @param bidderId mÃ£ ngÆ°á»i Ä‘áº·t giÃ¡
     * @param amount sá»‘ tiá»n Ä‘áº·t
     * @return Ä‘á»‘i tÆ°á»£ng Bid há»£p lá»‡ vá»«a Ä‘Æ°á»£c ghi nháº­n
     */
    Bid placeBid(String auctionId, String bidderId, double amount)
            throws InvalidBidException, AuctionClosedException;

    /**
     * ÄÄƒng kÃ½ háº¡n má»©c tá»± Ä‘á»™ng Ä‘áº¥u giÃ¡ (Auto-bid) cho má»™t bidder.
     */
    void registerAutoBid(String auctionId, String bidderId, double maxAmount)
            throws InvalidBidException, AuctionClosedException;

    /**
     * Kiá»ƒm tra vÃ  chá»‘t káº¿t quáº£ phiÃªn náº¿u Ä‘Ã£ háº¿t giá».
     * Äáº£m báº£o tÃ­nh cháº¥t IDEMPOTENT (an toÃ n tuyá»‡t Ä‘á»‘i khi Timer gá»i Ä‘á»‹nh ká»³ má»—i giÃ¢y).
     * 
     * @param auctionId mÃ£ phiÃªn Ä‘áº¥u giÃ¡
     * @return true náº¿u phiÃªn Ä‘Æ°á»£c chá»‘t trong láº§n gá»i nÃ y; false náº¿u chÆ°a Ä‘áº¿n giá» hoáº·c Ä‘Ã£ chá»‘t rá»“i
     */
    boolean settleIfEnded(String auctionId);

    /**
     * Há»§y phiÃªn Ä‘áº¥u giÃ¡ (chá»‰ Ä‘Æ°á»£c phÃ©p khi phiÃªn chÆ°a má»Ÿ - OPEN).
     */
    void cancelAuction(String auctionId) throws AuctionClosedException;

    /**
     * Láº¥y thÃ´ng tin chi tiáº¿t má»™t phiÃªn Ä‘áº¥u giÃ¡.
     */
    Auction getAuction(String auctionId);

    /**
     * Láº¥y toÃ n bá»™ danh sÃ¡ch cÃ¡c phiÃªn Ä‘áº¥u giÃ¡.
     */
    List<Auction> getAllAuctions();

    /**
     * Láº¥y danh sÃ¡ch cÃ¡c phiÃªn Ä‘ang diá»…n ra (RUNNING).
     */
    List<Auction> getRunningAuctions();
}
