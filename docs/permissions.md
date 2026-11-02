# Ma Trận Quyền và Chính Sách Phân Quyền (Permissions Matrix)

## 1. Vai trò người dùng (Roles)
- **ADMIN**: Quản trị viên hệ thống (xem danh sách user, ban/unban user, hủy phiên vi phạm).
- **SELLER**: Người bán (tạo/sửa/xóa sản phẩm của mình, tạo/hủy phiên đấu giá chưa diễn ra).
- **BIDDER**: Người tham gia đấu giá (xem danh sách, vào phòng, đặt giá, xác nhận thanh toán khi thắng).

## 2. Ma trận phân quyền theo lệnh (Command Matrix)

| Command | ADMIN | SELLER | BIDDER | Anonymous |
|---------|-------|--------|--------|-----------|
| REGISTER | - | Cho phép | Cho phép | Cho phép |
| LOGIN | Cho phép | Cho phép | Cho phép | Cho phép |
| LOGOUT | Cho phép | Cho phép | Cho phép | - |
| LIST_AUCTIONS | Cho phép | Cho phép | Cho phép | - |
| GET_AUCTION | Cho phép | Cho phép | Cho phép | - |
| JOIN_ROOM | Cho phép | Cho phép | Cho phép | - |
| LEAVE_ROOM | Cho phép | Cho phép | Cho phép | - |
| PLACE_BID | - | - | Cho phép | - |
| CREATE_AUCTION | - | Cho phép | - | - |
| CANCEL_AUCTION | Cho phép | Chủ sở hữu | - | - |
| CONFIRM_PAYMENT | - | - | Người thắng | - |
| LIST_MY_ITEMS | - | Cho phép | - | - |
| CREATE_ITEM | - | Cho phép | - | - |
| UPDATE_ITEM | - | Chủ sở hữu | - | - |
| DELETE_ITEM | - | Chủ sở hữu | - | - |
| LIST_USERS | Cho phép | - | - | - |
| BAN_USER | Cho phép | - | - | - |
| UNBAN_USER | Cho phép | - | - | - |

