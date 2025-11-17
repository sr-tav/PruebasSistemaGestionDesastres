package com.example.pruebassistemadesastres;

import com.example.pruebassistemadesastres.model.SistemaGestionDesastres;
import com.example.pruebassistemadesastres.viewController.LoginViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/example/pruebassistemadesastres/fxml/loginv2.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        LoginViewController controller = fxmlLoader.getController();
        controller.setSistemaGestionDesastres(SistemaGestionDesastres.cargarDatosQuemados());
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
