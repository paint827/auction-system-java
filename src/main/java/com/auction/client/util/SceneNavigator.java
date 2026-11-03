package com.auction.client.util;

import com.auction.client.controller.ScreenController;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneNavigator {

    private static Stage stage;
    private static ScreenController current; // controller của màn đang hiện

    private SceneNavigator() { }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void goTo(Screen screen) {
        goTo(screen, null);
    }

    public static void goTo(Screen screen, Object data) {
        URL url = SceneNavigator.class.getResource(Screen.BASE_PATH + screen.getFxml());
        if (url == null) {
            throw new IllegalStateException("Không tìm thấy file FXML: " + screen.getFxml());
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            if (current != null) {
                current.onHide(); // báo màn cũ dọn dẹp
            }

            // Dùng lại Scene cũ, chỉ đổi nội dung -> cửa sổ không bị chớp/đổi kích thước
            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root);
            }
            stage.setTitle(screen.getTitle());

            Object controller = loader.getController();
            if (controller instanceof ScreenController sc) {
                current = sc;
                sc.onShow(data);
            } else {
                current = null;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Không tải được màn hình " + screen, e);
        }
    }
}