package com.auction.server.core;

import com.auction.common.model.*;

import com.auction.common.exception.AuctionClosedException;
import com.auction.common.exception.InvalidBidException;
import com.auction.server.service.AuctionService;
import com.auction.server.service.AuctionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bá»™ kiá»ƒm thá»­ tá»± Ä‘á»™ng (Unit Test & Concurrency Test).
 * 
 * KIáº¾N THá»¨C BÃM SÃT GIÃO TRÃŒNH UET:
 * 1. Kiá»ƒm thá»­ pháº§n má»m vá»›i JUnit 5 (Slide 8.1):
 *    - Sá»­ dá»¥ng @BeforeEach Ä‘á»ƒ chuáº©n bá»‹ dá»¯ liá»‡u má»›i trÆ°á»›c má»—i test case.
 *    - Sá»­ dá»¥ng @Test, @DisplayName rÃµ rÃ ng.
 *    - CÃ¡c assertions chuáº©n: assertEquals, assertNotNull, assertThrows, assertTrue, assertFalse.
 * 2. Äa luá»“ng vÃ  ExecutorService (Slide 7 trang 25-27, 42-43):
 *    - Sá»­ dá»¥ng Executors.newFixedThreadPool() Ä‘á»ƒ táº¡o thread pool kiá»ƒm thá»­ Lost Update.
 *    - Chá»©ng minh ReentrantLock báº£o vá»‡ an toÃ n biáº¿n chia sáº» khi 20 luá»“ng cÃ¹ng tranh cháº¥p Ä‘áº·t giÃ¡.
 * 
 * Phá»¥ trÃ¡ch bá»Ÿi TV1 (Tuáº§n 6 - 7).
 */
public class AuctionCoreTest {

    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        // Reset Singleton giá»¯a má»—i bÃ i test Ä‘á»ƒ Ä‘áº£m báº£o tÃ­nh Ä‘á»™c láº­p (Slide 8.1)
        AuctionManager.getInstance().clearAll();
        auctionService = new AuctionServiceImpl();
    }

    @Test
    @DisplayName("1. Äáº·t giÃ¡ há»£p lá»‡ cho phiÃªn Ä‘ang diá»…n ra (RUNNING)")
    void testPlaceValidBid() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = auctionService.createAuction(
                "SELLER_1", "ITEM_1", "Laptop Dell XPS",
                100.0, 10.0, now.minusMinutes(1), now.plusMinutes(10));

        assertEquals(AuctionStatus.RUNNING, auction.getStatus());

        // LÆ°á»£t Ä‘áº·t Ä‘áº§u tiÃªn >= giÃ¡ khá»Ÿi Ä‘iá»ƒm 100.0
        Bid bid1 = auctionService.placeBid(auction.getAuctionId(), "BIDDER_A", 100.0);
        assertNotNull(bid1);
        assertEquals(100.0, auction.getCurrentPrice());

        // LÆ°á»£t tiáº¿p theo pháº£i >= 100 + 10 = 110.0
        Bid bid2 = auctionService.placeBid(auction.getAuctionId(), "BIDDER_B", 115.0);
        assertNotNull(bid2);
        assertEquals(115.0, auction.getCurrentPrice());
        assertEquals("BIDDER_B", auction.getHighestBid().getBidderId());
        assertEquals(2, auction.getBids().size());
    }

    @Test
    @DisplayName("2. Báº¯t lá»—i: NgÆ°á»i bÃ¡n khÃ´ng Ä‘Æ°á»£c tá»± Ä‘áº·t giÃ¡ sáº£n pháº©m cá»§a chÃ­nh mÃ¬nh")
    void testSellerCannotBidOwnItem() {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = auctionService.createAuction(
                "SELLER_1", "ITEM_1", "Laptop Dell",
                100.0, 10.0, now.minusMinutes(1), now.plusMinutes(10));

        assertThrows(InvalidBidException.class, () -> {
            auctionService.placeBid(auction.getAuctionId(), "SELLER_1", 150.0);
        });
    }

    @Test
    @DisplayName("3. Báº¯t lá»—i: Äáº·t giÃ¡ tháº¥p hÆ¡n giÃ¡ hiá»‡n táº¡i + bÆ°á»›c giÃ¡")
    void testBidLowerThanStepPrice() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = auctionService.createAuction(
                "SELLER_1", "ITEM_1", "Laptop Dell",
                100.0, 10.0, now.minusMinutes(1), now.plusMinutes(10));

        auctionService.placeBid(auction.getAuctionId(), "BIDDER_A", 100.0);

        // GiÃ¡ hiá»‡n táº¡i 100.0, bÆ°á»›c giÃ¡ 10.0 -> Äáº·t 105.0 pháº£i nÃ©m InvalidBidException
        assertThrows(InvalidBidException.class, () -> {
            auctionService.placeBid(auction.getAuctionId(), "BIDDER_B", 105.0);
        });
    }

    @Test
    @DisplayName("4. Báº¯t lá»—i: KhÃ´ng thá»ƒ Ä‘áº·t giÃ¡ vÃ o phiÃªn chÆ°a má»Ÿ (OPEN)")
    void testCannotBidOnOpenAuction() {
        LocalDateTime now = LocalDateTime.now();
        // PhiÃªn má»Ÿ sau 10 phÃºt ná»¯a
        Auction auction = auctionService.createAuction(
                "SELLER_1", "ITEM_1", "Tranh cá»•",
                500.0, 50.0, now.plusMinutes(10), now.plusMinutes(30));

        assertEquals(AuctionStatus.OPEN, auction.getStatus());

        assertThrows(AuctionClosedException.class, () -> {
            auctionService.placeBid(auction.getAuctionId(), "BIDDER_A", 500.0);
        });
    }

    @Test
    @DisplayName("5. settleIfEnded pháº£i cÃ³ tÃ­nh cháº¥t IDEMPOTENT (gá»i 10 láº§n váº«n an toÃ n)")
    void testSettleIdempotency() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        // PhiÃªn Ä‘Ã£ háº¿t giá» cÃ¡ch Ä‘Ã¢y 1 phÃºt
        Auction auction = auctionService.createAuction(
                "SELLER_1", "ITEM_1", "Xe mÃ¡y",
                200.0, 20.0, now.minusMinutes(10), now.minusMinutes(1));

        // Giáº£ láº­p lÆ°á»£t Ä‘áº·t giÃ¡ há»£p lá»‡
        auction.addBid(new Bid("B1", auction.getAuctionId(), "BIDDER_WINNER", 250.0, now.minusMinutes(5)));

        // Gá»i láº§n 1 -> Chá»‘t phiÃªn thÃ nh cÃ´ng (tráº£ vá» true)
        boolean firstCall = auctionService.settleIfEnded(auction.getAuctionId());
        assertTrue(firstCall);
        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
        assertEquals("BIDDER_WINNER", auction.getWinnerId());

        // Gá»i láº·p láº¡i 10 láº§n liÃªn tiáº¿p -> Äá»u pháº£i tráº£ vá» false vÃ  khÃ´ng Ä‘á»•i tráº¡ng thÃ¡i
        for (int i = 0; i < 10; i++) {
            boolean repeatedCall = auctionService.settleIfEnded(auction.getAuctionId());
            assertFalse(repeatedCall, "Láº§n gá»i thá»© " + (i + 2) + " pháº£i tráº£ vá» false!");
            assertEquals(AuctionStatus.FINISHED, auction.getStatus());
            assertEquals("BIDDER_WINNER", auction.getWinnerId());
        }
    }

    @Test
    @DisplayName("6. Kiá»ƒm tra Anti-sniping: Tá»± Ä‘á»™ng gia háº¡n khi cÃ³ ngÆ°á»i Ä‘áº·t giÃ¡ á»Ÿ giÃ¢y cuá»‘i")
    void testAntiSniping() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        // PhiÃªn káº¿t thÃºc sau Ä‘Ãºng 20 giÃ¢y (náº±m trong ngÆ°á»¡ng threshold 30s)
        LocalDateTime endTime = now.plusSeconds(20);
        Auction auction = auctionService.createAuction(
                "SELLER_1", "ITEM_1", "Äá»“ng há»“ cá»•",
                100.0, 10.0, now.minusMinutes(5), endTime);

        // Äáº·t giÃ¡ á»Ÿ giÃ¢y thá»© 20 trÆ°á»›c khi káº¿t thÃºc
        auctionService.placeBid(auction.getAuctionId(), "BIDDER_SNIPER", 100.0);

        // Kháº³ng Ä‘á»‹nh: Thá»i gian káº¿t thÃºc Ä‘Ã£ Ä‘Æ°á»£c tá»± Ä‘á»™ng cá»™ng thÃªm 60 giÃ¢y
        assertTrue(auction.getEndTime().isAfter(endTime), "Anti-sniping pháº£i gia háº¡n thÃªm thá»i gian káº¿t thÃºc");
    }

    @Test
    @DisplayName("7. Kiá»ƒm tra Auto-bidding: Tá»± Ä‘á»™ng nÃ¢ng giÃ¡ pháº£n há»“i khi bá»‹ vÆ°á»£t máº·t")
    void testAutoBidding() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = auctionService.createAuction(
                "SELLER_1", "ITEM_1", "iPad Pro",
                100.0, 10.0, now.minusMinutes(1), now.plusMinutes(10));

        // Bidder A Ä‘áº·t giÃ¡ khá»Ÿi Ä‘iá»ƒm 100.0
        auctionService.placeBid(auction.getAuctionId(), "BIDDER_A", 100.0);

        // Bidder B Ä‘Äƒng kÃ½ Auto-bid vá»›i háº¡n má»©c tráº§n 200.0
        auctionService.registerAutoBid(auction.getAuctionId(), "BIDDER_B", 200.0);

        // Ngay sau khi Bidder B Ä‘Äƒng kÃ½, há»‡ thá»‘ng tá»± Ä‘á»™ng Ä‘áº·t giÃ¡ 110.0 cho Bidder B
        assertEquals("BIDDER_B", auction.getHighestBid().getBidderId());
        assertEquals(110.0, auction.getCurrentPrice());

        // Bidder A cá»‘ gáº¯ng Ä‘áº·t 130.0 -> Há»‡ thá»‘ng tá»± Ä‘á»™ng nÃ¢ng cho Bidder B lÃªn 140.0
        auctionService.placeBid(auction.getAuctionId(), "BIDDER_A", 130.0);
        assertEquals("BIDDER_B", auction.getHighestBid().getBidderId());
        assertEquals(140.0, auction.getCurrentPrice());
    }

    @Test
    @DisplayName("8. KIá»‚M THá»¬ ÄA LUá»’NG: 20 luá»“ng cÃ¹ng Ä‘áº·t giÃ¡ Ä‘á»“ng thá»i (Slide 7 - Chá»‘ng Lost Update)")
    void testConcurrentBidding() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = auctionService.createAuction(
                "SELLER_1", "ITEM_1", "iPhone 15 Pro Max",
                100.0, 10.0, now.minusMinutes(1), now.plusMinutes(10));

        int threadCount = 20;
        // Sá»­ dá»¥ng ExecutorService chuáº©n theo Slide 7 trang 26 & 42
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successfulBids = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    // Chá» táº¥t cáº£ luá»“ng sáºµn sÃ ng rá»“i xuáº¥t phÃ¡t Ä‘á»“ng thá»i táº¡i mili-giÃ¢y thá»© 0
                    startLatch.await();
                    double bidAmount = 100.0 + (index + 1) * 10.0;
                    auctionService.placeBid(auction.getAuctionId(), "BIDDER_" + index, bidAmount);
                    successfulBids.incrementAndGet();
                } catch (Exception ignored) {
                    // Nhá»¯ng luá»“ng gá»­i giÃ¡ khÃ´ng há»£p lá»‡ hoáº·c cháº­m chÃ¢n sáº½ bá»‹ báº¯t exception an toÃ n
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown(); // PhÃ¡t lá»‡nh cháº¡y Ä‘á»“ng loáº¡t
        doneLatch.await();      // Chá» hoÃ n táº¥t

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Kháº³ng Ä‘á»‹nh: Dá»¯ liá»‡u khÃ´ng bá»‹ há»ng (No lost update), giÃ¡ hiá»‡n táº¡i luÃ´n khá»›p vá»›i HighestBid
        assertNotNull(auction.getHighestBid());
        assertEquals(auction.getCurrentPrice(), auction.getHighestBid().getAmount());
        assertTrue(successfulBids.get() > 0, "Ãt nháº¥t pháº£i cÃ³ cÃ¡c lÆ°á»£t Ä‘áº·t giÃ¡ thÃ nh cÃ´ng an toÃ n");
    }
}
