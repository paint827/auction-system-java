package client.controller;

import client.model.ClientSession;
import client.util.Screen;
import client.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AuctionListController implements ScreenController {

    @FXML private Label welcomeLabel;

    @Override
    public void onShow(Object data) {
        welcomeLabel.setText("Xin chào, " + ClientSession.getInstance().getUsername());
    }

    @FXML
    private void onLogout() {
        ClientSession.getInstance().logout();
        SceneNavigator.goTo(Screen.LOGIN);
    }
}