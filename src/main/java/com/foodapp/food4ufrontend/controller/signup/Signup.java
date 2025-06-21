package com.foodapp.food4ufrontend.controller.signup;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.User;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.foodapp.food4ufrontend.controller.login.Login;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Signup {

    @FXML
    private MFXTextField nameField;
    @FXML
    private MFXTextField phoneField;
    @FXML
    private MFXTextField emailField;
    @FXML
    private MFXPasswordField passwordField;
    @FXML
    private MFXTextField addressField;
    @FXML
    private MFXTextField bankNameField;
    @FXML
    private MFXTextField accountField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private MFXButton signupButton;
    @FXML
    private Label errorMessageLabel;

    @FXML
    public void initialize() {
        roleComboBox.setItems(getRoles());
    }

    public ObservableList<String> getRoles() {
        return FXCollections.observableArrayList("Buyer", "Seller","Courier");
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String address = addressField.getText();
        String bankName = bankNameField.getText();
        String accountNumber = accountField.getText();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || role == null) {
            errorMessageLabel.setText("Please fill in all fields.");
            return;
        }
        else if(role.equals("Buyer") || role.equals("Seller")) {
            if(address.isEmpty()){
                errorMessageLabel.setText("Please fill in address field.");
                return;
            }
        }

        try {
            Map<String, String> signupData = new HashMap<>();
            signupData.put("name", name);
            signupData.put("phone", phone);
            signupData.put("email", email);
            signupData.put("password", password);
            signupData.put("address", address);
            signupData.put("bankName", bankName);
            signupData.put("accountNumber", accountNumber);
            signupData.put("role", role);
            String signupJson = JsonUtil.getObjectMapper().writeValueAsString(signupData);

            Optional<HttpResponse<String>> responseOpt = ApiClient.post("/auth/register", signupJson, null);

            if (responseOpt.isPresent()) {
                HttpResponse<String> response = responseOpt.get();
                JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                if (response.statusCode() == 200) {
                    String message = rootNode.has("message") ? rootNode.get("message").asText() : "Sign Up successful!";
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
    private void login(ActionEvent event) throws IOException {
        System.out.println(" Login hyperlink clicked!");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/login.fxml"));
        Parent loginView = loader.load();
        Scene loginScene = new Scene(loginView);

        loginScene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.setTitle("Food4u - Login");
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
