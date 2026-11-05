# Báo Cáo Kiểm Thử (Test Report)

## Mục lục
1. Test tự động (Unit Test / Codec / DAO)
2. Test tích hợp mạng (Socket / Concurrency)
3. Báo cáo độ phủ JaCoCo (Target: >= 60% LINE COVERAGE)

## Nhật ký kiểm thử mẫu
| STT | Bước tái hiện | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả thực tế | Pass/Fail | Commit | Người kiểm tra |
|---|---|---|---|---|---|---|---|
| 1 | Test serialize & deserialize Request | Request hợp lệ | Object giữ nguyên trạng thái | Trùng khớp | PASS | - | TV2 |
| 2 | Đặt giá thấp hơn bước giá | Bid amount < minIncrement | Báo lỗi INVALID_BID | INVALID_BID | PASS | - | TV1/TV2 |

