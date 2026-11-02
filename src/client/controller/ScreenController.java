package client.controller;

public interface ScreenController {

    // Được gọi khi màn hình vừa hiện. data = dữ liệu truyền từ màn trước
    // (ví dụ: id phiên đấu giá khi bấm từ danh sách sang chi tiết).
    default void onShow(Object data) { }

    // Được gọi trước khi rời màn hình. Sau này dùng để HỦY ĐĂNG KÝ
    // nhận realtime, tránh màn cũ vẫn nhận tin của phiên khác.
    default void onHide() { }
}