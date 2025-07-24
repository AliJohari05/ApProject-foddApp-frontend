package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.Restaurant;
import com.foodapp.food4ufrontend.model.Order;
import com.foodapp.food4ufrontend.model.Transaction;
import com.foodapp.food4ufrontend.model.User;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Node;

import javafx.scene.control.RadioButton;

import javafx.scene.control.ToggleGroup;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.control.TextField; // if not already imported

import javax.swing.*;
import java.math.BigDecimal; // Import for walletBalance

public class BuyerDashboard {

    @FXML
    private ListView<String> actionList;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private TabPane mainTabPane;

    @FXML
    private TextField searchRestaurantField;
    @FXML
    private TableView<Restaurant> restaurantsTable;
    @FXML
    private TableColumn<Restaurant, String> restaurantIdColumn;
    @FXML
    private TableColumn<Restaurant, String> restaurantNameColumn;
    @FXML
    private TableColumn<Restaurant, String> restaurantAddressColumn;
    @FXML
    private TableColumn<Restaurant, String> restaurantPhoneColumn;

    @FXML
    private TableView<Order> orderHistoryTable;
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, Integer> orderVendorColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;
    @FXML
    private TableColumn<Order, Integer> orderPriceColumn;
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    @FXML
    private ToggleGroup paymentMethodGroup;
    @FXML
    private RadioButton walletRadioButton;
    @FXML
    private RadioButton onlineRadioButton;

    @FXML
    private TableView<Restaurant> favoriteRestaurantTable;
    @FXML
    private TableColumn<Restaurant, String> favRestaurantIdColumn;
    @FXML
    private TableColumn<Restaurant, String> favRestaurantNameColumn;
    @FXML
    private TableColumn<Restaurant, String> favRestaurantAddressColumn;
    @FXML
    private TableColumn<Restaurant, String> favRestaurantPhoneColumn;

    @FXML
    private Label currentWalletBalanceLabel;
    @FXML
    private TextField topUpAmountField;
    @FXML
    private TableView<Transaction> transactionsTable;
    @FXML
    private TableColumn<Transaction, Integer> transactionIdColumn;
    @FXML
    private TableColumn<Transaction, Integer> transactionOrderIdColumn;
    @FXML
    private TableColumn<Transaction, Integer> transactionUserIdColumn;
    @FXML
    private TableColumn<Transaction, String> transactionMethodColumn;
    @FXML
    private TableColumn<Transaction, String> transactionStatusColumn;

    @FXML
    private UserProfileController userProfileViewController;
    @FXML private MFXButton viewMenuButton;
    @FXML
    private AnchorPane myProfileContainer;

    @FXML private MFXButton rateOrderButton; // NEW: Field for Rate Order Button
    @FXML private TextField searchOrderField;
    @FXML private TextField searchVendorField;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        ObservableList<String> actions = FXCollections.observableArrayList(
                "View Restaurants",
                "Order History",
                "My Profile",
                "Manage Favorites",
                "Wallet and Payments",
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
        if (restaurantAddressColumn != null)
            restaurantAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (restaurantPhoneColumn != null)
            restaurantPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Initialize Order History Table Columns
        if (orderIdColumn != null) orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (orderVendorColumn != null) orderVendorColumn.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
        if (orderStatusColumn != null) orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (orderPriceColumn != null) orderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        if (orderDateColumn != null) orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        if (paymentMethodGroup != null) {walletRadioButton.setSelected(true);}

        if (favoriteRestaurantTable != null) {
            favRestaurantIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            favRestaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            favRestaurantAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            favRestaurantPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        }
        if (transactionsTable != null) {
            transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            transactionOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
            transactionUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("UserId"));
            transactionMethodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
            transactionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        // Load initial data
        viewRestaurants();
        viewOrderHistory();
        viewFavoriteRestaurants();
        viewWalletAndPayments();
        searchOrderField.clear();
        searchVendorField.clear();
        viewOrderHistory();

    }
    @FXML
    private void handleSearchOrders() {
        String search = searchOrderField.getText();
        String vendor = searchVendorField.getText();
        loadOrderHistoryWithFilters(search, vendor);
    }

    private void handleActionSelection(String action) {
        switch (action) {
            case "View Restaurants":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(0);
                viewRestaurants();
                break;
            case "Order History":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(1);
                viewOrderHistory();
                break;
            case "My Profile":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(2);
                loadUserProfileView();
                break;
            case "Manage Favorites":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(3);
                viewFavoriteRestaurants();
                break;
            case "Wallet and Payments":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(4);
                viewWalletAndPayments();
                break;
            case "Logout":
                logout();
                break;
            default:
                break;
        }
    }
    private void loadUserProfileView() {
        errorMessageLabel.setText("Loading profile view...");
        executorService.submit(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/UserProfileView.fxml"));
                Parent userProfileView = loader.load();

                Platform.runLater(() -> {
                    if (myProfileContainer != null) {
                        myProfileContainer.getChildren().setAll(userProfileView); // تزریق محتوای بارگذاری شده به کانتینر
                        errorMessageLabel.setText("Profile view loaded successfully.");

                    } else {
                        errorMessageLabel.setText("Error: My profile container (myProfileContainer) is null in FXML.");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Critical Error loading User Profile View dynamically: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
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

                Map<String, String> requestBody = new HashMap<>();
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
                    requestBody.put("search", searchTerm);
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
    private void handleMakePayment(ActionEvent event){
        Order selectedOrder = orderHistoryTable.getSelectionModel().getSelectedItem();
        if(selectedOrder == null){
            errorMessageLabel.setText("Please select an order to pay");
            return;
        }
        String paymentMethod;
        if (paymentMethodGroup.getSelectedToggle() != null){
            paymentMethod = (String) paymentMethodGroup.getSelectedToggle().getUserData();
        } else {
            paymentMethod = null;
        }
        if(paymentMethod == null || paymentMethod.isEmpty()){
            errorMessageLabel.setText("Please select a payment method");
            return;
        }
        errorMessageLabel.setText("Paying price with order id : " + selectedOrder.getId() );
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()){
                    Platform.runLater(() -> {errorMessageLabel.setText("Authentication token is missing. Please login again.");});
                    return;
                }
                Integer orderId = selectedOrder.getId();
                Map<String,Object> paymentData = new HashMap<>();
                paymentData.put("order_id",orderId);
                paymentData.put("method",paymentMethod);
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(paymentData);
                Optional<HttpResponse<String>> responseOptional= ApiClient.post("/payment/online",jsonBody,token);
                if(responseOptional.isPresent()){
                    HttpResponse<String> response = responseOptional.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                    Platform.runLater(() -> {
                    if (response.statusCode() == 200){

                        errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Payment was successful");
                        viewOrderHistory();
                        viewWalletAndPayments();
                    }else {
                        errorMessageLabel.setText("Error in payment :" + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                    }
                    });
                }else {
                    Platform.runLater(() ->{errorMessageLabel.setText("Could not connect to server to Payment");});
                }

            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred during payment: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    @FXML
    private void viewFavoriteRestaurants() {
        errorMessageLabel.setText("Loading favorite restaurant ...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/favorites", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                // تبدیل JSON به لیست آبجکت‌های Restaurant
                                List<Restaurant> favorites = JsonUtil.getObjectMapper().readerForListOf(Restaurant.class).readValue(rootNode);
                                ObservableList<Restaurant> favoriteObservableList = FXCollections.observableArrayList(favorites);
                                favoriteRestaurantTable.setItems(favoriteObservableList);
                                errorMessageLabel.setText("Favorite restaurants successfully loaded.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing favorite restaurants data:" + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing favorite restaurants:" + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Could not connect to server for favorite restaurants."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Unexpected error fetching favorite restaurants: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    @FXML
    private void handleAddFavorite(javafx.event.ActionEvent event) {
        Restaurant selectedRestaurant = restaurantsTable.getSelectionModel().getSelectedItem();
        if (selectedRestaurant == null) {
            errorMessageLabel.setText("Please select a restaurant to add to favorites.");
            return;
        }

        errorMessageLabel.setText("Adding " + selectedRestaurant.getName() + " to favorite...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token does not exist. Please log in again."));
                    return;
                }

                String restaurantId = selectedRestaurant.getId();
                Optional<HttpResponse<String>> responseOpt = ApiClient.put("/favorites/" + restaurantId, "", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Restaurant successfully added to favorites!");
                            viewFavoriteRestaurants();
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error in adding to favorite: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Could not connect to server to add to favorites."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Unexpected error adding to favorites:" + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    @FXML
    private void handleRemoveFavorite(javafx.event.ActionEvent event) { // از javafx.event.ActionEvent استفاده شود
        Restaurant selectedFavorite = favoriteRestaurantTable.getSelectionModel().getSelectedItem(); // رستوران انتخاب شده از جدول علاقه‌مندی‌ها
        if (selectedFavorite == null) {
            errorMessageLabel.setText("Please select a restaurant to remove from favorites.");
            return;
        }

        errorMessageLabel.setText("Removing " + selectedFavorite.getName() + " from favorites...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken(); // دریافت توکن احراز هویت
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token does not exist. Please log in again."));
                    return;
                }

                String restaurantId = selectedFavorite.getId();
                // API برای حذف از علاقه‌مندی‌ها یک DELETE request با restaurantId در مسیر نیاز دارد.
                Optional<HttpResponse<String>> responseOpt = ApiClient.delete("/favorites/" + restaurantId, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body()); // تجزیه پاسخ JSON

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "The restaurant was successfully removed from favorites.");
                            viewFavoriteRestaurants(); // رفرش لیست علاقه‌مندی‌ها
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error during removing: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Could not connect to server to remove from favorites."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Unexpected error while removing from favorites:" + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    @FXML
    private void viewWalletAndPayments() {
        errorMessageLabel.setText("Loading wallet and payment info ...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();

                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> profileResponseOptional = ApiClient.get("/auth/profile", token);
                if (profileResponseOptional.isPresent()) {
                    HttpResponse<String> profileResponse = profileResponseOptional.get();
                    JsonNode profileRootNode = JsonUtil.getObjectMapper().readTree(profileResponse.body());
                    Platform.runLater(() -> {
                        if (profileResponse.statusCode() == 200) {
                            try {
                                User currentUser = JsonUtil.getObjectMapper().treeToValue(profileRootNode, User.class);
                                currentWalletBalanceLabel.setText(currentUser.getWalletBalance() != null ? currentUser.getWalletBalance().toPlainString() : "0.00");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing profile data " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMassage = profileRootNode.has("error") ? profileRootNode.get("error").asText() : "An Unknown error occurred.";
                            errorMessageLabel.setText("Error loading wallet balance :" + errorMassage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for wallet balance."));
                }
                Optional<HttpResponse<String>> transactionResponseOpt = ApiClient.get("/transactions", token);
                if (transactionResponseOpt.isPresent()) {
                    HttpResponse<String> transactionResponse = transactionResponseOpt.get();
                    JsonNode transactionRootNode = JsonUtil.getObjectMapper().readTree(transactionResponse.body());
                    Platform.runLater( () -> {
                        if (transactionResponse.statusCode() == 200) {
                            try {
                                List<Transaction> transactions = JsonUtil.getObjectMapper().readerForListOf(Transaction.class).readValue(transactionRootNode);
                                ObservableList<Transaction> transactionObservableList = FXCollections.observableArrayList(transactions);
                                transactionsTable.setItems(transactionObservableList);
                                errorMessageLabel.setText("Wallet and transaction history loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing transaction data " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = transactionRootNode.has("error") ? transactionRootNode.get("error").asText() : "An unknown error occurred ";
                            errorMessageLabel.setText("Error viewing transaction :" + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> {errorMessageLabel.setText("Failed to connect to server for transaction history");});
                }


            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while loading wallet and transactions: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    @FXML
    private void handleTopUp(ActionEvent event) {
        String amountText = topUpAmountField.getText();
        if(amountText.isEmpty()){
            errorMessageLabel.setText("Please enter an amount to top up");
            return;
        }
        try{
            double amount = Double.parseDouble(amountText);
            if (amount < 0){
                errorMessageLabel.setText ("Amount must be a positive number.");
                return;
            }
            errorMessageLabel.setText("Topping up wallet with "+ amount +"...");
            executorService.submit(() -> {
                try {
                   String token= AuthManager.getJwtToken();
                   if (token == null || token.isEmpty()){
                       Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing.Please login again."));
                       return;
                   }
                   Map<String,Object> requestBody = new HashMap<>();
                   requestBody.put("amount",amount);
                   String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);
                   Optional<HttpResponse<String>> responseOptional = ApiClient.post("/wallet/top-up",jsonBody,token);
                   if (responseOptional.isPresent()){
                       HttpResponse<String> response = responseOptional.get();
                       JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                       Platform.runLater(() -> {
                           if (response.statusCode() == 200) {
                               errorMessageLabel.setText(rootNode.has("message" )? rootNode.get("message").asText() : "Wallet topped up successfully");
                               topUpAmountField.clear();
                               viewWalletAndPayments();
                           }else {
                               errorMessageLabel.setText("Error topping up wallet :" + (rootNode.has("error")?rootNode.get("error").asText() : "An unknown error occurred"));
                           }
                       });
                   }else {
                       Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for top up"));
                   }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> {errorMessageLabel.setText("An unexpected error in during top up : " + e.getMessage());
                    e.printStackTrace();
                    });
                }
            });
        } catch (NumberFormatException e) {
            Platform.runLater(() -> {errorMessageLabel.setText("Please enter a valid number for amount.");});
        }
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
            } catch (IOException e) {
                e.printStackTrace();
                errorMessageLabel.setText("Error navigating to login: " + e.getMessage());
            }
        });
    }
    public void shutdown() {
        executorService.shutdown();
    }
@FXML
    private void handleViewMenu(ActionEvent event) {
    errorMessageLabel.setText("");

    Restaurant selectedRestaurant = restaurantsTable.getSelectionModel().getSelectedItem(); //
    if (selectedRestaurant == null) {
        errorMessageLabel.setText("Please select a restaurant to view its menu."); //
        return;
    }

    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/RestaurantMenuView.fxml")); //
        Parent restaurantMenuView = loader.load();

        RestaurantMenuController controller = loader.getController();

        controller.setRestaurant(selectedRestaurant);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Menu for " + selectedRestaurant.getName());
        Scene scene = new Scene(restaurantMenuView);
        scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm()); // اعمال استایل CSS
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.showAndWait();

    } catch (IOException e) {
        errorMessageLabel.setText("Error opening menu view: " + e.getMessage()); //
        e.printStackTrace(); //
    }
    }
    @FXML
    private void handleRateOrder(ActionEvent event) {
        errorMessageLabel.setText("");

        Order selectedOrder = orderHistoryTable.getSelectionModel().getSelectedItem(); //
        if (selectedOrder == null) {
            errorMessageLabel.setText("Please select an order to rate."); //
            return;
        }

        if (!"completed".equalsIgnoreCase(selectedOrder.getStatus())) { //
            errorMessageLabel.setText("Only completed orders can be rated."); //
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/RatingFormView.fxml")); //
            Parent ratingFormView = loader.load(); //

            RatingFormController controller = loader.getController(); //

            controller.setOrder(selectedOrder);

            controller.setRefreshOrderHistoryCallback(aVoid -> viewOrderHistory()); //

            Stage stage = new Stage(); //
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Rate Order #" + selectedOrder.getId());
            Scene scene = new Scene(ratingFormView);
            scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm()); // اعمال استایل CSS
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.showAndWait();

        } catch (IOException e) {
            errorMessageLabel.setText("Error opening rating form: " + e.getMessage()); //
            e.printStackTrace();
        }
    }

    private void loadOrderHistoryWithFilters(String search, String vendor) {
        errorMessageLabel.setText("Filtering order history...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing."));
                    return;
                }

                StringBuilder urlBuilder = new StringBuilder("/orders/history?");
                if (search != null && !search.isBlank()) {
                    urlBuilder.append("search=").append(URLEncoder.encode(search, StandardCharsets.UTF_8)).append("&");
                }
                if (vendor != null && !vendor.isBlank()) {
                    urlBuilder.append("vendor=").append(URLEncoder.encode(vendor, StandardCharsets.UTF_8)).append("&");
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get(urlBuilder.toString(), token);
                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper()
                                        .readerForListOf(Order.class)
                                        .readValue(rootNode);
                                orderHistoryTable.setItems(FXCollections.observableArrayList(orders));
                                errorMessageLabel.setText("Order history filtered.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Parsing error: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "Error loading filtered orders.";
                            errorMessageLabel.setText(errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Server connection failed."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Unexpected error: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

}