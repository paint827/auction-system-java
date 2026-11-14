package com.auction.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Kho dữ liệu phiên đấu giá cố định phía Client dùng cho kiểm thử.
 * Đảm bảo tính nhất quán (cùng 1 ID khi hiển thị ở Danh sách và Chi tiết, không sinh ngẫu nhiên).
 */
public final class MockAuctionStore {

    private static final List<AuctionItemDisplay> AUCTIONS = new CopyOnWriteArrayList<>();

    static {
        resetData();
    }

    private MockAuctionStore() { }

    public static void resetData() {
        AUCTIONS.clear();

        // Phiên 1: Đang diễn ra (RUNNING) - MacBook
        AUCTIONS.add(new AuctionItemDisplay(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "A001",
                "MacBook Pro 16 M3 Max 36GB",
                "ELECTRONICS",
                "seller",
                60_000_000L,
                68_500_000L,
                1_000_000L,
                "RUNNING",
                1420L,
                "14/11/2026 21:30:00",
                14,
                "bidder_pro",
                null,
                "Máy chính hãng mới 99%, pin sạc 15 lần, bảo hành Apple Care+ đến năm 2027.",
                "Hãng sản xuất: Apple | Bảo hành: 24 tháng | Tình trạng: Like New 99%"
        ));

        // Phiên 2: Sắp mở (OPEN) - Tranh sơn dầu
        AUCTIONS.add(new AuctionItemDisplay(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "A002",
                "Tranh sơn dầu 'Phố Cổ Mùa Thu'",
                "ART",
                "seller",
                15_000_000L,
                15_000_000L,
                500_000L,
                "OPEN",
                86400L,
                "15/11/2026 09:00:00",
                0,
                null,
                null,
                "Tác phẩm độc bản sáng tác năm 2021, có chứng nhận tác quyền và chữ ký của họa sĩ.",
                "Họa sĩ: Bùi Xuân Phái | Năm sáng tác: 2021 | Kích thước: 80x120 cm"
        ));

        // Phiên 3: Đang diễn ra (RUNNING) - Ô tô điện VinFast
        AUCTIONS.add(new AuctionItemDisplay(
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                "A003",
                "VinFast VF8 Plus 2023",
                "VEHICLE",
                "auto_viet",
                800_000_000L,
                890_000_000L,
                10_000_000L,
                "RUNNING",
                620L,
                "14/11/2026 22:00:00",
                26,
                "kien_vip99",
                null,
                "Xe gia đình đi giữ gìn, lăn bánh 1.2 vạn km, pin thuê đã lên gói mới, bảo hiểm thân vỏ đầy đủ.",
                "Số km đã chạy: 12,000 km | Năm sản xuất: 2023 | Nhiên liệu: Điện | Hộp số: Tự động"
        ));

        // Phiên 4: Đã kết thúc (FINISHED) - Đồng hồ cổ
        AUCTIONS.add(new AuctionItemDisplay(
                UUID.fromString("00000000-0000-0000-0000-000000000004"),
                "A004",
                "Đồng hồ cổ Odo 36/10 Pháp",
                "ART",
                "vintage_collector",
                25_000_000L,
                38_000_000L,
                1_000_000L,
                "FINISHED",
                0L,
                "13/11/2026 20:00:00",
                19,
                "dongho_haiphong",
                "dongho_haiphong",
                "Chuông gính gai điệp khúc Westminster, máy vàng óng nguyên bản, thùng bè gỗ sồi nhập Pháp.",
                "Xuất xứ: Pháp | Niên đại: 1950 | Tình trạng: Nguyên bản | Chuông: 10 gông 10 búa"
        ));
    }

    public static List<AuctionItemDisplay> getAll() {
        return new ArrayList<>(AUCTIONS);
    }

    public static void remove(AuctionItemDisplay item) {
        AUCTIONS.removeIf(a -> a.getCode().equalsIgnoreCase(item.getCode()));
    }
}
