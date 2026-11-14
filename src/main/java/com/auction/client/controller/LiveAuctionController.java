package com.auction.client.controller;

import com.auction.client.model.AuctionItemDisplay;
import com.auction.client.model.BidRecord;
import com.auction.client.model.ClientSession;
import com.auction.client.model.MockAuctionStore;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.SceneNavigator;
import com.auction.client.util.Screen;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class LiveAuctionController implements ScreenController {

    @FXML private Label roomTitleLabel;
    @FXML private Label roomSubtitleLabel;
    @FXML private Label sessionRoleBadge;

    @FXML private Label livePriceLabel;
    @FXML private Label stepPriceLabel;
    @FXML private Label leadingBidderLabel;

    @FXML private VBox bidderActionArea;
    @FXML private TextField bidAmountField;
    @FXML private Button placeBidButton;
    @FXML private HBox bidMessageBox;
    @FXML private Label bidMessageLabel;

    @FXML private VBox monitorOnlyArea;
    @FXML private Label monitorMessageLabel;
    @FXML private Button adminCancelAuctionBtn;

    @FXML private LineChart<String, Number> priceLineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private Label statusBadge;
    @FXML private Label liveTimerLabel;
    @FXML private Label antiSnipingNotice;

    @FXML private VBox winnerBannerBox;
    @FXML private Label winnerTextLabel;

    @FXML private Label bidHistoryCountLabel;
    @FXML private TableView<BidRecord> historyTable;
    @FXML private TableColumn<BidRecord, String> timeCol;
    @FXML private TableColumn<BidRecord, String> bidderCol;
    @FXML private TableColumn<BidRecord, String> amountCol;
    @FXML private TableColumn<BidRecord, String> statusCol;

    private AuctionItemDisplay currentItem;
    private Timeline liveTimerTimeline;
    private final ObservableList<BidRecord> bidHistory = FXCollections.observableArrayList();
    private XYChart.Series<String, Number> chartSeries;
    private int currentBidIndex = 0;
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
    private boolean isFormattingText = false;

    @Override
    public void onShow(Object data) {
        stopTimer();

        if (data instanceof AuctionItemDisplay item) {
            this.currentItem = item;
            initTableColumns();
            initPriceFormatter();
            initChart(item);
            loadItemData(item);
        }
    }

    @Override
    public void onHide() {
        stopTimer();
    }

    private void stopTimer() {
        if (liveTimerTimeline != null) {
            liveTimerTimeline.stop();
            liveTimerTimeline = null;
        }
    }

    private void initTableColumns() {
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        bidderCol.setCellValueFactory(new PropertyValueFactory<>("bidder"));
        amountCol.setCellValueFactory(row -> new SimpleStringProperty(currencyFormatter.format(row.getValue().getAmount())));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Định dạng màu sắc sinh động cho cột trạng thái
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("DẪN ĐẦU".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
                    } else if ("TRÚNG GIÁ".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #facc15; -fx-font-weight: bold;");
                    } else { // "BỊ VƯỢT"
                        setStyle("-fx-text-fill: #94a3b8;");
                    }
                }
            }
        });

        historyTable.setItems(bidHistory);
    }

    private void initPriceFormatter() {
        // Tự động thêm dấu chấm phân cách hàng nghìn (VD: 940.000.000) khi gõ phím
        bidAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isFormattingText || newVal == null || newVal.isEmpty()) return;

            String clean = newVal.replaceAll("[^0-9]", "");
            if (clean.isEmpty()) return;

            try {
                long val = Long.parseLong(clean);
                String formatted = currencyFormatter.format(val);
                if (!newVal.equals(formatted)) {
                    isFormattingText = true;
                    Platform.runLater(() -> {
                        bidAmountField.setText(formatted);
                        bidAmountField.positionCaret(formatted.length());
                        isFormattingText = false;
                    });
                }
            } catch (NumberFormatException ignored) {}
        });
    }

    private void initChart(AuctionItemDisplay item) {
        // 1. Cấu hình trục Y: Không ép bắt đầu từ 0 để thấy rõ biên độ biến động giá
        yAxis.setForceZeroInRange(false);
        yAxis.setAutoRanging(true);

        // Định dạng nhãn trục Y thành đơn vị triệu/tỷ để gọn gàng dễ đọc
        yAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number object) {
                long val = object.longValue();
                if (val >= 1_000_000_000L) {
                    return String.format(Locale.US, "%.2f Tỷ", val / 1_000_000_000.0);
                } else if (val >= 1_000_000L) {
                    return (val / 1_000_000L) + " Tr";
                } else {
                    return currencyFormatter.format(val);
                }
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });

        // 2. Cấu hình Series dữ liệu
        priceLineChart.getData().clear();
        chartSeries = new XYChart.Series<>();
        chartSeries.setName("Mức giá (VNĐ)");

        currentBidIndex = 0;
        chartSeries.getData().add(new XYChart.Data<>("Khởi điểm", item.getStartingPrice()));

        if (item.getCurrentPrice() > item.getStartingPrice()) {
            currentBidIndex = 1;
            chartSeries.getData().add(new XYChart.Data<>("Lượt 1", item.getCurrentPrice()));
        }

        priceLineChart.getData().add(chartSeries);
    }

    private void loadItemData(AuctionItemDisplay item) {
        ClientSession session = ClientSession.getInstance();
        String currentRole = session.getRole() != null ? session.getRole().toUpperCase() : "BIDDER";
        String currentUsername = session.getUsername() != null ? session.getUsername() : "";
        boolean isMyAuction = currentUsername.equalsIgnoreCase(item.getSellerName());
        boolean isAdmin = "ADMIN".equals(currentRole);

        // Header
        roomTitleLabel.setText("PHÒNG ĐẤU GIÁ: " + item.getTitle().toUpperCase());
        roomSubtitleLabel.setText("Mã phiên: #" + item.getCode() + " | Danh mục: " + item.getCategory() + " | Người bán: " + item.getSellerName());
        sessionRoleBadge.setText("VAI TRÒ: " + currentRole);

        // Giá & Thông số
        livePriceLabel.setText(currencyFormatter.format(item.getCurrentPrice()) + " VNĐ");
        stepPriceLabel.setText("+" + currencyFormatter.format(item.getStepPrice()) + " VNĐ");
        leadingBidderLabel.setText(item.getLeadingBidder() != null ? item.getLeadingBidder() : "Chưa có ai đặt giá");

        // Lịch sử ban đầu
        bidHistory.clear();
        if (item.getLeadingBidder() != null) {
            String initialStatus = "FINISHED".equalsIgnoreCase(item.getStatus()) ? "TRÚNG GIÁ" : "DẪN ĐẦU";
            bidHistory.add(new BidRecord(item.getLeadingBidder(), item.getCurrentPrice(), initialStatus));
        }
        updateHistoryCount();
        hideBidMessage();

        // Kiểm tra nếu phiên đã kết thúc (FINISHED) từ trước khi vào phòng
        if ("FINISHED".equalsIgnoreCase(item.getStatus()) || item.getRemainingSeconds() <= 0) {
            applyFinishedStateDirectly(item, isAdmin);
            return;
        }

        // Phân quyền đặt giá theo Role
        if (isAdmin) {
            bidderActionArea.setVisible(false);
            bidderActionArea.setManaged(false);
            monitorOnlyArea.setVisible(true);
            monitorOnlyArea.setManaged(true);
            monitorMessageLabel.setText("Chế độ Giám sát: Bạn là Admin, không được phép tham gia đặt giá.");

            // Hiện nút Hủy phiên cho Admin
            adminCancelAuctionBtn.setVisible(true);
            adminCancelAuctionBtn.setManaged(true);
        } else if (isMyAuction) {
            bidderActionArea.setVisible(false);
            bidderActionArea.setManaged(false);
            monitorOnlyArea.setVisible(true);
            monitorOnlyArea.setManaged(true);
            monitorMessageLabel.setText("Chế độ Theo dõi: Đây là phiên do bạn đăng bán, không được tự đặt giá.");
            adminCancelAuctionBtn.setVisible(false);
            adminCancelAuctionBtn.setManaged(false);
        } else {
            bidderActionArea.setVisible(true);
            bidderActionArea.setManaged(true);
            monitorOnlyArea.setVisible(false);
            monitorOnlyArea.setManaged(false);

            // Gợi ý mức giá hợp lệ tiếp theo (được format có dấu chấm)
            long minValidBid = item.getCurrentPrice() + item.getStepPrice();
            bidAmountField.setText(currencyFormatter.format(minValidBid));
        }

        // Bắt đầu đồng hồ đếm ngược
        startLiveTimer(item);
    }

    private void startLiveTimer(AuctionItemDisplay item) {
        winnerBannerBox.setVisible(false);
        winnerBannerBox.setManaged(false);
        statusBadge.setText("RUNNING");
        statusBadge.setStyle("-fx-background-color: rgba(34,197,94,0.18); -fx-text-fill: #4ade80; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-weight: bold;");

        liveTimerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            item.decrementRemainingSeconds();
            long secs = item.getRemainingSeconds();

            if (secs <= 0) {
                stopTimer();
                item.setStatus("FINISHED");
                handleAuctionEnd();
            } else {
                updateTimerDisplay(secs);
            }
        }));

        liveTimerTimeline.setCycleCount(Animation.INDEFINITE);
        liveTimerTimeline.play();
    }

    private void updateTimerDisplay(long secs) {
        long hours = secs / 3600;
        long minutes = (secs % 3600) / 60;
        long seconds = secs % 60;
        liveTimerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        if (secs > 60) {
            liveTimerLabel.setStyle("-fx-text-fill: #facc15; -fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: monospace;");
        } else if (secs > 15) {
            liveTimerLabel.setStyle("-fx-text-fill: #fb923c; -fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: monospace;");
        } else {
            liveTimerLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 38px; -fx-font-weight: bold; -fx-font-family: monospace;");
        }
    }

    private void applyFinishedStateDirectly(AuctionItemDisplay item, boolean isAdmin) {
        liveTimerLabel.setText("00:00:00");
        liveTimerLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: monospace;");

        statusBadge.setText("FINISHED");
        statusBadge.setStyle("-fx-background-color: rgba(148,163,184,0.18); -fx-text-fill: #94a3b8; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-weight: bold;");

        bidderActionArea.setVisible(false);
        bidderActionArea.setManaged(false);
        monitorOnlyArea.setVisible(true);
        monitorOnlyArea.setManaged(true);
        monitorMessageLabel.setText("Phiên đấu giá này đã kết thúc.");
        adminCancelAuctionBtn.setVisible(false);
        adminCancelAuctionBtn.setManaged(false);

        String winner = item.getWinnerName() != null ? item.getWinnerName() : (item.getLeadingBidder() != null ? item.getLeadingBidder() : "Không có ai đặt giá");
        winnerBannerBox.setVisible(true);
        winnerBannerBox.setManaged(true);
        winnerTextLabel.setText("Người thắng: " + winner + " | Giá chốt cuối: " + currencyFormatter.format(item.getCurrentPrice()) + " VNĐ");
    }

    private void handleAuctionEnd() {
        liveTimerLabel.setText("00:00:00");
        liveTimerLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: monospace;");

        statusBadge.setText("FINISHED");
        statusBadge.setStyle("-fx-background-color: rgba(148,163,184,0.18); -fx-text-fill: #94a3b8; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-weight: bold;");

        placeBidButton.setDisable(true);
        bidAmountField.setDisable(true);

        String winner = currentItem.getLeadingBidder() != null ? currentItem.getLeadingBidder() : "Không có ai đặt giá";
        winnerBannerBox.setVisible(true);
        winnerBannerBox.setManaged(true);
        winnerTextLabel.setText("Người thắng: " + winner + " | Giá chốt cuối: " + currencyFormatter.format(currentItem.getCurrentPrice()) + " VNĐ");

        // Đổi trạng thái dòng đầu tiên thành TRÚNG GIÁ
        if (!bidHistory.isEmpty()) {
            bidHistory.get(0).setStatus("TRÚNG GIÁ");
            historyTable.refresh();
        }
    }

    @FXML
    private void onPlaceBid() {
        // Trường hợp 1: Phiên đã kết thúc mà vẫn bấm đặt giá
        if (currentItem == null || !"RUNNING".equalsIgnoreCase(currentItem.getStatus())) {
            showBidError("Phiên đấu giá đã kết thúc, không thể đặt giá thêm!");
            return;
        }

        String input = bidAmountField.getText() == null ? "" : bidAmountField.getText().trim();

        // Trường hợp 2: Để trống
        if (input.isEmpty()) {
            showBidError("Vui lòng nhập số tiền muốn đặt!");
            return;
        }

        // Trường hợp 3: Nhập chữ hoặc ký tự lạ không parse được
        String cleanNumber = input.replace(".", "").replace(",", "").trim();
        long bidAmount;
        try {
            bidAmount = Long.parseLong(cleanNumber);
        } catch (NumberFormatException e) {
            showBidError("Số tiền không hợp lệ, vui lòng chỉ nhập số!");
            return;
        }

        // Trường hợp 4: Nhập giá <= Giá hiện tại
        if (bidAmount <= currentItem.getCurrentPrice()) {
            showBidError("Mức giá đặt phải lớn hơn giá hiện tại (" + currencyFormatter.format(currentItem.getCurrentPrice()) + " VNĐ)!");
            return;
        }

        // Trường hợp 5: Nhập giá chưa đủ bước giá tối thiểu
        long minRequired = currentItem.getCurrentPrice() + currentItem.getStepPrice();
        if (bidAmount < minRequired) {
            showBidError("Giá đặt tối thiểu hợp lệ phải từ " + currencyFormatter.format(minRequired) + " VNĐ trở lên (Giá hiện tại + 1 bước giá)!");
            return;
        }

        // ĐẶT GIÁ HỢP LỆ VÀ THÀNH CÔNG:
        ClientSession session = ClientSession.getInstance();
        String bidderName = session.getUsername() != null ? session.getUsername() : "bidder_vip";

        // 1. Cập nhật Model & Giá hiển thị
        currentItem.recordNewBid(bidAmount, bidderName);
        livePriceLabel.setText(currencyFormatter.format(bidAmount) + " VNĐ");
        leadingBidderLabel.setText(bidderName);

        // 2. Cập nhật Bảng lịch sử:
        // Tất cả các lượt cũ trước đó đổi thành "BỊ VƯỢT"
        for (BidRecord oldRecord : bidHistory) {
            oldRecord.setStatus("BỊ VƯỢT");
        }
        // Dòng mới nhất ở vị trí đầu tiên mang trạng thái "DẪN ĐẦU"
        bidHistory.add(0, new BidRecord(bidderName, bidAmount, "DẪN ĐẦU"));
        historyTable.refresh();
        updateHistoryCount();

        // 3. Cập nhật Biểu đồ LineChart (Đánh số theo lượt 1, 2, 3...)
        currentBidIndex++;
        chartSeries.getData().add(new XYChart.Data<>("Lượt " + currentBidIndex, bidAmount));

        // 4. Kích hoạt Anti-sniping nếu còn <= 15s
        if (currentItem.getRemainingSeconds() <= 15) {
            currentItem.addRemainingSeconds(30);
            antiSnipingNotice.setText("Anti-sniping: Đã tự động gia hạn thêm +30 giây!");
            antiSnipingNotice.setStyle("-fx-text-fill: #facc15; -fx-font-weight: bold; -fx-font-size: 11px;");
        }

        // 5. Gợi ý mức giá hợp lệ tiếp theo có định dạng chấm
        bidAmountField.setText(currencyFormatter.format(bidAmount + currentItem.getStepPrice()));
        showBidSuccess("Đặt giá thành công: " + currencyFormatter.format(bidAmount) + " VNĐ!");
    }

    @FXML
    private void onQuickBidPlus1() {
        if (currentItem != null) {
            bidAmountField.setText(currencyFormatter.format(currentItem.getCurrentPrice() + currentItem.getStepPrice()));
        }
    }

    @FXML
    private void onQuickBidPlus2() {
        if (currentItem != null) {
            bidAmountField.setText(currencyFormatter.format(currentItem.getCurrentPrice() + currentItem.getStepPrice() * 2));
        }
    }

    @FXML
    private void onQuickBidPlus5() {
        if (currentItem != null) {
            bidAmountField.setText(currencyFormatter.format(currentItem.getCurrentPrice() + currentItem.getStepPrice() * 5));
        }
    }

    @FXML
    private void onAdminCancelAuction() {
        if (currentItem == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy phiên (Admin)");
        alert.setHeaderText("Hủy phiên đấu giá: #" + currentItem.getCode() + " - " + currentItem.getTitle());
        alert.setContentText("Bạn có chắc chắn muốn hủy phiên đấu giá này với quyền Admin không?\nHành động này không thể hoàn tác!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stopTimer();
            currentItem.setStatus("CANCELED");
            MockAuctionStore.remove(currentItem);
            AlertUtil.info("Thành công", "Đã hủy phiên đấu giá #" + currentItem.getCode() + " thành công!");
            SceneNavigator.goTo(Screen.AUCTION_LIST);
        }
    }

    private void updateHistoryCount() {
        bidHistoryCountLabel.setText(bidHistory.size() + " lượt");
    }

    private void showBidError(String msg) {
        bidMessageBox.setVisible(true);
        bidMessageBox.setManaged(true);
        bidMessageBox.setStyle("-fx-background-color: rgba(248,113,113,0.18); -fx-border-color: rgba(248,113,113,0.45); -fx-border-radius: 8; -fx-padding: 8 12;");
        bidMessageLabel.setText(msg);
        bidMessageLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold; -fx-font-size: 12.5px;");
    }

    private void showBidSuccess(String msg) {
        bidMessageBox.setVisible(true);
        bidMessageBox.setManaged(true);
        bidMessageBox.setStyle("-fx-background-color: rgba(52,211,153,0.18); -fx-border-color: rgba(52,211,153,0.45); -fx-border-radius: 8; -fx-padding: 8 12;");
        bidMessageLabel.setText(msg);
        bidMessageLabel.setStyle("-fx-text-fill: #34d399; -fx-font-weight: bold; -fx-font-size: 12.5px;");
    }

    private void hideBidMessage() {
        bidMessageBox.setVisible(false);
        bidMessageBox.setManaged(false);
        bidMessageLabel.setText("");
    }

    @FXML
    private void onLeaveRoom() {
        stopTimer();
        SceneNavigator.goTo(Screen.AUCTION_LIST);
    }
}
