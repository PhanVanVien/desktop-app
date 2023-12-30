package com.example.beetechdesktopapp.Controllers;

import com.example.beetechdesktopapp.Models.Asset;
import com.example.beetechdesktopapp.Models.Category;
import com.example.beetechdesktopapp.Models.Model;
import com.example.beetechdesktopapp.Models.TokenSingleton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AssetMenuController implements Initializable {
    private static final String PARAM_PAGE = "&page=";
    private static final String PARAM_NAME = "name=";
    private static final String ENDPOINT = "assets?";
    public TableView<Asset> tableView;
    public TableColumn<Asset, String> assetDetailsCol;
    public TableColumn<Asset, String> nameCol;
    public TableColumn<Asset, String> categoryCol;
    public TableColumn<Asset, String> typeCol;
    public TableColumn<Asset, String> parentCol;
    public TableColumn<Asset, String> barCodeCol;
    public TableColumn<Asset, String> rfidCol;
    public TableColumn<Asset, String> serialCol;
    public TableColumn<Asset, String> stateCol;
    public TableColumn<Asset, String> statusCol;
    public TableColumn<Asset, String> dateCol;
    public TableColumn<Asset, String> deptCol;
    public TableColumn<Asset, String> actionsCol;
    public Button add_btn;
    public Button import_btn;
    public Button issue_btn;
    public Text indexpagetext;
    public Button prev_page;
    public Button next_page;
    public Button refreshButton;
    public TextField searchTextField;
    private String authToken;
    private int currentPage = 1;
    private static final int ASSETS_PER_PAGE_LIMIT = 10;
    private static final String BASE_URL = "";
    private ScheduledExecutorService executor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        executor = Executors.newSingleThreadScheduledExecutor();
        addListener();
        loadItem();
        authToken = TokenSingleton.getInstance().getAuthToken();
        getAsset();
    }

    private void loadItem() {
        assetDetailsCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("assetDetails"));
        nameCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("category"));
        typeCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("type"));
        parentCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("parent"));
        barCodeCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("barCode"));
        rfidCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("rfid"));
        serialCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("serial"));
        stateCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("state"));
        statusCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("status"));
        dateCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("dueDate"));
        deptCol.setCellValueFactory(new PropertyValueFactory<Asset, String>("dept"));
        actionsCol.setCellFactory(createCellFactory());
    }

    private Callback<TableColumn<Asset, String>, TableCell<Asset, String>> createCellFactory() {
        return (TableColumn<Asset, String> param) -> {
            final TableCell<Asset, String> cell = new TableCell<Asset, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        setText(null);

                    } else {
                        FontAwesomeIconView viewIcon = new FontAwesomeIconView(FontAwesomeIcon.SEARCH_PLUS);
                        FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL_SQUARE);

                        configureIcon(viewIcon);
                        configureIcon(editIcon);

                        viewIcon.setOnMouseClicked(event -> handleViewIconClick());
                        editIcon.setOnMouseClicked(event -> handleEditIconClick());

                        HBox managebtn = new HBox(viewIcon, editIcon);
                        managebtn.setStyle("-fx-alignment:center;");
                        HBox.setMargin(editIcon, new Insets(2, 2, 0, 3));
                        HBox.setMargin(viewIcon, new Insets(2, 3, 0, 2));

                        setGraphic(managebtn);
                        setText(null);
                    }
                }

                private void configureIcon(FontAwesomeIconView icon) {
                    icon.setStyle("-glyph-size:20px;");
                    icon.setFill(Paint.valueOf("#000000"));
                    icon.setCursor(Cursor.HAND);
                }

                private void handleViewIconClick() {
                    System.out.println("Hello1");
                }

                private void handleEditIconClick() {
                    System.out.println("Hello2");
                }
            };
            return cell;
        };
    }

    private void setupButtonHandlers(int totalPage) {
        prev_page.setOnAction(event -> onPrevButtonClick());
        next_page.setOnAction(event -> onNextButtonClick(totalPage));
    }

    public void onPrevButtonClick() {
        if (currentPage > 1) {
            currentPage--;
            getAsset();
        }
    }

    public void onNextButtonClick(int totalPage) {
        if (currentPage < totalPage) {
            currentPage++;
            getAsset();
        }
    }

    private void getAsset() {
        ObservableList<Asset> data = fetchDataFromAPI();
        tableView.setItems(data);
    }

    private ObservableList<Asset> fetchDataFromAPI() {
        ObservableList<Asset> assetList = FXCollections.observableArrayList();
        try {
            String apiURL = getApiForAsset();
            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            configureConnection(connection);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                assetList = parseAssetData(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assetList;
    }

    private void configureConnection(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + authToken);
    }

    private ObservableList<Asset> parseAssetData(HttpURLConnection connection) throws Exception {
        ObservableList<Asset> assetList = FXCollections.observableArrayList();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray assetData = jsonResponse.getJSONObject("data").getJSONArray("data");
            int totalAsset = Integer.parseInt(jsonResponse.getJSONObject("data").getString("total"));
            int totalPage = calculateTotalPage(totalAsset);
            int lastItem = currentPage * 10;
            int firstItem = lastItem - 10 + 1;
            if (lastItem > totalAsset) lastItem = totalAsset;
            if (firstItem > totalAsset) firstItem = totalAsset;
            indexpagetext.setText(firstItem + "-" + lastItem + " of " + totalAsset);
            setupButtonHandlers(totalPage);

            for (int i = 0; i < assetData.length(); i++) {
                JSONObject asset = assetData.getJSONObject(i);
                Asset assetItem = parseAsset(asset);
                assetList.add(assetItem);
            }
        }

        return assetList;
    }

    public int calculateTotalPage(int totalAsset) {
        double originalNumber = (double) totalAsset / ASSETS_PER_PAGE_LIMIT;
        int roundedNumber = (int) Math.ceil(originalNumber);
        return roundedNumber;
    }

    private void addListener() {
        add_btn.setOnAction(event -> onAddAsset());
        refreshButton.setOnAction(event -> refreshPage());

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearAssetsBoxItems();
            currentPage = 1;
            executor.schedule(() -> {
                Platform.runLater(this::getAsset);
            }, 500, TimeUnit.MILLISECONDS);
        });
    }

    private String getApiForAsset() {
        String name = searchTextField.getText();
        name = name.replace(" ", "%20");
        return BASE_URL + "assets?name=" + name + PARAM_PAGE + currentPage + "&limit=" + ASSETS_PER_PAGE_LIMIT;
    }

    private void clearAssetsBoxItems() {
        tableView.getItems().clear();
    }

    private void refreshPage() {
        clearAssetsBoxItems();
        getAsset();
    }

    private void onAddAsset() {
        Model.getInstance().getViewFactory().getClientSelectedMenuItem().set("Add");
    }

    private Asset parseAsset(JSONObject asset) throws JSONException {
        String assetDetails = asset.getString("internalId");
        String name = asset.getString("name");
        String categoryName = getCategoryName(asset);
        String type = getType(asset);
        String parent = getParent(asset);
        String barCode = asset.getString("barCode");
        String rfid = asset.getString("rfidCode");
        String serial = asset.getString("serialNumberId");
        String state = getState(asset);
        String status = "";
        String dueDate = "";
        String dept = getDepartmentName(asset);
        String actions = "";

        return new Asset(assetDetails, name, categoryName, type, parent, barCode, rfid, serial, state, status, dueDate, dept, actions);
    }

    private String getCategoryName(JSONObject asset) throws JSONException {
        String categoryName = "";
        if (asset.has("category") && asset.get("category") instanceof JSONObject) {
            JSONObject category = asset.getJSONObject("category");
            categoryName = category.getString("name");
        }
        return categoryName;
    }

    private String getType(JSONObject asset) throws JSONException {
        String type = asset.getString("type");
        return type.equals("asset") ? "A" : "S";
    }

    private String getParent(JSONObject asset) throws JSONException {
        String parent;
        if (asset.has("parentAsset") && asset.get("parentAsset") instanceof JSONObject) {
            JSONObject parentAsset = asset.getJSONObject("parentAsset");
            parent = parentAsset.getString("parentAsset");
        } else {
            parent = asset.getString("parentAsset");
        }
        return parent;
    }

    private String getState(JSONObject asset) throws JSONException {
        if (asset.has("assetsStatus") && asset.get("assetsStatus") instanceof JSONObject) {
            JSONObject assetsStatus = asset.getJSONObject("assetsStatus");
            if (assetsStatus.has("lastMetadata") && assetsStatus.get("lastMetadata") instanceof JSONObject) {
                JSONObject lastMetadata = assetsStatus.getJSONObject("lastMetadata");
                return lastMetadata.getString("stateAsset");
            }
        }
        return ""; // Return a default value or handle the absence of data as needed.
    }

    private String getDepartmentName(JSONObject asset) throws JSONException {
        if (asset.has("department") && asset.get("department") instanceof JSONObject) {
            JSONObject department = asset.getJSONObject("department");
            return department.getString("name");
        }
        return "";
    }
}

