package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.User;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap; // For statusData map
import java.util.List;
import java.util.Map;   // For statusData map
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminDashboard {
    // Existing FXML elements (from previous version)
    @FXML private MFXTextField nameField;
    @FXML private MFXTextField phoneField;
    @FXML private MFXTextField emailField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXTextField addressField;
    @FXML private MFXTextField bankNameField;
    @FXML private MFXTextField accountField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private MFXButton signupButton;
    @FXML private Label errorMessageLabel;
    @FXML private ListView<String> actionList;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userIdColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userPhoneColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userAddressColumn;
    @FXML private TableColumn<User, String> userStatusColumn;
    @FXML private ComboBox<String> filterRoleComboBox;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        ObservableList<String> actions = FXCollections.observableArrayList(
                "View Users",
                "Approve Users",
                "View Orders",
                "View Transactions",
                "Manage Coupons",
                "Log Out"
        );
        actionList.setItems(actions);

        actionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleActionSelection(newValue);
            }
        });

        // Initialize TableColumns using PropertyValueFactory for direct property mapping
        if (userIdColumn != null) userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (userNameColumn != null) userNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (userPhoneColumn != null) userPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (userEmailColumn != null) userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (userRoleColumn != null) userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (userAddressColumn != null) userAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (userStatusColumn != null) userStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Populate the filterRoleComboBox (optional, based on your backend roles)
        ObservableList<String> roles = FXCollections.observableArrayList("All Roles", "Customer", "Seller", "Delivery", "Admin");
        if (filterRoleComboBox != null) {
            filterRoleComboBox.setItems(roles);
            filterRoleComboBox.getSelectionModel().selectFirst();
            filterRoleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> viewUsers());
        }

        viewUsers();
    }

    private void handleActionSelection(String action) {
        switch (action) {
            case "View Users":
                viewUsers();
                break;
            case "Approve Users":
                // Logic will be handled by specific buttons in the FXML
                errorMessageLabel.setText("Please use 'Approve Selected' or 'Reject Selected' buttons for user approval.");
                break;
            case "View Orders":
                // Placeholder for future implementation
                errorMessageLabel.setText("View Orders functionality not yet implemented.");
                break;
            case "View Transactions":
                // Placeholder for future implementation
                errorMessageLabel.setText("View Transactions functionality not yet implemented.");
                break;
            case "Manage Coupons":
                // Placeholder for future implementation
                errorMessageLabel.setText("Manage Coupons functionality not yet implemented.");
                break;
            case "Log Out":
                logout();
                break;
            default:
                break;
        }
    }

    @FXML // <--- ADDED @FXML ANNOTATION HERE
    private void viewUsers() {
        errorMessageLabel.setText("Loading users...");

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                String selectedRole;
                if (filterRoleComboBox != null && !filterRoleComboBox.getSelectionModel().getSelectedItem().equals("All Roles")) {
                    selectedRole = filterRoleComboBox.getSelectionModel().getSelectedItem();
                } else {
                    selectedRole = null;
                }

                String path = "/admin/users";
                // As per aut_food.yaml, /admin/users GET has no query parameters for filtering roles directly.
                // So, the filtering happens client-side after fetching all users.
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<User> users = JsonUtil.getObjectMapper().readerForListOf(User.class).readValue(rootNode);
                                ObservableList<User> userObservableList = FXCollections.observableArrayList(users);

                                // Client-side filtering based on selectedRole
                                if (selectedRole != null) {
                                    userObservableList = userObservableList.filtered(user ->
                                            user.getRole() != null && user.getRole().equalsIgnoreCase(selectedRole)
                                    );
                                }

                                usersTable.setItems(userObservableList);
                                errorMessageLabel.setText("Users loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing user data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing users: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for users. Please check server status."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching users: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML // <--- ADDED @FXML ANNOTATION HERE
    private void approveSelectedUsers() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            errorMessageLabel.setText("Please select a user to approve.");
            return;
        }

        // According to the PDF, only seller and courier accounts need approval.
        // aut_food.yaml also implies this with 'status' enum in PATCH /admin/users/{id}/status
        if (!"seller".equalsIgnoreCase(selectedUser.getRole()) && !"delivery".equalsIgnoreCase(selectedUser.getRole())) {
            errorMessageLabel.setText("Only 'Seller' and 'Courier' roles can be approved/rejected.");
            return;
        }

        updateUserStatus(selectedUser.getId(), "approved");
    }

    @FXML // <--- ADDED @FXML ANNOTATION HERE
    private void rejectSelectedUsers() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            errorMessageLabel.setText("Please select a user to reject.");
            return;
        }

        if (!"seller".equalsIgnoreCase(selectedUser.getRole()) && !"delivery".equalsIgnoreCase(selectedUser.getRole())) {
            errorMessageLabel.setText("Only 'Seller' and 'Courier' roles can be approved/rejected.");
            return;
        }

        updateUserStatus(selectedUser.getId(), "rejected");
    }

    private void updateUserStatus(String userId, String status) {
        errorMessageLabel.setText("Updating user status to " + status + " for user " + userId + "...");

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Map<String, String> statusData = new HashMap<>();
                statusData.put("status", status);
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(statusData);

                Optional<HttpResponse<String>> responseOpt = ApiClient.patch("/admin/users/" + userId + "/status", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText("User status updated successfully.");
                            viewUsers(); // Refresh the user list after update
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error updating user status: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for status update."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred during status update: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML // <--- ADDED @FXML ANNOTATION HERE
    private void logout() {
        AuthManager.logout();
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) actionList.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Food4u - Login");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                errorMessageLabel.setText("Error navigating to login: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}