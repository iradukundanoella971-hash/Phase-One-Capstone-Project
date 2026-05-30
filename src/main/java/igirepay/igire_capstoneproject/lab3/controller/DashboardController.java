package igirepay.igire_capstoneproject.lab3.controller;

import igirepay.igire_capstoneproject.lab1.model.Account;
import igirepay.igire_capstoneproject.lab1.model.Customer;
import igirepay.igire_capstoneproject.lab1.model.SavingsAccount;
import igirepay.igire_capstoneproject.lab1.model.Transaction;
import igirepay.igire_capstoneproject.lab3.session.UserSession;
import igirepay.igire_capstoneproject.lab3.util.AlertManager;
import igirepay.igire_capstoneproject.lab3.util.NavigationManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.List;

public class DashboardController {

    private static final String WALLET_LABEL = "Wallet";
    private static final String SAVINGS_LABEL = "Savings";

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label accountTypeLabel;
    @FXML
    private Label accountNumberLabel;
    @FXML
    private ComboBox<String> accountSelector;
    @FXML
    private ListView<String> recentTransactionsList;

    private NavigationManager navigationManager;
    private UserSession session;
    private AppController appController;
    private boolean suppressAccountChange;

    public void init(NavigationManager navigationManager, UserSession session, AppController appController) {
        this.navigationManager = navigationManager;
        this.session = session;
        this.appController = appController;

        if (!session.isLoggedIn()) {
            navigationManager.goToLogin();
            return;
        }
        setupAccountSelector();
        refreshUi();
    }

    private void setupAccountSelector() {
        suppressAccountChange = true;
        accountSelector.getItems().clear();

        Customer customer = session.getCustomer();
        Account wallet = appController.findWalletForCustomer(customer);
        Account savings = appController.findSavingsForCustomer(customer);
        if (wallet != null) {
            accountSelector.getItems().add(WALLET_LABEL);
        }
        if (savings != null) {
            accountSelector.getItems().add(SAVINGS_LABEL);
        }

        Account active = session.getActiveAccount();
        if (active instanceof SavingsAccount && savings != null) {
            accountSelector.setValue(SAVINGS_LABEL);
        } else if (wallet != null) {
            accountSelector.setValue(WALLET_LABEL);
        }

        accountSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressAccountChange || newVal == null || newVal.equals(oldVal)) {
                return;
            }
            if (WALLET_LABEL.equals(newVal)) {
                Account w = appController.findWalletForCustomer(customer);
                if (w != null) {
                    session.setActiveAccount(w);
                    refreshUi();
                }
            } else if (SAVINGS_LABEL.equals(newVal)) {
                Account s = appController.findSavingsForCustomer(customer);
                if (s != null) {
                    session.setActiveAccount(s);
                    refreshUi();
                }
            }
        });
        suppressAccountChange = false;
    }

    private void refreshUi() {
        session.setActiveAccount(appController.reloadAccount(session.getActiveAccount()));
        Customer customer = session.getCustomer();
        String firstName = customer.getFullName() == null ? "User" : customer.getFullName().split("\\s+")[0];
        welcomeLabel.setText("Welcome, " + firstName);

        Account account = session.getActiveAccount();
        double balance = appController.checkBalance(account, session.getPin());
        balanceLabel.setText(balance >= 0 ? String.format("%,.0f RWF", balance) : "--");
        accountNumberLabel.setText("Account: " + appController.maskAccountNumber(account.getAccountNumber()));
        accountTypeLabel.setText(account instanceof SavingsAccount
                ? "Savings - 100 RWF fee on withdrawals"
                : "Wallet - Instant transfers");

        List<String> recent = appController.getRecentTransactions(account, 5).stream()
                .map(this::formatTransaction)
                .toList();
        recentTransactionsList.setItems(FXCollections.observableArrayList(
                recent.isEmpty() ? List.of("No transactions yet") : recent
        ));
    }

    private String formatTransaction(Transaction tx) {
        return tx.getType() + "  " + String.format("%,.0f RWF", tx.getAmount()) + "  [" + tx.getStatus() + "]";
    }

    @FXML
    private void handleCopyAccount() {
        ClipboardContent content = new ClipboardContent();
        content.putString(session.getActiveAccount().getAccountNumber());
        Clipboard.getSystemClipboard().setContent(content);
        AlertManager.showInfo("Copied", "Account number copied.");
    }

    @FXML
    private void handleDeposit() {
        navigationManager.goToTransaction(NavigationManager.MODE_DEPOSIT);
    }

    @FXML
    private void handleWithdraw() {
        navigationManager.goToTransaction(NavigationManager.MODE_WITHDRAW);
    }

    @FXML
    private void handleTransfer() {
        navigationManager.goToTransaction(NavigationManager.MODE_TRANSFER);
    }

    @FXML
    private void handleOpenSavings() {
        Customer customer = session.getCustomer();
        Account savings = appController.findSavingsForCustomer(customer);
        if (savings == null) {
            savings = appController.createSavingsAccount(customer, session.getPin());
            if (savings == null) {
                AlertManager.showError("Savings", "Could not open savings account.");
                return;
            }
            if (!accountSelector.getItems().contains(SAVINGS_LABEL)) {
                accountSelector.getItems().add(SAVINGS_LABEL);
            }
            AlertManager.showInfo("Savings", "Savings account opened successfully.");
        }
        session.setActiveAccount(savings);
        accountSelector.setValue(SAVINGS_LABEL);
        refreshUi();
    }

    @FXML
    private void handleHistory() {
        navigationManager.goToTransaction(NavigationManager.MODE_HISTORY);
    }

    @FXML
    private void handleLoan() {
        navigationManager.goToLoan();
    }

    @FXML
    private void handleProfile() {
        navigationManager.goToProfile();
    }

    @FXML
    private void handleLogout() {
        if (!AlertManager.confirm("Logout", "Are you sure you want to sign out?")) {
            return;
        }
        session.clear();
        navigationManager.goToLogin();
    }
}
