package com.auction.client.controller;

import com.auction.client.model.ClientSession;
import com.auction.client.model.MockUserStore;
import com.auction.client.util.SceneNavigator;
import com.auction.client.util.Screen;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController implements ScreenController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @Override
    public void onShow(Object data) {
        passwordField.clear();

        // Nếu nhận dữ liệu từ màn Đăng ký chuyển về (tự động điền username)
        if (data instanceof String registeredUsername) {
            usernameField.setText(registeredUsername);
            errorLabel.setStyle("-fx-text-fill: #34d399; -fx-font-size: 12px;");
            errorLabel.setText("Đăng ký thành công! Vui lòng nhập mật khẩu để đăng nhập.");
        } else {
            errorLabel.setText("");
        }
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng điền đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        // 1. Kiểm tra tài khoản có tồn tại không
        if (!MockUserStore.exists(username)) {
            showError("Tài khoản này chưa tồn tại");
            return;
        }

        // 2. Tài khoản tồn tại nhưng sai mật khẩu
        MockUserStore.UserInfo user = MockUserStore.authenticate(username, password);
        if (user == null) {
            showError("Sai tài khoản hoặc mật khẩu, xin vui lòng kiểm tra lại");
            return;
        }

        // 3. Đăng nhập thành công
        errorLabel.setText("");
        ClientSession.getInstance().login(user.username(), user.role());
        // Chuyển sang màn Danh sách phiên
        SceneNavigator.goTo(Screen.AUCTION_LIST);
    }

    private void showError(String msg) {
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");
        errorLabel.setText(msg);
    }

    @FXML
    private void onOpenRegister() {
        SceneNavigator.goTo(Screen.REGISTER);
    }
}
