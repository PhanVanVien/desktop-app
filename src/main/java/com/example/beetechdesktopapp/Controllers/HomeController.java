package com.example.beetechdesktopapp.Controllers;

import com.example.beetechdesktopapp.Models.Category;
import com.example.beetechdesktopapp.Models.TokenSingleton;
import com.example.beetechdesktopapp.Models.Transaction;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

public class HomeController implements Initializable {

    private static final String BASE_URL = "";
    private static final String ENDPOINT = "transactions?";
    private static final String LIMIT_PARAM = "limit=";
    private static final int LIMIT = 5;
    public PieChart pieChart;
    public TableView<Transaction> transactionTableView;
    public TableColumn<Transaction, String> transactionIDCol;
    public TableColumn<Transaction, String> typeCol;
    public TableColumn<Transaction, String> dateCol;
    public TableColumn<Transaction, String> creatorCol;
    public Text totalAssetsText;
    public Text inStockAssetsText;
    public Text checkedOutAssetsText;
    public Text brokenAssetsText;
    public Text totalDepartmentsText;
    private String authToken;
    private static final String ENDPOINT_ASSETS = "statistics/assets?";
    private static final String ENDPOINT_DEPARTMENTS = "departments/statistic/total";
    private static final String PARAM_CONDITION = "condition=";
    private static final String CONDITION_CHECKOUT = "CHECKOUT";
    private static final String CONDITION_INSTOCK = "INSTOCK";
    private static final String CONDITION_BROKEN = "BROKEN";
    private String totalAssets;
    private String inStockAssets;
    private String checkedOutAssets;
    private String brokenAssets;
    private String totalDepartments;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        authToken = TokenSingleton.getInstance().getAuthToken();
        loadItem();
        getStatistics();
        showStatistics();
        getTransaction();
        setupPieChart();
    }

    private void setupPieChart() {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("In stock", Integer.parseInt(inStockAssets)),
                new PieChart.Data("Checked Out", Integer.parseInt(checkedOutAssets)),
                new PieChart.Data("Unusable/Broken Assets", Integer.parseInt(brokenAssets))
        );

        pieChart.setData(pieData);

        double total = pieData.stream().mapToDouble(data -> data.getPieValue()).sum();

        for (final PieChart.Data data : pieData) {
            final Tooltip tooltip = new Tooltip();
            Tooltip.install(data.getNode(), tooltip);

            double percentage = (data.getPieValue() / total) * 100.0;

            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
                tooltip.setText(data.getName() + ": " + (int) data.getPieValue() + " (" + String.format("%.2f%%", percentage) + ")");
                tooltip.show(data.getNode(), e.getScreenX(), e.getScreenY() + 10);
            });

            data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> {
                tooltip.hide();
            });
        }
    }

    private void getStatistics() {
        totalAssets = getTotal(getAPIUrlTotalAsset(""));
        inStockAssets = getTotal(getAPIUrlTotalAsset(CONDITION_INSTOCK));
        checkedOutAssets = getTotal(getAPIUrlTotalAsset(CONDITION_CHECKOUT));
        brokenAssets = getTotal(getAPIUrlTotalAsset(CONDITION_BROKEN));
        totalDepartments = getTotal(getAPIUrlTotalDepartment());
    }

    private void showStatistics() {
        totalAssetsText.setText(formatAssetsStatistics(totalAssets));
        inStockAssetsText.setText(formatAssetsStatistics(inStockAssets));
        checkedOutAssetsText.setText(formatAssetsStatistics(checkedOutAssets));
        brokenAssetsText.setText(formatAssetsStatistics(brokenAssets));
        totalDepartmentsText.setText(formatDepartmentsStatistics(totalDepartments));
    }

    private String getAPIUrlTotalAsset(String condition) {
        return BASE_URL + ENDPOINT_ASSETS + PARAM_CONDITION + condition;
    }

    private String getAPIUrlTotalDepartment() {
        return BASE_URL + ENDPOINT_DEPARTMENTS;
    }

    private String formatAssetsStatistics(String statistics) {
        return statistics + " assets";
    }

    private String formatDepartmentsStatistics(String statistics) {
        return statistics + " departments";
    }

    private void applyCustomColorSequence(ObservableList<PieChart.Data> pieChartData, String... pieColors) {
        int i = 0;
        for (PieChart.Data data : pieChartData) {
            data.getNode().setStyle("-fx-pie-color: " + pieColors[i % pieColors.length] + ";");
            i++;
        }
    }

    private void loadItem() {
        transactionIDCol.setCellValueFactory(new PropertyValueFactory<Transaction, String>("transactionID"));
        typeCol.setCellValueFactory(new PropertyValueFactory<Transaction, String>("type"));
        dateCol.setCellValueFactory(new PropertyValueFactory<Transaction, String>("date"));
        creatorCol.setCellValueFactory(new PropertyValueFactory<Transaction, String>("creator"));
    }

    private void getTransaction() {
        ObservableList<Transaction> data = fetchDataTransactionFromAPI();
        transactionTableView.setItems(data);
    }

    private ObservableList<Transaction> fetchDataTransactionFromAPI() {
        ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
        try {
            String apiURL = getApiForTransaction();
            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            configureConnection(connection);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                transactionList = parseTransactionData(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactionList;
    }

    private void configureConnection(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + authToken);
    }

    private String getApiForTransaction() {
        return BASE_URL + ENDPOINT + LIMIT_PARAM + LIMIT;
    }

    private ObservableList<Transaction> parseTransactionData(HttpURLConnection connection) throws Exception {
        ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray assetData = jsonResponse.getJSONObject("data").getJSONArray("transactions");

            for (int i = 0; i < assetData.length(); i++) {
                JSONObject transaction = assetData.getJSONObject(i);
                Transaction transactionItem = parseCategory(transaction);
                transactionList.add(transactionItem);
            }
        }

        return transactionList;
    }

    private Transaction parseCategory(JSONObject transaction) throws JSONException {
        String transactionID = transaction.getString("internalId");
        String type = transaction.getString("transactionType");
        String date = getDate(transaction);
        String creator = getCreator(transaction);

        return new Transaction(transactionID, type, date, creator);
    }

    private String getCreator(JSONObject transaction) throws JSONException {
        JSONObject creator = transaction.getJSONObject("creator");
        String nameCreator = creator.getString("name");
        return nameCreator;
    }

    private String getDate(JSONObject transaction) throws JSONException {
        String date = transaction.getString("date");
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Instant instant = Instant.parse(date);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));
        String formattedDateTime = zonedDateTime.format(targetFormatter);
        return formattedDateTime;
    }

    private String getTotal(String url) {
        String total = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + authToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject data = jsonResponse.getJSONObject("data");
                total = data.getString("total");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }
}
