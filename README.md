# Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

> Bài tập lớn môn Lập trình nâng cao (LTNC 2026)  
> Công nghệ: **Java 25**, **JavaFX 25**, **Maven**, **TCP Socket & Serialization**  
> Root Package chuẩn: **`com.auction`**

---

## 1. Yêu cầu môi trường
- **JDK**: Java 25 (Đã cấu hình trong `pom.xml` với `<maven.compiler.release>25</maven.compiler.release>`).
- **Maven**: 3.9.x trở lên.
- **Biến môi trường**: `JAVA_HOME` trỏ tới thư mục cài đặt JDK 25.

---

## 2. Cấu trúc thư mục dự án

```text
auction-system-java/
├── .github/
│   └── workflows/
│       └── ci.yml               # Workflow GitHub Actions tự động verify mỗi PR/push
├── config/
│   └── checkstyle.xml          # Quy ước chuẩn mã nguồn nhóm (Checkstyle 3.6.0)
├── data/
│   └── .gitkeep                # Thư mục lưu snapshot trạng thái hệ thống (snapshot.dat)
├── docs/
│   ├── protocol.md             # Hợp đồng giao thức mạng (Request, Response, Event)
│   ├── permissions.md          # Ma trận phân quyền các command theo vai trò
│   └── test-report.md          # Báo cáo kịch bản kiểm thử và độ phủ
├── src/
│   ├── main/
│   │   ├── java/com/auction/
│   │   │   ├── client/         # Phía Client (JavaFX + MVC)
│   │   │   │   ├── controller/ # [TV3, TV4] Controllers điều khiển GUI (Login, Register...)
│   │   │   │   ├── model/      # [TV3, TV4] Model phía Client (ClientSession...)
│   │   │   │   ├── network/    # [TV3] NetworkClient giao tiếp Socket
│   │   │   │   ├── util/       # [TV3] SceneNavigator, Screen, AlertUtil
│   │   │   │   ├── viewmodel/  # [TV3] ViewModel cho UI
│   │   │   │   └── ClientApp.java # [TV3] Điểm khởi chạy JavaFX Client
│   │   │   ├── common/         # Module dùng chung cả Client & Server
│   │   │   │   ├── dto/        # [TV2] Request, Response, Payload, Snapshot DTO
│   │   │   │   ├── exception/  # [TV1, TV4] Ngoại lệ nghiệp vụ
│   │   │   │   ├── model/      # [TV1: Auction, Bid; TV4: User, Item]
│   │   │   │   ├── protocol/   # [TV2] Command, EventType, ErrorCode, AuctionObserver
│   │   │   │   └── util/       # [TV4, TV2] Tiện ích dùng chung (HashUtil băm SHA-256)
│   │   │   └── server/         # Phía Server (TCP Socket & Engine)
│   │   │       ├── core/       # [TV1: Core Engine] Concurrency Locking, State Machine, Anti-sniping
│   │   │       ├── dao/        # [TV2] SnapshotDao, SystemSnapshot lưu trữ file (snapshot.dat)
│   │   │       ├── network/    # [TV2] ServerSocket, ClientHandler đa luồng, RealtimeHub
│   │   │       ├── service/    # [TV1: AuctionService; TV4: AuthService, ItemService]
│   │   │       └── ServerMain.java # [TV2] Điểm khởi chạy Server
│   │   └── resources/
│   │       ├── fxml/           # Giao diện FXML (LoginView, RegisterView, main.fxml)
│   │       ├── css/            # Stylesheet giao diện
│   │       └── images/         # Hình ảnh logo và icon
│   └── test/
│       └── java/com/auction/   # Unit tests & Integration tests
│           ├── SampleTest.java
│           └── common/
│               └── ProtocolCodecTest.java
├── .gitignore
├── pom.xml                     # Cấu hình Maven, Dependencies và Plugins cố định version
└── README.md
```

---

## 3. Phân công trách nhiệm theo Module

| Thư mục / Gói | Thành viên phụ trách | Nội dung chính |
|---|---|---|
| `common/dto`, `common/protocol` | **TV2** | Request, Response, Event, payload, mã lỗi, Observer interface |
| `common/exception`, `common/model` | **TV1**, **TV4** | Model Auction/Bid (TV1); User/Item (TV4); Custom Exceptions |
| `common/util` | **TV4**, **TV2** | Băm mật khẩu (HashUtil), tiện ích format dữ liệu dùng chung |
| `server/core` | **TV1** | Concurrency Locking, State Machine, Anti-sniping, Auto-bidding |
| `server/network`, `server/dao` | **TV2** | ServerSocket, ClientHandler đa luồng, RealtimeHub broadcast, SnapshotDao |
| `server/service` | **TV1**, **TV4** | AuctionService/Manager (TV1); AuthService, UserService, ItemService (TV4) |
| `client/network`, `client/controller` | **TV3**, **TV4** | Kết nối mạng & GUI chính (TV3); Form Login/Register/Quản lý (TV4) |
| `client/model`, `client/util` | **TV3**, **TV4** | ClientSession, SceneNavigator điều hướng màn hình |
| `client/viewmodel`, `resources/fxml` | **TV3** | Khung MVC/MVVM; các file fxml màn hình |

---

## 4. Hướng dẫn dòng lệnh (Commands)

### 4.1. Biên dịch, Kiểm tra quy ước mã và Coverage
```bash
mvn clean verify
```

### 4.2. Khởi chạy Server (Terminal 1)
```bash
mvn exec:java -Dexec.mainClass=com.auction.server.ServerMain
```
> Tùy chỉnh cổng: `-Dauction.port=5050`  
> Tùy chỉnh file lưu trữ: `-Dauction.file=data/snapshot.dat` (mặc định: `data/snapshot.dat`)

### 4.3. Khởi chạy Client JavaFX (Terminal 2, 3...)
```bash
mvn javafx:run
```

### 4.4. Chạy Unit Test
```bash
mvn test
```
Chạy cụ thể class test:
```bash
mvn -Dtest=ProtocolCodecTest test
```
