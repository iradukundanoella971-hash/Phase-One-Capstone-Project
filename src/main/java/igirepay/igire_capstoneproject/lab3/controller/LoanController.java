package igirepay.igire_capstoneproject.lab3.controller;

import igirepay.igire_capstoneproject.lab2.model.Loan;
import igirepay.igire_capstoneproject.lab3.session.UserSession;
import igirepay.igire_capstoneproject.lab3.util.AlertManager;
import igirepay.igire_capstoneproject.lab3.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoanController {

    @FXML
    private Label errorLabel;
    @FXML
    private Label activeLoanStatusLabel;
    @FXML
    private Label activeLoanAmountLabel;
    @FXML
    private Label activeLoanDueDateLabel;
    @FXML
    private Label activeLoanRepaymentLabel;
    @FXML
    private TextField loanAmountField;
    @FXML
    private PasswordField pinField;
    @FXML
    private Button repayButton;

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
        refreshLoanSummary();
    }

    @FXML
    private void handleRequestLoan() {
        errorLabel.setText("");
        double amount;
        try {
            amount = Double.parseDouble(loanAmountField.getText() == null ? "" : loanAmountField.getText().trim());
        } catch (Exception e) {
            amount = -1;
        }
        String pin = pinField.getText() == null ? "" : pinField.getText().trim();

        if (pin.isBlank()) {
            errorLabel.setText("Enter your PIN");
            return;
        }

        AppController.LoanResult result = appController.requestLoan(session.getActiveAccount(), session.getCustomer(), pin, amount);
        if (!result.ok()) {
            errorLabel.setText(result.message());
            return;
        }

        session.setActiveAccount(appController.reloadAccount(session.getActiveAccount()));
        refreshLoanSummary();
        AlertManager.showInfo("Loan approved",
                String.format("Loan approved for %,.0f RWF.\nDue date: %s", amount, result.loan().getDueDate()));
    }

    @FXML
    private void handleRepayLoan() {
        errorLabel.setText("");
        String pin = pinField.getText() == null ? "" : pinField.getText().trim();
        if (pin.isBlank()) {
            errorLabel.setText("Enter your PIN");
            return;
        }

        AppController.LoanRepaymentResult result = appController.repayLoan(session.getActiveAccount(), session.getCustomer(), pin);
        if (!result.ok()) {
            errorLabel.setText(result.message());
            return;
        }

        session.setActiveAccount(appController.reloadAccount(session.getActiveAccount()));
        refreshLoanSummary();
        AlertManager.showInfo("Loan repaid", "Your loan has been repaid successfully.");
    }

    @FXML
    private void handleBack() {
        navigationManager.goToDashboard();
    }

    private void refreshLoanSummary() {
        Loan activeLoan = appController.getOutstandingLoanForCustomer(session.getCustomer());
        if (activeLoan == null) {
            activeLoanStatusLabel.setText("No active loan");
            activeLoanAmountLabel.setText("Amount: --");
            activeLoanDueDateLabel.setText("Due date: --");
            activeLoanRepaymentLabel.setText("Repayment: --");
            repayButton.setDisable(true);
            return;
        }

        activeLoanStatusLabel.setText("Status: " + activeLoan.getStatus());
        activeLoanAmountLabel.setText(String.format("Amount: %,.0f RWF", activeLoan.getLoanAmount()));
        activeLoanDueDateLabel.setText("Due date: " + activeLoan.getDueDate());
        activeLoanRepaymentLabel.setText("Repayment status: " + activeLoan.getRepaymentStatus());
        repayButton.setDisable(false);
    }
}
