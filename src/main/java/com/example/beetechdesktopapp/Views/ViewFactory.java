package com.example.beetechdesktopapp.Views;

import com.example.beetechdesktopapp.Controllers.ClientController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ViewFactory {
    private final StringProperty clientSelectedMenuItem;
    private AnchorPane homeView;
    private AnchorPane categoryView;
    private AnchorPane assetView;
    private AnchorPane serviceView;
    private AnchorPane profileView;
    private AnchorPane addAssetMenu;
    public ViewFactory() {
        this.clientSelectedMenuItem = new SimpleStringProperty("");
    }

    public StringProperty getClientSelectedMenuItem() {
        return clientSelectedMenuItem;
    }

    private AnchorPane loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            return loader.load();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public AnchorPane getHomeView() {
        if (homeView == null) {
            homeView = loadView("/Fxml/Home.fxml");
        }
        return homeView;
    }

    public AnchorPane getCategoryView() {
        if (categoryView == null) {
            categoryView = loadView("/Fxml/Category.fxml");
        }
        return categoryView;
    }

    public AnchorPane getAssetView() {
        if (assetView == null) {
            assetView = loadView("/Fxml/AssetMenu.fxml");
        }
        return assetView;
    }

    public AnchorPane getServiceView() {
        if (serviceView == null) {
            serviceView = loadView("/Fxml/Service.fxml");
        }
        return serviceView;
    }

    public AnchorPane getProfileView() {
        if (profileView == null) {
            profileView = loadView("/Fxml/Profile.fxml");
        }
        return profileView;
    }

    public AnchorPane getAddView() {
        if (addAssetMenu == null) {
            addAssetMenu = loadView("/Fxml/Asset.fxml");
        }
        return addAssetMenu;
    }

    public void showLoginWindow() {
        createStage("/Fxml/Login.fxml", "Beetech");
    }

    public void showClientWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client.fxml"));
        ClientController clientController = new ClientController();
        loader.setController(clientController);
        createStage(loader, "Beetech");
    }

    private void createStage(String fxmlPath, String title) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        createStage(loader, title);
    }

    private void createStage(FXMLLoader loader, String title) {
        try {
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            Image image = new Image(getClass().getResource("/Images/logobeetech.jpg").toExternalForm());
            stage.getIcons().add(image);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeStage(Stage stage) {
        stage.close();
    }
}
