package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.FoodItem;
import com.foodapp.food4ufrontend.model.Restaurant;
import com.foodapp.food4ufrontend.model.Order;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.layout.AnchorPane; // اضافه شده
import javafx.scene.Node; // اضافه شده

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SellerDashboard {


    @FXML
    private ListView<String> actionList;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private TabPane mainTabPane;

    @FXML
    private TextField searchRestaurantField; // Not directly used in SellerDashboardView.fxml, consider removing
    @FXML
    private TableView<Restaurant> myRestaurantsTable;
    @FXML
    private TableColumn<Restaurant, String> myRestaurantIdColumn;
    @FXML
    private TableColumn<Restaurant, String> myRestaurantNameColumn;
    @FXML
    private TableColumn<Restaurant, String> myRestaurantAddressColumn;
    @FXML
    private TableColumn<Restaurant, String> myRestaurantPhoneColumn;
    @FXML
    private TableColumn<Restaurant, Integer> myRestaurantTaxFeeColumn;
    @FXML
    private TableColumn<Restaurant, Integer> myRestaurantAdditionalFeeColumn;

    @FXML
    private ComboBox<String> selectRestaurantForOrders;
    @FXML
    private ComboBox<String> filterOrderStatus;
    @FXML
    private TableView<Order> restaurantOrdersTable;
    @FXML
    private TableColumn<Order, Integer> sellerOrderIdColumn;
    @FXML
    private TableColumn<Order, Integer> sellerOrderCustomerColumn;
    @FXML
    private TableColumn<Order, String> sellerOrderAddressColumn;
    @FXML
    private TableColumn<Order, String> sellerOrderStatusColumn;
    @FXML
    private TableColumn<Order, Integer> sellerOrderPriceColumn;
    @FXML
    private TableColumn<Order, String> sellerOrderCreatedAtColumn;

    @FXML
    private TableView<FoodItem> foodItemTable;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemIdColumn;
    @FXML
    private TableColumn<FoodItem, String> foodItemNameColumn;
    @FXML
    private TableColumn<FoodItem, String> foodItemDescription;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemVendorId;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemPrice;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemSupply;
    @FXML
    private TableColumn<FoodItem, String> foodItemKeywords;
    @FXML
    private ComboBox<String> selectRestaurantForMenu;
    @FXML
    public ComboBox<String> selectMenuComboBox;

    @FXML
    private MFXButton viewRatingsButton;
    @FXML
    private MFXButton viewOrderRatingButton;
    private String selectedRestaurantForMenuId;

    private String selectedMenuTitle;

    @FXML
    private UserProfileController userProfileViewController;

    @FXML
    private AnchorPane myProfileContainer;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    // FIX: Explicitly specify generic type to resolve ambiguity
    private ObservableList<Restaurant> sellerRestaurants = FXCollections.<Restaurant>observableArrayList();

    @FXML
    public void initialize() {
        ObservableList<String> actions = FXCollections.observableArrayList(
                "My Restaurants",
                "Manage menu",
                "Restaurant Orders",
                "My Profile",
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
        if (myRestaurantNameColumn != null)
            myRestaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (myRestaurantAddressColumn != null)
            myRestaurantAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (myRestaurantPhoneColumn != null)
            myRestaurantPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (myRestaurantTaxFeeColumn != null)
            myRestaurantTaxFeeColumn.setCellValueFactory(new PropertyValueFactory<>("taxFee"));
        if (myRestaurantAdditionalFeeColumn != null)
            myRestaurantAdditionalFeeColumn.setCellValueFactory(new PropertyValueFactory<>("additionalFee"));

        // Initialize Restaurant Orders Table Columns
        if (sellerOrderIdColumn != null) sellerOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (sellerOrderCustomerColumn != null)
            sellerOrderCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        if (sellerOrderAddressColumn != null)
            sellerOrderAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (sellerOrderStatusColumn != null)
            sellerOrderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (sellerOrderPriceColumn != null)
            sellerOrderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        if (sellerOrderCreatedAtColumn != null)
            sellerOrderCreatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        if (foodItemIdColumn != null) foodItemIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (foodItemNameColumn != null) foodItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (foodItemDescription != null)
            foodItemDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (foodItemPrice != null) foodItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (foodItemVendorId != null) foodItemVendorId.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (foodItemSupply != null) foodItemSupply.setCellValueFactory(new PropertyValueFactory<>("supply"));
        if (foodItemKeywords != null) {
            foodItemKeywords.setCellValueFactory(cellData -> {
                List<String> keywords = cellData.getValue().getKeywords();
                String joined = (keywords != null) ? String.join(", ", keywords) : "";
                return new SimpleStringProperty(joined);
            });

        }


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
        if (selectRestaurantForMenu != null) {
            selectRestaurantForMenu.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("(ID: ")) {
                    int idStartIndex = newVal.lastIndexOf("(ID: ") + 5;
                    int idEndIndex = newVal.lastIndexOf(")");
                    selectedRestaurantForMenuId = newVal.substring(idStartIndex, idEndIndex);
                    errorMessageLabel.setText("Selected restaurant for menu: " + selectedRestaurantForMenuId);
                    viewManageMenu();
                } else {
                    selectedRestaurantForMenuId = null;
                    foodItemTable.setItems(FXCollections.emptyObservableList());
                    errorMessageLabel.setText("Please select a valid restaurant");
                }
            });
        }
        if (selectMenuComboBox !=null){
            selectMenuComboBox.valueProperty().addListener((obs,oldVal,newVal)->{
                if (newVal != null && !newVal.isEmpty()){
                    selectedMenuTitle=newVal;
                    errorMessageLabel.setText("Selected menu: "+selectedMenuTitle);
                }
            });
        }else {
            selectedMenuTitle=null;
            errorMessageLabel.setText("Please select a valid menu");
        }

        if (selectRestaurantForOrders != null) {
            selectRestaurantForOrders.valueProperty().addListener((obs, oldVal, newVal) -> {
                viewRestaurantOrders();
            });
        }

        // Initial data loading when dashboard is opened
        viewMyRestaurants();
        viewRestaurantOrders();
        viewManageMenu();
    }

    private void handleActionSelection(String action) {
        switch (action) {
            case "My Restaurants":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(0);
                viewMyRestaurants();
                break;
            case "Manage menu":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(1);
                viewManageMenu();
                break;
            case "Restaurant Orders":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(2);
                viewRestaurantOrders();
                break;
            case "My Profile":
                if (mainTabPane != null) mainTabPane.getSelectionModel().select(3);
                loadUserProfileView();
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
                        myProfileContainer.getChildren().setAll(userProfileView);
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
                                List<Restaurant> list = JsonUtil
                                        .getObjectMapper()
                                        .readerForListOf(Restaurant.class)
                                        .readValue(rootNode);
                                sellerRestaurants = FXCollections.observableList(list);
                                myRestaurantsTable.setItems(sellerRestaurants);

                                ObservableList<String> restaurantNames = FXCollections.observableArrayList();
                                sellerRestaurants.forEach(r -> restaurantNames.add(r.getName() + " (ID: " + r.getId() + ")"));
                                selectRestaurantForOrders.setItems(restaurantNames);
                                selectRestaurantForMenu.setItems(restaurantNames);
                                if (!restaurantNames.isEmpty()) {
                                    selectRestaurantForOrders.getSelectionModel().selectFirst();
                                    selectRestaurantForMenu.getSelectionModel().selectFirst();
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
    private void handleViewItemRatings(ActionEvent event) {
        errorMessageLabel.setText("");

        FoodItem selectedFoodItem = foodItemTable.getSelectionModel().getSelectedItem(); //
        if (selectedFoodItem == null) {
            errorMessageLabel.setText("Please select a food item to view its ratings."); //
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/ItemRatingsView.fxml")); //
            Parent itemRatingsView = loader.load(); //

            ItemRatingsController controller = loader.getController(); //

            controller.setFoodItem(selectedFoodItem); // متد جدید در ItemRatingsController

            Stage stage = new Stage(); //
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ratings for " + selectedFoodItem.getName()); // عنوان پنجره
            Scene scene = new Scene(itemRatingsView); //
            scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm()); // اعمال استایل CSS
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            errorMessageLabel.setText("Error opening ratings view: " + e.getMessage()); //
            e.printStackTrace(); //
        }
    }

    @FXML
    private void handleViewOrderRating(ActionEvent event) {
        errorMessageLabel.setText("");

        Order selectedOrder = restaurantOrdersTable.getSelectionModel().getSelectedItem(); //
        if (selectedOrder == null) {
            errorMessageLabel.setText("Please select an order to view its rating."); //
            return;
        }


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/OrderRatingsView.fxml")); // NEW: فایل FXML جدید
            Parent orderRatingsView = loader.load();

            OrderRatingsController controller = loader.getController(); // NEW: کنترلر جدید

            controller.setOrder(selectedOrder); // متد جدید در OrderRatingsController

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); //
            stage.setTitle("Rating for Order #" + selectedOrder.getId()); //
            Scene scene = new Scene(orderRatingsView);
            scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm()); //
            stage.setScene(scene);
            stage.showAndWait(); //

        } catch (IOException e) {
            errorMessageLabel.setText("Error opening order ratings view: " + e.getMessage()); //
            e.printStackTrace();
        }
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
    private void viewManageMenu() {
        errorMessageLabel.setText("Loading manage menu ...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Authentication token is missing. Please log in again");
                    });
                    return;
                }
                if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Please select a valid restaurant");
                        foodItemTable.setItems(FXCollections.emptyObservableList());
                    });
                    return;
                }
                String restaurantId = selectedRestaurantForMenuId;

                Optional<HttpResponse<String>> responseOptional = ApiClient.get("/vendors/" + restaurantId, token);


                if (responseOptional.isPresent()) {
                    HttpResponse<String> response = responseOptional.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<FoodItem> allFoodItems = new java.util.ArrayList<>();
                                ObservableList<String> menuTitles=FXCollections.observableArrayList();
                                JsonNode menuTitleNode = rootNode.get("menu_titles");
                                if (menuTitleNode != null && menuTitleNode.isArray()) {
                                    for (JsonNode titleNode : menuTitleNode) {
                                        String menuTitle = titleNode.asText();
                                        menuTitles.add(menuTitle);
                                        JsonNode itemsUnderTitle = rootNode.get(menuTitle);
                                        if (itemsUnderTitle != null && itemsUnderTitle.isArray()) {
                                            List<FoodItem> items = JsonUtil.getObjectMapper().readerForListOf(FoodItem.class).readValue(itemsUnderTitle);
                                            allFoodItems.addAll(items);
                                        }
                                    }
                                }
                                ObservableList<FoodItem> foodItemObservableList = FXCollections.observableArrayList(allFoodItems);
                                foodItemTable.setItems(foodItemObservableList);
                                selectMenuComboBox.setItems(menuTitles);
                                if (!menuTitles.isEmpty()){
                                    selectMenuComboBox.getSelectionModel().selectFirst();
                                }
                                errorMessageLabel.setText("Food item loaded successfully for restaurant ID: " + selectedRestaurantForMenuId);

                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing food items data: " + e.getMessage());
                                e.printStackTrace();
                            }


                        } else {
                            Platform.runLater(() -> {
                                errorMessageLabel.setText("Error fetching food items: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                            });
                        }
                    });

                } else {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Field to connect to server for food items");
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching food items: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void addNewRestaurant() {
        errorMessageLabel.setText("");
        try {
            FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/RestaurantFormView.fxml"));
            Parent restaurantFormView=fxmlLoader.load();
            RestaurantFormController controller=fxmlLoader.getController();
            controller.setRestaurantId(null);
            controller.setRestaurantEdited(null);
            controller.setRefreshRestaurantCallback(aVoid->viewManageMenu());
            Stage stage=new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add new restaurant");
            Scene scene=new Scene(restaurantFormView);
            scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.showAndWait();

        } catch (IOException e) {
            errorMessageLabel.setText("Error opening restaurant form: "+e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void addFoodItem(ActionEvent event) {
        errorMessageLabel.setText("");
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant to add a food item");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/FoodItemFormView.fxml"));
            Parent foodItemFormView = fxmlLoader.load();
            FoodItemFormController controller = fxmlLoader.getController();
            controller.setRestaurantId(selectedRestaurantForMenuId);
            controller.setRefreshFoodItemCallback(aVoid -> viewMyRestaurants());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add new food item");
            Scene scene = new Scene(foodItemFormView);
            scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.showAndWait();

        } catch (IOException e) {
            errorMessageLabel.setText("Error opening food item form" + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleEditRestaurant(ActionEvent event) {
        errorMessageLabel.setText("");
        Restaurant selectedRestaurant=myRestaurantsTable.getSelectionModel().getSelectedItem();
        if (selectedRestaurant==null){
            errorMessageLabel.setText("Please select a restaurant to edit");
            return;
        }
        try {
            FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/RestaurantFormView.fxml"));
            Parent RestaurantFormView=fxmlLoader.load();
            RestaurantFormController controller=fxmlLoader.getController();
            controller.setRestaurantEdited(selectedRestaurant);
            controller.setRestaurantId(selectedRestaurant.getId());
            controller.setRefreshRestaurantCallback(aVoid->viewMyRestaurants());
            Stage stage=new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit restaurant");
            Scene scene=new Scene(RestaurantFormView);
            scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.showAndWait();
        } catch (IOException e) {
            errorMessageLabel.setText("Error opening restaurant form: "+e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void editFoodItem(ActionEvent event) {
        errorMessageLabel.setText("");
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant to edit a food item");
            return;
        }
        FoodItem selectedFoodItem = foodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            errorMessageLabel.setText("Please select a food item to edit");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/FoodItemFormView.fxml"));
            Parent foodItemFormView = fxmlLoader.load();
            FoodItemFormController controller = fxmlLoader.getController();
            controller.setRestaurantId(selectedRestaurantForMenuId);
            controller.setFoodItemToEdit(selectedFoodItem);
            controller.setRefreshFoodItemCallback(aVoid -> viewManageMenu());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit food item");
            Scene scene = new Scene(foodItemFormView);
            scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.showAndWait();
        } catch (IOException e) {
            errorMessageLabel.setText("Error opening food item form" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteFoodItem(ActionEvent event) {
        FoodItem selectedFoodItem = foodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            errorMessageLabel.setText("Please select a food item to delete");
            return;
        }
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant to delete food item");
        }
        errorMessageLabel.setText("removing " + selectedFoodItem.getName() + " from food items");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Authentication token is missing.Please login again");
                    });
                    return;
                }
                Optional<HttpResponse<String>> optionalHttpResponse = ApiClient.delete("/restaurants/" + selectedRestaurantForMenuId + "/item/" + selectedFoodItem.getId(), token);
                if (optionalHttpResponse.isPresent()) {
                    HttpResponse<String> response = optionalHttpResponse.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Food item deleted successfully");
                            viewManageMenu();
                        } else {
                            errorMessageLabel.setText("Error during removing: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                        }
                    });

                } else {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Could not connect to server to remove food item");
                    });
                }

            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Unexpected error during removing: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void createMenu(ActionEvent event) {
        errorMessageLabel.setText("");
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant to creat a menu");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create new menu");
        dialog.setHeaderText("Enter the title for new menu:");
        dialog.setContentText("Menu Title:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String menuTitle = result.get().trim();
            if (menuTitle.isEmpty()) {
                errorMessageLabel.setText("Menu title cannot be empty");
                return;
            }
            errorMessageLabel.setText("Creating menu: " + menuTitle + "...");
            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Authentication token is missing.Please login again");
                        });
                        return;
                    }
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("title", menuTitle);
                    String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);
                    Optional<HttpResponse<String>> responseOptional = ApiClient.post("/restaurants/" + selectedRestaurantForMenuId + "/menu", jsonBody, token);
                    if (responseOptional.isPresent()) {
                        HttpResponse<String> response = responseOptional.get();
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                Platform.runLater(() -> {
                                    errorMessageLabel.setText("Menu created successfully");
                                    viewManageMenu();
                                });
                            } else {
                                Platform.runLater(() -> {
                                    errorMessageLabel.setText("Error creating menu: " + (rootNode.has("error") ? rootNode.get("error").asText() : "Unknown error"));
                                });
                            }
                        });

                    } else {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Failed to connect server to create menu");
                        });
                    }
                } catch (IOException|InterruptedException e) {
                    errorMessageLabel.setText("Unexpected error while creating menu: "+e.getMessage());
                    e.printStackTrace();
                }
            });
        }else {
            errorMessageLabel.setText("Creating menu was cancelled");
        }

    }

    @FXML
    private void deleteMenu() {
        errorMessageLabel.setText("");
        if (selectedRestaurantForMenuId==null||selectedRestaurantForMenuId.isEmpty()){
            errorMessageLabel.setText("Please select a restaurant to delete menu");
            return;
        }
        TextInputDialog dialog=new TextInputDialog();
        dialog.setTitle("delete menu");
        dialog.setHeaderText("Enter title of menu to delete");
        dialog.setContentText("Menu Title: ");
        Optional<String> result=dialog.showAndWait();
        if (result.isPresent()){
            String menuTitle = result.get().trim();
            if (menuTitle.isEmpty()){
                errorMessageLabel.setText("Menu Title cannot be empty");
                return;
            }
            errorMessageLabel.setText("Deleting menu: "+ menuTitle+"...");
            executorService.submit(()->{
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Authentication token is missing.Please login again");
                        });
                        return;
                    }
                    String encodedMenuTitle = URLEncoder.encode(menuTitle, StandardCharsets.UTF_8);
                    Optional<HttpResponse<String>> responseOptional = ApiClient.delete("/restaurants/" + selectedRestaurantForMenuId + "/menu/" + encodedMenuTitle, token);

                    if (responseOptional.isPresent()){
                        HttpResponse<String> response=responseOptional.get();
                        JsonNode rootNode=JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(()->{
                            if (response.statusCode()==200){
                                errorMessageLabel.setText(rootNode.has("message")?rootNode.get("message").asText():"Menu deleted successfully");
                            }else {
                                errorMessageLabel.setText("Error deleting menu: "+(rootNode.has("error")?rootNode.get("error").asText():"An unknown error occurred"));
                            }
                        });
                    }else {
                        Platform.runLater(()->{errorMessageLabel.setText("Failed to connect to server to delete menu");});
                    }
                } catch (IOException|InterruptedException e) {
                    Platform.runLater(()->{errorMessageLabel.setText("Unexpected error occurred: "+e.getMessage());
                    e.printStackTrace();
                    });
                }
            });
        }else {
            errorMessageLabel.setText("Menu deletion cancelled");
        }
    }

    @FXML
    private void addItemToMenu() {
        errorMessageLabel.setText("");
        if(selectedRestaurantForMenuId==null||selectedRestaurantForMenuId.isEmpty()){
            errorMessageLabel.setText("Please select a restaurant first");
            return;
        }
        FoodItem selectedFoodItem=foodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem==null){
            errorMessageLabel.setText("Please select a food item to add menu");
            return;
        }
        TextInputDialog dialog=new TextInputDialog();
        dialog.setTitle("Add item to menu");
        dialog.setHeaderText("Enter title of menu");
        dialog.setContentText("Menu Title");
        Optional<String> result=dialog.showAndWait();
        if (result.isPresent()){
            String menuTitle= result.get().trim();
            if (menuTitle.isEmpty()){
                errorMessageLabel.setText("Menu title cannot be empty");
                return;
            }
            errorMessageLabel.setText("Adding food to menu...");
            executorService.submit(()->{
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Authentication token is missing.Please login again");
                        });
                        return;
                    }
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("item_id", selectedFoodItem.getId());
                    String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);
                    String encodedMenuTitle = URLEncoder.encode(menuTitle, StandardCharsets.UTF_8);
                    Optional<HttpResponse<String>> responseOptional = ApiClient.put("/restaurants/" + selectedRestaurantForMenuId + "/menu/" + encodedMenuTitle, jsonBody, token);
                    if (responseOptional.isPresent()){
                        HttpResponse<String> response=responseOptional.get();
                        JsonNode rootNode=JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(()->{
                            if (response.statusCode()==200){
                                errorMessageLabel.setText(rootNode.has("message")?rootNode.get("message").asText():"Food added successfully");
                            }else {
                                errorMessageLabel.setText("Error adding food to menu"+(rootNode.has("error")?rootNode.get("error").asText():"An unknown error occurred"));
                            }
                        });
                    }else {
                        Platform.runLater(()->{errorMessageLabel.setText("Failed to connect to server for adding");});
                    }
                }  catch (IOException|InterruptedException e) {
                    Platform.runLater(()->{errorMessageLabel.setText("Unexpected error during Adding: "+e.getMessage());
                    e.printStackTrace();
                    });
                }
            });
        }else {
            errorMessageLabel.setText("Add food to menu was cancelled");
        }
    }

    @FXML
    private void deleteItemFromMenu() {
        errorMessageLabel.setText("");
        if(selectedRestaurantForMenuId==null||selectedRestaurantForMenuId.isEmpty()){
            errorMessageLabel.setText("Please select a restaurant first");
            return;
        }
        FoodItem selectedFood=foodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFood==null){
            errorMessageLabel.setText("Please select a food item to delete");
            return;
        }
        TextInputDialog dialog=new TextInputDialog();
        dialog.setTitle("Deleting food from menu");
        dialog.setHeaderText("Enter title of menu: ");
        dialog.setContentText("Menu Title");
        Optional<String> result=dialog.showAndWait();
        if (result.isPresent()){
            String menuTitle=result.get().trim();
            if (menuTitle.isEmpty()){
                errorMessageLabel.setText("Menu title cannot be empty");
                return;
            }
            errorMessageLabel.setText("Deleting food from menu: " + selectedFood.getName() + " from " + menuTitle + "...");
            executorService.submit(()->{
                try {
                   String token=AuthManager.getJwtToken();
                   if (token==null||token.isEmpty()){
                       Platform.runLater(()->{errorMessageLabel.setText("Authentication token is missing.Please login again");});
                       return;
                   }
                    String encodedMenuTitle = URLEncoder.encode(menuTitle, StandardCharsets.UTF_8);
                    Optional<HttpResponse<String>> responseOptional = ApiClient.delete("/restaurants/"+selectedRestaurantForMenuId+"/menu/"+encodedMenuTitle+"/"+selectedFood.getId(),token);
                    if (responseOptional.isPresent()){
                        HttpResponse<String> response=responseOptional.get();
                        JsonNode rootNode=JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(()->{
                            if (response.statusCode()==200){
                                errorMessageLabel.setText(rootNode.has("message")?rootNode.get("message").asText():"Food deleted successfully");
                            }else {
                                errorMessageLabel.setText("Error deleting food from menu: "+(rootNode.has("error")?rootNode.get("error").asText():"An unknown error occurred"));
                            }
                        });

                    }else {
                        Platform.runLater(()->{errorMessageLabel.setText("Failed to connect to server to delete food from menu");});
                    }
                } catch (IOException|InterruptedException e) {
                    Platform.runLater(()->{
                        errorMessageLabel.setText("Unexpected error occurred: "+e.getMessage());
                        e.printStackTrace();
                    });
                }
            });
        }else {
            errorMessageLabel.setText("Deleting food from menu cancelled");
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


}