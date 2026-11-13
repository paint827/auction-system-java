package com.auction.server.service;

import com.auction.common.exception.AuctionClosedException;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.model.Auction;
import com.auction.server.core.AuctionManager;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;
import com.auction.common.model.AutoBidRule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Hiá»‡n thá»±c hÃ³a nghiá»‡p vá»¥ Ä‘áº¥u giÃ¡ trá»±c tuyáº¿n vÃ  Ä‘iá»u phá»‘i an toÃ n Ä‘a luá»“ng.
 * 
 * KIáº¾N THá»¨C BÃM SÃT GIÃO TRÃŒNH UET:
 * 1. Máº«u Singleton (Slide 6.1 trang 9-10 & Slide 7 trang 41):
 *    - Sá»­ dá»¥ng AuctionManager.getInstance() Ä‘á»ƒ truy xuáº¥t dá»¯ liá»‡u RAM táº­p trung.
 * 2. KhÃ³a ReentrantLock (Slide 7 trang 44-46):
 *    - Má»—i thao tÃ¡c Ä‘áº·t giÃ¡ hoáº·c chá»‘t phiÃªn Ä‘á»u Ä‘Æ°á»£c khÃ³a báº±ng chÃ­nh ReentrantLock
 *      cá»§a phiÃªn Ä‘Ã³ (`auction.getLock()`).
 *    - Sá»­ dá»¥ng cáº¥u trÃºc chuáº©n `try { ... } finally { lock.unlock(); }` (Slide 7 trang 45).
 * 3. Chá»‘ng Lost Update & Race Condition (Slide 7 trang 38-42):
 *    - Äáº£m báº£o tÃ­nh nguyÃªn tá»­ (Atomic) khi kiá»ƒm tra giÃ¡ tá»‘i thiá»ƒu, táº¡o lÆ°á»£t Ä‘áº·t giÃ¡,
 *      kÃ­ch hoáº¡t Anti-sniping vÃ  kÃ­ch hoáº¡t chuá»—i pháº£n há»“i Auto-bid.
 * 
 * Phá»¥ trÃ¡ch bá»Ÿi TV1 (Core Engine & Concurrency).
 */
public class AuctionServiceImpl implements AuctionService {

    private final AuctionManager auctionManager;

    // Cáº¥u hÃ¬nh Anti-sniping: Náº¿u cÃ³ lÆ°á»£t Ä‘áº·t giÃ¡ trong 30 giÃ¢y cuá»‘i, tá»± Ä‘á»™ng cá»™ng thÃªm 60 giÃ¢y
    private static final long ANTI_SNIPING_THRESHOLD_SECONDS = 30;
    private static final long ANTI_SNIPING_EXTEND_SECONDS = 60;

    public AuctionServiceImpl() {
        this.auctionManager = AuctionManager.getInstance();
    }

    @Override
    public Auction createAuction(String sellerId, String itemId, String itemName,
                                double startingPrice, double stepPrice,
                                LocalDateTime startTime, LocalDateTime endTime) {
        String auctionId = "AUC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Auction auction = new Auction(auctionId, sellerId, itemId, itemName,
                startingPrice, stepPrice, startTime, endTime);

        // LÆ°u vÃ o Singleton AuctionManager trong RAM
        auctionManager.addAuction(auction);
        return auction;
    }

    @Override
    public Bid placeBid(String auctionId, String bidderId, double amount)
            throws InvalidBidException, AuctionClosedException {

        if (auctionId == null || bidderId == null) {
            throw new InvalidBidException("MÃ£ phiÃªn hoáº·c mÃ£ ngÆ°á»i Ä‘áº·t giÃ¡ khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng!");
        }

        Auction auction = auctionManager.getAuction(auctionId);
        if (auction == null) {
            throw new InvalidBidException("KhÃ´ng tÃ¬m tháº¥y phiÃªn Ä‘áº¥u giÃ¡ cÃ³ mÃ£: " + auctionId);
        }

        // Láº¥y khÃ³a ReentrantLock riÃªng cá»§a phiÃªn nÃ y (Slide 7 trang 44-46)
        ReentrantLock lock = auction.getLock();
        lock.lock();
        try {
            // 1. Táº¡o Ä‘á»‘i tÆ°á»£ng Bid má»›i
            String bidId = "BID-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Bid newBid = new Bid(bidId, auctionId, bidderId, amount, LocalDateTime.now());

            // 2. Chuyá»ƒn cho State Pattern xá»­ lÃ½ (kiá»ƒm tra tráº¡ng thÃ¡i, ngÆ°á»i bÃ¡n, bÆ°á»›c giÃ¡)
            auction.placeBid(newBid);

            // 3. Xá»­ lÃ½ Anti-sniping: Tá»± Ä‘á»™ng gia háº¡n náº¿u Ä‘áº·t trong nhá»¯ng giÃ¢y cuá»‘i
            LocalDateTime now = LocalDateTime.now();
            long secondsLeft = Duration.between(now, auction.getEndTime()).getSeconds();
            if (secondsLeft > 0 && secondsLeft <= ANTI_SNIPING_THRESHOLD_SECONDS) {
                auction.extendEndTime(ANTI_SNIPING_EXTEND_SECONDS);
            }

            // 4. KÃ­ch hoáº¡t pháº£n há»“i Auto-bid cá»§a cÃ¡c ngÆ°á»i tham gia khÃ¡c (náº¿u cÃ³)
            triggerAutoBids(auction, bidderId);

            return newBid;
        } finally {
            // LuÃ´n nháº£ khÃ³a trong khá»‘i finally Ä‘á»ƒ trÃ¡nh Deadlock (Slide 7 trang 45)
            lock.unlock();
        }
    }

    @Override
    public void registerAutoBid(String auctionId, String bidderId, double maxAmount)
            throws InvalidBidException, AuctionClosedException {
        if (auctionId == null || bidderId == null) {
            throw new InvalidBidException("MÃ£ phiÃªn vÃ  ngÆ°á»i Ä‘áº·t giÃ¡ khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng!");
        }

        Auction auction = auctionManager.getAuction(auctionId);
        if (auction == null) {
            throw new InvalidBidException("KhÃ´ng tÃ¬m tháº¥y phiÃªn Ä‘áº¥u giÃ¡: " + auctionId);
        }

        ReentrantLock lock = auction.getLock();
        lock.lock();
        try {
            if (auction.getStatus() == AuctionStatus.FINISHED || auction.getStatus() == AuctionStatus.CANCELED) {
                throw new AuctionClosedException("PhiÃªn Ä‘áº¥u giÃ¡ Ä‘Ã£ káº¿t thÃºc, khÃ´ng thá»ƒ Ä‘Äƒng kÃ½ Auto-bid!");
            }

            String ruleId = "RULE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            AutoBidRule rule = new AutoBidRule(ruleId, auctionId, bidderId, maxAmount);
            auction.addAutoBidRule(rule);

            // Thá»­ kÃ­ch hoáº¡t ngay náº¿u phiÃªn Ä‘ang RUNNING vÃ  bidder chÆ°a pháº£i lÃ  ngÆ°á»i giá»¯ giÃ¡ cao nháº¥t
            if (auction.getStatus() == AuctionStatus.RUNNING) {
                String currentWinner = auction.getHighestBid() != null ? auction.getHighestBid().getBidderId() : null;
                if (!bidderId.equals(currentWinner)) {
                    triggerAutoBids(auction, currentWinner);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * VÃ²ng láº·p tá»± Ä‘á»™ng nÃ¢ng giÃ¡ cá»§a cÃ¡c AutoBidRule cho Ä‘áº¿n khi Ä‘áº¡t tráº¡ng thÃ¡i cÃ¢n báº±ng.
     * ÄÆ°á»£c thá»±c thi hoÃ n toÃ n trong khá»‘i khÃ³a ReentrantLock cá»§a phiÃªn.
     */
    private void triggerAutoBids(Auction auction, String currentHighestBidderId) {
        boolean counterBidPlaced;
        do {
            counterBidPlaced = false;
            double nextPrice;
            if (auction.getBids().isEmpty()) {
                nextPrice = auction.getStartingPrice();
            } else {
                nextPrice = auction.getCurrentPrice() + auction.getStepPrice();
            }

            // TÃ¬m rule cá»§a ngÆ°á»i khÃ¡c Ä‘á»§ Ä‘iá»u kiá»‡n Ä‘áº·t tiáº¿p
            for (AutoBidRule rule : auction.getAutoBidRules()) {
                if (!rule.getBidderId().equals(currentHighestBidderId) && rule.getMaxAmount() >= nextPrice) {
                    try {
                        String autoBidId = "BID-AUTO-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                        Bid autoBid = new Bid(autoBidId, auction.getAuctionId(), rule.getBidderId(), nextPrice, LocalDateTime.now());
                        auction.placeBid(autoBid);
                        currentHighestBidderId = rule.getBidderId();
                        counterBidPlaced = true;
                        break; // Äáº·t 1 lÆ°á»£t rá»“i quÃ©t láº¡i cÃ¡c rule khÃ¡c
                    } catch (Exception ignored) {
                        // Bá» qua náº¿u vi pháº¡m rÃ ng buá»™c
                    }
                }
            }
        } while (counterBidPlaced);
    }

    @Override
    public boolean settleIfEnded(String auctionId) {
        if (auctionId == null) {
            return false;
        }

        Auction auction = auctionManager.getAuction(auctionId);
        if (auction == null) {
            return false;
        }

        // Bá» qua nhanh náº¿u tráº¡ng thÃ¡i Ä‘Ã£ káº¿t thÃºc tá»« trÆ°á»›c (tá»‘i Æ°u hiá»‡u nÄƒng)
        if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING) {
            return false;
        }

        ReentrantLock lock = auction.getLock();
        lock.lock();
        try {
            // á»¦y quyá»n xá»­ lÃ½ káº¿t thÃºc cho State Pattern (Idempotent)
            return auction.settle();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cancelAuction(String auctionId) throws AuctionClosedException {
        if (auctionId == null) {
            return;
        }
        Auction auction = auctionManager.getAuction(auctionId);
        if (auction != null) {
            auction.cancel();
        }
    }

    @Override
    public Auction getAuction(String auctionId) {
        return auctionManager.getAuction(auctionId);
    }

    @Override
    public List<Auction> getAllAuctions() {
        return auctionManager.getAllAuctions();
    }

    @Override
    public List<Auction> getRunningAuctions() {
        List<Auction> running = new ArrayList<>();
        for (Auction a : auctionManager.getAllAuctions()) {
            if (a.getStatus() == AuctionStatus.RUNNING) {
                running.add(a);
            }
        }
        return running;
    }
}
