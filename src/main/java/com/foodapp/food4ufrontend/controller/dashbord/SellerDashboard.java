package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.Restaurant;
import com.foodapp.food4ufrontend.model.Order;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.beans.Observable;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SellerDashboard {

    @FXML private ListView<String> actionList;
    @FXML private Label errorMessageLabel;
    @FXML private TabPane mainTabPane;

    @FXML private TextField searchRestaurantField; // Not directly used in SellerDashboardView.fxml, consider removing
    @FXML private TableView<Restaurant> myRestaurantsTable;
    @FXML private TableColumn<Restaurant, String> myRestaurantIdColumn;
    @FXML private TableColumn<Restaurant, String> myRestaurantNameColumn;
    @FXML private TableColumn<Restaurant, String> myRestaurantAddressColumn;
    @FXML private TableColumn<Restaurant, String> myRestaurantPhoneColumn;
    @FXML private TableColumn<Restaurant, Integer> myRestaurantTaxFeeColumn;
    @FXML private TableColumn<Restaurant, Integer> myRestaurantAdditionalFeeColumn;

    @FXML private ComboBox<String> selectRestaurantForOrders;
    @FXML private ComboBox<String> filterOrderStatus;
    @FXML private TableView<Order> restaurantOrdersTable;
    @FXML private TableColumn<Order, Integer> sellerOrderIdColumn;
    @FXML private TableColumn<Order, Integer> sellerOrderCustomerColumn;
    @FXML private TableColumn<Order, String> sellerOrderAddressColumn;
    @FXML private TableColumn<Order, String> sellerOrderStatusColumn;
    @FXML private TableColumn<Order, Integer> sellerOrderPriceColumn;
    @FXML private TableColumn<Order, String> sellerOrderCreatedAtColumn;

    // FXML for included UserProfileView
    @FXML private UserProfileController userProfileViewController;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    // FIX: Explicitly specify generic type to resolve ambiguity
    private ObservableList<Restaurant> sellerRestaurants = FXCollections.<Restaurant>observableArrayList();

    @FXML
    public void initialize() {
        ObservableList<String> actions = FXCollections.observableArrayList(
                "My Restaurants",
                "Manage Menu",
                "Restaurant Orders",
                "My Profile",
                "Manage Fees",
                "Logout"
        );
        actionList.setItems(actions);

        actionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleActionSelection(newValue);
            }
        });

        // Initialize My Restaurants Table Columns
        if (myRestaurantIdColumn != null) myRestaurantIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (myRestaurantNameColumn != null) myRestaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (myRestaurantAddressColumn != null) myRestaurantAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (myRestaurantPhoneColumn != null) myRestaurantPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (myRestaurantTaxFeeColumn != null) myRestaurantTaxFeeColumn.setCellValueFactory(new PropertyValueFactory<>("taxFee"));
        if (myRestaurantAdditionalFeeColumn != null) myRestaurantAdditionalFeeColumn.setCellValueFactory(new PropertyValueFactory<>("additionalFee"));

        // Initialize Restaurant Orders Table Columns
        if (sellerOrderIdColumn != null) sellerOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (sellerOrderCustomerColumn != null) sellerOrderCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        if (sellerOrderAddressColumn != null) sellerOrderAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (sellerOrderStatusColumn != null) sellerOrderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (sellerOrderPriceColumn != null) sellerOrderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        if (sellerOrderCreatedAtColumn != null) sellerOrderCreatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Setup filterOrderStatus ComboBox
        ObservableList<String> orderStatuses = FXCollections.observableArrayList(
                "All Statuses", "submitted", "unpaid and cancelled", "waiting vendor",
                "cancelled", "finding courier", "on the way", "completed", "accepted", "rejected", "served"
        );
        if (filterOrderStatus != null) {
            filterOrderStatus.setItems(orderStatuses);
            filterOrderStatus.getSelectionModel().selectFirst();
            filterOrderStatus.valueProperty().addListener((obs, oldVal, newVal) -> viewRestaurantOrders());
        }

        // Initial data loading when dashboard is opened
        viewMyRestaurants();
        viewRestaurantOrders();
    }

    private void handleActionSelection(String action) {
        switch (action) {
            case "My Restaurants":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(0);
                viewMyRestaurants();
                break;
            case "Manage Menu":
                errorMessageLabel.setText("Manage Menu functionality not yet implemented.");
                break;
            case "Restaurant Orders":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(1);
                viewRestaurantOrders();
                break;
            case "My Profile":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(2);
                break;
            case "Manage Fees":
                errorMessageLabel.setText("Manage Fees functionality not yet implemented.");
                break;
            case "Logout":
                logout();
                break;
            default:
                break;
        }
    }

    @FXML
    private void viewMyRestaurants() {
        errorMessageLabel.setText("Loading your restaurants...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/restaurants/mine", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                // FIX: Explicitly specify generic type to resolve ambiguity
                                sellerRestaurants = FXCollections.<Restaurant>observableArrayList(
                                        (Callback<Restaurant, Observable[]>) JsonUtil.getObjectMapper().readerForListOf(Restaurant.class).readValue(rootNode)
                                );
                                myRestaurantsTable.setItems(sellerRestaurants);

                                ObservableList<String> restaurantNames = FXCollections.observableArrayList();
                                sellerRestaurants.forEach(r -> restaurantNames.add(r.getName() + " (ID: " + r.getId() + ")"));
                                selectRestaurantForOrders.setItems(restaurantNames);
                                if (!restaurantNames.isEmpty()) {
                                    selectRestaurantForOrders.getSelectionModel().selectFirst();
                                    selectRestaurantForOrders.valueProperty().addListener((obs, oldVal, newVal) -> viewRestaurantOrders());
                                }
                                errorMessageLabel.setText("Your restaurants loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing restaurant data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing your restaurants: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for restaurants."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching your restaurants: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void addNewRestaurant() {
        errorMessageLabel.setText("Add New Restaurant functionality not yet implemented.");
    }

    @FXML
    private void viewRestaurantOrders() {
        errorMessageLabel.setText("Loading restaurant orders...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Restaurant selectedRestaurant = null;
                if (selectRestaurantForOrders.getSelectionModel().getSelectedItem() != null) {
                    String selectedRestaurantNameId = selectRestaurantForOrders.getSelectionModel().getSelectedItem();
                    String idStr = selectedRestaurantNameId.substring(selectedRestaurantNameId.lastIndexOf("ID: ") + 4, selectedRestaurantNameId.lastIndexOf(")"));
                    String restaurantId = idStr;

                    selectedRestaurant = sellerRestaurants.stream()
                            .filter(r -> r.getId() != null && r.getId().equals(restaurantId))
                            .findFirst()
                            .orElse(null);
                }

                if (selectedRestaurant == null) {
                    Platform.runLater(() -> errorMessageLabel.setText("Please select a restaurant to view orders."));
                    return;
                }

                String path = "/restaurants/" + selectedRestaurant.getId() + "/orders";
                String selectedStatus = filterOrderStatus.getSelectionModel().getSelectedItem();
                if (selectedStatus != null && !selectedStatus.equals("All Statuses")) {
                    path += "?status=" + selectedStatus;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                ObservableList<Order> orderObservableList = FXCollections.observableArrayList(orders);
                                restaurantOrdersTable.setItems(orderObservableList);
                                errorMessageLabel.setText("Restaurant orders loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing orders data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing restaurant orders: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for restaurant orders."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching restaurant orders: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void markOrderAccepted() {
        updateOrderStatus("accepted");
    }

    @FXML
    private void markOrderRejected() {
        updateOrderStatus("rejected");
    }

    @FXML
    private void markOrderServed() {
        updateOrderStatus("served");
    }

    private void updateOrderStatus(String status) {
        Order selectedOrder = restaurantOrdersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            errorMessageLabel.setText("Please select an order to update its status.");
            return;
        }

        errorMessageLabel.setText("Updating order " + selectedOrder.getId() + " status to " + status + "...");

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Map<String, String> statusData = new HashMap<>();
                statusData.put("status", status);
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(statusData);

                Optional<HttpResponse<String>> responseOpt = ApiClient.patch("/restaurants/orders/" + selectedOrder.getId(), jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText("Order status updated successfully.");
                            viewRestaurantOrders(); // Refresh orders list
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error updating order status: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for order status update."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred during order status update: " + e.getMessage());
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
