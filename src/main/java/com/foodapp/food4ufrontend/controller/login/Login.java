package com.foodapp.food4ufrontend.controller.login;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.User;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
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
    private JFXTextField phoneField;

    @FXML
    private JFXPasswordField passwordField;

    @FXML
    private JFXButton loginButton;

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
            errorMessageLabel.setText("Please enter phone number and password."); // این پیام از سمت فرانت‌اند است زیرا مربوط به ورودی‌های خالی است
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
                    String message = rootNode.has("message") ? rootNode.get("message").asText() : "Login successful!"; // Fallback message
                    String token = rootNode.get("token").asText();
                    User user = JsonUtil.getObjectMapper().treeToValue(rootNode.get("user"), User.class);

                    AuthManager.setJwtToken(token);
                    AuthManager.setCurrentUserRole(user.getRole());
                    AuthManager.setCurrentUserId(user.getId());

                    errorMessageLabel.setText(message);

                    navigateToDashboard(event, user.getRole());

                } else {
                    String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                    errorMessageLabel.setText(errorMessage);
                }
            } else {
                errorMessageLabel.setText("Failed to connect to server. Please check server status.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            errorMessageLabel.setText("An unexpected error occurred: " + e.getMessage());
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
        // نقش‌ها را از بک‌اند Role enum بگیرید
        // Role enum در بک‌اند شامل ADMIN, CUSTOMER, SELLER, DELIVERY است.
        switch (role.toUpperCase()) {
            case "CUSTOMER":
                fxmlPath = "/com/foodapp/food4ufrontend/view/dashboard/BuyerDashboardView.fxml";
                title = "Food4u - Buyer Dashboard";
                break;
            case "SELLER":
                fxmlPath = "/com/foodapp/food4ufrontend/view/dashboard/SellerDashboardView.fxml";
                title = "Food4u - Seller Dashboard";
                break;
            case "DELIVERY":
                fxmlPath = "/com/foodapp/food4ufrontend/view/dashboard/CourierDashboardView.fxml";
                title = "Food4u - Courier Dashboard";
                break;
            case "ADMIN":
                fxmlPath = "/com/foodapp/food4ufrontend/view/dashboard/AdminDashboardView.fxml";
                title = "Food4u - Admin Dashboard";
                break;
            default:
                errorMessageLabel.setText("Unknown user role. Cannot navigate.");
                return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent dashboardView = loader.load();
        Scene dashboardScene = new Scene(dashboardView);
        dashboardScene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(dashboardScene);
        window.setTitle(title);
        window.show();
    }
}