module com.foodapp.food4ufrontend {
    // Requires statements for JavaFX modules
    requires javafx.web;
    requires javafx.swing;
    requires javafx.controls;
    requires javafx.fxml;

    // Requires statements for Jackson (JSON processing)
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.annotation;

    // Requires statements for other UI libraries
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.jfoenix;
    requires java.net.http;
    requires MaterialFX;

    // Export the main package so LauncherImpl can access Food4uApp
    exports com.foodapp.food4ufrontend;

    // Exports for controllers (needed for instantiation)
    exports com.foodapp.food4ufrontend.controller.dashbord to javafx.fxml;

    // Opens statements for FXML controllers and models for Jackson
    // This allows JavaFX's FXMLLoader to access FXML files within these packages
    opens com.foodapp.food4ufrontend.controller.login to javafx.fxml;
    opens com.foodapp.food4ufrontend.controller.signup to javafx.fxml;
    opens com.foodapp.food4ufrontend.controller.dashbord to javafx.fxml; // This line was crucial and now should be present
    opens com.foodapp.food4ufrontend.model to com.fasterxml.jackson.databind, javafx.base;
    opens com.foodapp.food4ufrontend.util to com.fasterxml.jackson.databind;

    // FIX: ADD THIS LINE to open the CSS package for FXML to find application.css
    opens com.foodapp.food4ufrontend.css to javafx.fxml;
}