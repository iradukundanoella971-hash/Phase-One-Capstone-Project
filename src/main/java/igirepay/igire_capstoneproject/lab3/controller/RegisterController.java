package igirepay.igire_capstoneproject.lab3.controller;

import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.Customer;
import igirepay.igire_capstoneproject.lab1.util.ValidationUtils;
import igirepay.igire_capstoneproject.lab3.session.UserSession;
import igirepay.igire_capstoneproject.lab3.util.AlertManager;
import igirepay.igire_capstoneproject.lab3.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField pinField;
    @FXML
    private PasswordField confirmPinField;
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
    private void handleRegister() {
        errorLabel.setText("");

        String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String pin = pinField.getText() == null ? "" : pinField.getText().trim();
        String confirmPin = confirmPinField.getText() == null ? "" : confirmPinField.getText().trim();

        if (fullName.isBlank() || email.isBlank() || phone.isBlank() || pin.isBlank() || confirmPin.isBlank()) {
            errorLabel.setText("All fields are required");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            errorLabel.setText("Enter a valid email address");
            return;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            errorLabel.setText("Phone must be 10–12 digits");
            return;
        }
        if (!ValidationUtils.isValidPin(pin)) {
            errorLabel.setText("PIN must be exactly 5 digits");
            return;
        }
        if (!pin.equals(confirmPin)) {
            errorLabel.setText("PINs do not match");
            return;
        }
        if (appController.isEmailTaken(email)) {
            errorLabel.setText("Email is already registered");
            return;
        }
        if (appController.isPhoneTaken(phone)) {
            errorLabel.setText("Phone number is already registered");
            return;
        }

        try {
            Customer customer = appController.registerCustomer(fullName, email, phone);
            if (customer == null) {
                errorLabel.setText("Registration failed. Check your details.");
                return;
            }

            Account wallet = appController.createWalletAccount(customer, pin);
            if (wallet == null) {
                errorLabel.setText("Account could not be created. Try again.");
                return;
            }

            session.clear();
            AlertManager.showInfo("Success", "Account created. Please sign in with your phone and PIN.");
            navigationManager.goToLogin();
        } catch (Exception e) {
            AlertManager.showError("Registration failed", e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin() {
        navigationManager.goToLogin();
    }
}
