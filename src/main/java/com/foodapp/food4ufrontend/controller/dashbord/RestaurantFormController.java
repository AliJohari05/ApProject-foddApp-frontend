package com.foodapp.food4ufrontend.controller.dashbord;

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

public class RestaurantFormController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField taxField;
    @FXML
    private TextField additionalField;
    @FXML
    private ImageView restaurantImageView;
    @FXML
    private Label formErrorMessageLabel;

    private Restaurant restaurantEdited;
    private String base64ImageString;
    private Consumer<Void> refreshRestaurantCallback;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private String restaurantId;

    @FXML
    public void initialize() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com/foodapp/food4ufrontend/images/default_food_item.png");
            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                if (!defaultImage.isError()) {
                    restaurantImageView.setImage(defaultImage);
                } else {
                    System.err.println("Error loading default image from stream: " + defaultImage.getException().getMessage());
                }
            } else {
                System.err.println("Default restaurant logo resource stream is null");
            }
        } catch (Exception e) {
            System.err.println("Exception loading logo: " + e.getMessage());
        }
    }
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
    public void setRestaurantEdited(Restaurant restaurant) {
        this.restaurantEdited = restaurant;
        if (restaurant != null) {
            nameField.setText(restaurantEdited.getName());
            addressField.setText(restaurantEdited.getAddress());
            phoneField.setText(restaurantEdited.getPhone());
            taxField.setText(String.valueOf(restaurantEdited.getTaxFee()));
            additionalField.setText(String.valueOf(restaurantEdited.getAdditionalFee()));
        }
    }

    public void setRefreshRestaurantCallback(Consumer<Void> refreshRestaurantCallback) {
        this.refreshRestaurantCallback = refreshRestaurantCallback;
    }

    @FXML
    private void handleLogoUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select restaurant logo");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                base64ImageString = Base64.getEncoder().encodeToString(fileContent);
                Image image = new Image(selectedFile.toURI().toString());
                restaurantImageView.setImage(image);
                formErrorMessageLabel.setText("Logo selected: " + selectedFile.getName());
            } catch (IOException e) {
                formErrorMessageLabel.setText("Error reading logo file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            formErrorMessageLabel.setText("No logo selected");
        }
    }

    @FXML
    private void handleSaveRestaurant(ActionEvent event) {
        String name = nameField.getText();
        String address = addressField.getText();
        String phone = phoneField.getText();
        String taxFeeStr = taxField.getText();
        String additionalFeeStr = additionalField.getText();
        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            formErrorMessageLabel.setText("Please fill all required fields(Name, Address, phone)");
            return;
        }
        try {
            Integer taxFee = Integer.parseInt(taxFeeStr);
            Integer additionalFee = Integer.parseInt(additionalFeeStr);
            if (taxFee < 0 || additionalFee < 0) {
                formErrorMessageLabel.setText("Tax fee and additional fee cannot be negative");
                return;
            }
            Map<String, Object> restaurantData = new HashMap<>();
            restaurantData.put("name", name);
            restaurantData.put("address", address);
            restaurantData.put("phone", phone);
            if (base64ImageString != null && !base64ImageString.isEmpty()) {
                restaurantData.put("logoBase64", base64ImageString);
            }
            restaurantData.put("tax_fee", taxFee);
            restaurantData.put("additional_fee", additionalFee);
            String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(restaurantData);
            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> {
                            formErrorMessageLabel.setText("Authentication token is missing.Please login again");
                        });
                        return;
                    }
                    Optional<HttpResponse<String>> responseOptional;
                    if (restaurantEdited == null){
                        responseOptional = ApiClient.post("/restaurants", jsonBody, token);
                    }else {
                        responseOptional=ApiClient.put("/restaurants/"+restaurantId, jsonBody, token);
                    }
                    if (responseOptional.isPresent()) {
                        HttpResponse<String> response = responseOptional.get();
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200||response.statusCode()==201) {
                                formErrorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Restaurant saved successfully");
                                if (refreshRestaurantCallback !=null){
                                    refreshRestaurantCallback.accept(null);
                                }
                                closeForm();
                            } else {
                                formErrorMessageLabel.setText("Error saving restaurant: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                            }
                        });

                    } else {
                        Platform.runLater(() -> {
                            formErrorMessageLabel.setText("Failed to connect to server for saving restaurant");
                        });
                    }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> {
                        formErrorMessageLabel.setText("Unexpected error during adding restaurant:" + e.getMessage());
                        e.printStackTrace();
                    });
                }
            });
        } catch (NumberFormatException e) {
            formErrorMessageLabel.setText("Tax fee and additional fee must be valid numbers");
        } catch (JsonProcessingException e) {
            formErrorMessageLabel.setText("Error processing json: "+e.getMessage());
        }
    }

    private void closeForm() {
        Stage stage=(Stage) nameField.getScene().getWindow();
        stage.close();
        executorService.shutdown();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeForm();
    }
}
