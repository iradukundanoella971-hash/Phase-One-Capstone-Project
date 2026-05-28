package igirepay.igire_capstoneproject.lab3.controller;

import igirepay.igire_capstoneproject.lab1.model.Customer;
import igirepay.igire_capstoneproject.lab1.util.ValidationUtils;
import igirepay.igire_capstoneproject.lab3.session.UserSession;
import igirepay.igire_capstoneproject.lab3.util.AlertManager;
import igirepay.igire_capstoneproject.lab3.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ProfileController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField currentPinField;
    @FXML
    private PasswordField newPinField;
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

        if (!session.isLoggedIn()) {
            navigationManager.goToLogin();
            return;
        }

        Customer customer = session.getCustomer();
        fullNameField.setText(customer.getFullName());
        emailField.setText(customer.getEmail());
        phoneField.setText(customer.getPhoneNumber());
    }

    @FXML
    private void handleUpdateProfile() {
        String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String currentPin = currentPinField.getText() == null ? "" : currentPinField.getText().trim();
        String newPin = newPinField.getText() == null ? "" : newPinField.getText().trim();
        String confirmPin = confirmPinField.getText() == null ? "" : confirmPinField.getText().trim();
        errorLabel.setText("");

        if (fullName.isBlank() || email.isBlank() || phone.isBlank()) {
            errorLabel.setText("Name, email and phone are required");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            errorLabel.setText("Invalid email");
            return;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            errorLabel.setText("Phone must be 10-12 digits");
            return;
        }

        Customer current = session.getCustomer();
        if (!email.equalsIgnoreCase(current.getEmail()) && appController.isEmailTaken(email)) {
            errorLabel.setText("Email already exists");
            return;
        }
        if (!phone.equals(current.getPhoneNumber()) && appController.isPhoneTaken(phone)) {
            errorLabel.setText("Phone already exists");
            return;
        }

        boolean updated = appController.updateCustomerProfile(current.getCustomerId().toString(), fullName, email, phone);
        if (!updated) {
            errorLabel.setText("Could not update profile");
            return;
        }

        boolean wantsPinChange = !currentPin.isBlank() || !newPin.isBlank() || !confirmPin.isBlank();
        if (wantsPinChange) {
            if (!ValidationUtils.isValidPin(newPin)) {
                errorLabel.setText("New PIN must be 5 digits");
                return;
            }
            if (!newPin.equals(confirmPin)) {
                errorLabel.setText("PINs do not match");
                return;
            }
            if (!appController.updateAccountPin(session.getActiveAccount(), currentPin, newPin)) {
                errorLabel.setText("Could not update PIN");
                return;
            }
            session.setPin(newPin);
        }

        Customer refreshed = appController.refreshCustomer(current.getCustomerId().toString());
        if (refreshed != null) {
            session.setCustomer(refreshed);
        }
        AlertManager.showInfo("Profile", "Profile updated successfully.");
        navigationManager.goToDashboard();
    }

    @FXML
    private void handleBack() {
        navigationManager.goToDashboard();
    }
}
