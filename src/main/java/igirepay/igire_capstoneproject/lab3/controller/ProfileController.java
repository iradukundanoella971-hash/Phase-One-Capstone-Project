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
        errorLabel.setText("");

        String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String currentPin = currentPinField.getText() == null ? "" : currentPinField.getText().trim();
        String newPin = newPinField.getText() == null ? "" : newPinField.getText().trim();
        String confirmPin = confirmPinField.getText() == null ? "" : confirmPinField.getText().trim();

        if (fullName.isBlank() || email.isBlank() || phone.isBlank()) {
            errorLabel.setText("Name, email, and phone are required");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            errorLabel.setText("Enter a valid email");
            return;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            errorLabel.setText("Phone must be 10–12 digits");
            return;
        }

        Customer customer = session.getCustomer();
        String customerId = customer.getCustomerId().toString();

        if (!email.equalsIgnoreCase(customer.getEmail()) && appController.isEmailTaken(email)) {
            errorLabel.setText("Email is already used by another account");
            return;
        }
        if (!phone.equals(customer.getPhoneNumber()) && appController.isPhoneTaken(phone)) {
            errorLabel.setText("Phone number is already used by another account");
            return;
        }

        boolean profileUpdated = appController.updateCustomerProfile(customerId, fullName, email, phone);
        if (!profileUpdated) {
            errorLabel.setText("Could not update profile");
            return;
        }

        Customer refreshed = appController.refreshCustomer(customerId);
        if (refreshed != null) {
            session.setCustomer(refreshed);
        }

        boolean pinChangeRequested = !newPin.isBlank() || !confirmPin.isBlank() || !currentPin.isBlank();
        if (pinChangeRequested) {
            if (currentPin.isBlank()) {
                errorLabel.setText("Enter your current PIN to change PIN");
                return;
            }
            if (!ValidationUtils.isValidPin(newPin)) {
                errorLabel.setText("New PIN must be 5 digits");
                return;
            }
            if (!newPin.equals(confirmPin)) {
                errorLabel.setText("New PINs do not match");
                return;
            }
            boolean pinUpdated = appController.updateAccountPin(session.getActiveAccount(), currentPin, newPin);
            if (!pinUpdated) {
                errorLabel.setText("Could not update PIN. Check your current PIN.");
                return;
            }
            session.setPin(newPin);
            session.setActiveAccount(appController.reloadAccount(session.getActiveAccount()));
            currentPinField.clear();
            newPinField.clear();
            confirmPinField.clear();
        }

        AlertManager.showInfo("Profile", "Profile updated successfully.");
        navigationManager.goToDashboard();
    }

    @FXML
    private void handleBack() {
        navigationManager.goToDashboard();
    }
}
