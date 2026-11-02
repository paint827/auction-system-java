package client.util;

public enum Screen {
    LOGIN("LoginView.fxml", "Đăng nhập"),
    REGISTER("RegisterView.fxml", "Tạo tài khoản"),
    AUCTION_LIST("AuctionListView.fxml", "Danh sách phiên đấu giá"),
    LIVE_AUCTION("LiveAuctionView.fxml", "Đấu giá trực tiếp");

    public static final String BASE_PATH = "/client/view/";

    private final String fxml;
    private final String title;

    Screen(String fxml, String title) {
        this.fxml = fxml;
        this.title = title;
    }

    public String getFxml() { return fxml; }
    public String getTitle() { return title; }

    /**
     * Trả về đường dẫn đầy đủ từ classpath để nạp FXML
     */
    public String getFullPath() {
        return BASE_PATH + fxml;
    }
}