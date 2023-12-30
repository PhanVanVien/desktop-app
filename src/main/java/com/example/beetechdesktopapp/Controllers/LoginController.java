package com.example.beetechdesktopapp.Controllers;

import com.example.beetechdesktopapp.Models.Model;
import com.example.beetechdesktopapp.Models.NotificationManager;
import com.example.beetechdesktopapp.Models.TokenSingleton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    public TextField username_fld;
    public PasswordField password_fld;
    public Button login_btn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        login_btn.setOnAction(event -> {
            try {
                onLoginButtonClicked();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FXML
    protected void onLoginButtonClicked() throws IOException {
        String username = username_fld.getText();
        String password = password_fld.getText();
        validationInput(username_fld, password_fld);
        if (username.isEmpty() || password.isEmpty()) {
            NotificationManager.getInstance().showNotification("Login Failed", "Please enter both username and password.", "no.png");
            return;
        }

        String loginSuccessful = performLogin(username, password);

        if (loginSuccessful != null) {
            Stage stage = (Stage) login_btn.getScene().getWindow();
            Model.getInstance().getViewFactory().closeStage(stage);
            TokenSingleton.getInstance().setAuthToken(loginSuccessful);
            Model.getInstance().getViewFactory().showClientWindow();
            fetchUserInfo(loginSuccessful);
            NotificationManager.getInstance().showNotification("Login Successful", "Welcome back!", "yes.png");
        }
    }

    private void validationInput(TextField username, TextField password) {
        validation(username);
        validation(password);
    }

    private void validation(TextField textField) {
        String string = textField.getText();
        if (string.isEmpty()) {
            wrongInput(textField);
        } else {
            rightInput(textField);
        }
    }

    private void wrongInput(TextField textField) {
        textField.setStyle("-fx-border-color: red; -fx-border-radius: 3;");
    }

    private void rightInput(TextField textField) {
        textField.setStyle("");
    }

    private String performLogin(String username, String password) {
        try {
            HttpURLConnection connection = getHttpURLConnection(username, password);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONObject data = jsonObject.getJSONObject("data");
                    String token = data.getString("token");
                    return token;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                NotificationManager.getInstance().showNotification("ERROR", "Username or password is incorrect!", "no.png");
                return null;
            }
        } catch (IOException e) {
            NotificationManager.getInstance().showNotification("ERROR", "Check your connection!", "no.png");
            e.printStackTrace();
            return null;
        }
    }

    private static HttpURLConnection getHttpURLConnection(String username, String password) throws IOException {
        URL url = new URL("");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonInputString = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }

    private void fetchUserInfo(String token) {
        try {
            URL url = new URL("");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONObject data = jsonObject.getJSONObject("data");
                    String name = data.getString("name");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
