module com.foodapp.food4ufrontend {
    // Requires statements for JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.base;

    // Requires statements for Jackson (JSON processing)
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    // Requires statements for other UI libraries
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.jfoenix;
    requires java.net.http;

    // Export the main package so LauncherImpl can access Food4uApp
    exports com.foodapp.food4ufrontend;

    // Opens statements for FXML controllers and models for Jackson
    opens com.foodapp.food4ufrontend.controller.login to javafx.fxml;
    opens com.foodapp.food4ufrontend.model to com.fasterxml.jackson.databind, javafx.base;
    opens com.foodapp.food4ufrontend.util to com.fasterxml.jackson.databind;

    opens com.foodapp.food4ufrontend.controller.signup to javafx.fxml;
    opens com.foodapp.food4ufrontend.controller.dashbord to javafx.fxml; // این خط جدید اضافه شود
}