package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.User;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserProfileController {

    @FXML private ImageView profileImageView;
    @FXML private MFXTextField fullNameField;
    @FXML private MFXTextField phoneField;
    @FXML private MFXTextField emailField;
    @FXML private MFXTextField addressField;
    @FXML private MFXTextField roleField;
    @FXML private MFXTextField walletBalanceField;
    @FXML private Label profileErrorMessageLabel;
    @FXML private MFXButton saveButton;
    @FXML private MFXButton cancelButton;
    @FXML private MFXButton editProfileButton;


    private User currentUser;
    private String base64ImageString; // To store the Base64 image string for upload
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        setFieldsEditable(false); // Initially non-editable
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        loadUserProfile(); // Load user profile data when the view is initialized
    }

    private void setFieldsEditable(boolean editable) {
        fullNameField.setEditable(editable);
        phoneField.setEditable(editable);
        emailField.setEditable(editable);
        addressField.setEditable(editable);
        // Role and Wallet Balance should usually not be editable by the user
    }

    private void loadUserProfile() {
        profileErrorMessageLabel.setText("Loading profile...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> profileErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/auth/profile", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                currentUser = JsonUtil.getObjectMapper().treeToValue(rootNode, User.class);
                                populateProfileFields(currentUser);
                                profileErrorMessageLabel.setText("Profile loaded successfully.");
                            } catch (IOException e) {
                                profileErrorMessageLabel.setText("Error parsing profile data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            profileErrorMessageLabel.setText("Error loading profile: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> profileErrorMessageLabel.setText("Failed to connect to server to load profile."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    profileErrorMessageLabel.setText("An unexpected error occurred while loading profile: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    private void populateProfileFields(User user) {
        if (user != null) {
            fullNameField.setText(user.getFullName());
            phoneField.setText(user.getPhone());
            emailField.setText(user.getEmail());
            addressField.setText(user.getAddress());
            roleField.setText(user.getRole());
            walletBalanceField.setText(user.getWalletBalance() != null ? user.getWalletBalance().toPlainString() : "0.00");

            // Load profile image
            if (user.getProfileImageBase64() != null && !user.getProfileImageBase64().isEmpty()) {
                String imageUrl = user.getProfileImageBase64();
                if (!imageUrl.startsWith("http")) {
                    imageUrl = ApiClient.BASE_URL + imageUrl;
                }
                try {
                    Image profileImage = new Image(imageUrl, true);
                    profileImageView.setImage(profileImage);
                } catch (Exception e) {
                    loadDefaultProfileImage();
                }
            } else {
                loadDefaultProfileImage();
            }



        }
    }

    private void loadDefaultProfileImage() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com/foodapp/food4ufrontend/images/default_profile.jpg");
            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                profileImageView.setImage(defaultImage);
            } else {
                System.err.println("Default image resource stream is null for default loading.");
            }
        } catch (Exception e) {
            System.err.println("Exception loading default profile image for fallback: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePicture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                base64ImageString = Base64.getEncoder().encodeToString(fileContent);
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
                profileErrorMessageLabel.setText("New image selected. Click 'Save Changes' to upload.");
                editProfileButton.setVisible(false);
                saveButton.setVisible(true);
                cancelButton.setVisible(true);
                setFieldsEditable(true); // Allow other fields to be edited too
            } catch (IOException e) {
                profileErrorMessageLabel.setText("Error reading image file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        setFieldsEditable(true);
        editProfileButton.setVisible(false);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
        profileErrorMessageLabel.setText("You can now edit your profile.");
    }

    @FXML
    private void handleSaveChanges(ActionEvent event) {
        profileErrorMessageLabel.setText("Saving changes...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> profileErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Map<String, Object> profileData = new HashMap<>();
                profileData.put("full_name", fullNameField.getText());
                profileData.put("phone", phoneField.getText());
                profileData.put("email", emailField.getText());
                profileData.put("address", addressField.getText());

                if (base64ImageString != null) {
                    profileData.put("profileImageBase64", base64ImageString);
                }

                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(profileData);

                Optional<HttpResponse<String>> responseOpt = ApiClient.put("/auth/profile", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            profileErrorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Profile updated successfully.");
                            setFieldsEditable(false);
                            saveButton.setVisible(false);
                            cancelButton.setVisible(false);
                            editProfileButton.setVisible(true);
                            base64ImageString = null;
                            loadUserProfile();
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            profileErrorMessageLabel.setText("Error updating profile: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> profileErrorMessageLabel.setText("Failed to connect to server to update profile."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    profileErrorMessageLabel.setText("An unexpected error occurred while saving profile: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleCancelEdit(ActionEvent event) {
        setFieldsEditable(false);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        editProfileButton.setVisible(true);
        populateProfileFields(currentUser);
        base64ImageString = null;
        if (currentUser.getProfileImageBase64() == null || currentUser.getProfileImageBase64().isEmpty()) {
            loadDefaultProfileImage();
        } else {
            try {
                Image profileImage = new Image(ApiClient.BASE_URL + currentUser.getProfileImageBase64(), true);
                profileImageView.setImage(profileImage);
            } catch (Exception e) {
                System.err.println("Error reloading original image after cancel: " + e.getMessage());
                loadDefaultProfileImage();
            }
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}