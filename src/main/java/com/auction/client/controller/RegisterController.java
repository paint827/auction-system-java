package com.auction.client.controller;

import com.auction.client.model.MockUserStore;
import com.auction.client.util.SceneNavigator;
import com.auction.client.util.Screen;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

    @Override
    public void onShow(Object data) {
        // Làm mới form khi mở lại màn đăng ký
        if (usernameField != null) usernameField.clear();
        if (passwordField != null) passwordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        if (statusLabel != null) statusLabel.setText("");
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

        // Kiểm tra trùng username
        if (MockUserStore.exists(username)) {
            showError("Tên đăng nhập '" + username + "' đã tồn tại! Vui lòng chọn tên khác.");
            return;
        }

        // Chuyển đổi role sang mã chuẩn hệ thống
        String roleCode = (selectedRole != null && selectedRole.contains("SELLER")) ? "SELLER" : "BIDDER";

        // Lưu vào MockUserStore để màn Đăng nhập có thể xác thực được ngay
        boolean registered = MockUserStore.register(username, password, roleCode);
        if (!registered) {
            showError("Không thể đăng ký tài khoản lúc này, vui lòng thử lại!");
            return;
        }

        // 2. Thông báo thành công
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đăng ký thành công");
        alert.setHeaderText(null);
        alert.setContentText("Chúc mừng bạn đã tạo tài khoản [" + username + "] thành công!\nHệ thống sẽ chuyển về màn hình Đăng nhập.");
        alert.showAndWait();

        // 3. Quay lại màn hình Đăng nhập (Screen.LOGIN) và truyền username vừa tạo sang
        SceneNavigator.goTo(Screen.LOGIN, username);
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
