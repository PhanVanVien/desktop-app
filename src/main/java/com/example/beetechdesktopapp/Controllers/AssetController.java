package com.example.beetechdesktopapp.Controllers;

import com.example.beetechdesktopapp.Models.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AssetController implements Initializable {
    private static final String BASE_URL = "";
    private static final String ENDPOINT1 = "assets?";
    private static final String ENDPOINT2 = "categories?";
    private static final String PARAM_TYPE = "type=";
    private static final String PARAM_CATEGORY_IDS = "categoryIds=";
    private static final String PARAM_NAME = "name=";
    private static final String PARAM_PAGE = "&page=";
    private static final String PARAM_LIMIT = "&limit=";
    private static final String LIMIT = "10";
    @FXML
    public TextField searchCategoryTextField;
    public TextField searchParentAssetTextField;
    @FXML
    private VBox step1VBox, step2VBox, step3VBox, step4VBox;
    @FXML
    private TextField rfidTextField, barcodeTextField;
    @FXML
    private CheckBox rfidCheckBox, barcodeCheckBox;
    @FXML
    private Button nextButton, backButton, readButton, connectButton, completeButton;
    @FXML
    private AnchorPane buttonAnchorPane;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private ComboBox<ParentAsset> parentAssetComboBox;
    @FXML
    private ComboBox<String> currencyComboBox, timeRangeComboBox, maintenanceTimeComboBox, depreciationPolicyComboBox;
    @FXML
    private Text categoryText, originalCostText, maintenanceStartedText, parentText, depreciationTimeText, maintenanceTimeText, nameText, depreciationStartText, rfidText, depreciationPolicyText, barCodeText, serialNumberText, descriptionText;
    @FXML
    private TextField originalCostTextField, nameTextField, serialNumberTextField, descriptionTextField, depreciationTimeTextField;
    @FXML
    private DatePicker depreciationStartDateDatePicker, startedMaintenanceDate;
    @FXML
    private TextField maintenanceTimeTextField;
    @FXML
    private Text deviceText;
    private int currentStep = 1;
    private int currentPageOfCategory = 1;
    private int currentParentAssetPage = 1;
    private final String[] currency = {"USD", "VND"};
    private final String[] policy = {"Straight line", "Declining balance"};
    private final String[] timeRange = {"day", "week", "month", "year"};
    private String authToken;
    private String categoryID;
    private ScheduledExecutorService executor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        executor = Executors.newSingleThreadScheduledExecutor();
        showStep(currentStep);
        authToken = TokenSingleton.getInstance().getAuthToken();
        setupCheckBoxHandlers();
        setupComboBoxes();
        setupButtonHandlers();
    }

    private String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
        }
        return result.toString().toUpperCase();
    }

    private void setupCheckBoxHandlers() {
        rfidCheckBox.setOnAction(event -> handleCheckBoxClicked(rfidCheckBox, rfidTextField));
        barcodeCheckBox.setOnAction(event -> handleCheckBoxClicked(barcodeCheckBox, barcodeTextField));
        rfidCheckBox.setSelected(true);
        barcodeTextField.setDisable(true);
    }

    private void setupComboBoxes() {
        currencyComboBox.getItems().addAll(currency);
        timeRangeComboBox.getItems().addAll(timeRange);
        depreciationPolicyComboBox.getItems().addAll(policy);
        maintenanceTimeComboBox.getItems().addAll(timeRange);

        getCategoryByEditor();
        setupCategoryBoxEvents();
        getParentAssetByEditor();
        setupParentAssetBoxEvents();
    }

    private void setupCategoryBoxEvents() {
        categoryComboBox.setOnAction(event -> handleCategorySelection());

        categoryComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (isNowShowing) {
                categoryComboBox.lookup(".list-view").addEventFilter(ScrollEvent.SCROLL, this::handleCategoryScroll);
                ListView<?> listView = (ListView<?>) categoryComboBox.lookup(".list-view");
                listView.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode().isArrowKey()) {
                        keyEvent.consume(); // Consume the event to prevent it from being processed
                    }
                });
            }
        });

        AtomicBoolean isDropdownOpen = new AtomicBoolean(false);

        searchCategoryTextField.setOnMouseClicked(mouseEvent -> {
            if (isDropdownOpen.getAndSet(!isDropdownOpen.get())) {
                categoryComboBox.hide();
            } else {
                categoryComboBox.show();
            }
        });

        searchCategoryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearCategoryBoxItems();
            currentPageOfCategory = 1;
            getCategoryByEditor();
        });
    }

    private void setupParentAssetBoxEvents() {
        parentAssetComboBox.setOnAction(event -> handleParentAssetSelection());

        parentAssetComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (isNowShowing) {
                parentAssetComboBox.lookup(".list-view").addEventFilter(ScrollEvent.SCROLL, this::handleParentAssetScroll);
                ListView<?> listView = (ListView<?>) parentAssetComboBox.lookup(".list-view");
                listView.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode().isArrowKey()) {
                        keyEvent.consume(); // Consume the event to prevent it from being processed
                    }
                });
            }
        });

        AtomicBoolean isDropdownOpen = new AtomicBoolean(false);

        searchParentAssetTextField.setOnMouseClicked(mouseEvent -> {
            if (isDropdownOpen.getAndSet(!isDropdownOpen.get())) {
                parentAssetComboBox.hide();
            } else {
                parentAssetComboBox.show();
            }
        });

        searchParentAssetTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearParentAssetBoxItems();
            currentParentAssetPage = 1;
            getParentAssetByEditor();
        });
    }

    private void handleCategorySelection() {
        Category selectedCategory = categoryComboBox.getValue();
        clearParentAssetBoxItems();
        searchParentAssetTextField.setText("");
        categoryID = selectedCategory.getId();
        currentParentAssetPage = 1;
        getParentAssetByEditor();
        searchCategoryTextField.setText(categoryComboBox.getValue().getName());
    }

    private void handleParentAssetSelection() {
        ParentAsset selectedParentAsset = parentAssetComboBox.getValue();
        searchParentAssetTextField.setText(selectedParentAsset.getName());
    }

    private void getCategoryByEditor() {
        String api = getApiForCategory();
        ObservableList<Category> data = fetchDataCategory(api);
        categoryComboBox.getItems().addAll(data);
    }

    private void getParentAssetByEditor() {
        String api = getApiForParentAsset();
        ObservableList<ParentAsset> data = fetchDataParentAsset(api);
        parentAssetComboBox.getItems().addAll(data);
    }

    private void handleCategoryScroll(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        if (deltaY < 0) {
            currentPageOfCategory++;
            getCategoryByEditor();
        }
    }

    private String getApiForCategory() {
        String nameCategory = searchCategoryTextField.getText();
        nameCategory = nameCategory.replace(" ", "%20");
        return BASE_URL + ENDPOINT2 + PARAM_NAME + nameCategory + PARAM_PAGE + currentPageOfCategory + PARAM_LIMIT + LIMIT;
    }

    private String getApiForParentAsset() {
        String nameParentAsset = searchParentAssetTextField.getText();
        System.out.println(nameParentAsset + "-" + categoryID);
        nameParentAsset = nameParentAsset.replace(" ", "%20");
        System.out.println(BASE_URL + ENDPOINT1 + PARAM_TYPE + "asset&" + PARAM_CATEGORY_IDS + categoryID + "&" + PARAM_NAME + nameParentAsset + PARAM_PAGE + currentParentAssetPage + PARAM_LIMIT + LIMIT);
        return BASE_URL + ENDPOINT1 + PARAM_TYPE + "asset&" + PARAM_CATEGORY_IDS + categoryID + "&" + PARAM_NAME + nameParentAsset + PARAM_PAGE + currentParentAssetPage + PARAM_LIMIT + LIMIT;
    }

    private void handleParentAssetScroll(ScrollEvent scrollEvent) {
        double deltaY = scrollEvent.getDeltaY();
        if (deltaY < 0) {
            currentParentAssetPage++;
            getParentAssetByEditor();
        }
    }

    private void clearCategoryBoxItems() {
        categoryComboBox.getItems().clear();
    }

    private void clearParentAssetBoxItems() {
        parentAssetComboBox.getItems().clear();
//        searchParentAssetTextField.setText("");
    }

    private void setupButtonHandlers() {
        nextButton.setOnAction(event -> onNextButtonClick());
        backButton.setOnAction(event -> onBackButtonClick());
        completeButton.setOnAction(event -> openCustomDialog());
    }

    private void openCustomDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/AddAssetPopup.fxml"));
            DialogPane dialogPane = loader.load();

            AddAssetPopupController dialogController = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(dialogPane);

            Optional<ButtonType> result = dialog.showAndWait();

            if (dialogController.isOKButtonClicked()) {
                addAsset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCheckBoxClicked(CheckBox checkBox, TextField textField) {
        if (checkBox.isSelected()) {
            textField.setDisable(false);
        } else {
            textField.setDisable(true);
            textField.setText("");
            rightInput(textField);
        }
        if (!rfidCheckBox.isSelected() && !barcodeCheckBox.isSelected()) {
            checkBox.setSelected(true);
            textField.setDisable(false);
        }
    }

    public void onNextButtonClick() {
        if (canProceedToNextStep()) {
            currentStep++;
            showStep(currentStep);
        }
    }

    private boolean canProceedToNextStep() {
        switch (currentStep) {
            case 1:
                return validateStep1Inputs(categoryComboBox.getEditor().getText(), rfidTextField.getText(), barcodeTextField.getText());
            case 2:
                return validateStep2Inputs(originalCostTextField.getText(), currencyComboBox.getValue(), originalCostTextField.getText(), timeRangeComboBox.getValue(), depreciationStartDateDatePicker.getValue(), depreciationPolicyComboBox.getValue());
            case 3:
                return validateStep3Inputs(startedMaintenanceDate.getValue(), maintenanceTimeTextField.getText(), maintenanceTimeComboBox.getValue());
            default:
                return true;
        }
    }

    private boolean validateStep1Inputs(String categoryText, String rfidText, String barCode) {
//        boolean isCategoryValid = validateTextField(categoryText, categoryComboBox.getEditor());
        boolean isRFIDValid = rfidTextField.isDisable() || validateTextField(rfidText, rfidTextField);
        boolean isBarCodeValid = barcodeTextField.isDisable() || validateTextField(barCode, barcodeTextField);

        return isRFIDValid && isBarCodeValid;
    }

    private boolean validateStep2Inputs(String originalCost, String currency, String depreciationTime, String timeRange, LocalDate depreciationStartDate, String depreciationPolicy) {
        boolean isOriginalCostValid = validateTextField(originalCost, originalCostTextField);
        boolean isCurrencyValid = validateComboBox(currency, currencyComboBox);
        boolean isDepreciationTimeValid = validateTextField(depreciationTime, depreciationTimeTextField);
        boolean isTimeRangeValid = validateComboBox(timeRange, timeRangeComboBox);
        boolean isDepreciationStartDateValid = validateDatePicker(depreciationStartDate, depreciationStartDateDatePicker);
        boolean isDepreciationPolicyValid = validateComboBox(depreciationPolicy, depreciationPolicyComboBox);

        return isOriginalCostValid && isCurrencyValid && isDepreciationTimeValid && isTimeRangeValid && isDepreciationStartDateValid && isDepreciationPolicyValid;
    }

    private boolean validateStep3Inputs(LocalDate startMaintenanceDate, String maintenanceTime, String timeRange) {
        boolean isStartedMaintenanceDateValid = validateDatePicker(startMaintenanceDate, startedMaintenanceDate);
        boolean isMaintenanceTimeValid = validateTextField(maintenanceTime, maintenanceTimeTextField);
        boolean isTimeRangeValid = validateComboBox(timeRange, maintenanceTimeComboBox);

        return isStartedMaintenanceDateValid && isMaintenanceTimeValid && isTimeRangeValid;
    }

    private boolean validateTextField(String value, TextField textField) {
        boolean isValid = !value.isEmpty();
        if (!isValid) {
            wrongInput(textField);
        } else {
            rightInput(textField);
        }
        return isValid;
    }

    private boolean validateComboBox(String value, ComboBox<String> comboBox) {
        boolean isValid = value != null;
        if (!isValid) {
            wrongInput(comboBox);
        } else {
            rightInput(comboBox);
        }
        return isValid;
    }

    private boolean validateDatePicker(LocalDate value, DatePicker datePicker) {
        boolean isValid = value != null;
        if (!isValid) {
            wrongInput(datePicker);
        } else {
            rightInput(datePicker);
        }
        return isValid;
    }

    public void onBackButtonClick() {
        if (currentStep > 1) {
            currentStep--;
            showStep(currentStep);
        }
    }

    private void showStep(int step) {
        step1VBox.setVisible(step == 1);
        step2VBox.setVisible(step == 2);
        step3VBox.setVisible(step == 3);
        step4VBox.setVisible(step == 4);
        buttonAnchorPane.setVisible(true);
        backButton.setVisible(currentStep != 1);
        nextButton.setVisible(currentStep != 4);
        completeButton.setVisible(currentStep == 4);
        if (currentStep == 4) {
            showPreview();
        }
    }

    private void addAsset() {
        if (performAdd()) {
            NotificationManager.getInstance().showNotification("Success", "Create asset", "yes.png");
        }
    }

    private void showPreview() {
        nameText.setText(nameTextField.getText());
        rfidText.setText(rfidTextField.getText());
        barCodeText.setText(barcodeTextField.getText());
        serialNumberText.setText(serialNumberTextField.getText());
        descriptionText.setText(descriptionTextField.getText());
        originalCostText.setText(originalCostTextField.getText() + " " + currencyComboBox.getValue());
        depreciationTimeText.setText(formatDepreciationTime(originalCostTextField.getText(), timeRangeComboBox.getValue()));
        depreciationStartText.setText(formatDate(depreciationStartDateDatePicker));
        depreciationPolicyText.setText(depreciationPolicyComboBox.getValue());
        maintenanceStartedText.setText(formatDate(startedMaintenanceDate));
        maintenanceTimeText.setText(formatMaintenanceTime(maintenanceTimeTextField.getText(), maintenanceTimeComboBox.getValue()));
    }

    private void wrongInput(TextField textField) {
        textField.setStyle("-fx-border-color: red; -fx-border-radius: 3;");
    }

    private void wrongInput(ComboBox comboBox) {
        comboBox.setStyle("-fx-border-color: red; -fx-border-radius: 3;");
    }

    private void wrongInput(DatePicker datePicker) {
        datePicker.setStyle("-fx-border-color: red; -fx-border-radius: 3;");
    }

    private void rightInput(TextField textField) {
        textField.setStyle("");
    }

    private void rightInput(ComboBox comboBox) {
        comboBox.setStyle("");
    }

    private void rightInput(DatePicker datePicker) {
        datePicker.setStyle("");
    }

    private String formatDepreciationTime(String value, String unit) {
        if (value.equals("1")) {
            return value + " " + unit;
        } else {
            return value + " " + unit + "s";
        }
    }

    private String formatDate(DatePicker datePicker) {
        if (datePicker.getValue() == null) {
            return "";
        } else {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return dateFormatter.format(datePicker.getValue());
        }
    }

    private String formatMaintenanceTime(String value, String unit) {
        return formatDepreciationTime(value, unit);
    }

    private String formatCurrency(String value, String unit) {
        return value + " " + unit;
    }

    private String getDepreciationPolicy(ComboBox<String> policyBox) {
        return "Straight line".equals(policyBox.getValue()) ? "STRAIGHT_LINE" : "DECLINING_BALANCE";
    }

    private String createAssetJSON(String name, String rfidCode, String barCode, String originalCost, String serialNumberId, String depreciationTime, String depreciationPolicy) {
        return String.format("""
                {
                    "name": "AAAA-EMP-0001",
                    "type": "asset",
                    "buyerId": "AAAA-EMP-0001",
                    "categoryId": "AAAA-CAT-0001",
                    "serialNumberId": "",
                    "barCode": "",
                    "rfidCode": "%s",
                    "parentAsset": "AAAA-ASE-1",
                    "subAssets": [],
                    "maintenanceDetail": {
                        "guaranteeTime": "",
                        "maintenanceStartDate": "2023-10-16T15:11:34.270Z"
                    },
                    "depreciation": {
                        "originalCost": "1 USD",
                        "depreciationTime": "1 day",
                        "depreciationStartDate": "2023-10-16T15:11:34.268Z",
                        "depreciationPolicy": "STRAIGHT_LINE"
                    }
                }""", rfidCode);
    }

    private void handleBadRequestResponse(HttpURLConnection connection) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            String message = jsonResponse.getString("message");
            NotificationManager.getInstance().showNotification("ERROR", message, "no.png");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean performAdd() {
        try {
            String name = nameTextField.getText();
            String rfidCode = rfidTextField.getText();
            String barCode = barcodeTextField.getText();
            String originalCost = formatCurrency(originalCostTextField.getText(), currencyComboBox.getValue());
            String serialNumberId = serialNumberTextField.getText();
            String depreciationTime = formatDepreciationTime(originalCostTextField.getText(), timeRangeComboBox.getValue());
            String depreciationPolicy = getDepreciationPolicy(depreciationPolicyComboBox);

            URL url = new URL("");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + authToken);
            connection.setDoOutput(true);

            String jsonInputString = createAssetJSON(name, rfidCode, barCode, originalCost, serialNumberId, depreciationTime, depreciationPolicy);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                return true;
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                handleBadRequestResponse(connection);
                return false;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    private ObservableList<ParentAsset> fetchDataParentAssetFromAPI(String categoryId) {
        ObservableList<ParentAsset> parentAssetList = FXCollections.observableArrayList();

        try {
            String apiURL = BASE_URL + ENDPOINT1 + "?type=asset&categoryIds=" + categoryId + "&limit=10";
            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            configureConnectionTest(connection);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                parentAssetList = parseParentAssetData(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parentAssetList;
    }

    private ObservableList<ParentAsset> parseParentAssetData(HttpURLConnection connection) throws Exception {
        ObservableList<ParentAsset> parentAssetList = FXCollections.observableArrayList();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray parentAssetData = jsonResponse.getJSONObject("data").getJSONArray("data");

            for (int i = 0; i < parentAssetData.length(); i++) {
                JSONObject parentAsset = parentAssetData.getJSONObject(i);
                ParentAsset parentAssetItem = parseParentAsset(parentAsset);
                parentAssetList.add(parentAssetItem);
            }
        }

        return parentAssetList;
    }

    private ParentAsset parseParentAsset(JSONObject parentAsset) throws JSONException {
        String parentAssetId = parentAsset.getString("internalId");
        String name = parentAsset.getString("name");
        return new ParentAsset(parentAssetId, name);
    }

    private ObservableList<Category> fetchDataCategory(String apiURL) {
        ObservableList<Category> categoryList = FXCollections.observableArrayList();

        try {
            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            configureConnectionTest(connection);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                categoryList = parseDataCategory(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categoryList;
    }

    private void configureConnectionTest(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + authToken);
    }

    private ObservableList<Category> parseDataCategory(HttpURLConnection connection) throws Exception {
        ObservableList<Category> categoryList = FXCollections.observableArrayList();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray categoryData = jsonResponse.getJSONObject("data").getJSONArray("data");

            for (int i = 0; i < categoryData.length(); i++) {
                JSONObject category = categoryData.getJSONObject(i);
                Category categoryItem = parseCategory(category);
                categoryList.add(categoryItem);
            }
        }

        return categoryList;
    }

    private Category parseCategory(JSONObject category) throws JSONException {
        String id = category.getString("internalId");
        String name = category.getString("name");
        return new Category(id, name);
    }

    private ObservableList<ParentAsset> fetchDataParentAsset(String api) {
        ObservableList<ParentAsset> parentAssetList = FXCollections.observableArrayList();

        try {
            URL url = new URL(api);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            configureConnectionTest(connection);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                parentAssetList = parseParentAsset(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parentAssetList;
    }

    private ObservableList<ParentAsset> parseParentAsset(HttpURLConnection connection) throws Exception {
        ObservableList<ParentAsset> parentAssetList = FXCollections.observableArrayList();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray parentAssetData = jsonResponse.getJSONObject("data").getJSONArray("data");

            for (int i = 0; i < parentAssetData.length(); i++) {
                JSONObject parentAsset = parentAssetData.getJSONObject(i);
                ParentAsset parentAssetItem = parseParentAssetTest(parentAsset);
                parentAssetList.add(parentAssetItem);
            }
        }

        return parentAssetList;
    }

    private ParentAsset parseParentAssetTest(JSONObject parentAsset) throws JSONException {
        String parentAssetId = parentAsset.getString("internalId");
        String name = parentAsset.getString("name");
        return new ParentAsset(parentAssetId, name);
    }
}