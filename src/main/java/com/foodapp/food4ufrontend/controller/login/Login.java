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
import javafx.application.Platform;

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

    public void setErrorMessageText(String message) {
        System.out.println("Login Controller: setErrorMessageText called with: " + message);
        if (errorMessageLabel != null) {
            Platform.runLater(() -> {
                errorMessageLabel.setText(message);
                errorMessageLabel.setVisible(true);
                errorMessageLabel.setManaged(true);
                System.out.println("Login Controller: errorMessageLabel text set to: '" + errorMessageLabel.getText() + "' visibility: " + errorMessageLabel.isVisible());
            });
        } else {
            System.err.println("ERROR: errorMessageLabel is null in Login controller. Message: " + message);
        }
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
                    Stage currentStage = (Stage) phoneField.getScene().getWindow();
                    handleUserNavigation(currentStage,event, user);

                    System.out.println("login 2");


                } else {
                    // FIX: Ensure errorMessage is read from "error" key if present
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

    private void handleUserNavigation(Stage currentStage,ActionEvent event, User user) throws IOException {
        String userRole = user.getRole();
        String userStatus = user.getStatus();

        if ("SELLER".equals(userRole.toUpperCase()) || "COURIER".equals(userRole.toUpperCase())) {
            if ("PENDING_APPROVAL".equals(userStatus) || "REJECTED".equals(userStatus)) {
                errorMessageLabel.setText("");
                String messageToDisplay = "Your account was successfully registered, but you must wait for the admin to approve it. If you are approved, you can log in.";
                AuthManager.logout();

                System.out.println("Navigating to Login with propagated message: " + messageToDisplay);
                navigateToLoginScreenWithPropagatedMessage(currentStage,event, messageToDisplay);
                return;
            }
            else{
                navigateToDashboard(currentStage,event, userRole);

            }
        }
        navigateToDashboard(currentStage,event, userRole);
    }

    private void navigateToLoginScreen(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/login.fxml"));
        Parent loginView = loader.load();
        Scene loginScene = new Scene(loginView);
        loginScene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.setTitle("Food4u - Login");
        window.show();
    }

    private void navigateToLoginScreenWithPropagatedMessage(Stage currentStage,ActionEvent event, String message) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/login.fxml"));
        Parent loginView = loader.load();

        Login loginController = loader.getController();
        loginController.setErrorMessageText(message);

        Scene loginScene = new Scene(loginView);
        loginScene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());
        //Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        currentStage.setScene(loginScene);
        currentStage.setTitle("Food4u - Login");
        currentStage.show();
        System.out.println("Navigation to Login screen completed.");
    }
    private void navigateToDashboard(Stage currentStage,ActionEvent event, String role) throws IOException {
        String fxmlPath;
        String title;
        switch (role.toUpperCase()) {
            case "BUYER":
                fxmlPath = "/com/foodapp/food4ufrontend/view/dashbord/BuyerDashboardView.fxml";
                title = "Food4u - Buyer Dashboard";
                break;
            case "SELLER":
                fxmlPath = "/com/foodapp/food4ufrontend/view/dashbord/SellerDashboardView.fxml";
                title = "Food4u - Seller Dashboard";
                break;
            case "COURIER":
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
                errorMessageLabel.setText("Unknown user role. Cannot navigate.");
                return;
        }

        java.net.URL resourceUrl = getClass().getResource(fxmlPath);
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

        currentStage.setScene(dashboardScene);
        currentStage.setTitle(title);
        currentStage.show();
    }
}