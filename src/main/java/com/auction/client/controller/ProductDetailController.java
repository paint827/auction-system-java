package com.auction.client.controller;

import com.auction.client.model.AuctionItemDisplay;
import com.auction.client.model.ClientSession;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.SceneNavigator;
import com.auction.client.util.Screen;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailController implements ScreenController {

    @FXML private Label sessionRoleBadge;
    @FXML private Label productCodeBadge;
    @FXML private Label categoryBadge;
    @FXML private Label specificInfoLabel;
    @FXML private Label sellerLabel;

    @FXML private Label statusLabel;
    @FXML private Label codeHeaderLabel;
    @FXML private Label titleLabel;

    @FXML private Label timeHeaderLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label remainingTimeLabel;

    @FXML private Label priceHeaderLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label stepPriceLabel;
    @FXML private Label startPriceLabel;
    @FXML private Label bidCountLabel;

    @FXML private HBox leaderOrWinnerBox;
    @FXML private Label leaderOrWinnerTitle;
    @FXML private Label leaderOrWinnerValue;

    @FXML private Label descriptionLabel;
    @FXML private Button actionBidButton;

    private AuctionItemDisplay currentItem;
    private Timeline countdownTimeline;
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void onShow(Object data) {
        stopCountdown();

        ClientSession session = ClientSession.getInstance();
        String currentRole = session.getRole() != null ? session.getRole().toUpperCase() : "BIDDER";
        sessionRoleBadge.setText("VAI TRÒ: " + currentRole);

        if (data instanceof AuctionItemDisplay item) {
            this.currentItem = item;
            displayItemDetails(item, session);
        }
    }

    @Override
    public void onHide() {
        // Dọn dẹp Timer khi rời màn hình để giải phóng CPU
        stopCountdown();
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    private void displayItemDetails(AuctionItemDisplay item, ClientSession session) {
        String currentRole = session.getRole() != null ? session.getRole().toUpperCase() : "BIDDER";
        String currentUsername = session.getUsername() != null ? session.getUsername() : "";
        boolean isMyAuction = currentUsername.equalsIgnoreCase(item.getSellerName());
        boolean isAdmin = "ADMIN".equals(currentRole);
        boolean isSeller = "SELLER".equals(currentRole);

        // Cột trái
        productCodeBadge.setText("MÃ PHIÊN: #" + item.getCode());
        categoryBadge.setText("DANH MỤC: " + item.getCategory().toUpperCase());
        specificInfoLabel.setText(item.getSpecificInfo());
        sellerLabel.setText(item.getSellerName());

        // Tiêu đề & Mô tả
        codeHeaderLabel.setText("Phiên #" + item.getCode());
        titleLabel.setText(item.getTitle());
        descriptionLabel.setText(item.getDescription());

        // Bảng giá & Lượt đặt
        currentPriceLabel.setText(currencyFormatter.format(item.getCurrentPrice()) + " VNĐ");
        stepPriceLabel.setText("+" + currencyFormatter.format(item.getStepPrice()) + " VNĐ");
        startPriceLabel.setText(currencyFormatter.format(item.getStartingPrice()) + " VNĐ");
        bidCountLabel.setText(item.getBidCount() + " lượt");

        // Trạng thái, Thời gian & Người dẫn đầu / Người thắng
        statusLabel.setText(item.getStatus());

        if ("RUNNING".equalsIgnoreCase(item.getStatus())) {
            statusLabel.setStyle("-fx-background-color: rgba(34,197,94,0.18); -fx-text-fill: #4ade80; -fx-padding: 4 10; -fx-background-radius: 6; -fx-font-weight: bold;");
            timeHeaderLabel.setText("Thời gian còn lại (Đang trừ trực tiếp)");
            endTimeLabel.setText("Kết thúc lúc: " + item.getEndTimeString());
            remainingTimeLabel.setText(formatRemainingTime(item.getRemainingSeconds()));

            priceHeaderLabel.setText("Giá hiện tại cao nhất");

            leaderOrWinnerBox.setStyle("-fx-background-color: rgba(56,189,248,0.12); -fx-border-color: rgba(56,189,248,0.25); -fx-border-radius: 8; -fx-padding: 8 12;");
            leaderOrWinnerTitle.setText("Người đang dẫn đầu:");
            leaderOrWinnerTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 12.5px; -fx-font-weight: bold;");
            leaderOrWinnerValue.setText(item.getLeadingBidder() != null ? item.getLeadingBidder() : "Chưa có ai đặt giá");

            // Cấu hình nút hành động
            actionBidButton.setDisable(false);
            if (isAdmin) {
                actionBidButton.setText("VÀO PHÒNG GIÁM SÁT (ADMIN)");
                actionBidButton.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 12; -fx-cursor: hand;");
            } else if (isSeller && isMyAuction) {
                actionBidButton.setText("VÀO PHÒNG THEO DÕI (PHIÊN CỦA BẠN)");
                actionBidButton.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 12; -fx-cursor: hand;");
            } else {
                actionBidButton.setText("THAM GIA PHÒNG ĐẤU GIÁ");
                actionBidButton.setStyle("-fx-background-color: linear-gradient(to right, #0099ff, #2563eb); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,153,255,0.4), 16, 0.2, 0, 4);");
            }

            // KÍCH HOẠT BỘ ĐẾM GIỜ THỜI GIAN THỰC (TRỪ TRỰC TIẾP MỖI GIÂY)
            startCountdown(item);

        } else if ("OPEN".equalsIgnoreCase(item.getStatus())) {
            statusLabel.setStyle("-fx-background-color: rgba(234,179,8,0.18); -fx-text-fill: #facc15; -fx-padding: 4 10; -fx-background-radius: 6; -fx-font-weight: bold;");
            timeHeaderLabel.setText("Thời gian mở phiên");
            endTimeLabel.setText("Dự kiến bắt đầu lúc: " + item.getEndTimeString());
            remainingTimeLabel.setText("CHƯA BẮT ĐẦU");

            priceHeaderLabel.setText("Giá khởi điểm công bố");

            leaderOrWinnerBox.setStyle("-fx-background-color: rgba(234,179,8,0.1); -fx-border-color: rgba(234,179,8,0.25); -fx-border-radius: 8; -fx-padding: 8 12;");
            leaderOrWinnerTitle.setText("Trạng thái nhận giá:");
            leaderOrWinnerTitle.setStyle("-fx-text-fill: #facc15; -fx-font-size: 12.5px; -fx-font-weight: bold;");
            leaderOrWinnerValue.setText("Chưa mở phòng trả giá");

            actionBidButton.setDisable(true);
            actionBidButton.setText("PHIÊN ĐẤU GIÁ CHƯA MỞ");
            actionBidButton.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-background-radius: 12;");

        } else { // FINISHED
            applyFinishedState(item);
        }
    }

    private void startCountdown(AuctionItemDisplay item) {
        if (item.getRemainingSeconds() <= 0) {
            applyFinishedState(item);
            return;
        }

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            item.decrementRemainingSeconds();
            long currentSecs = item.getRemainingSeconds();

            if (currentSecs <= 0) {
                stopCountdown();
                item.setStatus("FINISHED");
                applyFinishedState(item);
            } else {
                remainingTimeLabel.setText(formatRemainingTime(currentSecs));
            }
        }));

        countdownTimeline.setCycleCount(Animation.INDEFINITE);
        countdownTimeline.play();
    }

    private void applyFinishedState(AuctionItemDisplay item) {
        statusLabel.setText("FINISHED");
        statusLabel.setStyle("-fx-background-color: rgba(148,163,184,0.18); -fx-text-fill: #94a3b8; -fx-padding: 4 10; -fx-background-radius: 6; -fx-font-weight: bold;");
        timeHeaderLabel.setText("Thời gian kết thúc");
        endTimeLabel.setText("Đã đóng lúc: " + item.getEndTimeString());
        remainingTimeLabel.setText("00:00:00 (ĐÃ ĐÓNG PHIÊN)");

        priceHeaderLabel.setText("Giá trúng đấu giá chính thức");

        leaderOrWinnerBox.setStyle("-fx-background-color: rgba(34,197,94,0.15); -fx-border-color: rgba(34,197,94,0.3); -fx-border-radius: 8; -fx-padding: 8 12;");
        leaderOrWinnerTitle.setText("Người trúng đấu giá:");
        leaderOrWinnerTitle.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 12.5px; -fx-font-weight: bold;");
        leaderOrWinnerValue.setText(item.getWinnerName() != null ? item.getWinnerName() : (item.getLeadingBidder() != null ? item.getLeadingBidder() : "Không có ai đặt giá"));

        actionBidButton.setDisable(true);
        actionBidButton.setText("PHIÊN ĐẤU GIÁ ĐÃ KẾT THÚC");
        actionBidButton.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #64748b; -fx-font-size: 14px; -fx-background-radius: 12;");
    }

    private String formatRemainingTime(long totalSeconds) {
        if (totalSeconds <= 0) return "00:00:00 (Hết giờ)";
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d (%d giây)", hours, minutes, seconds, totalSeconds);
    }

    @FXML
    private void onBackToList() {
        stopCountdown();
        SceneNavigator.goTo(Screen.AUCTION_LIST);
    }

    @FXML
    private void onJoinAuction() {
        if (currentItem != null && "RUNNING".equalsIgnoreCase(currentItem.getStatus())) {
            try {
                stopCountdown();
                SceneNavigator.goTo(Screen.LIVE_AUCTION, currentItem);
            } catch (Exception ex) {
                AlertUtil.info("Phòng đấu giá trực tiếp (STT 7 & 8)",
                        "Màn hình Đấu giá trực tiếp đang được kết nối.\nBạn đang chọn phiên: #" + currentItem.getCode() + " - " + currentItem.getTitle());
            }
        }
    }
}
