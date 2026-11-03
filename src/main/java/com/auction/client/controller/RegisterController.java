package com.auction.client.controller;

import com.auction.client.model.ClientSession;
import com.auction.client.util.SceneNavigator;
import com.auction.client.util.Screen;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController implements ScreenController {

    @FXML private TextField usernameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(
                "Người tham gia đấu giá (BIDDER)",
                "Người bán tài sản (SELLER)"
        ));
        roleComboBox.setValue("Người tham gia đấu giá (BIDDER)");

        // Định kiểu chữ màu trắng sáng cho ô ComboBox khi hiển thị trên nền tối
        roleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
                }
            }
        });
    }

    @FXML
    private void onRegister() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText().trim();
        String selectedRole = roleComboBox.getValue();

        // 1. Kiểm tra validation cơ bản phía Client
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Vui lòng nhập đầy đủ tất cả các trường thông tin!");
            return;
        }

        if (username.length() < 3) {
            showError("Tên đăng nhập phải có ít nhất 3 ký tự!");
            return;
        }

        if (password.length() < 3) {
            showError("Mật khẩu phải có độ dài tối thiểu 3 ký tự!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp!");
            return;
        }

        // Chuyển đổi role sang mã chuẩn hệ thống
        String roleCode = selectedRole.contains("SELLER") ? "SELLER" : "BIDDER";

        // 2. Thông báo thành công và chuyển thẳng vào hệ thống
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đăng ký thành công");
        alert.setHeaderText(null);
        alert.setContentText("Chúc mừng " + username + "! Tài khoản vai trò [" + roleCode + "] đã được tạo thành công.");
        alert.showAndWait();

        // Tự động đăng nhập vào ClientSession và chuyển sang màn danh sách phiên
        ClientSession.getInstance().login(username, roleCode);
        SceneNavigator.goTo(Screen.AUCTION_LIST);

        /*
         * Ghi chú tích hợp sau này khi TV2 & TV4 xong:
         * NetworkClient.getInstance().register(username, HashUtil.hash(password), roleCode);
         */
    }

    @FXML
    private void onBackToLogin() {
        SceneNavigator.goTo(Screen.LOGIN);
    }

    private void showError(String msg) {
        statusLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 11.5px;");
        statusLabel.setText(msg);
    }
}