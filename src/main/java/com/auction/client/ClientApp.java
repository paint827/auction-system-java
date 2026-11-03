package com.auction.client;

import com.auction.client.network.NetworkClient;
import com.auction.client.util.SceneNavigator;
import com.auction.client.util.Screen;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class ClientApp extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.init(stage);
        InputStream iconStream = getClass().getResourceAsStream("/client/images/logo-uet-clean.png");
        stage.getIcons().add(new Image(iconStream));
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        SceneNavigator.goTo(Screen.LOGIN);
        stage.show();
    }

    @Override
    public void stop() {
        // Đóng cửa sổ thì ngắt kết nối, tránh để socket "rác" bên server
        NetworkClient.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}