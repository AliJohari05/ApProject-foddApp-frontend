package com.foodapp.food4ufrontend.controller.dashbord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.food4ufrontend.model.Coupon;
import com.foodapp.food4ufrontend.util.ApiClient;
import com.foodapp.food4ufrontend.util.AuthManager;
import com.foodapp.food4ufrontend.util.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer; // To pass callback to parent controller

public class CouponFormController {

    @FXML private MFXTextField couponCodeField;
    @FXML private ComboBox<String> couponTypeComboBox;
    @FXML private MFXTextField couponValueField;
    @FXML private MFXTextField couponMinPriceField;
    @FXML private MFXTextField couponUserCountField;
    @FXML private DatePicker couponStartDatePicker;
    @FXML private DatePicker couponEndDatePicker;
    @FXML private Label formErrorMessageLabel;

    private Coupon couponToEdit; // Will be null for new coupon, set for editing
    private Consumer<Void> refreshCouponsCallback; // Callback to refresh parent table
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public void setRefreshCouponsCallback(Consumer<Void> callback) {
        this.refreshCouponsCallback = callback;
    }

    // Call this if editing an existing coupon
    public void setCouponToEdit(Coupon coupon) {
        this.couponToEdit = coupon;
        if (coupon != null) {
            couponCodeField.setText(coupon.getCouponCode());
            couponTypeComboBox.setValue(coupon.getType());
            couponValueField.setText(String.valueOf(coupon.getValue()));
            couponMinPriceField.setText(String.valueOf(coupon.getMinPrice()));
            couponUserCountField.setText(String.valueOf(coupon.getUserCount()));

            // Assuming start_date and end_date are String in Coupon model
            // and format is YYYY-MM-DD
            if (coupon.getStartDate() != null && !coupon.getStartDate().isEmpty()) {
                couponStartDatePicker.setValue(LocalDate.parse(coupon.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            }
            if (coupon.getEndDate() != null && !coupon.getEndDate().isEmpty()) {
                couponEndDatePicker.setValue(LocalDate.parse(coupon.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            }
        }
    }

    @FXML
    public void initialize() {
        ObservableList<String> couponTypes = FXCollections.observableArrayList("fixed", "percent");
        couponTypeComboBox.setItems(couponTypes);
    }

    @FXML
    private void handleSaveCoupon(ActionEvent event) {
        String couponCode = couponCodeField.getText();
        String type = couponTypeComboBox.getValue();
        String valueStr = couponValueField.getText();
        String minPriceStr = couponMinPriceField.getText();
        String userCountStr = couponUserCountField.getText();
        LocalDate startDate = couponStartDatePicker.getValue();
        LocalDate endDate = couponEndDatePicker.getValue();

        if (couponCode.isEmpty() || type == null || valueStr.isEmpty() || minPriceStr.isEmpty() ||
                userCountStr.isEmpty() || startDate == null || endDate == null) {
            formErrorMessageLabel.setText("Please fill all fields.");
            return;
        }

        try {
            double value = Double.parseDouble(valueStr);
            int minPrice = Integer.parseInt(minPriceStr);
            int userCount = Integer.parseInt(userCountStr);

            Map<String, Object> couponData = new HashMap<>();
            couponData.put("coupon_code", couponCode);
            couponData.put("type", type);
            couponData.put("value", value);
            couponData.put("min_price", minPrice);
            couponData.put("user_count", userCount);
            couponData.put("start_date", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            couponData.put("end_date", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

            String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(couponData);

            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> formErrorMessageLabel.setText("Authentication token missing."));
                        return;
                    }

                    Optional<HttpResponse<String>> responseOpt;
                    if (couponToEdit == null) { // Add new coupon
                        responseOpt = ApiClient.post("/admin/coupons", jsonBody, token);
                    } else { // Edit existing coupon
                        responseOpt = ApiClient.put("/admin/coupons/" + couponToEdit.getId(), jsonBody, token);
                    }

                    if (responseOpt.isPresent()) {
                        HttpResponse<String> response = responseOpt.get();
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                        Platform.runLater(() -> {
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                formErrorMessageLabel.setText("Coupon saved successfully!");
                                if (refreshCouponsCallback != null) {
                                    refreshCouponsCallback.accept(null); // Trigger refresh in parent
                                }
                                closeForm(); // Close the dialog/stage
                            } else {
                                String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                                formErrorMessageLabel.setText("Error saving coupon: " + errorMessage);
                            }
                        });
                    } else {
                        Platform.runLater(() -> formErrorMessageLabel.setText("Failed to connect to server."));
                    }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> {
                        formErrorMessageLabel.setText("An unexpected error occurred: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            });

        } catch (NumberFormatException e) {
            formErrorMessageLabel.setText("Value, Min. Price, and User Count must be numbers.");
        } catch (JsonProcessingException e) {
            formErrorMessageLabel.setText("Error processing JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) couponCodeField.getScene().getWindow();
        stage.close();
        executorService.shutdown(); // Shutdown executor when form is closed
    }
}