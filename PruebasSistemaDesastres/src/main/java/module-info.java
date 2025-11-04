module com.example.pruebassistemadesastres {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.media;

    // Map
    requires com.sothawo.mapjfx;

    // HTTP + JSON
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    // (opcional) si usas AWT/Swing en algún lado
    requires java.desktop;

    // FXML: paquetes que cargan controladores
    opens com.example.pruebassistemadesastres to javafx.fxml;
    opens com.example.pruebassistemadesastres.viewController to javafx.fxml;

    // Si Jackson serializa/lee tus POJOs por reflexión, abre tu paquete de modelos:
    // (si solo usas JsonNode no es estrictamente necesario, pero no estorba)
    opens com.example.pruebassistemadesastres.model to com.fasterxml.jackson.databind;

    // Exporta lo que usen otras capas/módulos
    exports com.example.pruebassistemadesastres;
    exports com.example.pruebassistemadesastres.model; // si otras clases fuera del módulo lo usan
}