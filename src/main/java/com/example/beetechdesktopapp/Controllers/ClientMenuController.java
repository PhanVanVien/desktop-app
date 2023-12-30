package com.example.beetechdesktopapp.Controllers;

import com.example.beetechdesktopapp.Models.Model;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientMenuController implements Initializable {
    public Button home_btn;
    public Button asset_btn;
    public Button service_btn;
    public Button profile_btn;
    public Button logout_btn;
    public Button categoryButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        home_btn.setOnAction(event -> setClientMenuItem("Home"));
        categoryButton.setOnAction(event -> setClientMenuItem("Category"));
        asset_btn.setOnAction(event -> setClientMenuItem("Asset"));
        service_btn.setOnAction(event -> setClientMenuItem("Service"));
        profile_btn.setOnAction(event -> setClientMenuItem("Profile"));
    }

    private void setClientMenuItem(String menuItem) {
        Model.getInstance().getViewFactory().getClientSelectedMenuItem().set(menuItem);
    }
}
