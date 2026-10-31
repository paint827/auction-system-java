# Online Auction System (Hệ thống Đấu giá Trực tuyến)
> Bài tập lớn Lập trình nâng cao (LTNC) – Học kỳ II, 2025–2026

## 1. Phân công vai trò & Trách nhiệm
* **TV1 (Trưởng nhóm - Core Engine & Concurrency)**: 
  - `server/core/`: `Auction`, `Bid`, `AuctionManager` (Singleton), State Machine (`OPEN -> RUNNING -> FINISHED -> PAID/CANCELED`), Concurrency Locking (`ReentrantLock`), Anti-sniping, Auto-bidding.
  - `common/exception/`: `InvalidBidException`, `AuctionClosedException`.
  - Chủ trì Class Diagram, Concurrency Test, E2E Test.
* **TV2 (Network, Serialization & DevOps)**:
  - `server/network/`: `AuctionServer`, `ClientHandler`, Timer đếm ngược, `RealtimeHub` (Observer Pattern).
  - `server/dao/`: Serialization file storage (`.dat`).
  - `common/protocol/`: DTOs (`Request`, `Response`, `ActionType`).
  - DevOps: Maven `pom.xml`, Checkstyle, JaCoCo, GitHub Actions CI.
* **TV3 (GUI chính & Phòng đấu giá Realtime)**:
  - `client/controller/`: `AuctionListController`, `ItemDetailController`, `LiveAuctionController` (Realtime bid & LineChart).
  - `client/network/`: `NetworkClient`, `ClientSocketListener`.
  - `resources/view/`: FXML screens & CSS styling.
* **TV4 (Domain, Services & GUI phụ)**:
  - `server/domain/`: `User` (Bidder, Seller, Admin), `Item` (Electronics, Art, Vehicle), `ItemFactory` (Factory Pattern).
  - `server/service/`: `AuthService`, `UserService`, `ItemService`, cơ chế phân quyền, `HashUtil`.
  - `client/controller/`: Màn hình Đăng nhập, Đăng ký, Seller Dashboard, Admin Dashboard.

## 2. Cấu trúc thư mục (Directory Layout)
```text
online-auction-system/
├── .github/workflows/       # GitHub Actions CI
├── config/                  # Checkstyle configuration
├── data/                    # Serialization storage (.dat)
├── docs/                    # Tài liệu kiến trúc, diagrams
├── src/
│   ├── main/
│   │   ├── java/com/auction/
│   │   │   ├── common/      # DTOs, Exceptions, Utils (Shared)
│   │   │   ├── server/      # Core Engine, Domain, Services, Sockets, DAO
│   │   │   └── client/      # JavaFX MVC (NetworkClient, Controllers)
│   │   └── resources/       # FXML, CSS, Images
│   └── test/java/com/auction/ # Unit test (Core, Domain, Network) & E2E
├── .gitignore
├── pom.xml
└── README.md
```

## 3. Quy tắc làm việc trên Git
1. Mỗi người một nhánh `feature/<ten>-<viec>`; tạo Pull Request vào `dev`.
2. Mỗi Pull Request cần ít nhất 1 thành viên review (TV1 ↔ TV2, TV3 ↔ TV4).
3. Commit theo chuẩn Conventional Commits (`feat:`, `fix:`, `test:`, `docs:`, `chore:`).
4. Cấu hình đúng `user.name` và `user.email` khớp tài khoản GitHub.
