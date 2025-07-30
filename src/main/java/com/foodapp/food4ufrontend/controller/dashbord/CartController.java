package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.foodapp.food4ufrontend.model.*;
import com.foodapp.food4ufrontend.util.ApiClient; // برای فراخوانی API
import com.foodapp.food4ufrontend.util.AuthManager; // برای دریافت توکن
import com.foodapp.food4ufrontend.util.JsonUtil; // برای پردازش JSON
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets; // برای VBox.setPadding
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog; // برای تغییر تعداد
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox; // برای دکمه‌های درون سلول
import javafx.scene.layout.VBox; // برای ساختار محتوای سلول
import javafx.stage.Stage;
import javafx.util.Callback; // برای CellFactory سفارشی

import java.io.IOException;
import java.math.BigDecimal; // برای محاسبات دقیق قیمت
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer; // برای Callback
import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.FoodItem; // مدل FoodItem
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
public class CartController {

    @FXML private TableView<CartItemDisplay> cartItemsTable;
    @FXML private TableColumn<CartItemDisplay, String> cartItemNameColumn;
    @FXML private TableColumn<CartItemDisplay, Integer> cartItemQuantityColumn;
    @FXML private TableColumn<CartItemDisplay, BigDecimal> cartItemPriceColumn;
    @FXML private TableColumn<CartItemDisplay, BigDecimal> cartItemSubtotalColumn;
    @FXML private TableColumn<CartItemDisplay, Void> cartItemActionsColumn;
    @FXML private Label totalPriceLabel;
    @FXML private Label cartErrorMessageLabel;

    @FXML private MFXTextField couponCodeField;
    @FXML private MFXButton applyCouponButton;
    @FXML private Label couponMessageLabel;

    private Map<FoodItem, Integer> cartData;
    private String currentRestaurantId;
    private Consumer<Void> clearCartCallback;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Coupon appliedCoupon;
    private BigDecimal currentCalculatedTotalPrice = BigDecimal.ZERO;

    public static class CartItemDisplay {
        private FoodItem foodItem;
        private int quantity;
        private BigDecimal subtotal;

        public CartItemDisplay(FoodItem foodItem, int quantity) {
            this.foodItem = foodItem;
            this.quantity = quantity;
            this.subtotal = BigDecimal.valueOf(foodItem.getPrice()).multiply(BigDecimal.valueOf(quantity));
        }

        public FoodItem getFoodItem() { return foodItem; }
        public String getItemName() { return foodItem.getName(); }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return BigDecimal.valueOf(foodItem.getPrice()); }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.subtotal = BigDecimal.valueOf(foodItem.getPrice()).multiply(BigDecimal.valueOf(quantity));
        }
    }

    @FXML
    public void initialize() {
        cartItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        cartItemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartItemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        cartItemSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        cartItemActionsColumn.setCellFactory(param -> new TableCell<CartItemDisplay, Void>() {
            private final Button deleteButton = new Button("Delete");
            private final Button editButton = new Button("Edit");
            private final HBox pane = new HBox(5, deleteButton, editButton);

            {
                deleteButton.setOnAction(event -> {
                    CartItemDisplay item = getTableView().getItems().get(getIndex());
                    handleRemoveItem(item.getFoodItem());
                });
                editButton.setOnAction(event -> {
                    CartItemDisplay item = getTableView().getItems().get(getIndex());
                    handleEditQuantity(item.getFoodItem());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    public void setCart(Map<FoodItem, Integer> cart) {
        this.cartData = cart;
        this.appliedCoupon = null;
        couponCodeField.setText("");
        couponMessageLabel.setText("");
        populateCartTable();
    }

    public void setCurrentRestaurantId(String restaurantId) {
        this.currentRestaurantId = restaurantId;
    }

    public void setClearCartCallback(Consumer<Void> callback) {
        this.clearCartCallback = callback;
    }

    private void populateCartTable() {
        ObservableList<CartItemDisplay> displayItems = FXCollections.observableArrayList();
        BigDecimal subtotalBeforeCoupon = BigDecimal.ZERO;

        for (Map.Entry<FoodItem, Integer> entry : cartData.entrySet()) {
            CartItemDisplay displayItem = new CartItemDisplay(entry.getKey(), entry.getValue());
            displayItems.add(displayItem);
            subtotalBeforeCoupon = subtotalBeforeCoupon.add(displayItem.getSubtotal());
        }

        cartItemsTable.setItems(displayItems);
        calculateAndDisplayTotalPrice(subtotalBeforeCoupon);
    }

    private void handleRemoveItem(FoodItem itemToRemove) {
        cartData.remove(itemToRemove);
        appliedCoupon = null;
        couponMessageLabel.setText("");
        populateCartTable();
        cartErrorMessageLabel.setText(itemToRemove.getName() + " removed from cart.");
    }

    private void handleEditQuantity(FoodItem itemToEdit) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(cartData.get(itemToEdit)));
        dialog.setTitle("Edit Quantity");
        dialog.setHeaderText("Enter new quantity for " + itemToEdit.getName() + ":");
        dialog.setContentText("Quantity:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int newQuantity = Integer.parseInt(result.get().trim());
                if (newQuantity <= 0) {
                    cartErrorMessageLabel.setText("Quantity must be a positive number. Item not updated.");
                    return;
                }
                cartData.put(itemToEdit, newQuantity);
                appliedCoupon = null;
                couponMessageLabel.setText("");
                populateCartTable();
                cartErrorMessageLabel.setText(itemToEdit.getName() + " quantity updated to " + newQuantity + ".");
            } catch (NumberFormatException e) {
                cartErrorMessageLabel.setText("Invalid quantity. Please enter a valid number.");
            }
        } else {
            cartErrorMessageLabel.setText("Quantity edit cancelled.");
        }
    }

    private void calculateAndDisplayTotalPrice(BigDecimal basePrice) {
        BigDecimal finalPrice = basePrice;
        if (appliedCoupon != null) {
            if ("percent".equalsIgnoreCase(appliedCoupon.getType())) {
                BigDecimal discountRate = BigDecimal.valueOf(appliedCoupon.getValue()).divide(BigDecimal.valueOf(100));
                BigDecimal discountAmount = basePrice.multiply(discountRate);
                finalPrice = basePrice.subtract(discountAmount);
            }
            else if ("fixed".equalsIgnoreCase(appliedCoupon.getType())) {
                finalPrice = basePrice.subtract(BigDecimal.valueOf(appliedCoupon.getValue()));
            }
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }
            couponMessageLabel.setText("Coupon applied: " + appliedCoupon.getCouponCode() + " (discount: " + (basePrice.subtract(finalPrice).toPlainString()) + " amount)");
        } else {
            couponMessageLabel.setText("");
        }

        this.currentCalculatedTotalPrice = finalPrice;
        totalPriceLabel.setText(finalPrice.toPlainString());
    }

    @FXML
    private void handleClearCart(ActionEvent event) {
        cartData.clear();
        appliedCoupon = null;
        couponMessageLabel.setText("");

        populateCartTable();
        cartErrorMessageLabel.setText("Cart cleared.");
    }


    @FXML
    private void handleApplyCoupon(ActionEvent event) {
        String couponCode = couponCodeField.getText().trim();
        if (couponCode.isEmpty()) {
            couponMessageLabel.setText("Please enter coupon code.");
            return;
        }

        if (cartData.isEmpty()) {
            couponMessageLabel.setText("Your cart is empty. Cannot apply coupon.");
            return;
        }

        couponMessageLabel.setText("Validating coupon...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> couponMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                String endpoint = "/coupons?coupon_code=" + URLEncoder.encode(couponCode, StandardCharsets.UTF_8);
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(endpoint, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                Coupon foundCoupon = JsonUtil.getObjectMapper().treeToValue(rootNode, Coupon.class);

                                BigDecimal rawTotalPrice = calculateRawTotalPrice();
                                LocalDate today = LocalDate.now();

                                LocalDate couponStartDate = foundCoupon.getStartDate();
                                LocalDate couponEndDate = foundCoupon.getEndDate();

                                if (foundCoupon.getMinPrice() != null &&
                                        rawTotalPrice.compareTo(BigDecimal.valueOf(foundCoupon.getMinPrice())) < 0) {
                                    couponMessageLabel.setText("This coupon requires a minimum purchase of " + foundCoupon.getMinPrice() + ".");
                                    appliedCoupon = null;
                                } else if (couponStartDate != null && today.isBefore(couponStartDate)) {
                                    couponMessageLabel.setText("This coupon is not yet active. Start Date: " + couponStartDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));
                                    appliedCoupon = null;
                                } else if (couponEndDate != null && today.isAfter(couponEndDate)) {
                                    couponMessageLabel.setText("This coupon has expired. Expiry Date: " + couponEndDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));
                                    appliedCoupon = null;
                                } else if (foundCoupon.getUserCount() != null && foundCoupon.getUserCount() <= 0) {
                                    couponMessageLabel.setText("This coupon is no longer usable.");
                                    appliedCoupon = null;
                                } else {
                                    appliedCoupon = foundCoupon;
                                    calculateAndDisplayTotalPrice(rawTotalPrice);
                                    cartErrorMessageLabel.setText("");
                                }

                                calculateAndDisplayTotalPrice(calculateRawTotalPrice());

                            } catch (JsonProcessingException e) {
                                couponMessageLabel.setText("Error parsing coupon: " + e.getMessage());
                                appliedCoupon = null;
                            }
                        } else {
                            String errorMessage = rootNode.has("error")
                                    ? rootNode.get("error").asText()
                                    : "Failed to retrieve coupon.";
                            couponMessageLabel.setText(errorMessage);
                            appliedCoupon = null;
                        }
                    });
                } else {
                    Platform.runLater(() -> couponMessageLabel.setText("Failed to connect to server to retrieve coupon."));
                    appliedCoupon = null;
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    couponMessageLabel.setText("An unexpected error occurred while applying coupon: " + e.getMessage());
                    e.printStackTrace();
                    appliedCoupon = null;
                });
            }
        });
    }


    private BigDecimal calculateRawTotalPrice() {
        BigDecimal rawTotal = BigDecimal.ZERO;
        for (Map.Entry<FoodItem, Integer> entry : cartData.entrySet()) {
            rawTotal = rawTotal.add(BigDecimal.valueOf(entry.getKey().getPrice()).multiply(BigDecimal.valueOf(entry.getValue())));
        }
        return rawTotal;
    }

    @FXML
    private void handlePlaceOrder(ActionEvent event) {
        if (cartData.isEmpty()) {
            cartErrorMessageLabel.setText("Your cart is empty. Cannot place an order.");
            return;
        }

        cartErrorMessageLabel.setText("Placing order...");

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> cartErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                // Prepare order items list for API
                List<Map<String, Integer>> itemsForOrder = new ArrayList<>();
                for (Map.Entry<FoodItem, Integer> entry : cartData.entrySet()) {
                    Map<String, Integer> item = new HashMap<>();
                    item.put("item_id", entry.getKey().getId());
                    item.put("quantity", entry.getValue());
                    itemsForOrder.add(item);
                }

                // Construct request body for POST /orders
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("delivery_address", "Customer's default address"); // TODO: Replace with actual customer address
                orderData.put("vendor_id", Integer.parseInt(currentRestaurantId)); // Restaurant ID is vendor_id in Order
                orderData.put("items", itemsForOrder);
                // Optional: orderData.put("coupon_id", someCouponId);
                if (appliedCoupon != null && appliedCoupon.getId() != null) {
                    orderData.put("coupon_id", appliedCoupon.getId());
                }
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(orderData); //

                Optional<HttpResponse<String>> responseOpt = ApiClient.post("/orders", jsonBody, token); //

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) { // API returns 200 OK on success
                            cartErrorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Order placed successfully!");
                            if (clearCartCallback != null) {
                                clearCartCallback.accept(null); // Notify parent to clear cart
                            }
                            appliedCoupon = null;
                            couponMessageLabel.setText("");
                            handleClose(event); // Close cart view
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            cartErrorMessageLabel.setText("Error placing order: " + errorMessage);
                            if (rootNode.has("error") && rootNode.get("error").asText().contains("Coupon")) {
                                couponMessageLabel.setText("The coupon applied in the backend is invalid: " + errorMessage);
                                appliedCoupon = null;
                                calculateAndDisplayTotalPrice(calculateRawTotalPrice());
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> cartErrorMessageLabel.setText("Failed to connect to server to place order."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    cartErrorMessageLabel.setText("An unexpected error occurred while placing order: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleClose(ActionEvent event) {
        if (event != null && event.getSource() != null) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); //
            stage.close();
        } else {
            System.err.println("Cannot close stage: ActionEvent or its source is null.");
        }
        executorService.shutdown();
    }
}