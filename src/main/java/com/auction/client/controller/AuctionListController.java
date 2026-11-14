package com.auction.client.controller;

import com.auction.client.model.AuctionItemDisplay;
import com.auction.client.model.ClientSession;
import com.auction.client.model.MockAuctionStore;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.SceneNavigator;
import com.auction.client.util.Screen;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.*;

public class AuctionListController implements ScreenController {

    @FXML private Button sellerDashboardBtn;
    @FXML private Button adminPanelBtn;
    @FXML private Label welcomeLabel;
    @FXML private Label roleBadge;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private Label resultCountLabel;
    @FXML private FlowPane cardsContainer;

    private final List<AuctionItemDisplay> allAuctions = new ArrayList<>();
    private final Map<AuctionItemDisplay, Label> runningCardTimers = new HashMap<>();
    private Timeline listTimeline;
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void onShow(Object data) {
        stopListTimeline();

        ClientSession session = ClientSession.getInstance();
        String username = session.getUsername() != null ? session.getUsername() : "Khách";
        String role = session.getRole() != null ? session.getRole().toUpperCase() : "BIDDER";

        welcomeLabel.setText("Xin chào, " + username);
        roleBadge.setText(role);

        // Phân quyền hiển thị các nút chức năng trên Header theo Role
        boolean isSeller = "SELLER".equals(role);
        boolean isAdmin = "ADMIN".equals(role);

        sellerDashboardBtn.setVisible(isSeller);
        sellerDashboardBtn.setManaged(isSeller);

        adminPanelBtn.setVisible(isAdmin);
        adminPanelBtn.setManaged(isAdmin);

        // Đổi màu Role Badge tương ứng
        switch (role) {
            case "ADMIN" -> roleBadge.setStyle("-fx-font-size: 10px; -fx-text-fill: #f43f5e; -fx-background-color: rgba(244,63,94,0.18); -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-weight: bold;");
            case "SELLER" -> roleBadge.setStyle("-fx-font-size: 10px; -fx-text-fill: #34d399; -fx-background-color: rgba(52,211,153,0.18); -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-weight: bold;");
            default -> roleBadge.setStyle("-fx-font-size: 10px; -fx-text-fill: #38bdf8; -fx-background-color: rgba(56,189,248,0.18); -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-weight: bold;");
        }

        initFilters();
        loadAuctionData();
    }

    @Override
    public void onHide() {
        stopListTimeline();
    }

    private void stopListTimeline() {
        if (listTimeline != null) {
            listTimeline.stop();
            listTimeline = null;
        }
        runningCardTimers.clear();
    }

    private void initFilters() {
        setupStyledComboBox(statusFilterCombo, Arrays.asList(
                "Tất cả trạng thái",
                "Đang diễn ra (RUNNING)",
                "Sắp diễn ra (OPEN)",
                "Đã kết thúc (FINISHED)"
        ));
        statusFilterCombo.setOnAction(e -> applyFilter());

        setupStyledComboBox(categoryFilterCombo, Arrays.asList(
                "Tất cả danh mục",
                "ELECTRONICS",
                "ART",
                "VEHICLE"
        ));
        categoryFilterCombo.setOnAction(e -> applyFilter());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    private void setupStyledComboBox(ComboBox<String> comboBox, List<String> items) {
        comboBox.setItems(FXCollections.observableArrayList(items));
        comboBox.getSelectionModel().selectFirst();

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12.5px; -fx-font-weight: bold;");
                }
            }
        });

        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #151f32;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #151f32; -fx-text-fill: #cbd5e1; -fx-font-size: 12.5px; -fx-padding: 8 12;");
                }
            }
        });
    }

    private void loadAuctionData() {
        allAuctions.clear();
        allAuctions.addAll(MockAuctionStore.getAll());
        applyFilter();
    }

    private void applyFilter() {
        stopListTimeline();

        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = statusFilterCombo.getSelectionModel().getSelectedItem();
        String selectedCategory = categoryFilterCombo.getSelectionModel().getSelectedItem();

        List<AuctionItemDisplay> filtered = allAuctions.stream().filter(item -> {
            boolean matchKeyword = keyword.isEmpty()
                    || item.getTitle().toLowerCase().contains(keyword)
                    || item.getCode().toLowerCase().contains(keyword);

            boolean matchStatus = selectedStatus == null || selectedStatus.startsWith("Tất cả")
                    || (selectedStatus.contains("RUNNING") && "RUNNING".equalsIgnoreCase(item.getStatus()))
                    || (selectedStatus.contains("OPEN") && "OPEN".equalsIgnoreCase(item.getStatus()))
                    || (selectedStatus.contains("FINISHED") && "FINISHED".equalsIgnoreCase(item.getStatus()));

            boolean matchCategory = selectedCategory == null || selectedCategory.startsWith("Tất cả")
                    || item.getCategory().equalsIgnoreCase(selectedCategory);

            return matchKeyword && matchStatus && matchCategory;
        }).toList();

        renderCards(filtered);
    }

    private void renderCards(List<AuctionItemDisplay> items) {
        cardsContainer.getChildren().clear();
        resultCountLabel.setText("Tìm thấy " + items.size() + " phiên đấu giá phù hợp");

        for (AuctionItemDisplay item : items) {
            cardsContainer.getChildren().add(createAuctionCard(item));
        }

        // Bắt đầu Timeline đếm lùi thời gian thực cho tất cả card RUNNING trên danh sách
        startListTimeline();
    }

    private void startListTimeline() {
        if (runningCardTimers.isEmpty()) return;

        listTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            for (Map.Entry<AuctionItemDisplay, Label> entry : runningCardTimers.entrySet()) {
                AuctionItemDisplay item = entry.getKey();
                Label lbl = entry.getValue();

                item.decrementRemainingSeconds();
                long secs = item.getRemainingSeconds();

                if (secs <= 0) {
                    item.setStatus("FINISHED");
                    lbl.setText("Hết giờ");
                    lbl.setStyle("-fx-text-fill: #f87171; -fx-font-size: 11px; -fx-font-weight: bold;");
                } else {
                    lbl.setText("Còn lại: " + formatShortTimer(secs));
                }
            }
        }));

        listTimeline.setCycleCount(Animation.INDEFINITE);
        listTimeline.play();
    }

    private String formatShortTimer(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private VBox createAuctionCard(AuctionItemDisplay item) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: #151f32; -fx-background-radius: 14; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 14; -fx-padding: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);");

        ClientSession session = ClientSession.getInstance();
        String currentRole = session.getRole() != null ? session.getRole().toUpperCase() : "BIDDER";
        String currentUsername = session.getUsername() != null ? session.getUsername() : "";
        boolean isMyAuction = currentUsername.equalsIgnoreCase(item.getSellerName());
        boolean isAdmin = "ADMIN".equals(currentRole);
        boolean isSeller = "SELLER".equals(currentRole);

        // Status Badge + Category + Mã phiên cố định
        HBox topBox = new HBox(8);
        topBox.setAlignment(Pos.CENTER_LEFT);

        Label statusLbl = new Label(item.getStatus());
        String statusStyle = switch (item.getStatus().toUpperCase()) {
            case "RUNNING" -> "-fx-background-color: rgba(34,197,94,0.15); -fx-text-fill: #4ade80;";
            case "OPEN" -> "-fx-background-color: rgba(234,179,8,0.15); -fx-text-fill: #facc15;";
            default -> "-fx-background-color: rgba(148,163,184,0.15); -fx-text-fill: #94a3b8;";
        };
        statusLbl.setStyle(statusStyle + " -fx-padding: 3 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label catLbl = new Label(item.getCategory());
        catLbl.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label codeLbl = new Label("#" + item.getCode());
        codeLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-family: monospace;");

        topBox.getChildren().addAll(statusLbl, catLbl, codeLbl);

        if (isSeller && isMyAuction) {
            Label mineLbl = new Label("CỦA BẠN");
            mineLbl.setStyle("-fx-background-color: rgba(52,211,153,0.2); -fx-text-fill: #34d399; -fx-padding: 3 6; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: bold;");
            topBox.getChildren().add(mineLbl);
        }

        // Title
        Label titleLbl = new Label(item.getTitle());
        titleLbl.setWrapText(true);
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-min-height: 42px;");

        // Price Information & Countdown Timer trên thẻ Card
        VBox priceBox = new VBox(2);
        priceBox.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8; -fx-padding: 10;");

        HBox priceHeader = new HBox();
        Label priceTitle = new Label("Giá hiện tại (" + item.getBidCount() + " lượt)");
        priceTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timerLbl = new Label();
        if ("RUNNING".equalsIgnoreCase(item.getStatus())) {
            timerLbl.setText("Còn lại: " + formatShortTimer(item.getRemainingSeconds()));
            timerLbl.setStyle("-fx-text-fill: #facc15; -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: monospace;");
            runningCardTimers.put(item, timerLbl);
        } else if ("OPEN".equalsIgnoreCase(item.getStatus())) {
            timerLbl.setText("Sắp mở");
            timerLbl.setStyle("-fx-text-fill: #facc15; -fx-font-size: 11px;");
        } else {
            timerLbl.setText("Đã đóng");
            timerLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        }

        priceHeader.getChildren().addAll(priceTitle, spacer, timerLbl);

        Label priceValue = new Label(currencyFormatter.format(item.getCurrentPrice()) + " VNĐ");
        priceValue.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 16px; -fx-font-weight: bold;");
        priceBox.getChildren().addAll(priceHeader, priceValue);

        // Seller Info
        Label sellerLbl = new Label("Người bán: " + item.getSellerName());
        sellerLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        // Action Buttons
        HBox btnBox = new HBox(8);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(6, 0, 0, 0));

        Button detailBtn = new Button("Chi tiết");
        detailBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(detailBtn, Priority.ALWAYS);
        detailBtn.setStyle("-fx-background-color: #1e2b42; -fx-text-fill: #cbd5e1; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        detailBtn.setOnAction(e -> {
            stopListTimeline();
            SceneNavigator.goTo(Screen.PRODUCT_DETAIL, item);
        });
        btnBox.getChildren().add(detailBtn);

        // Nút hành động chính theo Role
        if (isAdmin) {
            Button monitorBtn = new Button("Giám sát");
            monitorBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(monitorBtn, Priority.ALWAYS);
            monitorBtn.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
            monitorBtn.setOnAction(e -> handleJoinOrMonitor(item));
            btnBox.getChildren().add(monitorBtn);

            if (!"FINISHED".equalsIgnoreCase(item.getStatus())) {
                Button cancelBtn = new Button("Hủy");
                cancelBtn.setTooltip(new Tooltip("Hủy phiên đấu giá này (Admin)"));
                cancelBtn.setStyle("-fx-background-color: rgba(244,63,94,0.2); -fx-text-fill: #f43f5e; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                cancelBtn.setOnAction(e -> handleCancelAuction(item, "Admin"));
                btnBox.getChildren().add(cancelBtn);
            }
        } else if (isSeller && isMyAuction) {
            if ("RUNNING".equalsIgnoreCase(item.getStatus())) {
                Button trackBtn = new Button("Theo dõi");
                trackBtn.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(trackBtn, Priority.ALWAYS);
                trackBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                trackBtn.setOnAction(e -> handleJoinOrMonitor(item));
                btnBox.getChildren().add(trackBtn);
            } else if ("OPEN".equalsIgnoreCase(item.getStatus())) {
                Button cancelBtn = new Button("Hủy phiên");
                cancelBtn.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(cancelBtn, Priority.ALWAYS);
                cancelBtn.setStyle("-fx-background-color: rgba(244,63,94,0.2); -fx-text-fill: #f43f5e; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                cancelBtn.setOnAction(e -> handleCancelAuction(item, "Chủ phiên"));
                btnBox.getChildren().add(cancelBtn);
            } else {
                Button closedBtn = new Button("Đã kết thúc");
                closedBtn.setDisable(true);
                closedBtn.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(closedBtn, Priority.ALWAYS);
                closedBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #64748b; -fx-background-radius: 8;");
                btnBox.getChildren().add(closedBtn);
            }
        } else {
            Button joinBtn = new Button("Vào đấu giá");
            joinBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(joinBtn, Priority.ALWAYS);

            if ("RUNNING".equalsIgnoreCase(item.getStatus())) {
                joinBtn.setStyle("-fx-background-color: linear-gradient(to right, #0099ff, #2563eb); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                joinBtn.setOnAction(e -> handleJoinOrMonitor(item));
            } else {
                joinBtn.setDisable(true);
                joinBtn.setText("OPEN".equalsIgnoreCase(item.getStatus()) ? "Chưa mở" : "Đã kết thúc");
                joinBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #475569; -fx-background-radius: 8;");
            }
            btnBox.getChildren().add(joinBtn);
        }

        card.getChildren().addAll(topBox, titleLbl, priceBox, sellerLbl, btnBox);
        return card;
    }

    private void handleJoinOrMonitor(AuctionItemDisplay item) {
        try {
            stopListTimeline();
            SceneNavigator.goTo(Screen.LIVE_AUCTION, item);
        } catch (Exception ex) {
            AlertUtil.info("Phòng đấu giá trực tiếp (STT 7 & 8)",
                    "Màn hình Đấu giá trực tiếp đang được kết nối.\nBạn đang chọn phiên: #" + item.getCode() + " - " + item.getTitle());
        }
    }

    private void handleCancelAuction(AuctionItemDisplay item, String requester) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy phiên");
        alert.setHeaderText("Hủy phiên: #" + item.getCode() + " - " + item.getTitle());
        alert.setContentText("Bạn có chắc chắn muốn hủy phiên đấu giá này với quyền " + requester + " không?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            MockAuctionStore.remove(item);
            allAuctions.remove(item);
            applyFilter();
            AlertUtil.info("Thành công", "Đã hủy phiên đấu giá #" + item.getCode() + ": " + item.getTitle());
        }
    }

    @FXML
    private void onOpenSellerDashboard() {
        AlertUtil.info("Seller Dashboard (STT 11 của TV4)",
                "Khu vực CRUD sản phẩm và tạo phiên do TV4 phụ trách.\nKhi TV4 hoàn thiện màn hình, nút này sẽ chuyển trực tiếp sang Seller Dashboard!");
    }

    @FXML
    private void onOpenAdminPanel() {
        AlertUtil.info("Admin Panel (STT 13 của TV4)",
                "Khu vực Quản trị Người dùng (Ban / Unban) do TV4 phụ trách.\nKhi TV4 hoàn thiện màn hình, nút này sẽ chuyển trực tiếp sang Admin Dashboard!");
    }

    @FXML
    private void onRefresh() {
        loadAuctionData();
    }

    @FXML
    private void onLogout() {
        stopListTimeline();
        ClientSession.getInstance().logout();
        SceneNavigator.goTo(Screen.LOGIN);
    }
}
