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
import java.net.http.HttpResponse;
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

    @FXML private TableView<CartItemDisplay> cartItemsTable; // TableView برای نمایش آیتم‌های سبد خرید
    @FXML private TableColumn<CartItemDisplay, String> cartItemNameColumn;
    @FXML private TableColumn<CartItemDisplay, Integer> cartItemQuantityColumn;
    @FXML private TableColumn<CartItemDisplay, BigDecimal> cartItemPriceColumn; // استفاده از BigDecimal برای دقت قیمت
    @FXML private TableColumn<CartItemDisplay, BigDecimal> cartItemSubtotalColumn;
    @FXML private TableColumn<CartItemDisplay, Void> cartItemActionsColumn; // برای دکمه‌های حذف/ویرایش تعداد
    @FXML private Label totalPriceLabel; // لیبل برای نمایش قیمت کل
    @FXML private Label cartErrorMessageLabel; // لیبل برای نمایش پیام‌های خطا/وضعیت

    @FXML private MFXTextField couponCodeField; //
    @FXML private MFXButton applyCouponButton; //
    @FXML private Label couponMessageLabel; //

    private Map<FoodItem, Integer> cartData; // سبد خرید (از RestaurantMenuController منتقل می‌شود)
    private String currentRestaurantId; // ID رستوران (برای ارسال در درخواست سفارش)
    private Consumer<Void> clearCartCallback; // Callback برای پاک کردن سبد خرید در RestaurantMenuController
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Coupon appliedCoupon; // برای ذخیره جزئیات کوپن اعمال شده (بخش اضافه شده)
    private BigDecimal currentCalculatedTotalPrice = BigDecimal.ZERO; // برای ذخیره قیمت کل پس از اعمال تخفیف‌ها (بخش اضافه شده)

    // کلاس کمکی برای نمایش آیتم‌ها در TableView.
    // TableView نمی‌تواند مستقیماً Map<FoodItem, Integer> را نمایش دهد.
    // نیاز به یک کلاس مدل ساده‌تر برای نمایش داریم.
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
        public String getItemName() { return foodItem.getName(); } // برای ستون Name
        public int getQuantity() { return quantity; } // برای ستون Quantity
        public BigDecimal getPrice() { return BigDecimal.valueOf(foodItem.getPrice()); } // برای ستون Price
        public BigDecimal getSubtotal() { return subtotal; } // برای ستون Subtotal
        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.subtotal = BigDecimal.valueOf(foodItem.getPrice()).multiply(BigDecimal.valueOf(quantity));
        }
    }

    @FXML
    public void initialize() {
        // پیکربندی ستون‌های جدول
        cartItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        cartItemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartItemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        cartItemSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // پیکربندی ستون Actions با دکمه‌ها
        cartItemActionsColumn.setCellFactory(param -> new TableCell<CartItemDisplay, Void>() {
            private final Button deleteButton = new Button("Delete");
            private final Button editButton = new Button("Edit");
            private final HBox pane = new HBox(5, deleteButton, editButton);

            {
                deleteButton.setOnAction(event -> {
                    CartItemDisplay item = getTableView().getItems().get(getIndex());
                    handleRemoveItem(item.getFoodItem()); // فراخوانی متد حذف
                });
                editButton.setOnAction(event -> {
                    CartItemDisplay item = getTableView().getItems().get(getIndex());
                    handleEditQuantity(item.getFoodItem()); // فراخوانی متد ویرایش تعداد
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    // Setter برای دریافت سبد خرید از کنترلر والد (RestaurantMenuController)
    public void setCart(Map<FoodItem, Integer> cart) {
        this.cartData = cart;
        // بخش اصلاح شده: پاک کردن کوپن اعمال شده هنگام تنظیم سبد خرید جدید
        this.appliedCoupon = null;
        couponCodeField.setText("");
        couponMessageLabel.setText("");
        populateCartTable();
    }

    // Setter برای دریافت ID رستوران
    public void setCurrentRestaurantId(String restaurantId) {
        this.currentRestaurantId = restaurantId;
    }

    // Setter برای Callback جهت پاک کردن سبد خرید در کنترلر والد
    public void setClearCartCallback(Consumer<Void> callback) {
        this.clearCartCallback = callback;
    }

    private void populateCartTable() {
        // تبدیل Map<FoodItem, Integer> به ObservableList<CartItemDisplay>
        ObservableList<CartItemDisplay> displayItems = FXCollections.observableArrayList();
        BigDecimal subtotalBeforeCoupon = BigDecimal.ZERO; //

        for (Map.Entry<FoodItem, Integer> entry : cartData.entrySet()) {
            CartItemDisplay displayItem = new CartItemDisplay(entry.getKey(), entry.getValue());
            displayItems.add(displayItem);
            subtotalBeforeCoupon = subtotalBeforeCoupon.add(displayItem.getSubtotal());
        }

        cartItemsTable.setItems(displayItems);
        calculateAndDisplayTotalPrice(subtotalBeforeCoupon);
    }

    private void handleRemoveItem(FoodItem itemToRemove) {
        cartData.remove(itemToRemove); // حذف از Map اصلی
        appliedCoupon = null; // کوپن اعمال شده را ریست کنید (بخش اصلاح شده)
        couponMessageLabel.setText(""); // (بخش اصلاح شده)
        populateCartTable(); // رفرش جدول
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
                cartData.put(itemToEdit, newQuantity); // به‌روزرسانی در Map اصلی
                appliedCoupon = null; // کوپن اعمال شده را ریست کنید (بخش اصلاح شده)
                couponMessageLabel.setText(""); // (بخش اصلاح شده)
                populateCartTable(); // رفرش جدول
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
            // اعمال منطق کوپن بر اساس نوع (fixed یا percent) - این منطق باید در بک‌اند نیز تایید شود
            if ("percent".equalsIgnoreCase(appliedCoupon.getType())) {
                BigDecimal discountAmount = basePrice.multiply(BigDecimal.valueOf(appliedCoupon.getValue()));
                finalPrice = basePrice.subtract(discountAmount);
            } else if ("fixed".equalsIgnoreCase(appliedCoupon.getType())) {
                finalPrice = basePrice.subtract(BigDecimal.valueOf(appliedCoupon.getValue()));
            }
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }
            couponMessageLabel.setText("کوپن اعمال شد: " + appliedCoupon.getCouponCode() + " (تخفیف: " + (basePrice.subtract(finalPrice).toPlainString()) + " مبلغ)");
        } else {
            couponMessageLabel.setText(""); // پیام کوپن را پاک کنید
        }

        this.currentCalculatedTotalPrice = finalPrice;
        totalPriceLabel.setText(finalPrice.toPlainString());
    }

    @FXML
    private void handleClearCart(ActionEvent event) {
        cartData.clear(); // پاک کردن Map اصلی
        appliedCoupon = null; // کوپن اعمال شده را پاک کنید (بخش اصلاح شده)
        couponMessageLabel.setText(""); // پیام کوپن را پاک کنید (بخش اصلاح شده)

        populateCartTable(); // رفرش جدول
        cartErrorMessageLabel.setText("Cart cleared.");
    }
    // متد برای اعمال کوپن (اعتبارسنجی سمت کلاینت برای UX) (بخش اضافه شده)
    @FXML
    private void handleApplyCoupon(ActionEvent event) {
        String couponCode = couponCodeField.getText().trim();
        if (couponCode.isEmpty()) {
            couponMessageLabel.setText("لطفاً کد کوپن را وارد کنید.");
            return;
        }

        if (cartData.isEmpty()) {
            couponMessageLabel.setText("سبد خرید شما خالی است. نمی‌توانید کوپن اعمال کنید.");
            return;
        }

        couponMessageLabel.setText("در حال اعتبارسنجی کوپن...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> couponMessageLabel.setText("توکن احراز هویت موجود نیست. لطفاً دوباره وارد شوید."));
                    return;
                }

                // فراخوانی GET /coupons برای دریافت تمام کوپن‌ها از بک‌اند
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/coupons", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                // دسیریالایز کردن لیست کوپن‌ها
                                CollectionType listType = JsonUtil.getObjectMapper().getTypeFactory().constructCollectionType(List.class, Coupon.class);
                                List<Coupon> allCoupons = JsonUtil.getObjectMapper().readValue(rootNode.toString(), listType);

                                Coupon foundCoupon = null;
                                for (Coupon c : allCoupons) {
                                    if (c.getCouponCode().equalsIgnoreCase(couponCode)) {
                                        foundCoupon = c;
                                        break;
                                    }
                                }

                                if (foundCoupon != null) {
                                    // **اعتبارسنجی سمت کلاینت (فقط برای UX - اعتبارسنجی نهایی در بک‌اند انجام می‌شود)**
                                    BigDecimal rawTotalPrice = calculateRawTotalPrice();
                                    LocalDate today = LocalDate.now();

                                    // بخش اصلاح شده: تبدیل String به LocalDate برای مقایسه تاریخ‌ها
                                    LocalDate couponStartDate = foundCoupon.getStartDate(); // فرض می کنیم getStartDate() حالا LocalDate برمی گرداند (از تغییر قبلی)
                                    LocalDate couponEndDate = foundCoupon.getEndDate();     // فرض می کنیم getEndDate() حالا LocalDate برمی گرداند (از تغییر قبلی)


                                    if (foundCoupon.getMinPrice() != null && rawTotalPrice.compareTo(BigDecimal.valueOf(foundCoupon.getMinPrice())) < 0) {
                                        couponMessageLabel.setText("این کوپن نیاز به حداقل خرید " + foundCoupon.getMinPrice() + " دارد.");
                                        appliedCoupon = null;
                                    } else if (couponStartDate != null && today.isBefore(couponStartDate)) { // استفاده از متغیر تبدیل شده
                                        couponMessageLabel.setText("این کوپن هنوز فعال نیست.");
                                        appliedCoupon = null;
                                    } else if (couponEndDate != null && today.isAfter(couponEndDate)) { // استفاده از متغیر تبدیل شده
                                        couponMessageLabel.setText("این کوپن منقضی شده است.");
                                        appliedCoupon = null;
                                    }
                                    // توجه: اعتبارسنجی userCount به صورت کامل و دقیق نیاز به مدیریت در بک‌اند دارد
                                    else if (foundCoupon.getUserCount() != null && foundCoupon.getUserCount() <= 0) {
                                        couponMessageLabel.setText("این کوپن دیگر قابل استفاده نیست.");
                                        appliedCoupon = null;
                                    }
                                    else {
                                        appliedCoupon = foundCoupon; // کوپن معتبر در سمت کلاینت ذخیره می‌شود
                                        calculateAndDisplayTotalPrice(rawTotalPrice); // به‌روزرسانی نمایش قیمت با تخفیف
                                        cartErrorMessageLabel.setText(""); // پاک کردن هر خطای قبلی سبد خرید
                                    }
                                } else {
                                    couponMessageLabel.setText("کد کوپن نامعتبر است.");
                                    appliedCoupon = null;
                                }
                                // در هر صورت، نمایش قیمت کل را به‌روز کنید
                                calculateAndDisplayTotalPrice(calculateRawTotalPrice());
                            } catch (JsonProcessingException e) {
                                couponMessageLabel.setText("خطا در تجزیه و تحلیل لیست کوپن: " + e.getMessage());
                                appliedCoupon = null;
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "دریافت کوپن‌ها با شکست مواجه شد.";
                            couponMessageLabel.setText(errorMessage);
                            appliedCoupon = null;
                        }
                    });
                } else {
                    Platform.runLater(() -> couponMessageLabel.setText("عدم اتصال به سرور برای دریافت کوپن‌ها."));
                    appliedCoupon = null;
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    couponMessageLabel.setText("خطای غیرمنتظره در هنگام اعمال کوپن: " + e.getMessage());
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

        cartErrorMessageLabel.setText("Placing order..."); // برای نمایش در RestaurantMenuController

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
                    item.put("item_id", entry.getKey().getId()); //
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
                                couponMessageLabel.setText("کوپن اعمال شده در بک‌اند نامعتبر است: " + errorMessage);
                                appliedCoupon = null; // کوپن را پاک کنید تا کاربر بتواند دوباره امتحان کند
                                calculateAndDisplayTotalPrice(calculateRawTotalPrice()); // قیمت را بدون کوپن نمایش دهید
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
        // بررسی کنید که آیا event و source آن null نیستند
        if (event != null && event.getSource() != null) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); //
            stage.close(); // بستن پنجره
        } else {
            // اگر event یا source آن null باشد، یک پیام خطا نمایش دهید.
            // در شرایط واقعی، ممکن است نیاز به نگهداری یک مرجع به Stage در خود کنترلر داشته باشید
            // تا بتوانید در هر شرایطی پنجره را ببندید.
            System.err.println("Cannot close stage: ActionEvent or its source is null.");
        }
        executorService.shutdown(); // خاموش کردن ExecutorService
    }
}