// ApProject_foddApp_frontend/src/main/java/com/foodapp/food4ufrontend/controller/dashbord/OrderRatingsController.java

package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.foodapp.food4ufrontend.model.Order; //
import com.foodapp.food4ufrontend.model.Rating; //
import com.foodapp.food4ufrontend.model.Restaurant; // NEW: برای گرفتن نام رستوران از Order.restaurant
import com.foodapp.food4ufrontend.model.User; // NEW: برای گرفتن نام مشتری از Order.customer
import com.foodapp.food4ufrontend.dto.RatingResponseDto; //
import com.foodapp.food4ufrontend.util.ApiClient; //
import com.foodapp.food4ufrontend.util.AuthManager; //
import com.foodapp.food4ufrontend.util.JsonUtil; //
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets; //
import javafx.scene.Node; //
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream; // NEW: برای Base64 به Image
import java.net.http.HttpResponse;
import java.util.Base64; // NEW: برای Base64
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

public class OrderRatingsController { // NEW: نام کلاس

    @FXML private Label orderInfoLabel;
    @FXML private Label restaurantInfoLabel;
    @FXML private Label ratingScoreLabel;
    @FXML private TextArea commentDisplayArea;
    @FXML private ImageView ratingImageView;
    @FXML private Label viewRatingErrorMessageLabel; // لیبل خطا برای این کنترلر

    private Order currentOrder; // سفارش فعلی که ریتینگ آن را مشاهده می‌کنیم
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @FXML
    public void initialize() {
        // تنظیمات اولیه
        commentDisplayArea.setEditable(false); // نظر فقط برای نمایش است
        commentDisplayArea.setWrapText(true); // متن طولانی را بشکند
        // بارگذاری تصویر پیش‌فرض (مشابه سایر کنترلرها)
        loadDefaultRatingImage();
    }

    private void loadDefaultRatingImage(){
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com/foodapp/food4ufrontend/images/default_profile.jpg"); //
            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                if (!defaultImage.isError()) {
                    ratingImageView.setImage(defaultImage);
                } else {
                    System.err.println("Error loading default rating image from stream: " + defaultImage.getException().getMessage());
                }
            } else {
                System.err.println("Default rating image resource stream is null.");
            }
        } catch (Exception e) {
            System.err.println("Exception loading default rating image: " + e.getMessage());
        }
    }

    // Setter برای دریافت شیء Order از SellerDashboard
    public void setOrder(Order order) {
        this.currentOrder = order;
        if (currentOrder != null) {
            orderInfoLabel.setText("Order ID: #" + currentOrder.getId() + " - Status: " + currentOrder.getStatus()); //

            //loadRestaurantAndCustomerNames();

            // فراخوانی متد برای بارگذاری ریتینگ سفارش
            loadOrderRating();
        }
    }
    private void loadRestaurantAndCustomerNames() {
        executorService.submit(() -> {
            String restaurantName = "N/A";
            String customerName = "N/A";

            try {
                String token = AuthManager.getJwtToken(); //
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> viewRatingErrorMessageLabel.setText("Authentication token missing."));
                    return;
                }

                // 1. دریافت نام رستوران با استفاده از vendorId
                if (currentOrder.getVendorId() != null) { //
                    Optional<HttpResponse<String>> restaurantResponseOpt = ApiClient.get("/restaurants/" + currentOrder.getVendorId(), token); // API call
                    if (restaurantResponseOpt.isPresent() && restaurantResponseOpt.get().statusCode() == 200) {
                        JsonNode restaurantRootNode = objectMapper.readTree(restaurantResponseOpt.get().body());
                        Restaurant restaurant = objectMapper.treeToValue(restaurantRootNode, Restaurant.class); //
                        restaurantName = restaurant.getName(); //
                    } else {
                        System.err.println("Failed to fetch restaurant details for ID: " + currentOrder.getVendorId());
                    }
                }

                // 2. دریافت نام مشتری با استفاده از customerId
                if (currentOrder.getCustomerId() != null) { //
                    // API برای گرفتن User با ID (احتمالاً /admin/users/{id} یا /auth/profile)
                    // فرض می‌کنیم /admin/users/{id} برای همه کاربران قابل دسترسی است یا یک API عمومی‌تر وجود دارد
                    Optional<HttpResponse<String>> userResponseOpt = ApiClient.get("/admin/users/" + currentOrder.getCustomerId(), token); //
                    if (userResponseOpt.isPresent() && userResponseOpt.get().statusCode() == 200) {
                        JsonNode userRootNode = objectMapper.readTree(userResponseOpt.get().body());
                        User customer = objectMapper.treeToValue(userRootNode, User.class); //
                        customerName = customer.getFullName(); //
                    } else {
                        System.err.println("Failed to fetch customer details for ID: " + currentOrder.getCustomerId());
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error fetching restaurant/customer names: " + e.getMessage());
            } finally {
                String finalRestaurantName = restaurantName;
                String finalCustomerName = customerName;
                Platform.runLater(() -> {
                    restaurantInfoLabel.setText("Restaurant: " + finalRestaurantName + " - Customer: " + finalCustomerName);
                });
            }
        });
    }
    private void loadOrderRating() {
        viewRatingErrorMessageLabel.setText("Loading order rating...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken(); //
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> viewRatingErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                // API call to get rating for a specific order
                // GET /ratings/orders/{order_id} (این اندپوینت جدید در بک‌اند پیاده‌سازی شده است)
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/ratings/" + currentOrder.getId(), token); //

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = objectMapper.readTree(response.body()); //

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                // دسیریالایز کردن پاسخ به RatingResponseDto
                                RatingResponseDto ratingDetails = objectMapper.readValue(rootNode.toString(), RatingResponseDto.class); //

                                ratingScoreLabel.setText(String.valueOf(ratingDetails.getRating())); // نمایش امتیاز
                                commentDisplayArea.setText(ratingDetails.getComment() != null && !ratingDetails.getComment().isEmpty() ? ratingDetails.getComment() : "No comment provided."); // نمایش نظر

                                // بارگذاری تصویر ریتینگ (اگر موجود باشد)
                                if (ratingDetails.getImageBase64() != null && !ratingDetails.getImageBase64().isEmpty()) { //
                                    try {
                                        byte[] decodedImg = Base64.getDecoder().decode(ratingDetails.getImageBase64().get(0)); // فرض می‌کنیم فقط یک تصویر است
                                        Image ratingImage = new Image(new ByteArrayInputStream(decodedImg));
                                        ratingImageView.setImage(ratingImage);
                                    } catch (IllegalArgumentException e) {
                                        System.err.println("Error decoding Base64 rating image: " + e.getMessage());
                                        ratingImageView.setImage(null); // Clear image on error
                                    }
                                } else {
                                    ratingImageView.setImage(null); // اگر تصویری نبود، ImageView را خالی کنید.
                                }

                                viewRatingErrorMessageLabel.setText("Order rating loaded successfully.");
                            } catch (IOException e) {
                                viewRatingErrorMessageLabel.setText("Error parsing rating data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            // اگر سفارش امتیازی نداشته باشد (API 404 برگرداند)، یا خطای دیگری باشد
                            if (response.statusCode() == 404) {
                                ratingScoreLabel.setText("N/A");
                                commentDisplayArea.setText("No rating submitted for this order yet.");
                                ratingImageView.setImage(null);
                                viewRatingErrorMessageLabel.setText("No rating found for this order.");
                            } else {
                                viewRatingErrorMessageLabel.setText("Error fetching rating: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred."));
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> viewRatingErrorMessageLabel.setText("Failed to connect to server to load rating."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    viewRatingErrorMessageLabel.setText("An unexpected error occurred while fetching rating: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleClose(ActionEvent event) {
        // Stage را از یکی از فیلدهای @FXML خود فرم (مثلاً orderInfoLabel) می‌گیریم
        Stage stage = (Stage) orderInfoLabel.getScene().getWindow();
        stage.close();
        executorService.shutdown();
    }
}