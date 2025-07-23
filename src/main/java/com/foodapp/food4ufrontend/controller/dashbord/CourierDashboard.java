package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.AvailableDelivery;
import com.foodapp.food4ufrontend.model.Order;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane; // اضافه شده

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourierDashboard {

    @FXML private ListView<String> actionList;
    @FXML private Label errorMessageLabel;
    @FXML private TabPane mainTabPane;

    @FXML private TableView<AvailableDelivery> availableDeliveriesTable;
    @FXML private TableColumn<AvailableDelivery, Integer> availableOrderIdColumn;
    @FXML private TableColumn<AvailableDelivery, Integer> availableVendorIdColumn;
    @FXML private TableColumn<AvailableDelivery, String> availableDeliveryAddressColumn;
    @FXML private TableColumn<AvailableDelivery, String> availableStatusColumn;

    @FXML private TableView<Order> deliveryHistoryTable;
    @FXML private TableColumn<Order, Integer> historyOrderIdColumn;
    @FXML private TableColumn<Order, Integer> historyVendorIdColumn;
    @FXML private TableColumn<Order, String> historyDeliveryAddressColumn;
    @FXML private TableColumn<Order, String> historyStatusColumn;
    @FXML private TableColumn<Order, String> historyUpdatedAtColumn;

    // FXML for included UserProfileView (این فیلد ممکن است دیگر ضروری نباشد اگر به صورت دستی کنترلر را فراخوانی می‌کنید)
    @FXML private UserProfileController userProfileViewController;

    @FXML private AnchorPane myProfileContainer; // اضافه شده: کانتینر برای بارگذاری پروفایل کاربر

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        ObservableList<String> actions = FXCollections.observableArrayList(
                "Available Deliveries",
                "Delivery History",
                "My Profile",
                "Logout"
        );
        actionList.setItems(actions);

        actionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleActionSelection(newValue);
            }
        });

        // Initialize Available Deliveries Table Columns
        if (availableOrderIdColumn != null) availableOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (availableVendorIdColumn != null) availableVendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("restaurantId"));
        if (availableDeliveryAddressColumn != null) availableDeliveryAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (availableStatusColumn != null) availableStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize Delivery History Table Columns
        if (historyOrderIdColumn != null) historyOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        if (historyVendorIdColumn != null) historyVendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (historyDeliveryAddressColumn != null) historyDeliveryAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (historyStatusColumn != null) historyStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (historyUpdatedAtColumn != null) historyUpdatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        // Load initial data
        viewAvailableDeliveries();
        viewDeliveryHistory();
    }

    private void handleActionSelection(String action) {
        switch (action) {
            case "Available Deliveries":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(0);
                viewAvailableDeliveries();
                break;
            case "Delivery History":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(1);
                viewDeliveryHistory();
                break;
            case "My Profile":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(2);
                loadUserProfileView(); // فراخوانی متد جدید
                break;
            case "Logout":
                logout();
                break;
            default:
                break;
        }
    }

    // متد جدید برای بارگذاری پویا UserProfileView.fxml
    private void loadUserProfileView() {
        errorMessageLabel.setText("Loading profile view...");
        executorService.submit(() -> {
            try {
                // این بارگذاری به صورت مستقیم از classpath است
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/UserProfileView.fxml"));
                Parent userProfileView = loader.load(); // تلاش برای بارگذاری FXML

                Platform.runLater(() -> {
                    if (myProfileContainer != null) {
                        myProfileContainer.getChildren().setAll(userProfileView); // تزریق محتوای بارگذاری شده به کانتینر
                        errorMessageLabel.setText("Profile view loaded successfully.");

                        // اگر UserProfileController نیاز به مقداردهی اولیه یا بارگذاری داده دارد،
                        // می‌توانید کنترلر را دریافت کرده و متدهای آن را فراخوانی کنید.
                        // UserProfileController userProfileControllerInstance = loader.getController();
                        // if (userProfileControllerInstance != null) {
                        //     userProfileControllerInstance.loadUserProfile(); // فرض بر وجود چنین متدی در UserProfileController
                        // }
                    } else {
                        errorMessageLabel.setText("Error: My profile container (myProfileContainer) is null in FXML.");
                    }
                });
            } catch (IOException e) {
                // اگر بارگذاری در اینجا هم با شکست مواجه شود، خطای دقیق‌تری خواهیم داشت
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Critical Error loading User Profile View dynamically: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewAvailableDeliveries() {
        errorMessageLabel.setText("Loading available deliveries...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/deliveries/available", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<AvailableDelivery> deliveries = JsonUtil.getObjectMapper()
                                        .readerForListOf(AvailableDelivery.class)
                                        .readValue(rootNode);
                                ObservableList<AvailableDelivery> observableDeliveries = FXCollections.observableArrayList(deliveries);
                                availableDeliveriesTable.setItems(observableDeliveries);

                                errorMessageLabel.setText("Available deliveries loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing available deliveries: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing available deliveries: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for available deliveries."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching available deliveries: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void acceptDelivery() {
        updateDeliveryStatus("accepted");
    }

    @FXML
    private void markDeliveryReceived() {
        updateDeliveryStatus("received");
    }

    @FXML
    private void markDeliveryDelivered() {
        updateDeliveryStatus("delivered");
    }

    private void updateDeliveryStatus(String status) {
        AvailableDelivery selectedOrder = availableDeliveriesTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            errorMessageLabel.setText("Please select a delivery to update its status.");
            return;
        }

        errorMessageLabel.setText("Updating delivery " + selectedOrder.getId() + " status to " + status + "...");

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

                Optional<HttpResponse<String>> responseOpt = ApiClient.patch("/deliveries/" + selectedOrder.getId(), jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText("Delivery status updated successfully.");
                            viewAvailableDeliveries(); // Refresh available deliveries
                            viewDeliveryHistory(); // Refresh history as well
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error updating delivery status: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for delivery status update."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while saving delivery: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewDeliveryHistory() {
        errorMessageLabel.setText("Loading delivery history...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/deliveries/history", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                ObservableList<Order> orderObservableList = FXCollections.observableArrayList(orders);
                                deliveryHistoryTable.setItems(orderObservableList);
                                errorMessageLabel.setText("Delivery history loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing delivery history: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing delivery history: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for delivery history."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching delivery history: " + e.getMessage());
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
                stage.setMaximized(true);
                stage.show();
            }
            catch (IOException e) {
                e.printStackTrace();
                errorMessageLabel.setText("Error navigating to login: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}