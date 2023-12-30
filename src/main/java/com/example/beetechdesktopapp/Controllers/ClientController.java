package com.example.beetechdesktopapp.Controllers;

import com.example.beetechdesktopapp.Models.Model;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    public BorderPane client_parent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ChangeListener<String> clientMenuItemListener = (observableValue, oldVal, newVal) -> {
            switch (newVal) {
                case "Asset":
                    client_parent.setCenter(Model.getInstance().getViewFactory().getAssetView());
                    break;
                case "Service":
                    client_parent.setCenter(Model.getInstance().getViewFactory().getServiceView());
                    break;
                case "Profile":
                    client_parent.setCenter(Model.getInstance().getViewFactory().getProfileView());
                    break;
                case "Add":
                    client_parent.setCenter(Model.getInstance().getViewFactory().getAddView());
                    break;
                case "Category":
                    client_parent.setCenter(Model.getInstance().getViewFactory().getCategoryView());
                    break;
                default:
                    client_parent.setCenter(Model.getInstance().getViewFactory().getHomeView());
            }
        };

        Model.getInstance().getViewFactory().getClientSelectedMenuItem().addListener(clientMenuItemListener);
    }
}
