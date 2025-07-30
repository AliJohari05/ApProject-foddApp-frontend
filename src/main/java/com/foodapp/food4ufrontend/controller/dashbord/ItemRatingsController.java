package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType; // FIX: Import CollectionType
import com.foodapp.food4ufrontend.model.FoodItem;
import com.foodapp.food4ufrontend.model.Rating; // FIX: از این پکیج استفاده کنید
import com.foodapp.food4ufrontend.dto.RatingResponseDto; // FIX: از این پکیج استفاده کنید
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.math.BigDecimal; // برای price

public class ItemRatingsController {

    @FXML private ImageView itemImageView;
    @FXML private Label itemNameLabel;
    @FXML private Label itemDescriptionLabel;
    @FXML private Label itemPriceLabel;
    @FXML private Label averageRatingLabel;
    @FXML private TableView<RatingResponseDto> commentsTable;
    @FXML private TableColumn<RatingResponseDto, String> commentUserColumn;
    @FXML private TableColumn<RatingResponseDto, Integer> commentRatingColumn;
    @FXML private TableColumn<RatingResponseDto, String> commentTextColumn;
    @FXML private TableColumn<RatingResponseDto, String> commentImageColumn;
    @FXML private TableColumn<RatingResponseDto, String> commentDateColumn;
    @FXML private Label viewRatingsErrorMessageLabel;

    private FoodItem currentFoodItem;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    @FXML
    public void initialize() {
        commentUserColumn.setCellValueFactory(new PropertyValueFactory<>("userId")); // فرض می‌کنیم userId نام کاربر است (یا نیاز به فیلد username در RatingResponseDto داریم)
        commentRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentTextColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentImageColumn.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        commentDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }

    public void setFoodItem(FoodItem foodItem) {
        this.currentFoodItem = foodItem;
        if (currentFoodItem != null) {
            itemNameLabel.setText(currentFoodItem.getName());
            itemDescriptionLabel.setText(currentFoodItem.getDescription());
            itemPriceLabel.setText("Price: $" + String.valueOf(currentFoodItem.getPrice()));

            if (currentFoodItem.getImageBase64() != null && !currentFoodItem.getImageBase64().isEmpty()) {
                try {
                    byte[] decodedImg = Base64.getDecoder().decode(currentFoodItem.getImageBase64());
                    Image itemImage = new Image(new ByteArrayInputStream(decodedImg));
                    itemImageView.setImage(itemImage);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error decoding Base64 item image: " + e.getMessage());
                    loadDefaultItemImage();
                }
            } else {
                loadDefaultItemImage();
            }

            loadItemRatings();
        }
    }

    private void loadDefaultItemImage(){
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com/foodapp/food4ufrontend/images/default_food_item.png");
            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                if (!defaultImage.isError()) {
                    itemImageView.setImage(defaultImage);
                } else {
                    System.err.println("Error loading default item image from stream: " + defaultImage.getException().getMessage());
                }
            } else {
                System.err.println("Default item image resource stream is null.");
            }
        } catch (Exception e) {
            System.err.println("Exception loading default item image: " + e.getMessage());
        }
    }

    private void loadItemRatings() {
        viewRatingsErrorMessageLabel.setText("Loading ratings...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> viewRatingsErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                // API call to get ratings for a specific item
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/ratings/items/" + currentFoodItem.getId(), token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = objectMapper.readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                // Extract average rating
                                JsonNode avgRatingNode = rootNode.get("avg_rating");
                                if (avgRatingNode != null && avgRatingNode.isNumber()) {
                                    averageRatingLabel.setText("Average Rating: " + String.format("%.1f", avgRatingNode.asDouble()));
                                } else {
                                    averageRatingLabel.setText("Average Rating: N/A");
                                }

                                // Extract comments (list of RatingResponseDto)
                                JsonNode commentsNode = rootNode.get("comments");
                                if (commentsNode != null && commentsNode.isArray()) {
                                    CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, RatingResponseDto.class);
                                    List<RatingResponseDto> comments = objectMapper.readValue(commentsNode.toString(), listType);
                                    ObservableList<RatingResponseDto> commentObservableList = FXCollections.observableArrayList(comments);
                                    commentsTable.setItems(commentObservableList); // پر کردن جدول کامنت‌ها
                                } else {
                                    commentsTable.setItems(FXCollections.emptyObservableList());
                                    viewRatingsErrorMessageLabel.setText("No comments available for this item."); // FIX: اصلاح شد
                                }

                                viewRatingsErrorMessageLabel.setText("Ratings loaded successfully.");
                            } catch (IOException e) {
                                viewRatingsErrorMessageLabel.setText("Error parsing ratings data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            viewRatingsErrorMessageLabel.setText("Error fetching ratings: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred."));
                        }
                    });
                } else {
                    Platform.runLater(() -> viewRatingsErrorMessageLabel.setText("Failed to connect to server to load ratings."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    viewRatingsErrorMessageLabel.setText("An unexpected error occurred while fetching ratings: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        executorService.shutdown();
    }
}