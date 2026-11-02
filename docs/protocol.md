# Network Protocol Specification

## 1. Overview
Hệ thống sử dụng Java Object Serialization qua TCP Socket để giao tiếp giữa Client và Server.

- **Port mặc định**: `5050`
- **Host mặc định**: `127.0.0.1`
- **Giao thức**: `Request` - `Response` cho RPC, `RoomEvent` cho realtime broadcast qua Observer pattern.

## 2. Envelope & Data Transfer Objects (DTO)

### 2.1. Request Envelope
```java
public record Request(
    UUID requestId,
    Command command,
    Serializable payload
) implements Serializable {}
```

### 2.2. Response Envelope
```java
public record Response(
    UUID requestId,
    boolean success,
    Serializable data,
    String errorCode,
    String message
) implements Serializable {}
```

### 2.3. Room Event
```java
public record RoomEvent(
    EventType type,
    UUID auctionId,
    long version,
    AuctionSnapshot snapshot
) implements Serializable {}
```

## 3. Danh sách Command
- `REGISTER`
- `LOGIN`
- `LOGOUT`
- `LIST_AUCTIONS`
- `GET_AUCTION`
- `JOIN_ROOM`
- `LEAVE_ROOM`
- `PLACE_BID`
- `CREATE_AUCTION`
- `CANCEL_AUCTION`
- `CONFIRM_PAYMENT`
- `LIST_MY_ITEMS`
- `CREATE_ITEM`
- `UPDATE_ITEM`
- `DELETE_ITEM`
- `LIST_USERS`
- `BAN_USER`
- `UNBAN_USER`

## 4. Mã lỗi chuẩn (ErrorCode)
- `INVALID_BID`: Giá đặt không hợp lệ hoặc thấp hơn giá quy định.
- `AUCTION_CLOSED`: Phiên đấu giá đã kết thúc hoặc bị hủy.
- `UNAUTHENTICATED`: Chưa đăng nhập hoặc session hết hạn.
- `FORBIDDEN`: Không có quyền thực hiện hành động.
- `NOT_FOUND`: Không tìm thấy tài nguyên (ID sai).
- `INVALID_REQUEST`: Định dạng gói tin hoặc payload không hợp lệ.
- `INTERNAL_ERROR`: Lỗi nội bộ hệ thống trên server.

