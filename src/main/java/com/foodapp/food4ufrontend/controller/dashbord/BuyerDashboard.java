package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.Restaurant;
import com.foodapp.food4ufrontend.model.Order; // Assuming you created this model
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BuyerDashboard {

    @FXML private ListView<String> actionList;
    @FXML private Label errorMessageLabel;
    @FXML private TextField searchRestaurantField;
    @FXML private TableView<Restaurant> restaurantsTable;
    @FXML private TableColumn<Restaurant, String> restaurantIdColumn;
    @FXML private TableColumn<Restaurant, String> restaurantNameColumn;
    @FXML private TableColumn<Restaurant, String> restaurantAddressColumn;
    @FXML private TableColumn<Restaurant, String> restaurantPhoneColumn;

    @FXML private TableView<Order> orderHistoryTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, Integer> orderVendorColumn;
    @FXML private TableColumn<Order, String> orderStatusColumn;
    @FXML private TableColumn<Order, Integer> orderPriceColumn;
    @FXML private TableColumn<Order, String> orderDateColumn; // Assuming String for now, can be LocalDateTime

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        ObservableList<String> actions = FXCollections.observableArrayList(
                "View Restaurants",
                "Order History",
                "Manage Favorites", // From PDF
                "Wallet & Payments", // From PDF
                "Logout"
        );
        actionList.setItems(actions);

        actionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleActionSelection(newValue);
            }
        });

        // Initialize Restaurant Table Columns
        if (restaurantIdColumn != null) restaurantIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (restaurantNameColumn != null) restaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (restaurantAddressColumn != null) restaurantAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (restaurantPhoneColumn != null) restaurantPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Initialize Order History Table Columns
        if (orderIdColumn != null) orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (orderVendorColumn != null) orderVendorColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (orderStatusColumn != null) orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (orderPriceColumn != null) orderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice")); // Total price
        if (orderDateColumn != null) orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Load initial data
        viewRestaurants();
        viewOrderHistory();
    }

    private void handleActionSelection(String action) {
        switch (action) {
            case "View Restaurants":
                viewRestaurants();
                break;
            case "Order History":
                viewOrderHistory();
                break;
            case "Manage Favorites":
                errorMessageLabel.setText("Manage Favorites functionality not yet implemented.");
                break;
            case "Wallet & Payments":
                errorMessageLabel.setText("Wallet & Payments functionality not yet implemented.");
                break;
            case "Logout":
                logout();
                break;
            default:
                break;
        }
    }

    @FXML
    private void viewRestaurants() {
        errorMessageLabel.setText("Loading restaurants...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                // API call to get all restaurants for a buyer
                // aut_food.yaml: POST /vendors for listing vendors with filters
                // For simplicity, let's assume an empty body gets all.
                Map<String, String> requestBody = new HashMap<>(); // Empty map for no filters initially
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);

                Optional<HttpResponse<String>> responseOpt = ApiClient.post("/vendors", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Restaurant> restaurants = JsonUtil.getObjectMapper().readerForListOf(Restaurant.class).readValue(rootNode);
                                ObservableList<Restaurant> restaurantObservableList = FXCollections.observableArrayList(restaurants);
                                restaurantsTable.setItems(restaurantObservableList);
                                errorMessageLabel.setText("Restaurants loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing restaurant data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing restaurants: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for restaurants."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching restaurants: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void searchRestaurants() {
        String searchTerm = searchRestaurantField.getText();
        errorMessageLabel.setText("Searching for restaurants...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Map<String, String> requestBody = new HashMap<>();
                if (!searchTerm.isEmpty()) {
                    requestBody.put("search", searchTerm); // As per aut_food.yaml for /vendors POST
                }
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);

                Optional<HttpResponse<String>> responseOpt = ApiClient.post("/vendors", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Restaurant> restaurants = JsonUtil.getObjectMapper().readerForListOf(Restaurant.class).readValue(rootNode);
                                ObservableList<Restaurant> restaurantObservableList = FXCollections.observableArrayList(restaurants);
                                restaurantsTable.setItems(restaurantObservableList);
                                errorMessageLabel.setText("Search completed.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing search results: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error searching restaurants: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for search."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred during search: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }


    private void viewOrderHistory() {
        errorMessageLabel.setText("Loading order history...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/orders/history", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                ObservableList<Order> orderObservableList = FXCollections.observableArrayList(orders);
                                orderHistoryTable.setItems(orderObservableList);
                                errorMessageLabel.setText("Order history loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing order history: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing order history: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for order history."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching order history: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void logout() {
        AuthManager.logout();
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) actionList.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Food4u - Login");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                errorMessageLabel.setText("Error navigating to login: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}