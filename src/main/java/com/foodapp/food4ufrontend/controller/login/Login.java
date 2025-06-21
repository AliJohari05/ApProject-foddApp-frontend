package com.foodapp.food4ufrontend.controller.login;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.User;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Login {

    @FXML
    private MFXTextField phoneField;

    @FXML
    private MFXPasswordField passwordField;

    @FXML
    private MFXButton loginButton;

    @FXML
    private Label errorMessageLabel;

    @FXML
    public void initialize() {
        // Initialization logic if needed
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String phone = phoneField.getText();
        String password = passwordField.getText();

        if (phone.isEmpty() || password.isEmpty()) {
            errorMessageLabel.setText("Please enter phone number and password.");
            return;
        }

        try {
            Map<String, String> loginData = new HashMap<>();
            loginData.put("phone", phone);
            loginData.put("password", password);
            String loginJson = JsonUtil.getObjectMapper().writeValueAsString(loginData);

            Optional<HttpResponse<String>> responseOpt = ApiClient.post("/auth/login", loginJson, null);

            if (responseOpt.isPresent()) {
                HttpResponse<String> response = responseOpt.get();
                JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                if (response.statusCode() == 200) {
                    String message = rootNode.has("message") ? rootNode.get("message").asText() : "Login successful!";
                    String token = rootNode.get("token").asText();
                    User user = JsonUtil.getObjectMapper().treeToValue(rootNode.get("user"), User.class);

                    AuthManager.setJwtToken(token);
                    AuthManager.setCurrentUserRole(user.getRole());
                    AuthManager.setCurrentUserId(user.getId());
                    System.out.println("login 1");

                    navigateToDashboard(event, user.getRole());

                    System.out.println("login 2");


                } else {
                    String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                    errorMessageLabel.setText(errorMessage);
                }
            } else {
                errorMessageLabel.setText("Failed to connect to server. Please check server status.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void signup(ActionEvent event) throws IOException {
        System.out.println("Sign Up hyperlink clicked!");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/signup.fxml"));
        Parent signupView = loader.load();
        Scene signupScene = new Scene(signupView);

        signupScene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(signupScene);
        window.setTitle("Food4u - Sign Up");
        window.show();
    }

    private void navigateToDashboard(ActionEvent event, String role) throws IOException {
        String fxmlPath;
        String title;

        switch (role.toUpperCase()) {
            case "CUSTOMER":
                fxmlPath = "/com/foodapp/food4ufrontend/view/dashbord/BuyerDashboardView.fxml";
                title = "Food4u - Buyer Dashboard";
                break;
            case "SELLER":
                fxmlPath = "/com/foodapp/food4ufrontend/view/dashbord/SellerDashboardView.fxml";
                title = "Food4u - Seller Dashboard";
                break;
            case "DELIVERY":
                fxmlPath = "/com/foodapp/food4ufrontend/view/dashbord/CourierDashboardView.fxml";
                title = "Food4u - Courier Dashboard";
                break;
            case "ADMIN":
                System.out.println("وارد بخش ادمین شد");
                fxmlPath = "/com/foodapp/food4ufrontend/view/dashbord/AdminDashboardView.fxml";
                title = "Food4u - Admin Dashboard";
                System.out.println("ادرس درست بود");
                break;
            default:
                if (errorMessageLabel != null) { // Safeguard
                    errorMessageLabel.setText("Unknown user role. Cannot navigate.");
                } else {
                    System.err.println("ERROR: errorMessageLabel is null. Unknown user role.");
                }
                return;
        }

        // FIX ATTEMPT 1: Try ContextClassLoader
        java.net.URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(fxmlPath);
        // If that still returns null, fallback to getClass().getResource()
        if (resourceUrl == null) {
            resourceUrl = getClass().getResource(fxmlPath);
        }

        System.out.println("Attempting to load FXML: " + fxmlPath);
        System.out.println("Resource URL found: " + resourceUrl);
        if (resourceUrl == null) {
            System.err.println("ERROR: FXML resource was NOT found at path: " + fxmlPath + ". navigateToDashboard will fail.");
            Platform.runLater(() -> {
                if (errorMessageLabel != null) {
                    errorMessageLabel.setText("CRITICAL ERROR: Dashboard FXML not found at " + fxmlPath + ". Check console for details.");
                } else {
                    System.err.println("ERROR: errorMessageLabel is null. Cannot display error on UI. Is fx:id='errorMessageLabel' correct in login.fxml?");
                }
            });
            return;
        } else {
            System.out.println("SUCCESS: FXML resource URL: " + resourceUrl.toExternalForm());
        }

        FXMLLoader loader = new FXMLLoader(resourceUrl);
        Parent dashboardView = loader.load();
        Scene dashboardScene = new Scene(dashboardView);
        dashboardScene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(dashboardScene);
        window.setTitle(title);
        window.show();
    }

}