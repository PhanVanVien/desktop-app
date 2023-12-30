package com.example.beetechdesktopapp.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DialogPane;

public class AddAssetPopupController {

    @FXML
    private DialogPane customDialog;

    private boolean isOKButtonClicked = false;

    public boolean isOKButtonClicked() {
        return isOKButtonClicked;
    }

    @FXML
    private void handleOKButtonClick(ActionEvent event) {
        isOKButtonClicked = true;
        closeDialog();
    }

    @FXML
    private void handleCancelButtonClick(ActionEvent event) {
        isOKButtonClicked = false;
        closeDialog();
    }

    private void closeDialog() {
        customDialog.getScene().getWindow().hide();
    }
}
