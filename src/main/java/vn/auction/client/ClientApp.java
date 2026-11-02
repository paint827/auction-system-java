package vn.auction.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Điểm khởi chạy giao diện JavaFX Client.
 * Phụ trách: TV3.
 */
public class ClientApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Hệ thống đấu giá trực tuyến - Online Auction System");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 900, 600);

        primaryStage.setTitle("Online Auction Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

