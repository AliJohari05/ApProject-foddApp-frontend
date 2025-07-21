package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.FoodItem; // مدل FoodItem
import com.foodapp.food4ufrontend.model.Restaurant; // مدل Restaurant
import com.foodapp.food4ufrontend.util.ApiClient; // برای فراخوانی API
import com.foodapp.food4ufrontend.util.AuthManager; // برای دریافت توکن
import com.foodapp.food4ufrontend.util.JsonUtil; // برای پردازش JSON
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog; // NEW: برای گرفتن ورودی تعداد
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Node; // NEW: برای گرفتن Window
import javafx.fxml.FXMLLoader; // NEW: برای باز کردن فرم سبد خرید

import java.io.IOException;
import java.io.InputStream; //
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Base64; // NEW: برای Base64
import java.io.ByteArrayInputStream; // NEW: برای Base64 به Image
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.geometry.Insets; // NEW: برای Insets در VBox
import javafx.scene.layout.VBox; // NEW: برای محتوای Tab
import javafx.stage.Modality; // NEW: برای مودال بودن پنجره سبد خرید


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.Restaurant;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;



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
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class RestaurantMenuController {

    @FXML
    private ImageView restaurantLogoImageView;
    @FXML
    private Label restaurantNameLabel;
    @FXML
    private Label restaurantAddressLabel;
    @FXML
    private Label restaurantPhoneLabel;
    @FXML
    private TabPane menuTabPane; // برای تب‌های منو (Breakfast, Lunch)
    @FXML
    private Label menuErrorMessageLabel;

    private Restaurant currentRestaurant; // رستوران فعلی که منوی آن را مشاهده می‌کنیم
    private Map<FoodItem, Integer> cart = new HashMap<>(); // سبد خرید: FoodItem -> Quantity
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        // تنظیمات اولیه (در حال حاضر نیازی به کد اضافی نیست زیرا setup در setRestaurant انجام می‌شود)
    }

    // متد Setter برای دریافت شیء Restaurant از BuyerDashboard
    public void setRestaurant(Restaurant restaurant) {
        this.currentRestaurant = restaurant;
        if (currentRestaurant != null) {
            restaurantNameLabel.setText(currentRestaurant.getName());
            restaurantAddressLabel.setText(currentRestaurant.getAddress());
            restaurantPhoneLabel.setText(currentRestaurant.getPhone());

            // بارگذاری تصویر لوگو (اگر URL در مدل Restaurant باشد)
            if (currentRestaurant.getLogoBase64() != null && !currentRestaurant.getLogoBase64().isEmpty()) {
                // اگر لوگو به صورت Base64 در مدل Restaurant باشد
                try {
                    byte[] decodedImg = Base64.getDecoder().decode(currentRestaurant.getLogoBase64());
                    Image logoImage = new Image(new ByteArrayInputStream(decodedImg));
                    restaurantLogoImageView.setImage(logoImage);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error decoding Base64 logo: " + e.getMessage());
                    loadDefaultLogo(); // بارگذاری لوگوی پیش فرض در صورت خطا
                }
            } else {
                // بارگذاری لوگوی پیش فرض
                loadDefaultLogo();
            }

            // فراخوانی متد برای بارگذاری منو پس از تنظیم رستوران
            loadRestaurantMenu();
        }
    }

    private void loadDefaultLogo() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com/foodapp/food4ufrontend/images/default_restaurant_logo.png");
            if (imageStream != null) {
                Image defaultLogo = new Image(imageStream);
                if (!defaultLogo.isError()) {
                    restaurantLogoImageView.setImage(defaultLogo);
                } else {
                    System.err.println("Error loading default logo from stream: " + defaultLogo.getException().getMessage());
                }
            } else {
                System.err.println("Default restaurant logo resource stream is null.");
            }
        } catch (Exception e) {
            System.err.println("Exception loading default logo: " + e.getMessage());
        }
    }

    private void loadRestaurantMenu() {
        menuErrorMessageLabel.setText("Loading menu...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> menuErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                // API call to get vendor details and menu items
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/vendors/" + currentRestaurant.getId(), token); //

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                // Clear existing tabs before adding new ones
                                menuTabPane.getTabs().clear();
                                JsonNode menuTitlesNode = rootNode.get("menu_titles"); //
                                if (menuTitlesNode != null && menuTitlesNode.isArray()) {
                                    for (JsonNode titleNode : menuTitlesNode) {
                                        String menuTitle = titleNode.asText();

                                        // Create a Tab for each menu title
                                        Tab menuTab = new Tab(menuTitle);
                                        VBox tabContent = new VBox(10); // Content inside the tab
                                        tabContent.setPadding(new Insets(10));

                                        TableView<FoodItem> foodItemTable = new TableView<>();
                                        // Configure TableColumns dynamically for each tab's table
                                        TableColumn<FoodItem, Integer> idCol = new TableColumn<>("ID");
                                        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
                                        TableColumn<FoodItem, String> nameCol = new TableColumn<>("Name");
                                        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
                                        TableColumn<FoodItem, String> descCol = new TableColumn<>("Description");
                                        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
                                        TableColumn<FoodItem, Integer> priceCol = new TableColumn<>("Price");
                                        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
                                        TableColumn<FoodItem, Integer> supplyCol = new TableColumn<>("Supply");
                                        supplyCol.setCellValueFactory(new PropertyValueFactory<>("supply"));

                                        foodItemTable.getColumns().addAll(idCol, nameCol, descCol, priceCol, supplyCol);
                                        foodItemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                                        // Populate the table with items for this specific menu
                                        JsonNode itemsUnderTitle = rootNode.get(menuTitle); //
                                        if (itemsUnderTitle != null && itemsUnderTitle.isArray()) {
                                            List<FoodItem> items = JsonUtil.getObjectMapper().readerForListOf(FoodItem.class).readValue(itemsUnderTitle);
                                            // No need for HashSet here if items within a single menu are unique by API
                                            // and we are showing them menu-by-menu.
                                            ObservableList<FoodItem> foodItemObservableList = FXCollections.observableArrayList(items);
                                            foodItemTable.setItems(foodItemObservableList);
                                        }

                                        tabContent.getChildren().add(foodItemTable); // Add table to tab content
                                        menuTab.setContent(tabContent); // Set VBox as tab content
                                        menuTabPane.getTabs().add(menuTab); // Add tab to TabPane
                                    }
                                    menuErrorMessageLabel.setText("Menu loaded successfully.");
                                }else{
                                    menuErrorMessageLabel.setText("Error fetching menu: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred."));
                                }

                            }catch(IOException e){
                                menuErrorMessageLabel.setText("Error parsing menu data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }else{
                            menuErrorMessageLabel.setText("Error fetching menu: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred."));
                        }
                    });
                } else {
                    Platform.runLater(() -> menuErrorMessageLabel.setText("Failed to connect to server to load menu."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    menuErrorMessageLabel.setText("An unexpected error occurred while fetching menu: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        // 1. Get the selected FoodItem from the currently active TableView in menuTabPane.
        Tab selectedTab = menuTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            menuErrorMessageLabel.setText("Please select a menu tab first.");
            return;
        }

        // Get the TableView from the selected tab's content.
        // Assumes the TableView is the first (and only) child of the VBox tab content.
        TableView<FoodItem> activeFoodItemTable = null;
        if (selectedTab.getContent() instanceof VBox) {
            VBox tabContent = (VBox) selectedTab.getContent();
            if (!tabContent.getChildren().isEmpty() && tabContent.getChildren().get(0) instanceof TableView) {
                activeFoodItemTable = (TableView<FoodItem>) tabContent.getChildren().get(0);
            }
        }

        if (activeFoodItemTable == null) {
            menuErrorMessageLabel.setText("Could not find food items table in the selected tab.");
            return;
        }

        FoodItem selectedFoodItem = activeFoodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            menuErrorMessageLabel.setText("Please select a food item to add to cart.");
            return;
        }

        // 2. Getting quantity (e.g., via a dialog)
        TextInputDialog quantityDialog = new TextInputDialog("1");
        quantityDialog.setTitle("Add to Cart");
        quantityDialog.setHeaderText("Enter quantity for " + selectedFoodItem.getName() + ":");
        quantityDialog.setContentText("Quantity:");

        Optional<String> quantityResult = quantityDialog.showAndWait();
        if (quantityResult.isPresent()) {
            try {
                int quantity = Integer.parseInt(quantityResult.get().trim());
                if (quantity <= 0) {
                    menuErrorMessageLabel.setText("Quantity must be a positive number.");
                    return;
                }

                // 3. Adding to the 'cart' Map (FoodItem -> Quantity).
                // If item already in cart, update quantity. Otherwise, add new.
                cart.put(selectedFoodItem, cart.getOrDefault(selectedFoodItem, 0) + quantity);

                menuErrorMessageLabel.setText(selectedFoodItem.getName() + " x" + quantity + " added to cart. Total items in cart: " + cart.values().stream().mapToInt(Integer::intValue).sum());

            } catch (NumberFormatException e) {
                menuErrorMessageLabel.setText("Invalid quantity. Please enter a valid number.");
            }
        } else {
            menuErrorMessageLabel.setText("Adding to cart cancelled.");
        }
    }

    @FXML
    private void handleViewCartAndOrder(ActionEvent event) {
        // Logic to view cart details and finalize order
        if (cart.isEmpty()) {
            menuErrorMessageLabel.setText("Your cart is empty. Please add some items first.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/dashbord/CartView.fxml"));
            Parent cartView = loader.load();

            CartController controller = loader.getController();
            controller.setCart(cart); // Pass the cart to the CartController
            controller.setCurrentRestaurantId(currentRestaurant.getId()); // Pass the restaurant ID for order submission

            // Set a callback to clear cart if order is successful
            controller.setClearCartCallback(aVoid -> {
                cart.clear(); // Clear the cart in this controller
                menuErrorMessageLabel.setText("Order placed successfully. Cart cleared.");
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Your Cart");
            Scene scene = new Scene(cartView);
            // Assuming application.css is general
            scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait(); // Show the dialog and wait for it to be closed


        } catch (IOException e) {
            menuErrorMessageLabel.setText("Error opening cart view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        // Logic to close the menu view Stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        executorService.shutdown(); // Shutdown executor when form is closed
    }
}