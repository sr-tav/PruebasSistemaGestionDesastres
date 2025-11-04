module com.example.pruebassistemadesastres {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires com.sothawo.mapjfx;
    requires java.desktop;
    requires javafx.graphics;

    opens com.example.pruebassistemadesastres to javafx.fxml;
    opens com.example.pruebassistemadesastres.viewController to javafx.fxml;
    exports com.example.pruebassistemadesastres;
}