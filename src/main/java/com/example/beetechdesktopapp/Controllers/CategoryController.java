package com.example.beetechdesktopapp.Controllers;

import com.example.beetechdesktopapp.Models.Asset;
import com.example.beetechdesktopapp.Models.Category;
import com.example.beetechdesktopapp.Models.TokenSingleton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

public class CategoryController implements Initializable {
    private static final String BASE_URL = "";
    private static final String ENDPOINT = "categories?";
    private static final String PARAM_NAME = "name=";
    private static final String PARAM_PAGE = "&page=";
    private static final String PARAM_LIMIT = "&limit=";
    private static final int LIMIT = 10;
    public TableView<Category> categoryTableView;
    public TableColumn<Category, String> categoryIDCol;
    public TableColumn<Category, String> nameCol;
    public TableColumn<Category, String> imagesCol;
    public TableColumn<Category, String> describeCol;
    public TableColumn<Category, String> modelCol;
    public TableColumn<Category, String> manufacturerCol;
    public TableColumn<Category, String> actionsCol;
    public Button addButton;
    public Button exportButton;
    public Button refreshButton;
    public TextField searchTextField;
    public Text indexPageText;
    public Button prevPageButton;
    public Button nextPageButton;
    private int currentPage = 1;
    private String authToken;
    private ScheduledExecutorService executor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        authToken = TokenSingleton.getInstance().getAuthToken();
        executor = Executors.newSingleThreadScheduledExecutor();
        loadItem();
        addListener();
        getCategory();
    }

    private void loadItem() {
        categoryIDCol.setCellValueFactory(new PropertyValueFactory<Category, String>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<Category, String>("name"));
        imagesCol.setCellValueFactory(new PropertyValueFactory<Category, String>("images"));
        describeCol.setCellValueFactory(new PropertyValueFactory<Category, String>("describe"));
        modelCol.setCellValueFactory(new PropertyValueFactory<Category, String>("model"));
        manufacturerCol.setCellValueFactory(new PropertyValueFactory<Category, String>("manufacturer"));
        actionsCol.setCellFactory(createCellFactory());
    }

    private Callback<TableColumn<Category, String>, TableCell<Category, String>> createCellFactory() {
        return (TableColumn<Category, String> param) -> {
            final TableCell<Category, String> cell = new TableCell<Category, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        setText(null);

                    } else {
                        FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL_SQUARE);

                        configureIcon(editIcon);

                        editIcon.setOnMouseClicked(event -> handleEditIconClick());

                        HBox managebtn = new HBox(editIcon);
                        managebtn.setStyle("-fx-alignment:center;");
                        HBox.setMargin(editIcon, new Insets(2, 2, 0, 3));

                        setGraphic(managebtn);
                        setText(null);
                    }
                }

                private void configureIcon(FontAwesomeIconView icon) {
                    icon.setStyle("-glyph-size:20px;");
                    icon.setFill(Paint.valueOf("#000000"));
                    icon.setCursor(Cursor.HAND);
                }

                private void handleEditIconClick() {
                    System.out.println("Hello2");
                }
            };
            return cell;
        };
    }

    private void addListener() {
        refreshButton.setOnAction(event -> refreshPage());

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearCategoryData();
            currentPage = 1;
            executor.schedule(() -> {
                Platform.runLater(this::getCategory);
            }, 500, TimeUnit.MILLISECONDS);
        });
    }

    private void clearCategoryData() {
        categoryTableView.getItems().clear();
    }

    private void refreshPage() {
        clearCategoryData();
        getCategory();
    }

    private void setupButtonHandlers(int totalPage) {
        prevPageButton.setOnAction(event -> onPrevButtonClick());
        nextPageButton.setOnAction(event -> onNextButtonClick(totalPage));
    }

    public void onPrevButtonClick() {
        if (currentPage > 1) {
            currentPage--;
            getCategory();
        }
    }

    public void onNextButtonClick(int totalPage) {
        if (currentPage < totalPage) {
            currentPage++;
            getCategory();
        }
    }

    private void getCategory() {
        ObservableList<Category> data = fetchDataCategoryFromAPI();
        categoryTableView.setItems(data);
    }

    private ObservableList<Category> fetchDataCategoryFromAPI() {
        ObservableList<Category> categoryList = FXCollections.observableArrayList();
        try {
            String apiURL = getApiForCategory();
            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            configureConnection(connection);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                categoryList = parseCategoryData(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categoryList;
    }

    private void configureConnection(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + authToken);
    }

    private String getApiForCategory() {
        String name = searchTextField.getText();
        name = name.replace(" ", "%20");
        return BASE_URL + ENDPOINT + PARAM_NAME + name + PARAM_PAGE + currentPage + PARAM_LIMIT + LIMIT;
    }

    private ObservableList<Category> parseCategoryData(HttpURLConnection connection) throws Exception {
        ObservableList<Category> categoryList = FXCollections.observableArrayList();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray assetData = jsonResponse.getJSONObject("data").getJSONArray("data");
            int totalCategory = Integer.parseInt(jsonResponse.getJSONObject("data").getString("total"));
            int totalPage = calculateTotalPage(totalCategory);
            int lastItem = currentPage * 10;
            int firstItem = lastItem - 10 + 1;
            if (lastItem > totalCategory) lastItem = totalCategory;
            if (firstItem > totalCategory) firstItem = totalCategory;
            indexPageText.setText(firstItem + "-" + lastItem + " of " + totalCategory);
            setupButtonHandlers(totalPage);
            for (int i = 0; i < assetData.length(); i++) {
                JSONObject category = assetData.getJSONObject(i);
                Category assetItem = parseCategory(category);
                categoryList.add(assetItem);
            }
        }

        return categoryList;
    }

    public int calculateTotalPage(int totalAsset) {
        double originalNumber = (double) totalAsset / LIMIT;
        int roundedNumber = (int) Math.ceil(originalNumber);
        return roundedNumber;
    }

    private Category parseCategory(JSONObject category) throws JSONException {
        String id = category.getString("internalId");
        String name = category.getString("name");
        String images = "";
        String describe = category.getString("describe");
        String model = category.getString("model");
        String manufacturer = getManufacturer(category);
        String actions = "";
        return new Category(id, name, images, describe, model, manufacturer, actions);
    }

    private String getManufacturer(JSONObject category) throws JSONException {
        String nameManufacturer = "";
        if (category.has("manufacturer") && category.get("manufacturer") instanceof JSONObject) {
            JSONObject manufacturer = category.getJSONObject("manufacturer");
            nameManufacturer = manufacturer.getString("name");
        }
        return nameManufacturer;
    }
}
