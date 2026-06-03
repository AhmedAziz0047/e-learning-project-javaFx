package com.elearning.fx.controller;

import com.elearning.fx.service.ApiClient;
import com.elearning.fx.util.SceneManager;
import com.elearning.fx.util.SessionManager;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de l'écran d'inscription.
 */
public class RegisterController implements Initializable {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink loginLink;
    @FXML private ProgressIndicator loadingIndicator;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
        roleComboBox.getItems().addAll("ROLE_STUDENT", "ROLE_TEACHER");
        roleComboBox.setValue("ROLE_STUDENT");
    }

    @FXML
    private void handleRegister() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            try {
                JsonObject response = ApiClient.register(nom, prenom, email, password, role);

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
                            response.get("message").getAsString() : "Échec de l'inscription";
                    Platform.runLater(() -> showError(msg));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("Erreur de connexion au serveur"));
            } finally {
                Platform.runLater(() -> setLoading(false));
            }
        }).start();
    }

    @FXML
    private void handleLoginLink() {
        SceneManager.showLogin();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        registerButton.setDisable(loading);
    }
}
