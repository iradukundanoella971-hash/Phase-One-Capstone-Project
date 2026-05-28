package igirepay.igire_capstoneproject.lab3.controller;

import igirepay.igire_capstoneproject.lab3.session.UserSession;
import igirepay.igire_capstoneproject.lab3.util.AlertManager;
import igirepay.igire_capstoneproject.lab3.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField pinField;
    @FXML
    private Label errorLabel;

    private NavigationManager navigationManager;
    private UserSession session;
    private AppController appController;

    public void init(NavigationManager navigationManager, UserSession session, AppController appController) {
        this.navigationManager = navigationManager;
        this.session = session;
        this.appController = appController;
    }

    @FXML
    private void handleLogin() {
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String pin = pinField.getText() == null ? "" : pinField.getText().trim();
        errorLabel.setText("");

        if (phone.isBlank()) {
            errorLabel.setText("Enter your phone number");
            return;
        }
        if (pin.isBlank()) {
            errorLabel.setText("Enter your PIN");
            return;
        }

        try {
            AppController.LoginResult result = appController.loginByPhoneAndPin(phone, pin);
            if (!result.ok()) {
                errorLabel.setText(result.message());
                return;
            }
            session.clear();
            session.setCustomer(result.customer());
            session.setActiveAccount(result.account());
            session.setPin(result.pin());
            navigationManager.goToDashboard();
        } catch (Exception e) {
            AlertManager.showError("Login failed", e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        navigationManager.goTo(NavigationManager.REGISTER_FXML);
    }
}
