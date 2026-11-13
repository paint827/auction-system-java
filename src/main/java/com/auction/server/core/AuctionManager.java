package com.auction.server.core;

import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quáº£n lÃ½ táº­p trung toÃ n bá»™ cÃ¡c phiÃªn Ä‘áº¥u giÃ¡ trong bá»™ nhá»› RAM cá»§a Server.
 * 
 * KIáº¾N THá»¨C BÃM SÃT GIÃO TRÃŒNH UET:
 * 1. Singleton Pattern (Slide 6.1 trang 9-10):
 *    - Constructor lÃ  private ngÄƒn táº¡o má»›i báº±ng 'new'.
 *    - Biáº¿n tÄ©nh (static) lÆ°u trá»¯ instance duy nháº¥t.
 *    - Äiá»ƒm truy cáº­p toÃ n cá»¥c thÃ´ng qua phÆ°Æ¡ng thá»©c static getInstance().
 * 2. An toÃ n Ä‘a luá»“ng (Slide 7 trang 41 - Synchronized Methods):
 *    - ThÃªm tá»« khÃ³a 'synchronized' táº¡i phÆ°Æ¡ng thá»©c getInstance() Ä‘á»ƒ Ä‘áº£m báº£o
 *      chá»‰ cÃ³ Ä‘Ãºng 1 instance duy nháº¥t Ä‘Æ°á»£c táº¡o ra khi nhiá»u luá»“ng cÃ¹ng gá»i Ä‘á»“ng thá»i.
 * 3. Bá»™ sÆ°u táº­p luá»“ng (Slide 5.1 & Slide 7):
 *    - Sá»­ dá»¥ng ConcurrentHashMap lÆ°u trá»¯ auctions theo auctionId Ä‘á»ƒ Ä‘áº£m báº£o thread-safe.
 * 
 * Phá»¥ trÃ¡ch bá»Ÿi TV1.
 */
public class AuctionManager {

    // 1. Biáº¿n static lÆ°u Ä‘á»‘i tÆ°á»£ng duy nháº¥t (Slide 6.1 trang 9)
    private static AuctionManager instance = null;

    // Báº£ng bÄƒm lÆ°u trá»¯ cÃ¡c phiÃªn Ä‘áº¥u giÃ¡ trong bá»™ nhá»›
    private final Map<String, Auction> auctions;

    // 2. Private constructor (Slide 6.1 trang 9)
    private AuctionManager() {
        this.auctions = new ConcurrentHashMap<>();
    }

    // 3. Global access point káº¿t há»£p synchronized (Slide 6.1 trang 10 & Slide 7 trang 41)
    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    /**
     * ThÃªm má»™t phiÃªn Ä‘áº¥u giÃ¡ vÃ o há»‡ thá»‘ng.
     */
    public void addAuction(Auction auction) {
        if (auction != null) {
            auctions.put(auction.getAuctionId(), auction);
        }
    }

    /**
     * TÃ¬m phiÃªn Ä‘áº¥u giÃ¡ theo mÃ£ auctionId.
     */
    public Auction getAuction(String auctionId) {
        if (auctionId == null) {
            return null;
        }
        return auctions.get(auctionId);
    }

    /**
     * Láº¥y danh sÃ¡ch táº¥t cáº£ cÃ¡c phiÃªn Ä‘áº¥u giÃ¡.
     */
    public List<Auction> getAllAuctions() {
        return Collections.unmodifiableList(new ArrayList<>(auctions.values()));
    }

    /**
     * XÃ³a má»™t phiÃªn Ä‘áº¥u giÃ¡ khá»i bá»™ nhá»›.
     */
    public boolean removeAuction(String auctionId) {
        if (auctionId == null) {
            return false;
        }
        return auctions.remove(auctionId) != null;
    }

    /**
     * Äáº¿m tá»•ng sá»‘ phiÃªn hiá»‡n táº¡i.
     */
    public int count() {
        return auctions.size();
    }

    /**
     * XÃ³a sáº¡ch dá»¯ liá»‡u (phá»¥c vá»¥ reset giá»¯a cÃ¡c ca kiá»ƒm thá»­ Unit Test).
     */
    public void clearAll() {
        auctions.clear();
    }
}
