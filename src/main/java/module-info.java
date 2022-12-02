module app.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javax.json;

    opens app to javafx.fxml;
    exports app;
}