package com.example.pruebassistemadesastres.viewController;

import com.example.pruebassistemadesastres.App;
import com.example.pruebassistemadesastres.model.SistemaGestionDesastres;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.*;
import java.io.IOException;

public class LoginViewController {
    private SistemaGestionDesastres sistemaGestionDesastres;
    @FXML
    private Button btnIngresar;

    @FXML
    private TextField txtPass;

    @FXML
    private TextField txtUsuario;

    @FXML
    void clickIngresar(ActionEvent event) throws IOException {
        if (!txtPass.getText().isBlank() && !txtUsuario.getText().isBlank()) {
            if (sistemaGestionDesastres.autenticar(txtUsuario.getText(),txtPass.getText()).equals("ADMIN") ||
                    sistemaGestionDesastres.autenticar(txtUsuario.getText(),txtPass.getText()).equals("OPERADOR")) {
                txtPass.clear();
                txtUsuario.clear();
                abrirDashboard();
            }else{
                JOptionPane.showMessageDialog(null,"Este usuario no existe");
            }
        }
    }
    public void abrirDashboard() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/example/pruebassistemadesastres/fxml/DashboardAdmin.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1250, 720);
        DashboardAdminViewController controller = fxmlLoader.getController();
        controller.setSistemaGestionDesastres(SistemaGestionDesastres.getInstancia());
        Stage stage = new Stage();

        stage.initStyle(StageStyle.UNDECORATED);

        Stage stageLogin = (Stage) btnIngresar.getScene().getWindow();
        stageLogin.close();

        stage.setScene(scene);
        stage.show();
    }

    public SistemaGestionDesastres getSistemaGestionDesastres() {
        return sistemaGestionDesastres;
    }

    public void setSistemaGestionDesastres(SistemaGestionDesastres sistemaGestionDesastres) {
        this.sistemaGestionDesastres = sistemaGestionDesastres;
    }
}
