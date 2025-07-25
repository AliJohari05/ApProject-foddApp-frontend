
package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.food4ufrontend.model.Order; // مدل Order
import com.foodapp.food4ufrontend.util.ApiClient; //
import com.foodapp.food4ufrontend.util.AuthManager; //
import com.foodapp.food4ufrontend.util.JsonUtil; //
import javafx.application.Platform;
import javafx.collections.FXCollections; //
import javafx.collections.ObservableList; //
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets; //
import javafx.scene.Node; //
import javafx.scene.control.ComboBox; //
import javafx.scene.control.Label;
import javafx.scene.control.TextArea; //
import javafx.scene.control.TextField; //
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser; //
import javafx.stage.Stage;
import javafx.stage.Modality; //

import java.io.File; //
import java.io.IOException;
import java.io.InputStream; //
import java.net.http.HttpResponse;
import java.nio.file.Files; //
import java.util.Base64; //
import java.io.ByteArrayInputStream; //
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer; //
import java.util.List; // For imageBase64 array (if needed)

public class RatingFormController {

    @FXML private Label orderInfoLabel;
    @FXML private ComboBox<String> ratingComboBox;
    @FXML private TextArea commentField;
    @FXML private ImageView ratingImageView;
    @FXML private Label formErrorMessageLabel;

    private Order orderToRate;
    private Consumer<Void> refreshOrderHistoryCallback; // Callback برای رفرش تاریخچه سفارشات
    private String base64ImageString; // رشته Base64 تصویر آپلود شده
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ObjectMapper objectMapper = JsonUtil.getObjectMapper(); // استفاده از ObjectMapper موجود

    @FXML
    public void initialize() {
        // پر کردن ComboBox امتیازات
        ObservableList<String> ratings = FXCollections.observableArrayList("1", "2", "3", "4", "5"); //
        ratingComboBox.setItems(ratings);

        // بارگذاری تصویر پیش‌فرض (مشابه فرم FoodItem/Restaurant)
        loadDefaultRatingImage();
    }

    private void loadDefaultRatingImage(){
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com/foodapp/food4ufrontend/images/default_profile.jpg"); // می توانید نام دیگری برای تصویر پیش فرض ریتینگ بگذارید
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

    // Setter برای دریافت سفارش از BuyerDashboard
    public void setOrder(Order order) {
        this.orderToRate = order;
        if (orderToRate != null) {
            orderInfoLabel.setText("Order ID: #" + orderToRate.getId() + " - Total Price: $" + orderToRate.getPayPrice()); //
        }
    }

    // Setter برای Callback رفرش تاریخچه سفارشات
    public void setRefreshOrderHistoryCallback(Consumer<Void> callback) {
        this.refreshOrderHistoryCallback = callback;
    }

    @FXML
    private void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Rating Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                base64ImageString = Base64.getEncoder().encodeToString(fileContent);
                Image image = new Image(selectedFile.toURI().toString());
                ratingImageView.setImage(image);
                formErrorMessageLabel.setText("Image selected: " + selectedFile.getName());
            } catch (IOException e) {
                formErrorMessageLabel.setText("Error reading image file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            formErrorMessageLabel.setText("No image selected.");
        }
    }

    @FXML
    private void handleSubmitRating(ActionEvent event) {
        String ratingStr = ratingComboBox.getValue(); // دریافت امتیاز انتخاب شده
        String comment = commentField.getText(); // دریافت نظر

        if (ratingStr == null || ratingStr.isEmpty()) {
            formErrorMessageLabel.setText("Please select a rating (1-5).");
            return;
        }
        if (orderToRate == null) {
            formErrorMessageLabel.setText("Order information is missing. Cannot submit rating.");
            return;
        }

        try {
            Integer rating = Integer.parseInt(ratingStr);
            if (rating < 1 || rating > 5) { // اعتبارسنجی برای اطمینان از 1 تا 5 بودن
                formErrorMessageLabel.setText("Rating must be between 1 and 5.");
                return;
            }

            // ساخت بدنه درخواست برای API POST /ratings
            Map<String, Object> ratingData = new HashMap<>();
            ratingData.put("order_id", orderToRate.getId()); // NEW: ارسال order_id
            ratingData.put("rating", rating);
            if (comment != null && !comment.trim().isEmpty()) {
                ratingData.put("comment", comment.trim());
            }
            if (base64ImageString != null && !base64ImageString.isEmpty()) {
                ratingData.put("imageBase64", List.of(base64ImageString));
            } else {
                ratingData.put("imageBase64", List.of());
            }


            String jsonBody = objectMapper.writeValueAsString(ratingData);

            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> formErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                        return;
                    }

                    Optional<HttpResponse<String>> responseOpt = ApiClient.post("/ratings", jsonBody, token); //

                    if (responseOpt.isPresent()) {
                        HttpResponse<String> response = responseOpt.get();
                        JsonNode rootNode = objectMapper.readTree(response.body());

                        Platform.runLater(() -> {
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                formErrorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Rating submitted successfully!");
                                if (refreshOrderHistoryCallback != null) {
                                    refreshOrderHistoryCallback.accept(null); // رفرش تاریخچه سفارشات
                                }
                                closeForm(); // بستن فرم
                            } else {
                                String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                                formErrorMessageLabel.setText("Error submitting rating: " + errorMessage);
                            }
                        });
                    } else {
                        Platform.runLater(() -> formErrorMessageLabel.setText("Failed to connect to server to submit rating."));
                    }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> {
                        formErrorMessageLabel.setText("An unexpected error occurred: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            });

        } catch (NumberFormatException e) {
            formErrorMessageLabel.setText("Invalid rating value. Please select a number from 1 to 5.");
        } catch (JsonProcessingException e) {
            formErrorMessageLabel.setText("Error processing JSON for rating: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeForm();
    }


    private void closeForm() {
        // NEW: Stage را از یکی از فیلدهای @FXML خود فرم (مثلاً restaurantNameLabel) می‌گیریم
        // این کار تضمین می‌کند که Stage همیشه از یک Node معتبر در Scene graph گرفته شود.
        Stage stage = (Stage) orderInfoLabel.getScene().getWindow(); //
        stage.close(); // بستن پنجره (Stage)
        executorService.shutdown(); // خاموش کردن ExecutorService
    }
}