package client.controller;

import client.model.ClientSession;
import client.util.SceneNavigator;
import client.util.Screen;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController implements ScreenController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void onLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Vui lòng điền đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        // Mock role theo ma trận phân quyền: ADMIN, SELLER, BIDDER
        String role = resolveMockRole(username, password);

        if (role != null) {
            errorLabel.setText("");
            // Gọi đúng hàm của kiến trúc hiện tại
            ClientSession.getInstance().login(username, role);
            // Chuyển sang màn Danh sách phiên
            SceneNavigator.goTo(Screen.AUCTION_LIST);
        } else {
            errorLabel.setText("Sai tài khoản/mật khẩu! (Thử pass: 123 với user: admin, seller, bidder)");
        }
    }

    private String resolveMockRole(String username, String password) {
        // Tạm thời quy ước mật khẩu là 123 cho môi trường test Client
        if (!"123".equals(password)) {
            return null;
        }

        return switch (username.toLowerCase()) {
            case "admin" -> "ADMIN";
            case "seller" -> "SELLER";
            default -> "BIDDER";
        };
    }

    @FXML
    private void onOpenRegister() {
        SceneNavigator.goTo(Screen.REGISTER);
    }
}