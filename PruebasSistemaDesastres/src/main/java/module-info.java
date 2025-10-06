module com.example.pruebassistemadesastres {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.example.pruebassistemadesastres to javafx.fxml;
    exports com.example.pruebassistemadesastres;
}