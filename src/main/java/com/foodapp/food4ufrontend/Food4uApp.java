package com.foodapp.food4ufrontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Food4uApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // مسیر صحیح فایل FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodapp/food4ufrontend/view/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/foodapp/food4ufrontend/css/application.css").toExternalForm());

        primaryStage.setTitle("Food4u");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}