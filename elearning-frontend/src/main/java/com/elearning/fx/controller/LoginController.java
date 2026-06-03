package com.elearning.fx.controller;

import com.elearning.fx.service.ApiClient;
import com.elearning.fx.util.SceneManager;
import com.elearning.fx.util.SessionManager;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de l'écran de connexion.
 */
public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink registerLink;
    @FXML private VBox loginBox;
    @FXML private ProgressIndicator loadingIndicator;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            try {
                JsonObject response = ApiClient.login(email, password);

                if (response.has("token")) {
                    Platform.runLater(() -> {
                        SessionManager.setSession(
                                response.get("token").getAsString(),
                                response.get("id").getAsLong(),
                                response.get("nom").getAsString(),
                                response.get("prenom").getAsString(),
                                response.get("email").getAsString(),
                                response.get("role").getAsString()
                        );
                        SceneManager.showDashboard();
                    });
                } else {
                    String msg = response.has("message") ?
                            response.get("message").getAsString() : "Échec de la connexion";
                    Platform.runLater(() -> showError(msg));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("Erreur de connexion au serveur : " + e.getMessage()));
            } finally {
                Platform.runLater(() -> setLoading(false));
            }
        }).start();
    }

    @FXML
    private void handleRegisterLink() {
        SceneManager.showRegister();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
    }
}
